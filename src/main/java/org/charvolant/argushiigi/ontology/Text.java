/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

/**
 * A textual representation of a value.
 * <p>
 * This is partially structured text, intended to allow
 * links to be embedded in formatted text.
 * <p>
 * Instances of this class are expected to be immutable once
 * created.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
abstract public class Text {

  /**
   * Default constructor
   */
  public Text() {
    super();
  }
  
  /**
   * Respond to a visitor.
   * 
   * @param visitor The visitor
   */
  abstract public void visit(TextVisitor visitor);
  
  /**
   * Compose two texts.
   * <p>
   * Literals are combined into a single literal.
   * If either the first or second text element is null, the other is returned.
   * 
   * @param first The first text to compose
   * @param second The second text to compose
   * 
   * @return The combined text
   */
  public static Text compose(Text first, Text second) {
    if (first == null)
      return second;
    if (second == null)
      return first;
    if (first instanceof LiteralText && second instanceof LiteralText)
      return new LiteralText(((LiteralText) first).getLiteral() + ((LiteralText) second).getLiteral());
    return new CompositeText(first, second);
  }
  
  /**
   * Provide a string representation of this text.
   * 
   * @return A plain text version of the value.
   * 
   * @see PlainVisitor
   */
  public String toString() {
    PlainVisitor visitor = new PlainVisitor();
    
    this.visit(visitor);
    return visitor.getString();
  }
  
}
