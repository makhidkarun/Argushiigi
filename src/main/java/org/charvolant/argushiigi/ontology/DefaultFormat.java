/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DefaultFormat extends AbstractFormat {

  /**
   * A default formatter for an object with no
   * specific format.
   * <p>
   * Literals are rendered as literals.
   * Resources that have a URI are rendered as links to the URI.
   * Anonymous resources are rendered as a list of properties.
   */
  public DefaultFormat() {
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

  /**
   * Format a value, ignoring already seen values
   * 
   * @param value The value
   * @param sorter The sorter for display purposes
   * @param locale The locale to use
   * @param seen Already seen resources
   * 
   * @return The formatted value
   */
  protected Text format(RDFNode value, DisplaySorter sorter, Locale locale, Set<Resource> seen) {
    Resource resource;
    Text text, predicate, object;
    StatementCollector collector;
    
    if (value.isLiteral())
      return this.defaultLiteral(value.asLiteral(), locale);
    resource = value.asResource();
    if (seen.contains(resource))
      return null;
    seen.add(resource);
    if (resource.isURIResource())
      return new LinkText(resource.getURI(), new LiteralText(sorter.getName(resource, locale)));
    collector = new StatementCollector(sorter);
    text = null;
    for (Statement s: collector.buildStatementList(resource, resource.getModel())) {
      predicate = this.format(s.getPredicate(), sorter, locale, seen);
      object = this.format(s.getObject(), sorter, locale, seen);
      if (predicate != null || object != null) {
        if (text != null)
          text = Text.compose(text, new LiteralText(", "));
        if (predicate != null) {
          text = Text.compose(text, predicate);
          text = Text.compose(text, new LiteralText(" "));
        }
        if (object != null)
          text = Text.compose(text, object);
      }
    }
    return text;
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
  @Override
  public Text format(RDFNode value, DisplaySorter sorter, Locale locale) {
    return this.format(value, sorter, locale, new HashSet<Resource>());
  }
  
  

}
