/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;

/**
 * A format for a statement or an individual.
 * <p>
 * Derived from the {@link Argushiigi#Format} class.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class Format extends AbstractFormat implements Comparable<Format> {
  /** The format priority */
  private int priority;
  /** The locale this format is for */
  private Locale locale;
  /** The format patterns */
  private List<AbstractFormat> patterns;
  
  /**
   * Default constructor
   */
  public Format() {
    this.priority = Integer.MIN_VALUE;
    this.patterns = new ArrayList<AbstractFormat>();
  }
  
  /**
   * Construct a format based on a resource.
   *
   * @param resource The format resource
   */
  public Format(Resource resource) {
    this();
    
    Seq pattern;
    NodeIterator pi;
    RDFNode node;
    
    if (resource.hasProperty(Argushiigi.priority))
      this.priority = resource.getProperty(Argushiigi.priority).getInt();
    if (resource.hasProperty(Argushiigi.language))
      this.locale = Locale.forLanguageTag(resource.getProperty(Argushiigi.language).getString());
    if (!resource.hasProperty(Argushiigi.pattern))
      throw new IllegalArgumentException("Expecting " + Argushiigi.pattern);
    pattern = resource.getProperty(Argushiigi.pattern).getSeq();
    pi = pattern.iterator();
    while (pi.hasNext()) {
      node = pi.next();
      this.patterns.add(AbstractFormat.create(node));
    }
  }
  
  /**
   * Format a value.
   * <p>
   * Formatting may fail a required pattern is unable to format its value.
   * 
   * @param value The value to format
   * @param sorter The sorter
   * @param locale The locale to format under
   * 
   * @return The formatted value, or null if not able to format the value
   */
  public Text format(RDFNode value, DisplaySorter sorter, Locale locale) {
    Text text = null;
    Text item;
    
    if (this.locale != null && (
        locale == null ||
        !this.locale.getLanguage().equals(locale.getLanguage()) || 
        (
            !this.locale.getCountry().isEmpty() && 
            !this.locale.getCountry().equals(locale.getCountry())
        )
    ))
      return null;
    for (AbstractFormat pattern: this.patterns) {
      item = pattern.format(value, sorter, locale);
      if (item != null)
        text = Text.compose(text, item);
      else if (pattern.isRequired())
        return null;
    }
    return text;
  }

  /**
   * Compare two formats based on priority.
   * 
   * @param o The other format
   * 
   * @return A comparison of the priorities
   */
  @Override
  public int compareTo(Format o) {
    return this.priority - o.priority;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Actual formats are not required.
   *
   * @see org.charvolant.argushiigi.ontology.AbstractFormat#isRequired()
   */
  @Override
  public boolean isRequired() {
    return false;
  }

}
