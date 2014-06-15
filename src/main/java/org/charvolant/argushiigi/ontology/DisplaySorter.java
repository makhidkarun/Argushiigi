/**
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Sort a collection of properties, classes, etc. into a
 * display hierarchy.
 * <p>
 * This sorter uses the annotations provided by the
 * {@link Argushiigi} ontology to determine what order
 * things should appear in and how they should be rendered.
 * <p>
 * There are two special categories, defined in the ontology.
 * {@link #TOP_URI} is the top category, used by anything that is not specifically categorised.
 * {@link #NAME_URI} is the name category, used to find labels for resources; 
 * the properties that are listed in the name category are tried in order for
 * a suitable name.
 * <p>
 * The sorter also supplies names. Properties in the 
 * name category are tried in priority order for a suitable
 * string that names a resource.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 * 
 * @see https://github.com/makhidkarun/Argushiigi/wiki/Display-Ontology
 *
 */
public class DisplaySorter implements Comparator<Resource> {
  /** The logger for this display sorter */
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(DisplaySorter.class);

  /** The URI of the top catch-all category for display */
  public static final String TOP_URI = "http://data.travellerrpg.com/argushiigi/top";
  /** The URI of the reference category for display */
  public static final String REFERENCE_URI = "http://data.travellerrpg.com/argushiigi/reference";
  /** The URI of the name category for display */
  public static final String NAME_URI = "http://data.travellerrpg.com/argushiigi/name";

  /** The model that contains the sort information */
  private Model model;
  /** The top category */
  private Resource top;
  /** The reference category */
  private Resource reference;
  /** The name category */
  private Resource name;
  /** The list of properties to try for a name */
  private List<Property> nameOrder;
  /** The category map */
  private Map<Resource, Resource> categories;
  /** The label format map */
  private Map<Resource, List<Format>> labelFormats;
  /** The value format map */
  private Map<Resource, List<Format>> valueFormats;
  /** The priority list */
  private Map<Resource, Integer> priorities;
  /** The property order */
  private Map<Resource, Integer> propertyOrder;
  /** The class order */
  private Map<Resource, Integer> classOrder;
  /** Inheritable properties */
  private Map<Property, Boolean> inherits;
  /** The default format to use */
  private AbstractFormat defaultFormat;

  /**
   * Construct with a source model.
   * <p>
   * The model should <em>not</em> produce inferences,
   * otherwise the hierarchy of annotations will not work
   * as the reasoner merrily adds transitive properties and
   * classes.
   *
   * @param ontology The ontology
   */
  public DisplaySorter(Model model) {
    super();
    this.model = model;
    this.top = this.model.createResource(this.TOP_URI);
    this.reference = this.model.createResource(this.REFERENCE_URI);
    this.name = this.model.createResource(this.NAME_URI);
    this.defaultFormat = new DefaultFormat();
    this.inherits = new HashMap<Property, Boolean>();
    this.buildPriorityMap();
    this.buildCategoryMap();
    this.buildFormatMap();
    this.buildPropertyOrder();
    this.buildClassOrder();
    this.buildNameOrder();
  }

  /**
   * Get the top category.
   * <p>
   * The top category is the ultimate parent category
   * for all categories and is also the category used
   * if no specific category is specified.
   *
   * @return the top category
   */
  public Resource getTop() {
    return this.top;
  }

  /**
   * Get the reference category.
   *
   * @return the reference category
   */
  public Resource getReference() {
    return this.reference;
  }


  /**
   * Get the categories.
   * <p>
   * For testing.
   *
   * @return the categories
   */
  protected Map<Resource, Resource> getCategories() {
    return this.categories;
  }

  /**
   * Get the priorities.
   * <p>
   * For testing.
   *
   * @return the priorities
   */
  protected Map<Resource, Integer> getPriorities() {
    return this.priorities;
  }


