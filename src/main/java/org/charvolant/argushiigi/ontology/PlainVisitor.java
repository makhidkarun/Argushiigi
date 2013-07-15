/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

/**
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class PlainVisitor implements TextVisitor {
  /** The buffer used to build the string */
  private StringBuffer buffer;
  
  /**
   * Default constructor.
   */
  public PlainVisitor() {
    this.buffer = new StringBuffer();
  }

  /**
   * Get the built string.
   * 
   * @return The string built from the resulting text.
   */
  public String getString() {
    return this.buffer.toString();
  }

  /**
   * @inheritDoc
   * <p>
   * Add the literal value to the string.
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.LiteralText)
   */
  @Override
  public void visited(LiteralText text) {
    this.buffer.append(text.getLiteral());
  }

  /**
   * @inheritDoc
   * <p>
   * Add the underlying text and ignore the link.
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.LinkText)
   */
  @Override
  public void visited(LinkText text) {
    text.getText().visit(this);
  }

  /**
   * @inheritDoc
   * <p>
   * Add the components of the composite.
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.CompositeText)
   */
  @Override
  public void visited(CompositeText text) {
    for (Text component: text.getComponents())
      component.visit(this);
  }  
}
