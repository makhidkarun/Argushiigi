/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

/**
 * Value text that contains a link to something.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class LinkText extends Text {
  /** The link */
  private String uri;
  /** The contained value text */
  private Text text;
  /**
   * Construct for a link and embedded value.
   *
   * @param uri The link URI
   * @param text The text to embed
   */
  public LinkText(String uri, Text text) {
    super();
    this.uri = uri;
    this.text = text;
  }
  
  /**
   * Get the uri.
   *
   * @return the uri
   */
  public String getUri() {
    return this.uri;
  }
  
  /**
   * Get the text.
   *
   * @return the text
   */
  public Text getText() {
    return this.text;
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