  /**
   * Add bottom elements of a particular type to the bottom set.
   * 
   * @param roots The root set
   * @param type The type of resource
   * @param exclude The property to exclude on (something refers to this, it's not at bottom)
   */
  private void addBottoms(Set<Resource> bottoms, Resource type, Property exclude) {
    StmtIterator si = this.model.listStatements(null, RDF.type, type);
    Resource r;

    while (si.hasNext()) {
      r = si.next().getSubject();
      if (!this.model.contains(null, exclude, r))
        bottoms.add(r);
    }
  }


  /**
   * Build a map for some property.
   * <p>
   * Work upwards through super-classes and super-properties, breadth-first
   * until a specific property is discovered.
   * 
   * @param map The map to fill out
   * @param property The property to use
   * @param clazz The class of property value
   * @param comparator The comparator to use when deciding between candidates
   * @param The default value if not specified (may be null)
   */
  @SuppressWarnings("unchecked")
  synchronized private <T> void buildMap(Map<Resource, T> map, Property property, Class<T> clazz, Comparator<T> comparator, T dflt) {
    Set<Resource> work, nwork, supers, nsupers;
    StmtIterator si;
    T current, candidate;

    si = this.model.listStatements(null, property, (Resource) null);
    while (si.hasNext()) {
      Statement s = si.next();
      if (clazz == Resource.class)
        current = (T) s.getResource();
      else
        current = (T) s.getLiteral().getValue();
      map.put(s.getSubject(), current);
    }
    work = new HashSet<Resource>();
    this.addBottoms(work, RDF.Property, RDFS.subPropertyOf);
    this.addBottoms(work, OWL.AnnotationProperty, RDFS.subPropertyOf);
    this.addBottoms(work, OWL.DatatypeProperty, RDFS.subPropertyOf);
    this.addBottoms(work, OWL.ObjectProperty, RDFS.subPropertyOf);
    this.addBottoms(work, RDFS.Class, RDFS.subClassOf);
    this.addBottoms(work, OWL.Class, RDFS.subClassOf);
    while (!work.isEmpty()) {
      nwork = new HashSet<Resource>(work.size() * 2);
      for (Resource wr: work) {
        if (!map.containsKey(wr)) {
          current = null;
          supers = new HashSet<Resource>();
          supers.add(wr);
          while (current == null && !supers.isEmpty()) {
            nsupers = new HashSet<Resource>();
            for (Resource sr: supers) {
              if (sr.hasProperty(property)) {
                if (clazz == Resource.class)
                  candidate = (T) sr.getPropertyResourceValue(property);
                else
                  candidate = (T) sr.getProperty(property).getLiteral().getValue();
                if (current == null || comparator.compare(candidate, current) < 0)
                  current = candidate;
              }
              if (current == null) {
                si = this.model.listStatements(sr, RDFS.subPropertyOf, (Resource) null);
                while (si.hasNext()) nsupers.add(si.next().getResource());
                si = this.model.listStatements(sr, RDFS.subClassOf, (Resource) null);
                while (si.hasNext()) nsupers.add(si.next().getResource());
              }
            }
            supers = nsupers;
          }
          if (current == null)
            current = dflt;
          map.put(wr, current);
        }
        si = this.model.listStatements(wr, RDFS.subPropertyOf, (Resource) null);
        while (si.hasNext()) nwork.add(si.next().getResource());
        si = this.model.listStatements(wr, RDFS.subClassOf, (Resource) null);
        while (si.hasNext()) nwork.add(si.next().getResource());
      }
      work = nwork;
    }
  }

