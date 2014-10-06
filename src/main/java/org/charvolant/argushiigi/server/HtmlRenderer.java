/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.charvolant.argushiigi.ontology.Category;
import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * A renderer that generates HTML for a resource
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class HtmlRenderer extends Renderer {
  private static final Logger logger = LoggerFactory.getLogger(HtmlRenderer.class);

  /** The message catalogue entries to pass in as variables */
  private static final String[] MESSAGES = {
    "label.copyright",
    "label.copyright.detail",
    "label.home",
    "label.home.detail",
    "label.html",
    "label.html.detail",
    "label.references",
    "label.references.detail",
    "label.rdf",
    "label.rdf.detail",
    "label.ttl",
    "label.ttl.detail",
    "label.xml",
    "label.xml.detail"
  };
  
  /** The transformation templates */
  private static Templates templates = null;
  
  /** The XML renderer */
  private XmlRenderer renderer;

  /**
   * Get the transformation template.
   * 
   * @return The transformation template
   */
  synchronized private static Templates getTemplates() {
    if (templates == null) {
    try {
      StreamSource source = new StreamSource(HtmlRenderer.class.getResourceAsStream("resource-html.xsl"));
      templates = TransformerFactory.newInstance().newTemplates(source);
    } catch (Exception ex) {
      logger.error("Unable to read template", ex);
    }
    }
    return templates;
  }
  
  /**
   * Constructor for HtmlRenderer.
   *
   * @param locale The locale
   * @param sorter The sorter
   * @param application The application
   * @param request The request
   * 
   * @throws Exception if unable to construct the renderer
   */
  public HtmlRenderer(Locale locale, DisplaySorter sorter, ServerApplication application, Request request) throws Exception {
    super(locale, sorter, application, request);
    this.renderer = new XmlRenderer(locale, sorter, application, request);
  }
  
  /**
   * Ignored, since this is all done by the XML renderer.
   *
   * @see org.charvolant.argushiigi.ontology.CategoryVisitor#visit(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  public void visit(Category category) {
  }

  /**
   * Ignored, since this is all done by the XML renderer.
   *
   * @see org.charvolant.argushiigi.ontology.CategoryVisitor#visited(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  public void visited(Category category) {
  }

  /**
   * Pre-render by generating an XML representaion of the resource.
   *
   * @see org.charvolant.argushiigi.server.Renderer#preRender(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  protected void preRender(Category top) {
    this.renderer.render(this.resource, top);
  }

  /**
   * Ignored, since this is all done by the XML renderer.
   *
   * @see org.charvolant.argushiigi.server.Renderer#postRender(org.charvolant.argushiigi.ontology.Category)
   */
  @Override
  protected void postRender(Category top) {
  }

  /**
   * Get the representation.
   * <p>
   * This is the XML representation, wrapped in a transformer.
   *
   * @see org.charvolant.argushiigi.server.Renderer#getRepresentation()
   */
  @Override
  public Representation getRepresentation() {
    Transformer transformer;
    DomRepresentation dr;
    DomRepresentation tr;
    DOMResult result;
    ResourceBundle bundle;
    
    try {
      bundle = ResourceBundle.getBundle("org.charvolant.argushiigi.server.Messages", this.locale);
      dr = this.renderer.getRepresentation();
      result = new DOMResult();
      transformer = this.getTemplates().newTransformer();
      for (String label: this.MESSAGES)
        transformer.setParameter(label, bundle.getString(label));
      transformer.setParameter("link.references", new Reference(this.request.getRootRef(), "data/query/references?resource=" + Reference.encode(this.resource.getURI(), true)).getTargetRef().toString());
      transformer.transform(dr.getDomSource(),result);
      tr = new DomRepresentation(MediaType.TEXT_HTML, (Document) result.getNode());
      tr.setIndenting(true);
      tr.setLanguages(dr.getLanguages());
      return tr;
    } catch (Exception ex) {
      this.application.getLogger().log(Level.SEVERE, "Unable to transform into HTML", ex);
      return new StringRepresentation("Unable to transform into HTML: " + ex.getMessage());
    }
  }

}
