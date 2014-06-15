/*
 *  $Id$
 *
 * Copyright (c) 2014 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A format that can be applied to an RDF node or statement.
 * <p>
 * Subclasses implement specific behaviours.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public abstract class AbstractFormat {

  /**
   * Construct an empty abstract format.
   */
  public AbstractFormat() {
  }
  
  /**
   * Is this format required?
   * <p>
   * This is generally relevant to a sub-format element,
   * such as a pattern.
   * 
   * @return true if the format must succeed for the parent format to succeed
   */
  abstract public boolean isRequired();
  
  /**
   * Format part of a value using this pattern.
   * <p>
   * Formatting may fail if there are missing properties in the value.
   * 
   * @param value The value to format
   * @param sorter The sorter
   * @param locale The locale to format under
   * 
   * @return The formatted value, or null if not able to format the value
   */
  abstract public Text format(RDFNode value, DisplaySorter sorter, Locale locale);
  
  /**
   * Convert a literal into a suitable object.
   * 
   * @param value The literal
   * 
   * @return The matching object
   */
  public static Object decodeLiteral(Literal value) {
    Object literal = value.getValue();

    if (literal instanceof XSDDateTime)
      literal = ((XSDDateTime) literal).asCalendar().getTime();
    return literal;
  }
  
  /**
   * Decode a literal using default formats.
   * 
   * @param literal The literal
   * @param locale The locale to decode under
   * 
   * @return The decoded literal 
   */
  public static Text defaultLiteral(Literal literal, Locale locale) {
    Object decoded = decodeLiteral(literal);
    java.text.Format format = null;
    String text;
    
    if (decoded instanceof Integer) {
        format = NumberFormat.getIntegerInstance(locale);
    } else if (decoded instanceof Number)
      format = NumberFormat.getNumberInstance(locale);
    else if (decoded instanceof Date)
      format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    if (format != null)
      text = format.format(decoded);
    else
      text = decoded.toString();
    return new LiteralText(text);
  }

  

  /**
   * Construct a pattern from a node.
   * 
   * @param node The node that contains the pattern information
   * 
   * @return The built pattern
   * 
   * @throws IllegalArgumentException if unable to find a matching type of pattern
   */
  public static AbstractFormat create(RDFNode node) throws IllegalArgumentException {
    Resource resource;
    
    if (node.isLiteral())
      return new LiteralPattern(node.asLiteral());
    if (node.isResource()) {
      resource = node.asResource();
      if (resource.hasProperty(RDF.type, Argushiigi.SubPattern))
        return new SubPattern(resource);
      if (resource.hasProperty(RDF.type, Argushiigi.Pattern))
        return new Pattern(resource);
    }
    throw new IllegalArgumentException("Unable to derive pattern from " + node);
  }
}