  /**
   * Build a list map for some property.
   * <p>
   * Work upwards through super-classes and super-properties, breadth-first
   * until a specific property is discovered. If there are multiple entries,
   * then a list of entries, sorted by priority is built.
   * <p>
   * Note that if there is already a map entry for a resource then it
   * is not added to.
   * 
   * @param map The map to fill out
   * @param property The property to use
   * @param comparator The comparator to use when deciding between candidates
   */
  synchronized private void buildFormatMap(Map<Resource, List<Format>> map, Property property) {
    Set<Resource> work, nwork, supers, nsupers;
    StmtIterator si;
    Format current;
    List<Format> candidate, list;

    si = this.model.listStatements(null, property, (Resource) null);
    while (si.hasNext()) {
      Statement s = si.next();

      if (s.getSubject().isAnon())
        continue;
      current = new Format(s.getResource());
      list = map.get(s.getSubject());
      if (list == null) {
        list = new ArrayList<Format>();
        map.put(s.getSubject(), list);
      }
      list.add(current);
    }
    work = new HashSet<Resource>();
    this.addBottoms(work, RDF.Property, RDFS.subPropertyOf);
    this.addBottoms(work, OWL.AnnotationProperty, RDFS.subPropertyOf);
    this.addBottoms(work, OWL.DatatypeProperty, RDFS.subPropertyOf);
    this.addBottoms(work, OWL.ObjectProperty, RDFS.subPropertyOf);
    this.addBottoms(work, RDFS.Class, RDFS.subClassOf);
    this.addBottoms(work, OWL.Class, RDFS.subClassOf);
    while (!work.isEmpty()) {
      nwork = new HashSet<Resource>(work.size() * 2);
      for (Resource wr: work) {
        if (!map.containsKey(wr)) {
          candidate = null;
          supers = new HashSet<Resource>();
          supers.add(wr);
          while (candidate == null && !supers.isEmpty()) {
            nsupers = new HashSet<Resource>();
            for (Resource sr: supers) {
              list = map.get(sr);
              if (list != null) {
                if (candidate == null)
                  candidate = new ArrayList<Format>(list);
                else
                  candidate.addAll(list);
              }
              if (candidate == null) {
                si = this.model.listStatements(sr, RDFS.subPropertyOf, (Resource) null);
                while (si.hasNext()) nsupers.add(si.next().getResource());
                si = this.model.listStatements(sr, RDFS.subClassOf, (Resource) null);
                while (si.hasNext()) nsupers.add(si.next().getResource());
              }
            }
            supers = nsupers;
          }
          if (candidate != null)
            map.put(wr, candidate);
        }
        si = this.model.listStatements(wr, RDFS.subPropertyOf, (Resource) null);
        while (si.hasNext()) nwork.add(si.next().getResource());
        si = this.model.listStatements(wr, RDFS.subClassOf, (Resource) null);
        while (si.hasNext()) nwork.add(si.next().getResource());
      }
      work = nwork;
    }
    for (List<Format> v: map.values())
      Collections.sort(v);
  }


  /**
   * Build the priorities map.
   * <p>
   * Work upwards through super-classes, breadth-first
   * until a priority is discovered.
   */
  synchronized private void buildPriorityMap() {
    this.priorities = new HashMap<Resource, Integer>();
    this.buildMap(
        this.priorities, 
        Argushiigi.priority, 
        Integer.class, 
        new Comparator<Integer>() {
          @Override
          public int compare(Integer o1, Integer o2) {
            return o1.intValue() - o2.intValue();
          }
        }, 
        null);
  }

  /**
   * Add a new priority to the priority map.
   * 
   * @param r The resource
   * @param priority The resource priority
   */
  synchronized private void addPriority(Resource r, int priority) {
    this.priorities.put(r, priority);
  }

  /**
   * Build the category and priorities map.
   * <p>
   * Work upwards through super-classes, breadth-first
   * until a category or priority is discovered.
   */
  synchronized private void buildCategoryMap() {
    this.categories = new HashMap<Resource, Resource>();
    this.buildMap(
        this.categories, 
        Argushiigi.category, 
        Resource.class, 
        new Comparator<Resource>() {
          @Override
          public int compare(Resource o1, Resource o2) {
            return DisplaySorter.this.compare(o1, o2);
          }
        }, 
        this.top);
  }

  /**
   * Add a new category to the category map.
   * 
   * @param r The resource
   * @param category The resource category
   */
  synchronized private void addCategory(Resource r, Resource category) {
    this.categories.put(r, category);
  }

