/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * A pattern that can be applied to a value or
 * a statement.
 * <p>
 * Derived from the {@link Argishiigi#Pattern} class.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class Pattern extends AbstractFormat {
  public static final String[] EHEX = {
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
    "A", "B", "C", "D", "E", "F", "G", "H", "J", "L",
    "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W",
    "X", "Y", "Z"
  };
  
  /** Display using eHex? */
  protected boolean eHex;
  /** The java format */
  protected String javaFormat;
  /** The property to access */
  protected Property property;
  /** Is this a required pattern? */
  protected boolean required;
  /** Is this a multiple-property pattern? */
  protected boolean multiple;
  
  /**
   * Construct an empty pattern.
   *
   */
  public Pattern() {
  }
  
  /**
   * Construct a pattern from a resource.
   *
   * @param resource
   */
  public Pattern(Resource resource) {
    this.eHex = resource.hasLiteral(Argushiigi.eHex, true);
    this.required = !resource.hasLiteral(Argushiigi.required, false);
    this.multiple = resource.hasLiteral(Argushiigi.multiple, true);
    if (resource.hasProperty(Argushiigi.javaFormat))
      this.javaFormat = resource.getProperty(Argushiigi.javaFormat).getString();
    if (resource.hasProperty(Argushiigi.property))
      this.property = resource.getModel().createProperty(resource.getPropertyResourceValue(Argushiigi.property).getURI());
  }

  
  /**
   * Get the eHex flag.
   *
   * @return true if the value should be expressed in eHex
   */
  public boolean iseHex() {
    return this.eHex;
  }

  /**
   * Set the eHex flag.
   *
   * @param eHex the new eHex flag 
   */
  public void seteHex(boolean eHex) {
    this.eHex = eHex;
  }

  /**
   * Get the java format.
   * <p>
   * Used to allow complex formatting of values.
   * See {@link MessageFormat} for how the format is parsed.
   *
   * @return the javaFormat
   */
  public String getJavaFormat() {
    return this.javaFormat;
  }

  /**
   * Set the java format.
   *
   * @param javaFormat the new java format 
   */
  public void setJavaFormat(String javaFormat) {
    this.javaFormat = javaFormat;
  }

  /**
   * Get the property.
   * <p>
   * The property of the value to use. 
   *
   * @return the property (null for the value itself)
   */
  public Property getProperty() {
    return this.property;
  }

  /**
   * Set the property.
   *
   * @param property the new property 
   */
  public void setProperty(Property property) {
    this.property = property;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRequired() {
    return this.required;
  }

  /**
   * Set the required flag.
   *
   * @param required the new required flag
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * Format a specific value according to the format.
   * 
   * @param original The original value
   * @param value The value
   * @param sorter The sorter
   * @param locale The locale to format for, or null for none
   * 
   * @return The formatted value or null for a failed format
   */
  private Text formatValue(RDFNode original, RDFNode value, DisplaySorter sorter, Locale locale) {
    Resource rv;
    Object literal;
    MessageFormat format;
    String text;
    
    if (value.isLiteral()) {
      literal = this.decodeLiteral(value.asLiteral());
      if (this.javaFormat != null) {
        format = new MessageFormat(this.javaFormat, locale);
        if (literal instanceof Boolean)
          literal = ((Boolean) literal).booleanValue() ? 1 : 0;
        return new LiteralText(format.format(new Object[] { literal }));
      }
      if (literal instanceof Integer) {
        int val = ((Integer) literal).intValue();
        
        if (this.eHex && val < this.EHEX.length)
          return new LiteralText(this.EHEX[val]);
        return new LiteralText(NumberFormat.getIntegerInstance(locale).format(literal));
      } 
      if (literal instanceof Number)
        return new LiteralText(NumberFormat.getNumberInstance(locale).format(literal));
      if (literal instanceof Date)
        return new LiteralText(DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(literal));
      new LiteralText(literal.toString());
    }
    if (original != value)
      return sorter.format(value, locale);
    // No further formatting, so just go with the resource as-is
    rv = value.asResource();
    text = sorter.getName(rv, locale);
    if (this.javaFormat != null) {
      format = new MessageFormat(this.javaFormat, locale);
      text = format.format(new Object[] { text });
    }
    return rv.isAnon() ? new LiteralText(text) : new LinkText(rv.getURI(), new LiteralText(text));
  }

  /**
   * {@inheritDoc}
   * <p>
   * Format a pattern based on the format and property.
   */
  @Override
  public Text format(RDFNode value, DisplaySorter sorter, Locale locale) {
    RDFNode original = value;
    Resource rv;
    Text text = null;
    Text item;
    
    // First check for a property
    if (this.property != null) {
      if (!value.isResource())
        return null;
      rv = value.asResource();
      if (!rv.hasProperty(this.property))
        return null;
      if (this.multiple) {
        StmtIterator si = rv.listProperties(this.property);
        
        while (si.hasNext()) {
          item = this.formatValue(original, si.next().getObject(), sorter, locale);
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
      return this.formatValue(original, rv.getProperty(this.property).getObject(), sorter, locale);
    }
    return this.formatValue(original, value, sorter, locale);
  }
}
