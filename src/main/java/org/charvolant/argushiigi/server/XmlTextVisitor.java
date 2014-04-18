/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import org.charvolant.argushiigi.ontology.CompositeText;
import org.charvolant.argushiigi.ontology.LinkText;
import org.charvolant.argushiigi.ontology.LiteralText;
import org.charvolant.argushiigi.ontology.Text;
import org.charvolant.argushiigi.ontology.TextVisitor;
import org.restlet.data.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Convert 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class XmlTextVisitor implements TextVisitor {
  /** The document to create nodes for */
  private Document document;
  /** The top level element */
  private Element element;
  /** The builder of target references */
  private HrefBuilder builder;
  
  /**
   * Construct the visitor.
   *
   * @param document The document to add to
   * @param element The element that this text is to be added to
   * @param builder The resolvable reference builder
   */
  public XmlTextVisitor(Document document, Element element, HrefBuilder builder) {
    this.document = document;
    this.element = element;
    this.builder = builder;
  }

  /**
   * @inheritDoc
   * <p>
   * Append plain text as plain text.
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.LiteralText)
   */
  @Override
  public void visited(LiteralText text) {
    this.element.appendChild(this.document.createTextNode(text.getLiteral()));
  }

  /**
   * @inheritDoc
   * <p>
   * Add a link to the value.
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.LinkText)
   */
  @Override
  public void visited(LinkText text) {
    Element old = this.element;
    Reference uri = new Reference(text.getUri()).normalize();
    
    this.element = this.document.createElementNS(XmlRenderer.NS, "ag:link");
    this.element.setAttributeNS(XmlRenderer.NS, "ag:uri", uri.toString());
    this.element.setAttributeNS(XmlRenderer.NS, "ag:href", this.builder.asHref(uri));
    if (this.builder.isExternal(uri))
      this.element.setAttributeNS(XmlRenderer.NS, "ag:external", "true");
    text.getText().visit(this);
    old.appendChild(this.element);
    this.element = old;
  }

  /**
   * @inheritDoc
   * <p>
   * Process each component in turn.
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.CompositeText)
   */
  @Override
  public void visited(CompositeText text) {
    for (Text component: text.getComponents())
      component.visit(this);
  }

}