  /**
   * Build the format map.
   * <p>
   * Work upwards through super-classes, breadth-first
   * until a category or priority is discovered.
   */
  synchronized private void buildFormatMap() {
    this.labelFormats = new HashMap<Resource, List<Format>>();
    this.buildFormatMap(this.labelFormats, Argushiigi.labelFormat);
    this.valueFormats = new HashMap<Resource, List<Format>>();
    this.buildFormatMap(this.valueFormats, Argushiigi.valueFormat);
    this.buildFormatMap(this.valueFormats, Argushiigi.format);
  }

  /**
   * Build the name order.
   * <p>
   * This is all the properties that are in the name category.
   */
  private void buildNameOrder() {
    this.nameOrder = new ArrayList<Property>(16);
    for (Map.Entry<Resource, Resource> category: this.categories.entrySet()) {
      if (category.getValue().equals(this.name)) {
        Resource property = category.getKey();
        boolean prop = property.hasProperty(RDF.type, RDF.Property);

        prop = prop || property.hasProperty(RDF.type, OWL.AnnotationProperty);
        prop = prop || property.hasProperty(RDF.type, OWL.ObjectProperty);
        prop = prop || property.hasProperty(RDF.type, OWL.DatatypeProperty);
        if (prop)
          this.nameOrder.add(property.as(Property.class));
      }
    }
    this.nameOrder.add(this.model.createProperty(RDFS.label.getURI()));
    Collections.sort(this.nameOrder, this);
  }

  /**
   * Build an order based on a subXxxOf relationship.
   * <p>
   * The most specific elements are first, working up
   * to more general elements.
   * 
   * @param sub
   * @param types The types of element to build the order out of
   * @return
   */
  private Map<Resource, Integer> buildOrder(Property sub, Resource... types) {
    Set<Resource> work = new HashSet<Resource>(32);
    Set<Resource> nwork;
    List<Resource> order = new ArrayList<Resource>(32);
    Map<Resource, Integer> result;
    StmtIterator si;
    int index;

    for (Resource type: types)
      this.addBottoms(work, type, sub);
    while (!work.isEmpty()) {
      order.addAll(work);
      nwork = new HashSet<Resource>(work.size() * 2 + 1);
      for (Resource wr: work) {
        si = this.model.listStatements(wr, sub, (Resource) null);
        while (si.hasNext()) nwork.add(si.next().getResource());
      }
      work = nwork;
    }
    result = new HashMap<Resource, Integer>(order.size());
    index = 0;
    for (Resource r: order)
      result.put(r, index++);
    return result;

  }

  /**
   * Build the property order, for which properties are
   * lowest in the property hierarchy.
   */
  private void buildPropertyOrder() {
    this.propertyOrder = this.buildOrder(RDFS.subPropertyOf, RDF.Property, OWL.AnnotationProperty, OWL.DatatypeProperty, OWL.ObjectProperty);
  }

  /**
   * Build the class order, for which classes are
   * lowest in the class hierarchy.
   */
  private void buildClassOrder() {
    this.classOrder = this.buildOrder(RDFS.subClassOf, RDFS.Class, OWL.Class);
  }

