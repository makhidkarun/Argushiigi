/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.charvolant.argushiigi.ontology.Argushiigi;
import org.charvolant.argushiigi.ontology.Category;
import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.charvolant.argushiigi.ontology.Text;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Render a resource as an XML document.
 * <p>
 * This document can either be used directly or via a
 * XSLT transform to make something more friendly.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class XmlRenderer extends Renderer implements HrefBuilder {
  private static final Logger logger = LoggerFactory.getLogger(XmlRenderer.class);

  /** The namespace URI for the document (same as the Argushiigi namespace) */
  public static final String NS = Argushiigi.NS;

  /** The schema for this document */
  private static Schema schema = null;

  /** The document representation */
  private DomRepresentation representation;
  /** The document */
  private Document document;
  /** The current dom element */
  private Element current;

  /**
   * Construct an XmlRenderer.
   *
   * @param locale The locale
   * @param sorter The sorter
   * @param application The application this is done for
   * @param request The request to render
   * 
   * @throws Exception if unable to construct the renderer
   */
  public XmlRenderer(Locale locale, DisplaySorter sorter, ServerApplication application, Request request) throws Exception {
    super(locale, sorter, application, request);
    this.representation = new DomRepresentation(MediaType.TEXT_XML);
    this.document = this.representation.getDocument();
    this.representation.setLanguages(this.getLanguages());
  }

  /**
   * Get the schema the generated XML conforms to.
   * 
   * @return The schema
   */
  synchronized public static Schema getSchema() {
    if (schema == null) {
      try {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = factory.newSchema(XmlRenderer.class.getResource("argushiigi.xsd"));
      } catch (SAXException ex) {
        logger.error("Unable to intialise schema", ex);
      }
    }
    return schema;
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
    Reference ref = new Reference(this.resource.getURI());

    this.representation.setIndenting(true);
    this.representation.setNamespaceAware(true);
    this.representation.getNamespaces().put("ag", this.NS);
    this.representation.setSchema(this.getSchema());
    this.current = this.document.createElementNS(this.NS, "ag:resource");
    this.document.appendChild(this.current);
    this.current.setAttributeNS(this.NS, "ag:name", this.sorter.getName(this.resource, this.locale));
    this.current.setAttributeNS(this.NS, "ag:uri", ref.toString());
    this.current.setAttributeNS(this.NS, "ag:href", this.asHref(ref));
    this.current.setAttributeNS(this.NS, "ag:base", this.application.getApparentRef().toString());
  }

  /**
   * Add an XML statement.
   * 
   * @param statement The statement
   * @param parent The parent element
   * @param reference Is this a reference to the main resource?
   * @param direct Is this a direct reference to the main resource?
   */
  private void addStatement(Statement statement, Element parent, boolean reference, boolean direct) {
    Element prop, key, val; 
    Text keyText, valueText;
    XmlTextVisitor xv;
    
    prop = this.document.createElementNS(this.NS, "ag:property");
    prop.setAttributeNS(this.NS, "ag:direct", direct ? "true" : "false");
    parent.appendChild(prop);
    key = this.document.createElementNS(this.NS, "ag:key");
    keyText = this.sorter.formatStatementLabel(statement, this.locale);
    key.setAttributeNS(this.NS, "ag:name", keyText.toString());
    xv = new XmlTextVisitor(this.document, key, this);
    keyText.visit(xv);
    prop.appendChild(key);
    val = this.document.createElementNS(this.NS, "ag:value");
    valueText = this.sorter.formatStatementValue(statement, this.locale, reference);
    val.setAttributeNS(this.NS, "ag:name", valueText.toString());
    xv = new XmlTextVisitor(this.document, val, this);
    valueText.visit(xv);
    prop.appendChild(val);
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
   * Start a category visit.
   * <p>
   * If this is the top category, then build the header.
   * Otherwise, render the title.
   *
   * @see org.charvolant.argushiigi.ontology.CategoryVisitor#visit(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  public void visit(Category category) {
    Element previous = this.current;

    category.sort(this.resourceComparator, this.statementComparator);
    this.current = this.document.createElementNS(this.NS, "ag:category");
    this.current.setAttributeNS(this.NS, "ag:name", this.sorter.getName(category.getCategory(), this.locale));
    this.current.setAttributeNS(this.NS, "ag:id", "category-" + category.getCategory().getLocalName());
    previous.appendChild(this.current);
  }

  /**
   * Once we have visited the categories, add elements for
   * any statements that we have and pop up to the parent
   * category. 
   *
   * @see org.charvolant.argushiigi.ontology.CategoryVisitor#visited(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  public void visited(Category category) {
    for (Statement statement: category.getStatements()) {
      this.addStatement(statement, this.current, category.isReference(), statement.getSubject().equals(this.resource));
    }
    this.current = (Element) this.current.getParentNode();
  }
  
  /**
   * Convert a reference into a local reference.
   * <p>
   * If there is a fragment, then use the uri parameter,
   * otherwise, directly translate the reference to a local
   * reference.
   * 
   * @param ref The original reference
   * 
   * @return A suitable href
   */
  @Override
  public String asHref(Reference ref) {
    if (this.isExternal(ref))
      return ref.toString();
    if (ref.hasFragment()) {
      Reference local = new Reference(this.application.getApparentRef());
      
      local.addQueryParameter(RendererResource.PARAM_URI, ref.encode(ref.toString(), true));
      return local.toString();
    }
    return this.application.getLocalRef(ref).toString();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.charvolant.argushiigi.server.HrefBuilder#isExternal(org.restlet.data.Reference)
   */
  @Override
  public boolean isExternal(Reference ref) {
    return !this.application.getRootRef().isParent(ref);
  }

  /**
   * Get the DOM representation for this renderer
   *
   * @see org.charvolant.argushiigi.server.Renderer#getRepresentation()
   */
  @Override
  public DomRepresentation getRepresentation() {
    return this.representation;
  }

}
