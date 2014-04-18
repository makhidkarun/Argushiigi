/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.charvolant.argushiigi.ontology.Category;
import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.charvolant.argushiigi.ontology.RPG;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
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
public class RendererResource extends ServerResource {
  private static final Logger logger = LoggerFactory.getLogger(RendererResource.class);

  /** The URI parameter */
  public static final String PARAM_URI = "uri";
  /** The format parameter */
  public static final String PARAM_FORMAT = "format";
  /** The xml format  */
  public static final String FORMAT_XML = "xml";
  /** The html format  */
  public static final String FORMAT_HTML = "html";
  /** The rdf (rdf/xml) format  */
  public static final String FORMAT_RDF = "rdf";
  /** The rdf (turtle) format  */
  public static final String FORMAT_TURTLE = "ttl";

  /** The application */
  private ServerApplication application;
  /** The display sorter (from the application) */
  private DisplaySorter sorter;
  
  
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
  private List<Statement> buildStatementList(Resource resource, Model model) {
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
   * from the templates are added, provided that the properties haven't
   * already been added.
   * 
   * @param top The top category
   * @param resource The resource
   * @param model The model to use
   * @param templates Include template information
   * 
   * @return The top category
   */
  private void buildStatements(Category top, Resource resource, Model model, boolean templates) {
    Set<Property> seen, nseen;
    Set<Resource> work, nwork;
    StmtIterator si;
    Resource category;

    seen = new HashSet<Property>(64);
    work = new HashSet<Resource>();
    work.add(resource);
    while (!work.isEmpty()) {
      nwork = new HashSet<Resource>();
      nseen = new HashSet<Property>();
      for (Resource wr: work) {
        for (Statement s: this.buildStatementList(wr, model)) {
          if (!seen.contains(s.getPredicate())) {
            category = this.sorter.category(s.getPredicate());
            top.add(s, category);
            nseen.add(s.getPredicate());
          }
        }
        if (templates) {
          si = wr.listProperties(RPG.template);
          while (si.hasNext()) nwork.add(si.next().getResource());
        }
      }
      work = nwork;
      seen.addAll(nseen);
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
  private void buildReferences(Category top, Resource resource, Model model) {
    StmtIterator si = model.listStatements(null, null, resource);
    Statement statement;
    
    while (si.hasNext()) {
      statement = si.next();
      if (!this.ignore(statement))
        top.add(statement, this.sorter.getReference());
    }
  }

  /**
   * Initialise the resource by collecting the contextual
   * information from the {@link ServerApplication} application. 
   *
   * @see org.restlet.resource.UniformResource#doInit()
   */
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();
    this.application = (ServerApplication) this.getApplication();
    this.sorter = this.application.getSorter();
  }

  @Get
  public Representation get() {
    String format = this.getQueryValue(this.PARAM_FORMAT);
    Reference uri = null;
    Resource resource;
    Category top = new Category(this.sorter.getTop());
    Category references = new Category(this.sorter.getReference(), true);
    Renderer renderer;

    top.getSubCategories().add(references);
    if (format == null)
      format = this.FORMAT_HTML;
    if (this.getQueryValue(this.PARAM_URI) != null)
      uri = new Reference(Reference.decode(this.getQueryValue(this.PARAM_URI)));
    else {
      uri = this.application.getExternalRef(this.getOriginalRef());
      uri.setQuery(null);
    }
    try {
      resource = this.application.getInference().createResource(uri.toString());
      if (format.equals(this.FORMAT_HTML) || format.equals(this.FORMAT_XML)) {
        this.buildStatements(top, resource, this.application.getInference(), true);
        this.buildReferences(top, resource, this.application.getInference());
      } else {
        this.buildStatements(top, resource, this.application.getOntology(), false);
        this.buildStatements(top, resource, this.application.getDataset(), false);
      }
      if (format.equals(this.FORMAT_XML))
        renderer = new XmlRenderer(Locale.ENGLISH, this.sorter, this.application);
      else if (format.equals(this.FORMAT_RDF))
        renderer = new RdfRenderer(Locale.ENGLISH, this.sorter, this.application, false);
      else if (format.equals(this.FORMAT_TURTLE))
        renderer = new RdfRenderer(Locale.ENGLISH, this.sorter, this.application, true);
      else
        renderer = new HtmlRenderer(Locale.ENGLISH, this.sorter, this.application);
      renderer.render(resource, top);
      return renderer.getRepresentation();
    } catch (Exception ex) {
      this.logger.error("Unable to get resource " + uri, ex);
      this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, ex);
      return new StringRepresentation(ex.getMessage());
    }
  }
}
