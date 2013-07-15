/**
*  $Id$
*
* Copyright (c) 2013 Doug Palmer
*
* See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.io.StringWriter;
import java.util.Iterator;

import org.junit.Assert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Useful utility methods for testing ontological reasoning
 * with Jena.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public abstract class JenaTest {

  /**
   * Print all statements about a resource.
   * 
   * @param m The model
   * @param s The subject
   * @param p The property (verb)
   * @param o The object
   */
  public void printStatements(Model m, Resource s, Property p, Resource o) {
    for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
      Statement stmt = i.nextStatement();
      System.out.println(" - " + PrintUtil.print(stmt));
    }
  }
  
  /**
   * Print all the data types of a resource.
   * 
   * @param m The model
   * @param s The resource
   */
  public void printTypeData(Model m, Resource s) {
    for (StmtIterator i = m.listStatements(s, RDF.type, (Resource) null); i.hasNext(); ) {
      Statement stmt = i.nextStatement();
      System.out.println(" + " + PrintUtil.print(stmt));
      this.printStatements(m, stmt.getResource(), null, null);
    }
  }

  /**
   * Test a model for validity.
   * 
   * @param validity The validity report
   */
  public void validityTest(ValidityReport validity) {
    if (!validity.isValid()) {
      StringWriter sw = new StringWriter();

      sw.append("Model invalid: ");
      for (Iterator<ValidityReport.Report> i = validity.getReports(); i.hasNext(); ) {
        ValidityReport.Report report = i.next();
        sw.append(report.toString());
      }
      Assert.fail(sw.toString());
    }
  }

}
