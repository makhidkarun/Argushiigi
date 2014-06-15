/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * A pattern that represents a literal value.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class LiteralPattern extends AbstractFormat {
  /** The literal value */
  private Literal literal;
  
  /**
   * Constructor for an empty LiteralPattern.
   */
  public LiteralPattern() {
  }
  

  /**
   * Constructor for given literal.
   *
   * @param literal The literal value
   */
  public LiteralPattern(Literal literal) {
    super();
    this.literal = literal;
  }

  /**
   * @inheritDoc
   *
   * @see org.charvolant.argushiigi.ontology.AbstractFormat#isRequired()
   */
  @Override
  public boolean isRequired() {
    return true;
  }

  /**
   * @inheritDoc
   * <p>
   * Returns the literal value, formatted according to the default locale
   * for that value.
   *
   * @see org.charvolant.argushiigi.ontology.AbstractFormat#format(com.hp.hpl.jena.rdf.model.RDFNode, org.charvolant.argushiigi.ontology.DisplaySorter, java.util.Locale)
   */
  @Override
  public Text format(RDFNode value, DisplaySorter sorter, Locale locale) {
    return this.defaultLiteral(this.literal, locale);
  }

}
