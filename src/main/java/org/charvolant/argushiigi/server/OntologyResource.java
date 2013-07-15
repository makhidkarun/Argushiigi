/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.io.InputStream;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that serves up ontology descriptions whole.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class OntologyResource extends ServerResource {
  private static final Logger logger = LoggerFactory.getLogger(OntologyResource.class);

  /**
   * Get an internally stored ontology document.
   *
   * @see org.restlet.resource.ServerResource#get()
   */
  @Get
  @Override
  public Representation get() throws ResourceException {
    Reference uri = this.getOriginalRef();

    try {
      String path = "/org/charvolant/argushiigi" + uri.getPath();
      InputStream is = this.getClass().getResourceAsStream(path);

      if (is == null) {
        this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Can't find " + uri);
        return new StringRepresentation(uri.toString() + " not found");
      }
      return new InputRepresentation(is);
    } catch (Exception ex) {
      this.logger.error("Unable to get resource " + uri, ex);
      this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, ex);
      return new StringRepresentation(ex.getMessage());
    }
  }

}
