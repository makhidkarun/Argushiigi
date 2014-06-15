/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;

import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;

import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A query end-point for queries for references to a resource.
 * <p>
 * Generates a JSON document listing all the results.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class ReferenceQueryResource extends ServerResource {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(ReferenceQueryResource.class);

  public static final String PARAM_RESOURCE = "resource";

  /** The application */
  private ServerApplication application;
  /** The model */
  private Model model;
  /** The display sorter (from the application) */
  private DisplaySorter sorter;

  /**
   * Constructor for a query resource.
   *
   */
  public ReferenceQueryResource() {
  }

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
    this.model = this.application.getDataset();
    this.sorter = this.application.getSorter();
  }

  /**
   * Get a list of matching resources as a JSON document.
   *
   * @see org.restlet.resource.ServerResource#get()
   */
  @Get
  public Representation get() {
    String resource = Reference.decode(this.getQueryValue(this.PARAM_RESOURCE));

    try {
      JSONObject wrap = new JSONObject();
      JSONArray result = new JSONArray();
      StmtIterator si = this.model.listStatements(null, null, this.model.createResource(resource));
      Comparator<Resource> classComparator = this.sorter.getClassComparator();

      while (si.hasNext()) {
        Statement s = si.next();
        Resource res = s.getSubject();

        if (res.isURIResource()) {
          JSONObject obj = new JSONObject();
          Reference ref = this.application.getLocalRef(new Reference(res.getURI()));
          StmtIterator ci = this.model.listStatements(res, RDF.type, (Resource) null);
          Resource cls = null;

          obj.put("name", this.sorter.getName(s.getSubject(), Locale.ENGLISH));
          obj.put("uri", s.getSubject().getURI());
          obj.put("href", ref);
          while (ci.hasNext()) {
            Resource c = ci.next().getResource();

            if (c.isAnon())
              continue;
            if (cls == null || classComparator.compare(cls, c) > 0)
              cls = c;
          }
          if (cls == null)
            cls = OWL.Thing;
          ref = this.application.getLocalRef(new Reference(cls.getURI()));
          obj.put("cls", this.sorter.getName(cls, Locale.ENGLISH));
          obj.put("clsUri", cls.getURI());
          obj.put("clsHref", ref);
          result.put(obj);
        }
      }
      wrap.put("aaData", result);
      return new JsonRepresentation(wrap);
    } catch (Exception ex) {
      this.application.getLogger().log(Level.SEVERE, "Unable to get references to " + resource, ex);
      this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, ex);
      return new StringRepresentation(ex.getMessage());
    }
  }

}
