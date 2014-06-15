/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.charvolant.argushiigi.ontology.Category;
import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.restlet.Request;
import org.restlet.data.Language;
import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Link;
import org.restlet.ext.rdf.Literal;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Render a resource as RDF document.
 * <p>
 * This document can either be used directly or via a
 * XSLT transform to make something more friendly.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class RdfRenderer extends Renderer {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(RdfRenderer.class);

  /** The blank reference format */
  private static final NumberFormat BLANK_FORMAT = new DecimalFormat("0000");
  
  /** Use turtle format? */
  private boolean turtle;
  /** The graph */
  private Graph graph;
  /** The blank reference counter */
  private int blanks;

  /**
   * Construct an RdfRenderer.
   *
   * @param locale The locale
   * @param sorter The sorter
   * @param application The application this is done for
   * @param request The request to render
   * @param turtle Use turtle format for output (otherwise RDF/XML)
   * 
   * @throws Exception if unable to construct the renderer
   */
  public RdfRenderer(Locale locale, DisplaySorter sorter, ServerApplication application, Request request, boolean turtle) throws Exception {
    super(locale, sorter, application, request);
    this.turtle = turtle;
    this.graph = new Graph();  
  }

  /**
   * Perform pre-rendering activities.
   * <p>
   * Set up the namespaces and the current element.
   *
   * @see org.charvolant.argushiigi.server.Renderer#preRender(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  protected void preRender(Category top) {
  }



  /**
   * Perform post-rendering activities.
   *
   * @see org.charvolant.argushiigi.server.Renderer#postRender(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  protected void postRender(Category top) {
  }


  /**
   * Add a statement.
   * <p>
   * If the object of the statement is an anonymous node,
   * then add the anonymous node, as well.
   * 
   * @param subject The subject to use when adding a statement
   * @param statement The statement
   */
  private void add(Reference subject, Statement statement) {
    String predicate = statement.getPredicate().getURI();
    
    if (statement.getObject().isLiteral()) {
      String lang = statement.getLanguage();
      Language language = lang == null ? null : new Language(lang);
      String datatypeUri = statement.getObject().asLiteral().getDatatypeURI();
      Reference datatype = datatypeUri == null ? null : new Reference(datatypeUri);
      
      Literal literal = new Literal(statement.getString(), datatype, language);
      this.graph.add(subject, predicate, literal);
    } else {
      Reference object;
      
      if (!statement.getObject().isAnon()) {
        object = new Reference(statement.getResource().getURI());
      } else {
        StmtIterator si = statement.getObject().asResource().listProperties();
        
        object = Link.createBlankRef(this.BLANK_FORMAT.format(this.blanks++));
        while (si.hasNext())
          this.add(object, si.next());
      }
      this.graph.add(subject, predicate, object);
    }
  }

  /**
   * Start a category visit.
   *
   * @see org.charvolant.argushiigi.ontology.CategoryVisitor#visit(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  public void visit(Category category) {
  }

  /**
   * Once we have visited the categories, add any statements from the category
   *
   * @see org.charvolant.argushiigi.ontology.CategoryVisitor#visited(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  public void visited(Category category) {
    for (Statement s: category.getStatements()) {
      Reference subject = new Reference(s.getSubject().getURI());
      
      this.add(subject, s);
    }
  }

  /**
   * Get the representation for this renderer.
   * <p>
   * This is either a turtle representation or RDF/XML representation.
   *
   * @see org.charvolant.argushiigi.server.Renderer#getRepresentation()
   */
  @Override
  public Representation getRepresentation() {
    return this.turtle ? this.graph.getRdfTurtleRepresentation() : this.graph.getRdfXmlRepresentation();
  }

}
