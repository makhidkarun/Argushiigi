/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.util.Locale;
import java.util.logging.Level;

import org.charvolant.argushiigi.ontology.Category;
import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.charvolant.argushiigi.ontology.StatementCollector;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Render a specific URI.
 * <p>
 * Representation is based on the chosen format.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class RendererResource extends ServerResource {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(RendererResource.class);

  /** The URI parameter */
  public static final String PARAM_URI = "uri";
  /** The format parameter */
  public static final String PARAM_FORMAT = "format";
  /** The xml format  */
  public static final String FORMAT_XML = "xml";
  /** The html format  */
  public static final String FORMAT_HTML = "html";
  /** The rdf (rdf/xml) format  */
  public static final String FORMAT_RDF = "rdf";
  /** The rdf (turtle) format  */
  public static final String FORMAT_TURTLE = "ttl";

  /** The application */
  private ServerApplication application;
  /** The display sorter (from the application) */
  private DisplaySorter sorter;


  /**
   * Initialise the resource by collecting the contextual
   * information from the {@link ServerApplication} application. 
   *
   * @see org.restlet.resource.UniformResource#doInit()
   */
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();
    this.application = (ServerApplication) this.getApplication();
    this.sorter = this.application.getSorter();
  }

  @Get
  public Representation get() {
    String format = this.getQueryValue(this.PARAM_FORMAT);
    Reference uri = null;
    Resource resource;
    Category top = new Category(this.sorter.getTop());
    StatementCollector collector = new StatementCollector(this.sorter);
    Renderer renderer;

    if (format == null)
      format = this.FORMAT_HTML;
    if (this.getQueryValue(this.PARAM_URI) != null)
      uri = new Reference(Reference.decode(this.getQueryValue(this.PARAM_URI)));
    else {
      uri = this.application.getExternalRef(this.getOriginalRef());
      uri.setQuery(null);
    }
    try {
      resource = this.application.getInference().createResource(uri.toString());
      if (format.equals(this.FORMAT_HTML) || format.equals(this.FORMAT_XML)) {
        collector.buildStatements(top, resource, this.application.getInference(), true);
      } else {
        collector.buildStatements(top, resource, this.application.getOntology(), false);
      }
      if (format.equals(this.FORMAT_XML))
        renderer = new XmlRenderer(Locale.ENGLISH, this.sorter, this.application, this.getRequest());
      else if (format.equals(this.FORMAT_RDF))
        renderer = new RdfRenderer(Locale.ENGLISH, this.sorter, this.application, this.getRequest(), false);
      else if (format.equals(this.FORMAT_TURTLE))
        renderer = new RdfRenderer(Locale.ENGLISH, this.sorter, this.application, this.getRequest(), true);
      else
        renderer = new HtmlRenderer(Locale.ENGLISH, this.sorter, this.application, this.getRequest());
      renderer.render(resource, top);
      return renderer.getRepresentation();
    } catch (Exception ex) {
      this.getLogger().log(Level.SEVERE, "Unable to get resource " + uri, ex);
      this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, ex);
      return new StringRepresentation(ex.getMessage());
    }
  }
}
