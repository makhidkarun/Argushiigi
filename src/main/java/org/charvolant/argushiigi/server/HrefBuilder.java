/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import org.restlet.data.Reference;

/**
 * Convert a reference URI into a suitable href.
 * <p>
 * Since the URI may refer to something not resolvable
 * or something that actually needs to be accessed indirectly,
 * implementors of this interface need to build the correct
 * href.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public interface HrefBuilder {
  /**
   * Convert a reference into a resolvable URI.
   * 
   * @param ref The reference
   * 
   * @return The equivalent resolvable reference
   */
  public String asHref(Reference ref);
  
  /**
   * Is this an external link?
   * 
   * @param ref The link to test
   * 
   * @return True if this link is external to the application
   */
  public boolean isExternal(Reference ref);
}
