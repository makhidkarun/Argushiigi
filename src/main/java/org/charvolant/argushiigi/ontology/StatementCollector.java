/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 * Render a specific URI.
 * <p>
 * Representation is based on the chosen format.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class StatementCollector {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(StatementCollector.class);

  /** The display sorter (from the application) */
  private DisplaySorter sorter;


  /**
   * Construct a statement collector.
   *
   * @param sorter
   */
  public StatementCollector(DisplaySorter sorter) {
    super();
    this.sorter = sorter;
  }

  /**
   * Should we ignore this statement?
   * <p>
   * Because it is a redundant piece of information from the reasoner, usually.
   *  
   * @param statement The statement to check
   * 
   * @return True if it should be ignored
   */
  private boolean ignore(Statement statement) {
    Resource subject = statement.getSubject();
    Property predicate = statement.getPredicate();
    RDFNode object = statement.getObject();

    // Ignore self-references
    if (object.equals(subject))
      return true;
    // Ignore A p Resource
    if (object.equals(RDFS.Resource))
      return true;
    // Ignore A p Thing
    if (object.equals(OWL.Thing))
      return true;    
    // Ignore A differentFrom B
    if (predicate.equals(OWL.differentFrom))
      return true;
    // Ignore A sameAs A
    if (predicate.equals(OWL.sameAs) && object.equals(subject))
      return true;
    // Ignore A equivalentClass A
    if (predicate.equals(OWL.equivalentClass) && object.equals(subject))
      return true;
    // Ignore A subPropertyOf A
    if (predicate.equals(RDFS.subPropertyOf) && object.equals(subject))
      return true;
    // Ignore A subClassOf A
    if (predicate.equals(RDFS.subClassOf) && object.equals(subject))
      return true;
    // Ignore A p Resource
    if (object.equals(RDFS.Resource))
      return true;
    // Ignore A p Thing
    if (object.equals(OWL.Thing))
      return true;  
    return false;
  }

  /**
   * Build a filtered list of statements.
   * <p>
   * Statements that effectively echo more specific statements,
   * either through property inheritance or class inheritance
   * are not included.
   * <p>
   * Boring tautologies, such as type of {@link RDFS#Resource}
   * or A {@link OWL#sameAs} A are also ignored.
   * 
   * @param resource The resource to get statements for
   * @param model The model to use
   * 
   * @return A filtered list of the resource statements
   */
  public List<Statement> buildStatementList(Resource resource, Model model) {
    StmtIterator si = model.listStatements(resource, null, (Resource) null);
    List<Statement> stmts = new ArrayList<Statement>(32);
    List<Statement> filtered = new ArrayList<Statement>(32);
    Map<RDFNode, Set<Property>> seen = new HashMap<RDFNode, Set<Property>>(32);
    Map<Property, Set<Resource>> seenClasses = new HashMap<Property, Set<Resource>>(32);

    while (si.hasNext()) stmts.add(si.next());
    Collections.sort(stmts, this.sorter.getStatementPropertyComparator());
    for (Statement s: stmts) {
      RDFNode object = s.getObject();
      Property predicate = s.getPredicate();
      Set<Property> sp = seen.get(object);
      boolean found = false;

      if (this.ignore(s))
        continue;
      // Ignore A p B where A q B has been seen and q -> p
      if (sp == null) {
        sp = new HashSet<Property>();
        seen.put(object, sp);
      } else {
        for (Property p: sp) {
          if (p.hasProperty(RDFS.subPropertyOf, predicate)) {
            found = true;
            break;
          }
        }
      }

      // Ignore A p C where C is a class and A q C with q -> p has been seen 
      if (!found && object.isResource()) {
        Resource r = object.asResource();
        if (r.hasProperty(RDF.type, RDFS.Class) || r.hasProperty(RDF.type, OWL.Class)) {
          Set<Resource> sc;

          if (r.isAnon()) // Skip anonymous classes
            continue;
          sc = seenClasses.get(predicate);
          if (sc == null) {
            sc = new HashSet<Resource>();
            seenClasses.put(predicate, sc);
          } else {
            for (Resource c: sc)
              if (c.hasProperty(RDFS.subClassOf, r)) {
                found = true;
                break;
              }
          }
          if (!found)
            sc.add(r);
        }
      }
      if (!found) {
        filtered.add(s);
        sp.add(predicate);
      }
    }
    return filtered;
  }

  /**
   * Build a list of statements to display.
   * <p>
   * The there is a statement with a property that is a super-property of
   * another statement and that statement has the same value, then
   * that property is ignored.
   * <p>
   * If the resource has one or more templates, then information
   * from the templates are added, provided that there isn't already
   * a suitable property that has already been added.
   * <p>
   * 
   * @param top The top category
   * @param resource The resource
   * @param model The model to use
   * @param templates Include template information
   * 
   * @return The top category
   */
  public void buildStatements(Category top, Resource resource, Model model, boolean templates) {
    Set<Seen> seen, nseen;
    Set<Resource> work, nwork;
    StmtIterator si;
    Resource category;
    Seen sn;
    boolean original = true;

    seen = new HashSet<Seen>(64);
    work = new HashSet<Resource>();
    work.add(resource);
    while (!work.isEmpty()) {
      nwork = new HashSet<Resource>();
      nseen = new HashSet<Seen>();
      for (Resource wr: work) {
        for (Statement s: this.buildStatementList(wr, model)) {
          if (!original && !this.sorter.inherit(s.getPredicate()))
            continue;
          sn = new Seen(s);
          if (seen.contains(sn))
            continue;
            category = this.sorter.category(s.getPredicate());
            top.add(s, category);
            nseen.add(sn);
        }
        if (templates) {
          si = wr.listProperties(RPG.template);
          while (si.hasNext()) nwork.add(si.next().getResource());
        }
      }
      work = nwork;
      seen.addAll(nseen);
      original = false;
    }
  }

  /**
   * Get a list of references to the resource.
   * 
   * @param resource The resource
   * @param model The model to query
   * 
   * @return A list of references
   */
  public void buildReferences(Category top, Resource resource, Model model) {
    StmtIterator si = model.listStatements(null, null, resource);
    Statement statement;
    Category references = null;

    while (si.hasNext()) {
      statement = si.next();
      if (!this.ignore(statement)) {
        if (references == null) {
          references = new Category(this.sorter.getReference(), true);
          top.getSubCategories().add(references);
        }
        top.add(statement, this.sorter.getReference());
      }
    }
  }
  
  /**
   * A class of things already seen.
   * <p>
   * This can be used by the template system to determine
   * whether something already exists or not.
   * At it's base level, this uses a property, with a possible
   * additional resource.
   * 
   * 
   * @author Doug Palmer <doug@charvolant.org>
   *
   */
  private static class Seen {
    private Property property;
    private RDFNode node;
    private Seen next;
    
    public Seen(Property property, RDFNode node) {
      this.property = property;
      this.node = node;
      this.next = null;
    }
   
    @SuppressWarnings("unused")
    public Seen(Property property) {
      this(property, null);
    }
    
    public Seen(Statement statement) {
      Resource discriminator;
      RDFNode object = statement.getObject();
      Statement ds;
      
      this.property = statement.getPredicate();
      discriminator = this.property.getPropertyResourceValue(RPG.discriminator);
      if (discriminator != null && !(discriminator instanceof Property))
        discriminator = discriminator.getModel().createProperty(discriminator.getURI());
      if (discriminator != null && object.isResource()) {
        ds = object.asResource().getProperty((Property) discriminator);
        if (ds != null)
          this.node = ds.getObject();
      }       
    }

    /**
     * @inheritDoc
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.property.getURI().hashCode();
      if (this.node != null)
        if (this.node.isLiteral())
          result = prime * result + this.node.asLiteral().getLexicalForm().hashCode();
        else if (this.node.isAnon())
          result = prime * result + this.node.asResource().getId().hashCode();
        else
          result = prime * result + this.node.asResource().getURI().hashCode();
      result = prime * result + ((this.next == null) ? 0 : this.next.hashCode());
      return result;
    }

    /**
     * @inheritDoc
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Seen other = (Seen) obj;
      if (!this.property.getURI().equals(other.property.getURI()))
        return false;
      if (this.node == null) {
        if (other.node != null)
          return false;
      } else {
        if (other.node == null)
          return false;
        if (this.node.isLiteral()) {
          if (!other.node.isLiteral() || !this.node.asLiteral().getLexicalForm().equals(other.node.asLiteral().getLexicalForm()))
            return false;
        } else if (this.node.isAnon()) {
          if (!other.node.isAnon() || !this.node.asResource().getId().equals(other.node.asResource().getId()))
            return false;
        } else {
          if (!other.node.isResource() || !this.node.asResource().getURI().equals(other.node.asResource().getURI()))
            return false;
        }
      }
      if (this.next == null) {
        if (other.next != null)
          return false;
      } else if (!this.next.equals(other.next))
        return false;
      return true;
    }
  }
}
