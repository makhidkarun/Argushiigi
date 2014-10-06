/**
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.server;


import java.io.File;

import org.charvolant.argushiigi.ontology.DisplaySorter;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.tdb.base.file.Location;

/**
 * The application that serves data.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class ServerApplication extends Application {
  private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

  /** The key to the model, available through the application context */
  public static final String MODEL_KEY = ServerApplication.class.getName() + ".model";
  /** The key to the sorter, available through the application context */
  public static final String SORTER_KEY = ServerApplication.class.getName() + ".sorter";
  /** The key to the root URI (where things are supposed to come from), available through the application context */
  public static final String ROOT_KEY = ServerApplication.class.getName() + ".root";
  /** The key to the source URI (where things really come from), available through the application context */
  public static final String SOURCE_KEY = ServerApplication.class.getName() + ".source";
  
  /** The root URI for things to serve */
  private Reference root;
  /** The source URI for things to serve */
  private Reference source;
  /** The apparent source URI for things to serve */
  private Reference apparent;
  /** The schema ontology */
  private Model ontology;
  /** The schema display information */
  private Model display;
  /** The data */
  private Model dataset;
  /** The inference model from the data and the ontology */
  private InfModel inference;
  /** The display sorter */
  private DisplaySorter sorter;
  
  /**
   * Constructor for Argushiigi server.
   * 
   * @param root The root URI of what is being served
   * @param source The source URI for where it actually comes from
   *
   */
  public ServerApplication(Reference root, Reference source) {
    this.root = root;
    this.source = source;
    this.apparent = source;
    this.build();
  }

  /**
   * Constructor for Argushiigi.
   *
   * @param context
   */
  public ServerApplication(Context context) {
    super(context);
    this.root = new Reference("http://data.travellerrpg.com/");
    this.source = new Reference("http://localhost:8080/argushiigi/data/");
    this.apparent = new Reference("http://www.charvolant.org/argushiigi/data/");
    this.build();
  }

  /**
   * Constructor for Argushiigi.
   */
  public ServerApplication() {
    super();
    this.root = new Reference("http://data.travellerrpg.com/");
    this.source = new Reference("http://localhost:8080/argushiigi/data/");
    this.apparent = new Reference("http://www.charvolant.org/argushiigi/data/");
    //this.apparent = new Reference("http://localhost:8080/argushiigi/data/");
    this.build();
  }
  
  
  
  /**
   * Get the root reference, the URI where things are supposed to come from.
   *
   * @return the root
   */
  public Reference getRootRef() {
    return this.root;
  }

  /**
   * Get the source reference, the URI where things actually come from.
   *
   * @return the source
   */
  public Reference getSourceRef() {
    return this.source;
  }

  /**
   * Get the apparent source reference, the URI where things appear to actually come from.
   *
   * @return the source
   */
  public Reference getApparentRef() {
    return this.apparent;
  }

  /**
   * Get the ontology.
   * <p>
   * The ontology contains the schema under which the data operates
   *
   * @return the ontology
   */
  public Model getOntology() {
    return this.ontology;
  }

  /**
   * Get the display data.
   * <p>
   * The display data gives rules for how elements are expected
   * to be displayed.
   *
   * @return the display
   */
  public Model getDisplay() {
    return this.display;
  }

  /**
   * Get the dataset.
   * <p>
   * The dataset contains the actual, uninterpreted data
   *
   * @return the dataset
   */
  public Model getDataset() {
    return this.dataset;
  }

  /**
   * Get the inference model.
   * <p>
   * The inference model contains the dataset, the ontology
   * and information inferred from applying the ontology to the
   * dataset.
   *
   * @return the inference
   */
  public InfModel getInference() {
    return this.inference;
  }

  /**
   * Get the sorter.
   * <p>
   * The display sorted can be used to categorise and sort
   * statements about a resource before rendering it.
   *
   * @return the sorter
   */
  public DisplaySorter getSorter() {
    return this.sorter;
  }

  /**
   * Build the data sources for the application.
   */
  protected void build() {
    Reasoner reasoner;
    Location dataset = new Location("/tmp/argushiigi");
    File dsdir = new File(dataset.getDirectoryPath());

    if (!dsdir.exists())
      dsdir.mkdirs();
    this.ontology = ModelFactory.createDefaultModel();
    this.ontology.read(this.getClass().getResource("../ontology/foaf.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("../ontology/skos.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("../ontology/argushiigi.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("../ontology/rpg.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("../ontology/t5.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("../ontology/t5-characters.rdf").toExternalForm());
    this.display = ModelFactory.createDefaultModel();
    this.display.read(this.getClass().getResource("../ontology/argushiigi-rpg.rdf").toExternalForm());
    this.display.read(this.getClass().getResource("../ontology/argushiigi-t5.rdf").toExternalForm());
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    //this.dataset = TDBFactory.createDataset(dataset);
    this.dataset = ModelFactory.createDefaultModel();
    this.dataset.read(this.getClass().getResource("../ontology/traveller-universe.rdf").toExternalForm());
    this.dataset.read(this.getClass().getResource("../ontology/spinward-marches.ttl").toExternalForm(), "TTL");
    this.dataset.read(this.getClass().getResource("../ontology/examples.ttl").toExternalForm(), "TTL");
    this.inference = ModelFactory.createInfModel(reasoner, this.ontology, this.dataset);
    this.sorter = new DisplaySorter(ModelFactory.createUnion(this.ontology, this.display));
    this.logger.debug("Created data source");
  }

  /**
   * @inheritDoc
   *
   * @see org.restlet.Application#createInboundRoot()
   */
  @Override
  public Restlet createInboundRoot() {
    Router router = new Router(this.getContext());
    
    router.attach("/css", new Directory(this.getContext(), this.getClass().getResource("/META-INF/css/").toExternalForm()));
    router.attach("/query/type", TypeQueryResource.class);
    router.attach("/query/references", ReferenceQueryResource.class);
    router.attach("/ontology/{ontology}.rdf", OntologyResource.class);
    router.attachDefault(RendererResource.class);
    return router;
  }
  
  /**
   * @inheritDoc
   *
   * @see org.restlet.Application#start()
   */
  @Override
  public synchronized void start() throws Exception {
    super.start();
    this.getContext().getAttributes().put(this.ROOT_KEY, this.root);
    this.getContext().getAttributes().put(this.SOURCE_KEY, this.source);
  }

  /**
   * @inheritDoc
   * <p>
   * Close any persistent datasets.
   *
   * @see org.restlet.Application#stop()
   */
  @Override
  public synchronized void stop() throws Exception {
    if (this.inference != null)
      this.inference.close();
    this.inference = null;
    if (this.dataset != null)
      this.dataset.close();
    this.dataset = null;
    if (this.display != null)
      this.display.close();
    this.display = null;
    if (this.ontology != null)
      this.ontology.close();
    this.ontology = null;
    super.stop();
  }
  
  /**
   * Convert a local reference (with a base of the source URI)
   * to an external reference (with a base of root URI)
   * 
   * @param local The local reference
   * 
   * @return The translated reference
   */
  public Reference getExternalRef(Reference local) {
    return new Reference(this.root, local.getRelativeRef(this.source)).getTargetRef();
  }
  
  /**
   * Convert an external reference (with a base of the root URI)
   * to a local reference (with a base of source URI)
   * 
   * @param external The external reference
   * 
   * @return The translated reference
   */
  public Reference getLocalRef(Reference external) {
    return new Reference(this.apparent, external.getRelativeRef(this.root)).getTargetRef();
  }

  /**
   * Start a stand-alone Argushiigi server on port 8182.
   * 
   * @param args Not used
   */
  public static void main(String[] args) throws Exception {
    Component component = new Component();
    ServerApplication application = new ServerApplication(new Reference("http://data.travellerrpg.com/"), new Reference("http://localhost:8182/"));

    component.getServers().add(Protocol.HTTP, 8182);  
    component.getClients().add(Protocol.FILE);  
    component.getDefaultHost().attach(application);  
    component.start();  
  }
}
