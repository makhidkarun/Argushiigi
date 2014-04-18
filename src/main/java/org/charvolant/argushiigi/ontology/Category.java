/*
 *  $Id$
 *
 * Copyright (c) 2013 Doug Palmer
 *
 * See LICENSE for licensing details
 */
package org.charvolant.argushiigi.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * A category of resources.
 * <p>
 * This class can contain statements, further categories and
 * other things.
 * 
 * @author Doug Palmer <doug@charvolant.org>
 *
 */
public class Category {
  /** The category this represents */
  private Resource category;
  /** Is this a category that collects references to a resource? */
  private boolean reference;
  /** Sub-categories */
  private List<Category> subCategories;
  /** Statements that fit in this category */
  private List<Statement> statements;

  /**
   * Construct for a category.
   *
   * @param category The category
   * @param reference is this a reference category? (false for a direct category)
   */
  public Category(Resource category, boolean reference) {
    super();
    this.category = category;
    this.reference = reference;
    this.subCategories = new ArrayList<Category>();
    this.statements = new ArrayList<Statement>();
  }

  /**
   * Construct for a direct category.
   *
   * @param category The category
   */
  public Category(Resource category) {
    this(category, false);
  }


  /**
   * Get the category.
   *
   * @return the category
   */
  public Resource getCategory() {
    return this.category;
  }


  /**
   * Is this a reference category?
   * <p>
   * Reference categories collect statements that are references to the main resource,
   * rather than direct categories that collect statements about the resource.
   *
   * @return the reference
   */
  public boolean isReference() {
    return this.reference;
  }


  /**
   * Get the sub-categories.
   *
   * @return the list of sub-categories
   */
  public List<Category> getSubCategories() {
    return this.subCategories;
  }

  /**
   * Get the statements.
   * <p>
   * The items are statements that have been allocated to
   * this category.
   *
   * @return the items
   */
  public List<Statement> getStatements() {
    return this.statements;
  }

  /**
   * Add a resource to a category.
   * <p>
   * If this is the category, then add it here.
   * Otherwise, go down the chain of categories
   * until the right one has been found and add it
   * there.
   * <p>
   * The cat resource needs to come from an model
   * that has a suitable chain of {@link Argushiigi#parentCategory}
   * from the category to this category.
   * 
   * @param resource The resource
   * @param cat The category
   */
  public void add(Statement statement, Resource cat) {
    List<Resource> parents;
    Resource bottom = cat;

    if (this.category.equals(cat)) {
      this.statements.add(statement);
      return;
    }
    parents = new ArrayList<Resource>(8);
    while (cat != null && !cat.equals(this.category)) {
      parents.add(cat);
      cat = cat.getPropertyResourceValue(Argushiigi.parentCategory);
    }
    if (cat ==  null)
      throw new IllegalArgumentException("No path from " + bottom + " to " + this.category);
    this.add(statement, parents.size() - 1, parents);
  }

  private void add(Statement statement, int index, List<Resource> parents) {
    Resource parent;
    Category sc;

    if (index == -1) {
      this.statements.add(statement);
      return;
    }
    parent = parents.get(index);
    for (Category sub: this.subCategories) {
      if (parent.equals(sub.getCategory())) {
        sub.add(statement, index - 1, parents);
        return;
      }
    }
    sc = new Category(parent);
    this.subCategories.add(sc);
    sc.add(statement, index - 1, parents);
  }

  /**
   * Sort the categories and statements into an order.
   * 
   * @param rc The comparator to use for category sorting
   * @param sc The comparator to use for statement sorting
   */
  public void sort(Comparator<Resource> rc, Comparator<Statement> sc) {
    Collections.sort(this.subCategories, new CategoryComparator(rc));
    Collections.sort(this.statements, sc);
  }

  /**
   * Visit this category.
   * <p>
   * Visitors can be used to process the category
   * as required.
   * 
   * @param visitor The category visitor.
   */
  public void visit(CategoryVisitor visitor) {
    visitor.visit(this);
    for (Category sub: this.subCategories)
      sub.visit(visitor);
    visitor.visited(this);
  }

  /**
   * A comparator for categories, based on the category resource.
   */
  private class CategoryComparator implements Comparator<Category> {
    private Comparator<Resource> rc; 

    public CategoryComparator(Comparator<Resource> rc) {
      this.rc = rc;
    }

    @Override
    public int compare(Category o1, Category o2) {
      return this.rc.compare(o1.getCategory(), o2.getCategory());
    }
  }
}