  /**
   * Find the category of the resource.
   * <p>
   * Categories are chosen according to the following order:
   * <ol>
   * <li>If the resource has a category specified, then that category is used.</li>
   * <li>If the resource is a property, then super-properties are explored and the highest priority category is returned.</li>
   * <li>If the resource is a class, then the super-classes are explored in the same way.</li>
   * <li>Otherwise the classes of the resource are explored.</li>
   * <li>If all else fails, then the top category (see {@link #TOP_URI}) is used.</li>
   * </ol>
   * <p>
   * The search is breadth-first, so the lowest super-object that has a specific category is
   * chosen. Categorisations are cached, for efficiency.
   * 
   * @param resource The resource to categorise
   * 
   * @return The resource category
   */
  public Resource category(Resource resource) {
    Resource category = this.categories.get(resource);
    Resource candidate = null;
    StmtIterator si = null;

    if (category != null)
      return category;
    category = resource.getPropertyResourceValue(Argushiigi.category);
    if (category != null) {
      this.addCategory(resource, category);
      return category;
    }
    if (
        !resource.hasProperty(RDF.type, RDFS.Class) && 
        !resource.hasProperty(RDF.type, OWL.Class) && 
        !resource.hasProperty(RDF.type, RDF.Property) &&
        !resource.hasProperty(RDF.type, OWL.AnnotationProperty) &&
        !resource.hasProperty(RDF.type, OWL.DatatypeProperty) &&
        !resource.hasProperty(RDF.type, OWL.ObjectProperty)
        ) {
      si = resource.listProperties(RDF.type);
      while (si.hasNext()) {
        candidate = this.category(si.next().getResource());
        if (category == null || this.compare(candidate, category) < 0)
          category = candidate;
      }
    }
    if (category == null)
      category = this.top;
    this.addCategory(resource, category);
    return category; 
  }

  /**
   * Find the priority of the resource.
   * <p>
   * Categories are chosen according to the following order:
   * <ol>
   * <li>If the resource has a category specified, then that category is used.</li>
   * <li>If the resource is a property, then super-properties are explored and the highest priority category is returned.</li>
   * <li>If the resource is a class, then the super-classes are explored in the same way.</li>
   * <li>Otherwise the classes of the resource are explored.</li>
   * <li>If all else fails, then the largest priority (see {@link Integer#MAX_VALUE}) is used.</li>
   * </ol>
   * <p>
   * The search is breadth-first, so the lowest super-object that has a specific category is
   * chosen. Categorisations are cached, for efficiency.
   * 
   * @param resource The resource to categorise
   * 
   * @return The resource category
   */
  public int priority(Resource resource) {
    Integer cached = this.priorities.get(resource);
    int priority;
    StmtIterator si = null;


    if (cached != null)
      return cached;
    if (resource.hasProperty(Argushiigi.priority)) {
      priority = resource.getProperty(Argushiigi.priority).getInt();
      this.addPriority(resource, priority);
      return priority;
    }
    priority = Integer.MAX_VALUE;
    if (
        !resource.hasProperty(RDF.type, RDFS.Class) && 
        !resource.hasProperty(RDF.type, OWL.Class) && 
        !resource.hasProperty(RDF.type, RDF.Property) &&
        !resource.hasProperty(RDF.type, OWL.AnnotationProperty) &&
        !resource.hasProperty(RDF.type, OWL.DatatypeProperty) &&
        !resource.hasProperty(RDF.type, OWL.ObjectProperty)
        ) {
      si = resource.listProperties(RDF.type);
      while (si.hasNext()) {
        priority = Math.min(priority, this.priority(si.next().getResource()));
      }
    }
    this.addPriority(resource, priority);
    return priority; 
  }
  
  /**
   * Is this a template-inheritable property?
   * 
   * @param property The property
   * 
   * @return True if the property is inherited
   */
  public boolean inherit(Property property) {
    Boolean inherit = this.inherits.get(property);
    
    if (inherit != null)
      return inherit;
    synchronized (this.inherits) {
      inherit = !this.model.containsLiteral(property, RPG.inherit, false);
      this.inherits.put(property, inherit);
    }
    return inherit;
  }

