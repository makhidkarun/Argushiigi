/**
 *  $Id$
*
* Copyright (c) 2013 Doug Palmer
*
* See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Test cases for the {@link DisplaySorter} class.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class DisplaySorterTest extends JenaTest {
  private static final String PREFIX = "http://data.travellerrpg.com/ontology/argushiigi/test1/";
  
  private Model ontology;
  private Model data;
  private InfModel inference;
  private DisplaySorter sorter;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    Reasoner reasoner;

    this.ontology = ModelFactory.createDefaultModel();
    this.ontology.read(this.getClass().getResource("skos.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("argushiigi.rdf").toExternalForm());
    reasoner = ReasonerRegistry.getOWLMiniReasoner();
    this.data = ModelFactory.createDefaultModel();
    this.data.read(this.getClass().getResource("argushiigi-test.rdf").toExternalForm());
    this.inference = ModelFactory.createInfModel(reasoner, this.ontology, this.data);
    this.sorter = new DisplaySorter(ModelFactory.createUnion(this.ontology, this.data));
 }
  
  @Test
  public void testDisplaySorter1() {
    Resource p1 = this.inference.createResource(this.PREFIX + "p1");
    Resource p2 = this.inference.createResource(this.PREFIX + "p2");
    Resource p3 = this.inference.createResource(this.PREFIX + "p3");
    Resource p4 = this.inference.createResource(this.PREFIX + "p4");
    Resource p5 = this.inference.createResource(this.PREFIX + "p5");
    Resource p6 = this.inference.createResource(this.PREFIX + "p6");
    Resource p7 = this.inference.createResource(this.PREFIX + "p7");

    Assert.assertEquals(100, this.sorter.getPriorities().get(p1).intValue());
    Assert.assertEquals(20, this.sorter.getPriorities().get(p2).intValue());
    Assert.assertEquals(40, this.sorter.getPriorities().get(p3).intValue());
    Assert.assertEquals(100, this.sorter.getPriorities().get(p4).intValue());
    Assert.assertEquals(20, this.sorter.getPriorities().get(p5).intValue());
    Assert.assertEquals(40, this.sorter.getPriorities().get(p6).intValue());
    Assert.assertEquals(100, this.sorter.getPriorities().get(p7).intValue());
}
  
  @Test
  public void testDisplaySorter2() {
    Resource c1 = this.inference.createResource(this.PREFIX + "C1");
    Resource c2 = this.inference.createResource(this.PREFIX + "C2");
    Resource c3 = this.inference.createResource(this.PREFIX + "C3");
    Resource c4 = this.inference.createResource(this.PREFIX + "C4");
    Resource c5 = this.inference.createResource(this.PREFIX + "C5");
    Resource c6 = this.inference.createResource(this.PREFIX + "C6");
    
    Assert.assertEquals(100, this.sorter.getPriorities().get(c1).intValue());
    Assert.assertEquals(20, this.sorter.getPriorities().get(c2).intValue());
    Assert.assertEquals(40, this.sorter.getPriorities().get(c3).intValue());
    Assert.assertEquals(100, this.sorter.getPriorities().get(c4).intValue());
    Assert.assertEquals(20, this.sorter.getPriorities().get(c5).intValue());
    Assert.assertEquals(100, this.sorter.getPriorities().get(c6).intValue());
  }
  
  @Test
  public void testDisplaySorter3() {
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Resource c2 = this.inference.createResource(this.PREFIX + "c2");
    Resource c3 = this.inference.createResource(this.PREFIX + "c3");
    Resource cl1 = this.inference.createResource(this.PREFIX + "C1");
    Resource cl2 = this.inference.createResource(this.PREFIX + "C2");
    Resource cl3 = this.inference.createResource(this.PREFIX + "C3");
    Resource cl4 = this.inference.createResource(this.PREFIX + "C4");
    Resource cl5 = this.inference.createResource(this.PREFIX + "C5");
    Resource cl6 = this.inference.createResource(this.PREFIX + "C6");
    
    Assert.assertEquals(c1, this.sorter.getCategories().get(cl1));
    Assert.assertEquals(c2, this.sorter.getCategories().get(cl2));
    Assert.assertEquals(c3, this.sorter.getCategories().get(cl3));
    Assert.assertEquals(c1, this.sorter.getCategories().get(cl4));
    Assert.assertEquals(c2, this.sorter.getCategories().get(cl5));
    Assert.assertEquals(c1, this.sorter.getCategories().get(cl6));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP1() {
    Resource p = this.inference.createResource(this.PREFIX + "p1");
    Resource c = this.inference.createResource(this.PREFIX + "c1");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP2() {
    Resource p = this.inference.createResource(this.PREFIX + "p2");
    Resource c = this.inference.createResource(this.PREFIX + "c2");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP3() {
    Resource p = this.inference.createResource(this.PREFIX + "p3");
    Resource c = this.inference.createResource(this.PREFIX + "c3");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP4() {
    Resource p = this.inference.createResource(this.PREFIX + "p4");
    Resource c = this.inference.createResource(this.PREFIX + "c1");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP5() {
    Resource p = this.inference.createResource(this.PREFIX + "p5");
    Resource c = this.inference.createResource(this.PREFIX + "c2");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP6() {
    Resource p = this.inference.createResource(this.PREFIX + "p6");
    Resource c = this.inference.createResource(this.PREFIX + "c3");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryP7() {
    Resource p = this.inference.createResource(this.PREFIX + "p7");
    Resource c = this.inference.createResource(this.PREFIX + "c1");

    Assert.assertEquals(c,  this.sorter.category(p));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryC1() {
    Resource cl = this.inference.createResource(this.PREFIX + "C1");
    Resource c = this.inference.createResource(this.PREFIX + "c1");

    Assert.assertEquals(c,  this.sorter.category(cl));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryC2() {
    Resource cl = this.inference.createResource(this.PREFIX + "C2");
    Resource c = this.inference.createResource(this.PREFIX + "c2");

    Assert.assertEquals(c,  this.sorter.category(cl));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryC3() {
    Resource cl = this.inference.createResource(this.PREFIX + "C3");
    Resource c = this.inference.createResource(this.PREFIX + "c3");

    Assert.assertEquals(c,  this.sorter.category(cl));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryC4() {
    Resource cl = this.inference.createResource(this.PREFIX + "C4");
    Resource c = this.inference.createResource(this.PREFIX + "c1");

    Assert.assertEquals(c,  this.sorter.category(cl));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#category(com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCategoryC5() {
    Resource cl = this.inference.createResource(this.PREFIX + "C5");
    Resource c = this.inference.createResource(this.PREFIX + "c2"); // C1 -> C2 means that C2 has priority over C1

    Assert.assertEquals(c,  this.sorter.category(cl));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#compare(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCompareCat1() {
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Resource c2 = this.inference.createResource(this.PREFIX + "c2");
    
    Assert.assertTrue(this.sorter.compare(c1, c1) == 0);
    Assert.assertTrue(this.sorter.compare(c2, c2) == 0);
    Assert.assertTrue(this.sorter.compare(c1, c2) < 0);
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#compare(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCompareCat2() {
    Resource c2 = this.inference.createResource(this.PREFIX + "c2");
    Resource c3 = this.inference.createResource(this.PREFIX + "c3");
    
    Assert.assertTrue(this.sorter.compare(c2, c3) < 0);
    Assert.assertTrue(this.sorter.compare(c3, c2) > 0);
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#compare(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCompareCat3() {
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Resource c3 = this.inference.createResource(this.PREFIX + "c3");
    
    Assert.assertTrue(this.sorter.compare(c1, c3) < 0);
    Assert.assertTrue(this.sorter.compare(c3, c1) > 0);
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#compare(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCompareP1() {
    Resource p1 = this.inference.createResource(this.PREFIX + "p1");
    Resource p2 = this.inference.createResource(this.PREFIX + "p2");
    Resource p3 = this.inference.createResource(this.PREFIX + "p3");
    Resource p4 = this.inference.createResource(this.PREFIX + "p4");
    Resource p5 = this.inference.createResource(this.PREFIX + "p5");
    Resource p6 = this.inference.createResource(this.PREFIX + "p6");
    Resource p7 = this.inference.createResource(this.PREFIX + "p7");
    
    Assert.assertTrue(this.sorter.compare(p1, p1) == 0);
    Assert.assertTrue(this.sorter.compare(p1, p2) > 0);
    Assert.assertTrue(this.sorter.compare(p1, p3) > 0);
    Assert.assertTrue(this.sorter.compare(p1, p4) == 0);
    Assert.assertTrue(this.sorter.compare(p1, p5) > 0);
    Assert.assertTrue(this.sorter.compare(p1, p6) > 0);
    Assert.assertTrue(this.sorter.compare(p1, p7) == 0);
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#compare(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource)}.
   */
  @Test
  public void testCompareP2() {
    Resource p1 = this.inference.createResource(this.PREFIX + "p1");
    Resource p2 = this.inference.createResource(this.PREFIX + "p2");
    Resource p3 = this.inference.createResource(this.PREFIX + "p3");
    Resource p4 = this.inference.createResource(this.PREFIX + "p4");
    Resource p5 = this.inference.createResource(this.PREFIX + "p5");
    Resource p6 = this.inference.createResource(this.PREFIX + "p6");
    Resource p7 = this.inference.createResource(this.PREFIX + "p7");
    
    Assert.assertTrue(this.sorter.compare(p2, p1) < 0);
    Assert.assertTrue(this.sorter.compare(p2, p2) == 0);
    Assert.assertTrue(this.sorter.compare(p2, p3) < 0);
    Assert.assertTrue(this.sorter.compare(p2, p4) < 0);
    Assert.assertTrue(this.sorter.compare(p2, p5) == 0);
    Assert.assertTrue(this.sorter.compare(p2, p6) < 0);
    Assert.assertTrue(this.sorter.compare(p2, p7) < 0);
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName1() {
    Resource c = this.inference.createResource(this.PREFIX + "c1");
    
    Assert.assertEquals("Category 1", this.sorter.getName(c, "en", false));
    Assert.assertEquals("Categorie 1", this.sorter.getName(c, "fr", false));
    Assert.assertNull(this.sorter.getName(c, "de", false));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName2() {
    Resource c = this.inference.createResource(this.PREFIX + "c2");
    
    Assert.assertEquals("Category 2", this.sorter.getName(c, "en-gb", false));
    Assert.assertNull(this.sorter.getName(c, "en", false));
    Assert.assertEquals("Category 2", this.sorter.getName(c, "en", true));
    Assert.assertEquals("Cat 2", this.sorter.getName(c, null, false));
    Assert.assertNull(this.sorter.getName(c, "fr", false));
    Assert.assertNull(this.sorter.getName(c, "fr", true));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName3() {
    Resource c = this.inference.createResource(this.PREFIX + "c3");
    
    Assert.assertEquals("Category 3", this.sorter.getName(c, "en-gb", false));
    Assert.assertEquals("Cat-3", this.sorter.getName(c, "de", false));
    Assert.assertEquals("Category 3", this.sorter.getName(c, "en", true));
    Assert.assertNull(this.sorter.getName(c, "fr", false));
    Assert.assertNull(this.sorter.getName(c, "fr", true));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName4() {
    Resource c = this.inference.createResource(this.PREFIX + "c1");
    
    Assert.assertEquals("Category 1", this.sorter.getName(c, Locale.ENGLISH));
    Assert.assertEquals("Categorie 1", this.sorter.getName(c, Locale.FRENCH));
    Assert.assertEquals("Cat1", this.sorter.getName(c, Locale.GERMAN));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName5() {
    Resource c = this.inference.createResource(this.PREFIX + "c2");
    
    Assert.assertEquals("Category 2", this.sorter.getName(c, new Locale("en", "GB")));
    Assert.assertEquals("Category 2", this.sorter.getName(c, Locale.ENGLISH));
    Assert.assertEquals("Cat 2", this.sorter.getName(c, null));
    Assert.assertEquals("Cat 2", this.sorter.getName(c, Locale.FRENCH));
    Assert.assertEquals("Cat 2", this.sorter.getName(c, Locale.GERMAN));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName6() {
    Resource c = this.inference.createResource(this.PREFIX + "c3");
    
    Assert.assertEquals("Category 3", this.sorter.getName(c, new Locale("en", "GB")));
    Assert.assertEquals("Category 3", this.sorter.getName(c, Locale.ENGLISH));
    Assert.assertEquals("Category 3", this.sorter.getName(c, Locale.FRENCH));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName7() {
    Resource c = this.inference.createResource(this.PREFIX + "thing7");
    
    Assert.assertEquals("7T", this.sorter.getName(c, new Locale("en", "GB")));
    Assert.assertEquals("7T", this.sorter.getName(c, Locale.ENGLISH));
    Assert.assertEquals("7T", this.sorter.getName(c, Locale.FRENCH));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testGetName8() {
    Resource c = this.inference.createResource(this.PREFIX + "thing6");
    
    Assert.assertEquals("thing6", this.sorter.getName(c, new Locale("en", "GB")));
    Assert.assertEquals("thing6", this.sorter.getName(c, Locale.ENGLISH));
    Assert.assertEquals("thing6", this.sorter.getName(c, Locale.FRENCH));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat1() {
    Resource c41 = this.inference.createResource(this.PREFIX + "c4-1");
    Text text;
    
    text = this.sorter.format(c41, new Locale("en", "AU"));
    Assert.assertNotNull(text);
    Assert.assertEquals("0020.5:8/05/14", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat2() {
    Resource c41 = this.inference.createResource(this.PREFIX + "c4-1");
    Text text;
    
    text = this.sorter.format(c41, new Locale("en", "US"));
    Assert.assertNotNull(text);
    Assert.assertEquals("0020.5:5/8/14", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat3() {
    Resource c41 = this.inference.createResource(this.PREFIX + "c4-1");
    Text text;
    
    text = this.sorter.format(c41, new Locale("en", "AU"));
    Assert.assertNotNull(text);
    Assert.assertEquals("0020.5:8/05/14", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat4() {
    Resource c41 = this.inference.createResource(this.PREFIX + "c4-1");
    
    Assert.assertEquals("0020.5:8/05/14", this.sorter.format(c41, new Locale("en", "AU")).toString());
    Assert.assertEquals("0020.5:5/8/14", this.sorter.format(c41, Locale.ENGLISH).toString());
    Assert.assertEquals("0020,5:08/05/14", this.sorter.format(c41, Locale.FRENCH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat5() {
    Resource thing1 = this.inference.createResource(this.PREFIX + "thing1");
    
    Assert.assertEquals("thing1", this.sorter.format(thing1, Locale.ENGLISH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat6() {
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    
    Assert.assertEquals("Category 1", this.sorter.format(c1, Locale.ENGLISH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat7() {
    Literal l1 = this.inference.createLiteral("Hello");
    
    Assert.assertEquals("Hello", this.sorter.format(l1, Locale.ENGLISH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat8() {
    Literal l1 = this.inference.createTypedLiteral(12);
    
    Assert.assertEquals("12", this.sorter.format(l1, Locale.ENGLISH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat9() {
    Literal l1 = this.inference.createTypedLiteral(12.6);
    
    Assert.assertEquals("12.6", this.sorter.format(l1, Locale.ENGLISH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat10() {
    Date date = new Date();
    Literal l1 = this.inference.createTypedLiteral(date);
    DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
    
    Assert.assertEquals(format.format(date), this.sorter.format(l1, Locale.ENGLISH).toString());
  }
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat11() {
    String uri = "http://localhost/nothing";
    Resource r1 = this.inference.createResource(uri);
    
    Assert.assertEquals("nothing", this.sorter.format(r1, Locale.ENGLISH).toString());
  }
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormat12() {
    String uri = "http://localhost/nothing/";
    Resource r1 = this.inference.createResource(uri);
    
    Assert.assertEquals(uri, this.sorter.format(r1, Locale.ENGLISH).toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementLabel1() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Property d2 = this.inference.createProperty(this.PREFIX + "d2");
    Statement s1 = this.inference.createLiteralStatement(t1, d2, 12);
    Text text;
    
    text = this.sorter.formatStatementLabel(s1, Locale.ENGLISH);
    Assert.assertNotNull(text);
    Assert.assertEquals("12 data2", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementLabel2() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource t2 = this.inference.createResource(this.PREFIX + "thing2");
    Property p7 = this.inference.createProperty(this.PREFIX + "p7");
    Statement s1 = this.inference.createStatement(t1, p7, t2);
    Text text;
    
    text = this.sorter.formatStatementLabel(s1, Locale.ENGLISH);
    Assert.assertNotNull(text);
    Assert.assertEquals("thing2 p7", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementLabel3() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource t2 = this.inference.createResource(this.PREFIX + "thing2");
    Property p3 = this.inference.createProperty(this.PREFIX + "p3");
    Statement s1 = this.inference.createStatement(t1, p3, t2);
    Text text;
    
    text = this.sorter.formatStatementLabel(s1, Locale.ENGLISH);
    Assert.assertNotNull(text);
    Assert.assertEquals("p3", text.toString());
  }


  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementLabel4() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource t2 = this.inference.createResource(this.PREFIX + "thing2");
    Property p6 = this.inference.createProperty(this.PREFIX + "p6");
    Statement s1 = this.inference.createStatement(t1, p6, t2);
    Text text;
    
    text = this.sorter.formatStatementLabel(s1, Locale.ENGLISH);
    Assert.assertNotNull(text);
    Assert.assertEquals("Property 6", text.toString());
   }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementValue1() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Property d2 = this.inference.createProperty(this.PREFIX + "d2");
    Statement s1 = this.inference.createLiteralStatement(t1, d2, 12);
    Text text;
    
    text = this.sorter.formatStatementValue(s1, Locale.ENGLISH, false);
    Assert.assertNotNull(text);
    Assert.assertEquals("12", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementValue2() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource t2 = this.inference.createResource(this.PREFIX + "thing2");
    Property p7 = this.inference.createProperty(this.PREFIX + "p7");
    Statement s1 = this.inference.createStatement(t1, p7, t2);
    Text text;
    
    text = this.sorter.formatStatementValue(s1, Locale.ENGLISH, false);
    Assert.assertNotNull(text);
    Assert.assertEquals("thing2", text.toString());
  }
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementValue3() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource coti = this.inference.createResource("http://www.travellerrpg.com/CotI/Discuss/showthread.php?t=30041");
    Statement s1 = this.inference.createStatement(t1, RDFS.seeAlso, coti);
    Text text;
    
    text = this.sorter.formatStatementValue(s1, Locale.ENGLISH, false);
    Assert.assertNotNull(text);
    Assert.assertEquals("http://www.travellerrpg.com/CotI/Discuss/showthread.php?t=30041", text.toString());
  }
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getName(Resource, String, boolean)}.
   */
  @Test
  public void testFormatStatementValue4() {
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource coti = this.inference.createResource("http://www.travellerrpg.com/CotI/Discuss/showthread.php?t=30041");
    Statement s1 = this.inference.createStatement(t1, RDFS.seeAlso, coti);
    Text text;
    
    text = this.sorter.formatStatementValue(s1, Locale.ENGLISH, true);
    Assert.assertNotNull(text);
    Assert.assertEquals("thing1", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getResourceComparator(Locale)}.
   */
  @Test
  public void testGetResourceComparator1() {
    ArrayList<Resource> list = new ArrayList<Resource>();
    Resource c1 = this.inference.createResource(this.PREFIX + "C1");
    Resource c2 = this.inference.createResource(this.PREFIX + "C2");
    Resource c3 = this.inference.createResource(this.PREFIX + "C3");
    Resource c4 = this.inference.createResource(this.PREFIX + "C4");
    Resource c5 = this.inference.createResource(this.PREFIX + "C5");
    Resource c6 = this.inference.createResource(this.PREFIX + "C6");
    
    list.add(c1);
    list.add(c2);
    list.add(c3);
    list.add(c4);
    list.add(c5);
    list.add(c6);
    Collections.sort(list, this.sorter.getResourceComparator(Locale.ENGLISH));
    Assert.assertEquals(c2, list.get(0));
    Assert.assertEquals(c5, list.get(1));
    Assert.assertEquals(c3, list.get(2));
    Assert.assertEquals(c1, list.get(3));
    Assert.assertEquals(c4, list.get(4));
    Assert.assertEquals(c6, list.get(5));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getResourceComparator(Locale)}.
   */
  @Test
  public void testGetResourceComparator2() {
    ArrayList<Resource> list = new ArrayList<Resource>();
    Resource p1 = this.inference.createResource(this.PREFIX + "p1");
    Resource p2 = this.inference.createResource(this.PREFIX + "p2");
    Resource p3 = this.inference.createResource(this.PREFIX + "p3");
    Resource p4 = this.inference.createResource(this.PREFIX + "p4");
    Resource p5 = this.inference.createResource(this.PREFIX + "p5");
    Resource p6 = this.inference.createResource(this.PREFIX + "p6");
    Resource p7 = this.inference.createResource(this.PREFIX + "p7");
    
    list.add(p1);
    list.add(p2);
    list.add(p3);
    list.add(p4);
    list.add(p5);
    list.add(p6);
    list.add(p7);
    Collections.sort(list, this.sorter.getResourceComparator(Locale.ENGLISH));
    Assert.assertEquals(p2, list.get(0));
    Assert.assertEquals(p5, list.get(1));
    Assert.assertEquals(p3, list.get(2));
    Assert.assertEquals(p6, list.get(3));
    Assert.assertEquals(p1, list.get(4));
    Assert.assertEquals(p4, list.get(5));
    Assert.assertEquals(p7, list.get(6));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getStatementComparator(Locale)}.
   */
  @Test
  public void testGetStatementComparator1() {
    ArrayList<Statement> list = new ArrayList<Statement>();
    Property p1 = this.inference.createProperty(this.PREFIX + "p1");
    Property p2 = this.inference.createProperty(this.PREFIX + "p2");
    Property p3 = this.inference.createProperty(this.PREFIX + "p3");
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource t2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource t3 = this.inference.createResource(this.PREFIX + "thing3");
    Statement s1 = this.inference.createStatement(t1, p1, t3);
    Statement s2 = this.inference.createStatement(t1, p1, t2);
    Statement s3 = this.inference.createStatement(t1, p2, t2);
    Statement s4 = this.inference.createStatement(t1, p3, t2);
    Statement s5 = this.inference.createStatement(t1, p3, t3);
    
    list.add(s1);
    list.add(s2);
    list.add(s3);
    list.add(s4);
    list.add(s5);
    Collections.sort(list, this.sorter.getStatementComparator(Locale.ENGLISH));
    Assert.assertEquals(s3, list.get(0));
    Assert.assertEquals(s4, list.get(1));
    Assert.assertEquals(s5, list.get(2));
    Assert.assertEquals(s2, list.get(3));
    Assert.assertEquals(s1, list.get(4));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.DisplaySorter#getStatementComparator(Locale)}.
   */
  @Test
  public void testGetStatementComparator2() {
    ArrayList<Statement> list = new ArrayList<Statement>();
    Property p2 = this.inference.createProperty(this.PREFIX + "p2");
    Property p3 = this.inference.createProperty(this.PREFIX + "p3");
    Property d1 = this.inference.createProperty(this.PREFIX + "d1");
    Resource t1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource t2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource t3 = this.inference.createResource(this.PREFIX + "thing3");
    Statement s1 = this.inference.createLiteralStatement(t1, d1, "45");
    Statement s2 = this.inference.createLiteralStatement(t1, d1, "33");
    Statement s3 = this.inference.createStatement(t1, p2, t2);
    Statement s4 = this.inference.createStatement(t1, p3, t2);
    Statement s5 = this.inference.createStatement(t1, p3, t3);
    
    list.add(s1);
    list.add(s2);
    list.add(s3);
    list.add(s4);
    list.add(s5);
    Collections.sort(list, this.sorter.getStatementComparator(Locale.ENGLISH));
    Assert.assertEquals(s3, list.get(0));
    Assert.assertEquals(s4, list.get(1));
    Assert.assertEquals(s5, list.get(2));
    Assert.assertEquals(s2, list.get(3));
    Assert.assertEquals(s1, list.get(4));
  }

}
