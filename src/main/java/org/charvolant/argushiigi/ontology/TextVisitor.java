/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

/**
 * A visitor for value text.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public interface TextVisitor {
  /**
   * Respond to a visitation to some literal value text.
   * 
   * @param text The text element
   */
  public void visited(LiteralText text);
  
  /**
   * Respond to a visitation to some linked text.
   * 
   * @param text The text element
   */
  public void visited(LinkText text);
  
  /**
   * Respond to a visitation to some linked text.
   * 
   * @param text The text element
   */
  public void visited(CompositeText text);
}
