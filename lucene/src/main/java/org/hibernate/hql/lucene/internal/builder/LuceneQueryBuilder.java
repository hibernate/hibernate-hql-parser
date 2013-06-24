/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.hql.lucene.internal.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.search.Query;
import org.hibernate.hql.internal.util.Strings;
import org.hibernate.hql.lucene.internal.builder.predicate.ConjunctionPredicate;
import org.hibernate.hql.lucene.internal.builder.predicate.DisjunctionPredicate;
import org.hibernate.hql.lucene.internal.builder.predicate.EqualsPredicate;
import org.hibernate.hql.lucene.internal.builder.predicate.NegationPredicate;
import org.hibernate.hql.lucene.internal.builder.predicate.ParentPredicate;
import org.hibernate.hql.lucene.internal.builder.predicate.Predicate;
import org.hibernate.hql.lucene.internal.builder.predicate.RangePredicate;
import org.hibernate.hql.lucene.internal.builder.predicate.RootPredicate;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.QueryContextBuilder;

/**
 * Builder for the creation of Lucene queries.
 * </p>
 * Implemented as a stack of {@link Predicate}s which allows to add elements to the constructed query in a uniform
 * manner while traversing through the original HQL/JPQL query parse tree.
 *
 * @author Gunnar Morling
 */
public class LuceneQueryBuilder {

	private final QueryContextBuilder queryContextBuilder;
	private final PropertyHelper propertyHelper;

	/**
	 * Used to build the actual query in the end.
	 */
	private QueryBuilder queryBuilder;

	/**
	 * The targeted entity type of the built query.
	 */
	private Class<?> entityType;

	/**
	 * The root predicate of the {@code WHERE} clause of the built query.
	 */
	private RootPredicate rootPredicate;

	/**
	 * Keeps track of all the parent predicates ({@code AND}, {@code OR} etc.) the {@code WHERE} clause of the built
	 * query.
	 */
	private final Stack<ParentPredicate> predicates = new Stack<ParentPredicate>();

	public LuceneQueryBuilder(QueryContextBuilder queryContextBuilder, PropertyHelper propertyHelper) {
		this.queryContextBuilder = queryContextBuilder;
		this.propertyHelper = propertyHelper;
	}

	public LuceneQueryBuilder setEntityType(Class<?> entityType) {
		queryBuilder = queryContextBuilder.forEntity( entityType ).get();
		this.entityType = entityType;

		rootPredicate = new RootPredicate( queryBuilder );
		predicates.push( rootPredicate );

		return this;
	}

	public LuceneQueryBuilder addEqualsPredicate(String property, Object value) {
		return addEqualsPredicate( Arrays.asList( property ), value );
	}

	public LuceneQueryBuilder addEqualsPredicate(List<String> propertyPath, Object value) {
		Object typedValue = propertyHelper.convertToPropertyType( value, entityType, propertyPath );
		pushPredicate( new EqualsPredicate( queryBuilder, Strings.join( propertyPath, "." ), typedValue ) );

		return this;
	}

	public LuceneQueryBuilder addRangePredicate(String property, Object lower, Object upper) {
		return addRangePredicate( Arrays.asList( property ), lower, upper );
	}

	public LuceneQueryBuilder addRangePredicate(List<String> propertyPath, Object lower, Object upper) {
		Object lowerValue = propertyHelper.convertToPropertyType( lower, entityType, propertyPath );
		Object upperValue = propertyHelper.convertToPropertyType( upper, entityType, propertyPath );
		pushPredicate( new RangePredicate( queryBuilder, Strings.join( propertyPath, "." ), lowerValue, upperValue ) );

		return this;
	}

	public LuceneQueryBuilder pushAndPredicate() {
		pushPredicate( new ConjunctionPredicate( queryBuilder ) );
		return this;
	}

	public LuceneQueryBuilder pushOrPredicate() {
		pushPredicate( new DisjunctionPredicate( queryBuilder ) );
		return this;
	}

	public LuceneQueryBuilder pushNotPredicate() {
		pushPredicate( new NegationPredicate( queryBuilder ) );
		return this;
	}

	private void pushPredicate(Predicate predicate) {
		// Add as sub-predicate to the current top predicate
		predicates.peek().add( predicate );

		// push to parent predicate stack if required
		if ( predicate.getType().isParent() ) {
			predicates.push( predicate.as( ParentPredicate.class ) );
		}
	}

	public LuceneQueryBuilder popBooleanPredicate() {
		predicates.pop();
		return this;
	}

	/**
	 * Returns the Lucene query created by this builder.
	 *
	 * @return the Lucene query created by this builder
	 */
	public Query build() {
		return rootPredicate.getQuery();
	}

	@Override
	public String toString() {
		return "LuceneQueryBuilder [entityType=" + entityType + ", rootPredicate=" + rootPredicate + "]";
	}
}