  /**
   * Find the a list of possible formats of the resource.
   * <p>
   * Formats are chosen according to the following order:
   * <ol>
   * <li>If the resource is a class, then the super-classes are explored in the same way.</li>
   * <li>Otherwise the classes of the resource are explored.</li>
   * <li>If all else fails, then there is no format (null).</li>
   * </ol>
   * <p>
   * The search is breadth-first, so the lowest super-object that has a specific format is
   * chosen. Formats are cached, for efficiency.
   * 
   * @param map The format map
   * @param resource The resource to find a format for
   * @param properties Properties to seacrh
   * 
   * @return The resource format
   */
  private List<Format> getFormats(Map<Resource, List<Format>> map, Resource resource) {
    List<Format> format = map.get(resource);
    StmtIterator si = null;

    if (format != null || map.containsKey(resource))
      return format;
    if (
        !resource.hasProperty(RDF.type, RDFS.Class) && 
        !resource.hasProperty(RDF.type, OWL.Class)
        ) {
      si = resource.listProperties(RDF.type);
      while (si.hasNext() && format == null)
        format = map.get(si.next().getResource());
    }
    synchronized (map) {
      map.put(resource, format);
    }
    return format; 
  }

  /**
   * @inheritDoc
   * 
   * Simply compares two resources, based on priority.
   * 
   * @param r1 The first resource
   * @param r2 The second resource
   * 
   * @return < 0 if r1 should come before r2, > 0 if r2 should come after r2 and 0 for no specific order
   */
  @Override
  public int compare(Resource r1, Resource r2) {
    int p1 = this.priority(r1);
    int p2 = this.priority(r2);

    return p1 - p2;
  }

  /**
   * Find a name for a resource.
   * <p>
   * Properties are tried in name order, looking for one which matches
   * the specified language. If the property has a literal, then the
   * literal is returned. If the property has a resource, then the
   * name of the resource is returned.
   * <p>
   * Languages use the XML form: language or language-country, all lower case.
   * 
   * @param resource The resource
   * @param language The language (if null, then the same as the no-language empty string)
   * @param loose True for loose matching (the name language starts with the language parameter)
   * 
   * @return A name matching the language, or null for not found
   */
  public String getName(Resource resource, String language, boolean loose) {
    StmtIterator si;
    Statement s;
    String name = null;

    if (language == null)
      language = "";
    for (Property property: this.nameOrder) {
      si = resource.listProperties(property);
      while (si.hasNext()) {
        s = si.next();
        if (s.getObject().isResource())
          name = this.getName(s.getResource(), language, loose);
        else if (s.getObject().isLiteral()) {
          String lang = s.getLanguage();

          if (loose && lang.startsWith(language))
            name = s.getString();
          else if (!loose && lang.equalsIgnoreCase(language))
            name = s.getString();
        }
        if (name != null)
          return name;
      }
    }
    return null;
  }

  /**
   * Get a name for this resource, based on locale.
   * <p>
   * First languages are tried in the following order:
   * language-country, language, language(loose), null, English (loose).
   * Where loose means that a country specific variant of the language
   * will be chosen even if it doesn't fit.
   * If there is no explicit name and the resource is not anonymous, then
   * the {@link Resource#getLocalName()} is returned.
   * 
   * @param resource The resource
   * @param locale The locale (if null, then only the default and English name are tried)
   * 
   * @return The name, or null for not found
   * 
   * @see #getName(Resource, String, boolean)
   */
  public String getName(Resource resource, Locale locale) {
    String name;
    if (locale != null) {
      if (locale.getCountry() != null)
        if ((name = this.getName(resource, locale.getLanguage() + "-" + locale.getCountry().toLowerCase(), false)) != null)
          return name;
      if ((name = this.getName(resource, locale.getLanguage(), false)) != null)
        return name;
      if ((name = this.getName(resource, locale.getLanguage(), true)) != null)
        return name;
    }
    if ((name = this.getName(resource, null, false)) != null)
      return name;
    if (locale == null || !locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
      if ((name = this.getName(resource, Locale.ENGLISH.getLanguage(), true)) != null)
        return name;
    }
    if (!resource.isAnon())
      return resource.getLocalName().isEmpty() ? resource.getURI() : resource.getLocalName();
    return null;
  }

