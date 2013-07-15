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
public class LiteralText extends Text {
  /** The literal */
  private String literal;

  /**
   * Construct for a literal.
   *
   * @param literal The literal.
   */
  public LiteralText(String literal) {
    super();
    this.literal = literal;
  }

  /**
   * Get the literal.
   *
   * @return the literal
   */
  public String getLiteral() {
    return this.literal;
  }

  /**
   * @inhertiDoc
   *
   * @see org.charvolant.argushiigi.ontology.Text#visit(org.charvolant.argushiigi.ontology.TextVisitor)
   * @see TextVisitor#visited(LiteralValueText)
   */
  @Override
  public void visit(TextVisitor visitor) {
    visitor.visited(this);
  }
}
