package org.charvolant.argushiigi.ontology;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

public class CategoryTest {
  private static final String PREFIX = "http://data.travellerrpg.com/ontology/argushiigi/test1/";

  private Model ontology;
  private Model data;
  private InfModel inference;
  private DisplaySorter sorter;
  private Category top;

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
    this.top = new Category(this.sorter.getTop());
  }

  /**
   * Test case for {@link Category#add(Statement, Resource)}
   */
  @Test
  public void testAdd1() {
    Resource thing1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource thing2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Property p1 = this.inference.createProperty(this.PREFIX + "p1");
    Statement s1 = this.inference.createStatement(thing1, p1, thing2);
    Category cat1;

    this.top.add(s1, c1);
    Assert.assertEquals(1, this.top.getSubCategories().size());
    cat1 = this.top.getSubCategories().get(0);
    Assert.assertEquals(0, cat1.getSubCategories().size());
    Assert.assertEquals(1, cat1.getStatements().size());
    Assert.assertEquals(s1, cat1.getStatements().get(0));
  }

  /**
   * Test case for {@link Category#add(Statement, Resource)}
   */
  @Test
  public void testAdd2() {
    Resource thing1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource thing2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource thing3 = this.inference.createResource(this.PREFIX + "thing3");
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Property p1 = this.inference.createProperty(this.PREFIX + "p1");
    Statement s1 = this.inference.createStatement(thing1, p1, thing2);
    Statement s2 = this.inference.createStatement(thing1, p1, thing3);
    Category cat1;

    this.top.add(s1, c1);
    this.top.add(s2, c1);
    Assert.assertEquals(1, this.top.getSubCategories().size());
    cat1 = this.top.getSubCategories().get(0);
    Assert.assertEquals(0, cat1.getSubCategories().size());
    Assert.assertEquals(2, cat1.getStatements().size());
    Assert.assertEquals(s1, cat1.getStatements().get(0));
    Assert.assertEquals(s2, cat1.getStatements().get(1));
  }

  /**
   * Test case for {@link Category#add(Statement, Resource)}
   */
  @Test
  public void testAdd3() {
    Resource thing1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource thing2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource thing3 = this.inference.createResource(this.PREFIX + "thing3");
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Resource c2 = this.inference.createResource(this.PREFIX + "c2");
    Property p1 = this.inference.createProperty(this.PREFIX + "p1");
    Statement s1 = this.inference.createStatement(thing1, p1, thing2);
    Statement s2 = this.inference.createStatement(thing1, p1, thing3);
    Category cat1, cat2;

    this.top.add(s1, c1);
    this.top.add(s2, c2);
    Assert.assertEquals(1, this.top.getSubCategories().size());
    cat1 = this.top.getSubCategories().get(0);
    Assert.assertEquals(1, cat1.getSubCategories().size());
    Assert.assertEquals(1, cat1.getStatements().size());
    Assert.assertEquals(s1, cat1.getStatements().get(0));
    cat2 = cat1.getSubCategories().get(0);
    Assert.assertEquals(0, cat2.getSubCategories().size());
    Assert.assertEquals(1, cat1.getStatements().size());
    Assert.assertEquals(s2, cat2.getStatements().get(0));
  }

  /**
   * Test case for {@link Category#visit(CategoryVisitor)}
   */
  @Test
  public void testVisit1() {
    Resource thing1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource thing2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Property p1 = this.inference.createProperty(this.PREFIX + "p1");
    Statement s1 = this.inference.createStatement(thing1, p1, thing2);
    Category cat1;
    TestVisitor visitor = new TestVisitor();

    this.top.add(s1, c1);
    this.top.visit(visitor);
    Assert.assertEquals(1, this.top.getSubCategories().size());
    cat1 = this.top.getSubCategories().get(0);
    Assert.assertEquals(2, visitor.pre.size());
    Assert.assertTrue(visitor.pre.contains(this.top));
    Assert.assertTrue(visitor.pre.contains(cat1));
    Assert.assertEquals(2, visitor.post.size());
    Assert.assertTrue(visitor.post.contains(this.top));
    Assert.assertTrue(visitor.post.contains(cat1));
  }

  /**
   * Test case for {@link Category#visit(CategoryVisitor)}
   */
  @Test
  public void testVisit2() {
    Resource thing1 = this.inference.createResource(this.PREFIX + "thing1");
    Resource thing2 = this.inference.createResource(this.PREFIX + "thing2");
    Resource thing3 = this.inference.createResource(this.PREFIX + "thing3");
    Resource c1 = this.inference.createResource(this.PREFIX + "c1");
    Resource c2 = this.inference.createResource(this.PREFIX + "c2");
    Property p1 = this.inference.createProperty(this.PREFIX + "p1");
    Statement s1 = this.inference.createStatement(thing1, p1, thing2);
    Statement s2 = this.inference.createStatement(thing1, p1, thing3);
    Category cat1, cat2;
    TestVisitor visitor = new TestVisitor();

    this.top.add(s1, c1);
    this.top.add(s2, c2);
    this.top.visit(visitor);
    Assert.assertEquals(1, this.top.getSubCategories().size());
    cat1 = this.top.getSubCategories().get(0);
    Assert.assertEquals(1, cat1.getSubCategories().size());
    cat2 = cat1.getSubCategories().get(0);
    Assert.assertEquals(3, visitor.pre.size());
    Assert.assertTrue(visitor.pre.contains(this.top));
    Assert.assertTrue(visitor.pre.contains(cat1));
    Assert.assertTrue(visitor.pre.contains(cat2));
    Assert.assertEquals(3, visitor.post.size());
    Assert.assertTrue(visitor.post.contains(this.top));
    Assert.assertTrue(visitor.post.contains(cat1));
    Assert.assertTrue(visitor.post.contains(cat2));
  }

  private class TestVisitor implements CategoryVisitor {
    public List<Category> pre;
    public List<Category> post;
    
    public TestVisitor() {
      this.pre = new ArrayList<Category>();
      this.post = new ArrayList<Category>();
    }

    @Override
    public void visit(Category category) {
      this.pre.add(category);
    }

    @Override
    public void visited(Category category) {
      this.post.add(category);
    }
    
  }
}
