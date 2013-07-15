/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

/**
 * A vistor for category items.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public interface CategoryVisitor {
  /**
   * Respond to visiting a category before processing sub-categories.
   * 
   * @param category
   */
  public void visit(Category category);
  
  /**
   * Respond to visiting a category after processing sub-categories.
   * 
   * @param category
   */
  public void visited(Category category);
}
