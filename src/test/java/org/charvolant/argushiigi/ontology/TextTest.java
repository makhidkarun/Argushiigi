/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class TextTest implements TextVisitor {
  private LiteralText literalVisit;
  private LinkText linkVisit;
  private CompositeText compositeVisit;
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#visit(org.charvolant.argushiigi.ontology.TextVisitor)}.
   */
  @Test
  public void testVisit1() {
    Text text = new LiteralText("Hello");
    
    text.visit(this);
    Assert.assertSame(text, this.literalVisit);
  }
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#visit(org.charvolant.argushiigi.ontology.TextVisitor)}.
   */
  @Test
  public void testVisit2() {
    Text text = new LinkText("Hello", new LiteralText("Hello"));
    
    text.visit(this);
    Assert.assertSame(text, this.linkVisit);
  }
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#visit(org.charvolant.argushiigi.ontology.TextVisitor)}.
   */
  @Test
  public void testVisit3() {
    Text text1 = new LiteralText("Hello1");
    Text text2 = new LiteralText("Hello2");
    Text text = new CompositeText(text1, text2);
    
    text.visit(this);
    Assert.assertSame(text, this.compositeVisit);
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#compose(org.charvolant.argushiigi.ontology.Text, org.charvolant.argushiigi.ontology.Text)}.
   */
  @Test
  public void testCompose1() {
    Text text1 = new LiteralText("Hello1");
    Text text2 = new LiteralText("Hello2");
    Text text = Text.compose(text1, text2);
    LiteralText lt;
    
    Assert.assertTrue(text instanceof LiteralText);
    lt = (LiteralText) text;
    Assert.assertEquals("Hello1Hello2", lt.getLiteral());
  }
  
  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#compose(org.charvolant.argushiigi.ontology.Text, org.charvolant.argushiigi.ontology.Text)}.
   */
  @Test
  public void testCompose2() {
    Text text1 = new LiteralText("Hello1");
    Text text2 = new LinkText("Hello2", new LiteralText("Hello2"));
    Text text = Text.compose(text1, text2);
    CompositeText ct;
    
    Assert.assertTrue(text instanceof CompositeText);
    ct = (CompositeText) text;
    Assert.assertEquals(2, ct.getComponents().size());
    Assert.assertSame(text1, ct.getComponents().get(0));
    Assert.assertSame(text2, ct.getComponents().get(1));
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#toString()}.
   */
  @Test
  public void testToString1() {
    Text text = new LiteralText("Hello");
    
    Assert.assertEquals("Hello", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#toString()}.
   */
  @Test
  public void testToString2() {
    Text text = new LinkText("Hello", new LiteralText("Fred"));
    
    Assert.assertEquals("Fred", text.toString());
  }

  /**
   * Test method for {@link org.charvolant.argushiigi.ontology.Text#toString()}.
   */
  @Test
  public void testToString3() {
    Text text1 = new LiteralText("Hello1");
    Text text2 = new LiteralText("Hello2");
    Text text = new CompositeText(text1, text2);
    
    Assert.assertEquals("Hello1Hello2", text.toString());
  }

  /**
   * @inheritDoc
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.LiteralText)
   */
  @Override
  public void visited(LiteralText text) {
    this.literalVisit = text;
  }

  /**
   * @inheritDoc
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.LinkText)
   */
  @Override
  public void visited(LinkText text) {
    this.linkVisit = text;
    
  }

  /**
   * @inheritDoc
   *
   * @see org.charvolant.argushiigi.ontology.TextVisitor#visited(org.charvolant.argushiigi.ontology.CompositeText)
   */
  @Override
  public void visited(CompositeText text) {
    this.compositeVisit = text;
  }

}
