/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.Locale;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * A sub-pattern that can be used to extend an existing pattern.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class SubPattern extends Pattern {
  /** The format to use */
  private Format format;

  /**
   * Constructor for and empty sub-pattern.
   *
   */
  public SubPattern() {
    super();
  }

  /**
   * Constructor for SubPattern.
   *
   * @param resource
   */
  public SubPattern(Resource resource) {
    super(resource);
    this.format = new Format(resource.getPropertyResourceValue(Argushiigi.format));
  }


  /**
   * {@inheritDoc}
   * <p>
   * Format a pattern based on the property and the sub-format to use.
   */
  @Override
  public Text format(RDFNode value, DisplaySorter sorter, Locale locale) {
    Resource rv;
    Text text = null;
    Text item;

    if (this.property != null) {
      if (!value.isResource())
        return null;
      rv = value.asResource();
      if (!rv.hasProperty(this.property))
        return null;
      if (this.multiple) {
        StmtIterator si = rv.listProperties(this.property);

        while (si.hasNext()) {
          item = this.format.format(si.next().getObject(), sorter, locale);
          if (item == null && this.required)
            return null;
          if (item != null) {
            if (text != null)
              text = Text.compose(text, new LiteralText(", "));
            text = Text.compose(text, item);
          }
        }
        return text;
      } 
      return this.format.format(rv.getProperty(this.property).getObject(), sorter, locale);
    }
    return this.format.format(value, sorter, locale);
  }

}
