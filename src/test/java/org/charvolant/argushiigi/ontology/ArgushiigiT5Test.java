/**
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Test cases for the Argushiigi-T5 ontology
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class ArgushiigiT5Test extends JenaTest {
  private Model ontology;
  private Model data;
  private DisplaySorter sorter;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    Model model;

    OntDocumentManager.getInstance().setProcessImports(false);
    this.ontology = ModelFactory.createDefaultModel();
    this.ontology.read(this.getClass().getResource("foaf.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("skos.owl").toExternalForm());
    this.ontology.read(this.getClass().getResource("rpg.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("t5.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("t5-characters.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("argushiigi.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("argushiigi-rpg.rdf").toExternalForm());
    this.ontology.read(this.getClass().getResource("argushiigi-t5.rdf").toExternalForm());
    model = ModelFactory.createDefaultModel();
    model.read(this.getClass().getResource("traveller-universe.rdf").toExternalForm());
    this.data = ModelFactory.createUnion(this.ontology, model);
    this.sorter = new DisplaySorter(this.ontology);
  }

  @Test
  public void testOntology1() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, this.ontology);

    this.validityTest(model.validate());
  }

  @Test
  public void testFormatDate1() {
    Resource date = this.data.createResource(RPG.Date);
    Text text;

    date.addLiteral(RPG.year, 1150);
    text = this.sorter.format(date, Locale.ENGLISH);
    Assert.assertTrue(text instanceof LiteralText);
    Assert.assertEquals("1150", text.toString());
  }

  @Test
  public void testFormatDate2() {
    Resource date = this.data.createResource(RPG.Date);
    Text text;

    date.addLiteral(RPG.dayOfYear, 25);
    date.addLiteral(RPG.year, 1150);
    text = this.sorter.format(date, Locale.ENGLISH);
    Assert.assertTrue(text instanceof LiteralText);
    Assert.assertEquals("025-1150", text.toString());
  }

  @Test
  public void testFormatDate3() {
    Resource date = this.data.createResource(RPG.Date);
    Text text;

    date.addLiteral(RPG.dayOfMonth, 5);
    date.addLiteral(RPG.month, 7);
    date.addLiteral(RPG.year, 1150);
    text = this.sorter.format(date, Locale.ENGLISH);
    Assert.assertTrue(text instanceof LiteralText);
    Assert.assertEquals("05-07-1150", text.toString());
  }

  @Test
  public void testFormatPrice() {
    Resource pr = this.data.createResource(RPG.Price);
    Resource cr = this.data.createResource("http://data.travellerrpg.com/unit/credit");
    Text text;

    pr.addProperty(RPG.unit, cr);
    pr.addLiteral(RDF.value, 30.0);
    text = this.sorter.format(pr, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("Cr30.00", text.toString());
  }

  @Test
  public void testFormatDuration() {
    Resource du = this.data.createResource(RPG.Duration);
    Resource sec = this.data.createResource("http://data.travellerrpg.com/unit/second");
    Text text;

    du.addProperty(RPG.unit, sec);
    du.addLiteral(RDF.value, 300.0);
    text = this.sorter.format(du, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("300.0s", text.toString());
  }

  @Test
  public void testFormatActivity1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Activity");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property type = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#activityType");
    Property at = this.data.createProperty("http://data.travellerrpg.com/rules/t5/careers/marine/activity/battle");
    Text text;

    act.addLiteral(age, 25);
    act.addProperty(type, at);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("25 Battle", text.toString());
  }

  @Test
  public void testFormatActivity2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Activity");
    Resource act = this.data.createResource(cls);
    Resource date = this.data.createResource(RPG.Date);
    Property type = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#activityType");
    Property at = this.data.createProperty("http://data.travellerrpg.com/rules/t5/careers/marine/activity/battle");
    Text text;

    date.addLiteral(RPG.year, 1107);
    date.addLiteral(RPG.dayOfYear, 203);
    act.addProperty(RPG.date, date);
    act.addProperty(type, at);
    act.addProperty(RDFS.label, "Defence of somewhere");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("203-1107 Battle, Defence of somewhere", text.toString());
  }

  @Test
  public void testFormatActivity3() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Activity");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property type = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#activityType");
    Property at = this.data.createProperty("http://data.travellerrpg.com/rules/t5/careers/marine/activity/battle");
    Text text;

    act.addLiteral(age, 25);
    act.addProperty(type, at);
    act.addProperty(RDFS.label, "Defence of nowhere");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("25 Battle, Defence of nowhere", text.toString());
  }


  @Test
  public void testFormatBranch1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Branch");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property branch = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#branch");
    Property cb = this.data.createProperty("http://data.travellerrpg.com/rules/t5/careers/marine/branch/artillery");
    Text text;

    act.addLiteral(age, 25);
    act.addProperty(branch, cb);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("25 branch Artillery", text.toString());
  }

  @Test
  public void testFormatContinuation1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Continuation");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Text text;

    act.addLiteral(age, 28);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof LiteralText);
    Assert.assertEquals("28 continues", text.toString());
  }

  @Test
  public void testFormatContinuation2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Continuation");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property succ = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#backstorySuccess");
    Text text;

    act.addLiteral(age, 28);
    act.addLiteral(succ, false);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof LiteralText);
    Assert.assertEquals("28 continues Failed", text.toString());
  }

  @Test
  public void testFormatEducation1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Education");
    Resource chem = this.data.createResource("http://data.travellerrpg.com/rules/t5/skills/academic-knowledge/chemistry");
    Resource robo = this.data.createResource("http://data.travellerrpg.com/rules/t5/skills/academic-knowledge/robotics");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property ema = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#educationMajor");
    Property emi = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#educationMinor");
    Property succ = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#backstorySuccess");
    Text text;

    act.addLiteral(age, 28);
    act.addLiteral(succ, false);
    act.addProperty(ema, chem);
    act.addProperty(emi, robo);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 education, major Chemistry, minor Robotics Failed", text.toString());
  }

  @Test
  public void testFormatEducation2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Education");
    Resource chem = this.data.createResource("http://data.travellerrpg.com/rules/t5/skills/academic-knowledge/chemistry");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property ema = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#educationMajor");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(ema, chem);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 education, major Chemistry", text.toString());
  }

  @Test
  public void testFormatEducation3() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Education");
    Resource chem = this.data.createResource("http://data.travellerrpg.com/rules/t5/skills/academic-knowledge/chemistry");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property ema = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#educationMajor");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(ema, chem);
    act.addProperty(RDFS.label, "Summer School");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 education, major Chemistry, Summer School", text.toString());
  }


  @Test
  public void testFormatEnrolment1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Enrolment");
    Resource org = this.data.createResource("http://data.travellerrpg.com/setting/traveller/organisation/imperial-marines");
    Resource rank = this.data.createResource("http://data.travellerrpg.com/rules/t5/ranks/marine/officer1");
    Resource career = this.data.createResource("http://data.travellerrpg.com/rules/t5/careers/marine");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property cp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#career");
    Property rp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#rank");
    Property op = this.data.createProperty("http://data.travellerrpg.com/ontology/rpg#organisation");
    Property succ = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#backstorySuccess");
    Text text;

    act.addLiteral(age, 28);
    act.addLiteral(succ, false);
    act.addProperty(cp, career);
    act.addProperty(rp, rank);
    act.addProperty(op, org);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 enrolled Marine, Imperial Marines, 2nd Lieutenant Failed", text.toString());
  }

  @Test
  public void testFormatEnrolment2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Enrolment");
    Resource career = this.data.createResource("http://data.travellerrpg.com/rules/t5/careers/marine");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property cp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#career");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(cp, career);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 enrolled Marine", text.toString());
  }

  @Test
  public void testFormatEnrolment3() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Enrolment");
    Resource career = this.data.createResource("http://data.travellerrpg.com/rules/t5/careers/marine");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property cp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#career");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(cp, career);
    act.addLiteral(RDFS.label, "2nd try");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 enrolled Marine, 2nd try", text.toString());
  }


  @Test
  public void testFormatDamage1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource act = this.data.createResource(cls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Property pp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicPermanentDamage");
    Property rp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicRecovery");
    Text text;

    act.addProperty(dcp, str);
    act.addLiteral(dp, 6);
    act.addLiteral(pp, 2);
    act.addLiteral(rp, 4);
    act.addLiteral(RDFS.label, "Oopsie!");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("Str 6, recovered 4, permanent 2, Oopsie!", text.toString());
  }

  @Test
  public void testFormatDamage2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource act = this.data.createResource(cls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Property rp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicRecovery");
    Text text;

    act.addProperty(dcp, str);
    act.addLiteral(dp, 6);
    act.addLiteral(rp, 4);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("Str 6, recovered 4", text.toString());
  }

  @Test
  public void testFormatDamage3() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource act = this.data.createResource(cls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Text text;

    act.addProperty(dcp, str);
    act.addLiteral(dp, 6);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("Str 6", text.toString());
  }

  @Test
  public void testFormatAging1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Aging");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property hd = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#hasDamage");
    Resource acls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource dam = this.data.createResource(acls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Property pp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicPermanentDamage");
    Property rp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicRecovery");
    Text text;

    act.addLiteral(age, 28);
    dam.addProperty(dcp,  str);
    dam.addLiteral(dp, 11);
    dam.addLiteral(pp, 1);
    dam.addLiteral(rp, 10);
    act.addProperty(hd, dam);
    act.addLiteral(RDFS.label, "drinks too much");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 aging Str 11, recovered 10, permanent 1, drinks too much", text.toString());
  }

  @Test
  public void testFormatAging2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Aging");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property hd = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#hasDamage");
    Resource acls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource end = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Endurance");
    Resource dam1 = this.data.createResource(acls);
    Resource dam2 = this.data.createResource(acls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Property pp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicPermanentDamage");
    Text text;

    act.addLiteral(age, 28);
    dam1.addProperty(dcp,  str);
    dam1.addLiteral(dp, 1);
    dam1.addLiteral(pp, 1);
    act.addProperty(hd, dam1);
    dam2.addProperty(dcp,  end);
    dam2.addLiteral(dp, 2);
    dam2.addLiteral(pp, 2);
    act.addProperty(hd, dam2);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 aging End 2, permanent 2, Str 1, permanent 1", text.toString());
  }

  @Test
  public void testFormatInjury1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Injury");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property hd = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#hasDamage");
    Resource acls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource dam = this.data.createResource(acls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Property rp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicRecovery");
    Text text;

    act.addLiteral(age, 28);
    dam.addProperty(dcp,  str);
    dam.addLiteral(dp, 5);
    dam.addLiteral(rp, 5);
    act.addProperty(hd, dam);
    act.addLiteral(RDFS.label, "weapons malfunction");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 injury Str 5, recovered 5, weapons malfunction", text.toString());
  }

  @Test
  public void testFormatInjury2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Injury");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property hd = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#hasDamage");
    Resource acls = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Damage");
    Resource str = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Strength");
    Resource end = this.data.createResource("http://data.travellerrpg.com/ontology/t5#Endurance");
    Resource dam1 = this.data.createResource(acls);
    Resource dam2 = this.data.createResource(acls);
    Property dcp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#damageToCharacteristic");
    Property dp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicDamage");
    Property pp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5#characteristicPermanentDamage");
    Text text;

    act.addLiteral(age, 28);
    dam1.addProperty(dcp,  str);
    dam1.addLiteral(dp, 1);
    dam1.addLiteral(pp, 1);
    act.addProperty(hd, dam1);
    dam2.addProperty(dcp,  end);
    dam2.addLiteral(dp, 2);
    dam2.addLiteral(pp, 2);
    act.addProperty(hd, dam2);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 injury End 2, permanent 2, Str 1, permanent 1", text.toString());
  }

  @Test
  public void testFormatPromotion1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Promotion");
    Resource rank = this.data.createResource("http://data.travellerrpg.com/rules/t5/ranks/marine/officer2");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property rp = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#rank");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(rp, rank);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 promoted to 1st Lieutenant", text.toString());
  }

  @Test
  public void testFormatReward1() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Reward");
    Resource medal = this.data.createResource("http://data.travellerrpg.com/setting/decorations/campaign-ribbon");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property ap = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#awarded");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(ap, medal);
    act.addLiteral(RDFS.label, "Invasion of Sus'liek");
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 awarded Campaign Ribbon, Invasion of Sus'liek", text.toString());
  }

  @Test
  public void testFormatReward2() {
    Resource cls = this.data.createResource("http://data.travellerrpg.com/ontology/t5/characters#Reward");
    Resource medal1 = this.data.createResource("http://data.travellerrpg.com/setting/traveller/decoration/military/mcg");
    Resource medal2 = this.data.createResource("http://data.travellerrpg.com/setting/traveller/decoration/military/xs");
    Resource act = this.data.createResource(cls);
    Property age = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#atAge");
    Property ap = this.data.createProperty("http://data.travellerrpg.com/ontology/t5/characters#awarded");
    Text text;

    act.addLiteral(age, 28);
    act.addProperty(ap, medal1);
    act.addProperty(ap, medal2);
    text = this.sorter.format(act, Locale.ENGLISH);
    Assert.assertTrue(text instanceof CompositeText);
    Assert.assertEquals("28 awarded Exemplary Service, Medal for Conspicuous Gallantry", text.toString());
  }

}