  /**
   * Categorise a collection of statements.
   * <p>
   * Statements are categorised by predicate category.
   * 
   * @param statements The statements
   * 
   * @return The category tree for the statements.
   */
  public Category categorise(Collection<Statement> statements) {
    Category tc = new Category(this.top);

    for (Statement s: statements) {
      Resource category = this.category(s.getPredicate());

      tc.add(s, category);
    }
    return tc;
  }


  /**
   * Format a statement label.
   * <p>
   * If the predicate has a format that includes a label pattern,
   * then use the label pattern. Otherwise return the name of the
   * predicate.
   * 
   * @param statement The statement to format
   * @param locale The locale to format in
   * @param document The document to create the label for
   * 
   * @return True if there was a format and that format was successful
   */
  public Text formatStatementLabel(Statement statement, Locale locale) {
    Property predicate = statement.getPredicate();
    RDFNode object = statement.getObject();
    
    return formatLabel(predicate, object, locale);
  }

  /**
   * Format a statement value.
   * <p>
   * If the property has a format that includes a value pattern,
   * then use the value pattern. Otherwise, if the value has a
   * format itself then use that format.
   * 
   * @param statement The statement to format
   * @param locale The locale to format in
   * @param reference Is this a reference (use the subject) or not (use the object)
   * 
   * @return The resulting DOM object, or null for not formatted
   */
  public Text formatStatementValue(Statement statement, Locale locale, boolean reference) {
    RDFNode object = reference ? statement.getSubject() : statement.getObject();
    
    return this.format(object, locale);
  }
  
  /**
   * Format a statement label.
   * <p>
   * If a resource has a format then that is used against the statement object.
   * Otherwise, the predicate label is used.
   * 
   * @param node The node to format
   * @param locale The locale to format against
   * 
   * @return The formatted value, or null for none
   */
  public Text formatLabel(Property predicate, RDFNode node, Locale locale) {
    List<Format> formats;
    Text text = null;
    
    formats = this.getFormats(this.labelFormats, predicate);
    if (formats != null)
      for (Format format: formats)
        if ((text = format.format(node, this, locale)) != null)
          return text;
    return new LinkText(predicate.getURI(), new LiteralText(this.getName(predicate, locale)));
    }

  /**
   * Format a node.
   * <p>
   * Literals are formatted into a default pattern.
   * If a resource has a format then that is used.
   * Resources with URIs link to the URI.
   * Anonymous resources
   * 
   * @param node The node to format
   * @param locale The locale to format against
   * 
   * @return The formatted value, or null for none
   */
  public Text format(RDFNode node, Locale locale) {
    Resource resource;
    List<Format> formats;
    Text text = null;
    
    if (node.isLiteral())
      return AbstractFormat.defaultLiteral(node.asLiteral(), locale);
    resource = node.asResource();
    formats = this.getFormats(this.valueFormats, resource);
    if (formats != null)
      for (Format format: formats)
        if ((text = format.format(node, this, locale)) != null)
          return text;
    return this.defaultFormat.format(node, this, locale);
    }

  /**
   * Get a resource comparator for sorting.
   * 
   * @param locale The sort locale
   * 
   * @return A resource comparator that sorts resources by priority and by name
   */
  public Comparator<Resource> getResourceComparator(Locale locale) {
    return new ResourceComparator(locale);
  }

  /**
   * Get a statement comparator for sorting.
   * 
   * @param locale The sort locale
   * 
   * @return A statement comparator that sorts statements by predicate and object
   */
  public Comparator<Statement> getStatementComparator(Locale locale) {
    return new StatementComparator(locale);
  }

  /**
   * Get a statement property comparator for sorting.
   * 
   * @return A property comparator that sorts statements by property inheritance
   */
  public Comparator<Statement> getStatementPropertyComparator() {
    return new StatementPropertyComparator();
  }

  /**
   * Get a class property comparator for sorting.
   * 
   * @return A class comparator that sorts statements by class inheritance
   */
  public Comparator<Resource> getClassComparator() {
    return new ClassComparator();
  }

