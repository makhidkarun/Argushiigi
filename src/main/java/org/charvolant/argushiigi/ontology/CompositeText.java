/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value text that is a composite of other pieces of text.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class CompositeText extends Text {
  /** The things to form as a composite */
  private List<Text> components;

  /**
   * Construct from two texts.
   * <p>
   * Component composites are unwound.
   *
   * @param first The first element
   * @param second The second element
   */
  public CompositeText(Text first, Text second) {
    this.components = new ArrayList<Text>();
    if (first instanceof CompositeText)
      this.components.addAll(((CompositeText) first).components);
    else
      this.components.add(first);
    if (second instanceof CompositeText)
      this.components.addAll(((CompositeText) second).components);
    else
      this.components.add(second);
    this.components = Collections.unmodifiableList(this.components);
  }
  
  /**
   * Get the components.
   *
   * @return an unmodifiable the components that form this composite.
   */
  public List<Text> getComponents() {
    return this.components;
  }

  /**
   * @inheritDoc
   *
   * @see org.charvolant.argushiigi.ontology.Text#visit(org.charvolant.argushiigi.ontology.TextVisitor)
   */
  @Override
  public void visit(TextVisitor visitor) {
    visitor.visited(this);
  }

}