  /**
   * A comparator that sorts resources.
   * <p>
   * Resources are first sorted by priority and then by name.
   */
  private class ResourceComparator implements Comparator<Resource> {
    public Locale locale;
    public Collator collator;

    public ResourceComparator(Locale locale) {
      super();
      this.locale = locale;
      this.collator = Collator.getInstance(this.locale);
    }

    @Override
    public int compare(Resource o1, Resource o2) {
      int result = DisplaySorter.this.compare(o1, o2);
      String s1, s2;

      if (result != 0)
        return result;
      s1 = DisplaySorter.this.getName(o1, this.locale);
      s2 = DisplaySorter.this.getName(o2, this.locale);
      if (s1 == null)
        s1 = o1.getLocalName();
      if (s2 == null)
        s2 = o2.getLocalName();
      if (s1 == null)
        s1 = o1.toString();
      if (s2 == null)
        s2 = o2.toString();
      return this.collator.compare(s1, s2);
    } 
  }

  /**
   * A comparator that sorts statements.
   * <p>
   * Statements are sorted first by predicate, then by object.
   * Within objects, literals come before resources.
   * Literals are sorted lexically, resources by name.
   */
  private class StatementComparator implements Comparator<Statement> {
    private ResourceComparator rc;

    public StatementComparator(Locale locale) {
      super();
      this.rc = new ResourceComparator(locale);
    }

    @Override
    public int compare(Statement o1, Statement o2) {
      Property p1 = o1.getPredicate();
      Property p2 = o2.getPredicate();
      int result = this.rc.compare(p1, p2);
      RDFNode v1, v2;

      if (result != 0)
        return result;
      v1 = o1.getObject();
      v2 = o2.getObject();
      if (v1.isLiteral()) {
        if (v2.isLiteral())
          return this.rc.collator.compare(v1.asLiteral().getLexicalForm(), v2.asLiteral().getLexicalForm());
        else
          return -1;
      }
      if (v2.isLiteral())
        return 1;
      return this.rc.compare(v1.asResource(), v2.asResource());     
    }
  }

  /**
   * A comparator that sorts classess into class order.
   * <p>
   * Classes are sorted into inheritance order, with the 
   * bottom (most specific) classes first.
   */
  private class ClassComparator implements Comparator<Resource> {

    public ClassComparator() {
      super();
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public int compare(Resource o1, Resource o2) {
      Integer p1 = DisplaySorter.this.classOrder.get(o1);
      Integer p2 = DisplaySorter.this.classOrder.get(o2);

      if (p1 != null && p2 != null) {
        return p1.intValue() - p2.intValue();
      }
      if (o1.isAnon() && !o2.isAnon())
        return 1;
      if (o2.isAnon() && !o1.isAnon())
        return -1;
      if (p1 != null && p2 == null)
        return -1;
      if (p1 == null && p2 != null)
        return 1;
      return 0;
    } 
  }
  /**
   * A comparator that sorts statements into property order.
   * <p>
   * Statements are sorted into inheritance order, with the 
   * bottom (most specific) properties first.
   */
  private class StatementPropertyComparator implements Comparator<Statement> {

    public StatementPropertyComparator() {
      super();
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public int compare(Statement o1, Statement o2) {
      Integer p1 = DisplaySorter.this.propertyOrder.get(o1.getPredicate());
      Integer p2 = DisplaySorter.this.propertyOrder.get(o2.getPredicate());

      if (p1 != null && p2 != null) {
        int r = p1.intValue() - p2.intValue();
        if (r == 0 && o1.getObject().isResource() && o2.getObject().isResource()) {
          p1 = DisplaySorter.this.classOrder.get(o1.getResource());
          p2 = DisplaySorter.this.classOrder.get(o1.getResource());

          if (p1 != null && p2 != null)
            r = p1.intValue() - p2.intValue();
        }
        return r;
      }
      if (p1 == null && p2 != null)
        return -1;
      if (p1 != null && p2 == null)
        return 1;
      return 0;
    } 
  }
}
