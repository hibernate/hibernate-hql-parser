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
package org.hibernate.hql.ast.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate.Type;
import org.hibernate.hql.ast.spi.predicate.ParentPredicate;
import org.hibernate.hql.ast.spi.predicate.Predicate;
import org.hibernate.hql.ast.spi.predicate.PredicateFactory;
import org.hibernate.hql.ast.spi.predicate.RootPredicate;

/**
 * Builder for the creation of queries targeting a single entity, based on HQL/JPQL queries.
 * <p>
 * Implemented as a stack of {@link Predicate}s which allows to add elements to the constructed query in a uniform
 * manner while traversing through the original HQL/JPQL query parse tree.
 *
 * @param <Q> the type of query created by this builder
 * @author Gunnar Morling
 */
public class SingleEntityQueryBuilder<Q> {

	private final PredicateFactory<Q> predicateFactory;
	private final PropertyHelper propertyHelper;

	/**
	 * The targeted entity type of the built query.
	 */
	private Class<?> entityType;

	/**
	 * The root predicate of the {@code WHERE} clause of the built query.
	 */
	private RootPredicate<Q> rootPredicate;

	/**
	 * Keeps track of all the parent predicates ({@code AND}, {@code OR} etc.) of the {@code WHERE} clause of the built
	 * query.
	 */
	private final Stack<ParentPredicate<Q>> predicates = new Stack<ParentPredicate<Q>>();

	private SingleEntityQueryBuilder(PredicateFactory<Q> predicateFactory, PropertyHelper propertyHelper) {
		this.predicateFactory = predicateFactory;
		this.propertyHelper = propertyHelper;
	}

	public static <Q> SingleEntityQueryBuilder<Q> getInstance(PredicateFactory<Q> predicateFactory, PropertyHelper propertyHelper) {
		return new SingleEntityQueryBuilder<Q>( predicateFactory, propertyHelper );
	}

	public SingleEntityQueryBuilder<Q> setEntityType(Class<?> entityType) {
		this.entityType = entityType;

		rootPredicate = predicateFactory.getRootPredicate( entityType );
		predicates.push( rootPredicate );

		return this;
	}

	public SingleEntityQueryBuilder<Q> addComparisonPredicate(List<String> propertyPath, Type comparisonType, Object value) {
		Object typedValue = value instanceof String ?
				propertyHelper.convertToPropertyType( entityType, propertyPath, (String) value ) :
				value;

		pushPredicate( predicateFactory.getComparisonPredicate( entityType, comparisonType, propertyPath, typedValue ) );

		return this;
	}

	public SingleEntityQueryBuilder<Q> addRangePredicate(String property, Object lower, Object upper) {
		return addRangePredicate( Arrays.asList( property ), lower, upper );
	}

	public SingleEntityQueryBuilder<Q> addRangePredicate(List<String> propertyPath, Object lower, Object upper) {
		Object lowerValue = lower instanceof String ?
				propertyHelper.convertToPropertyType( entityType, propertyPath, (String) lower ) :
				lower;

		Object upperValue = upper instanceof String ?
				propertyHelper.convertToPropertyType( entityType, propertyPath, (String) upper ) :
				upper;

		pushPredicate( predicateFactory.getRangePredicate( entityType, propertyPath, lowerValue, upperValue ) );

		return this;
	}

	public SingleEntityQueryBuilder<Q> addInPredicate(List<String> propertyPath, List<Object> elements) {
		List<Object> typedElements = new ArrayList<Object>( elements.size() );

		for ( Object element : elements ) {
			Object typedElement = element instanceof String ?
					propertyHelper.convertToPropertyType( entityType, propertyPath, (String) element ) :
					element;

			typedElements.add( typedElement );
		}

		pushPredicate( predicateFactory.getInPredicate( entityType, propertyPath, typedElements ) );

		return this;
	}

	public SingleEntityQueryBuilder<Q> addLikePredicate(List<String> propertyPath, String patternValue, Character escapeCharacter) {
		pushPredicate( predicateFactory.getLikePredicate( entityType, propertyPath, patternValue, escapeCharacter ) );
		return this;
	}

	public SingleEntityQueryBuilder<Q> addIsNullPredicate(List<String> propertyPath) {
		pushPredicate( predicateFactory.getIsNullPredicate( entityType, propertyPath ) );
		return this;
	}

	public SingleEntityQueryBuilder<Q> pushAndPredicate() {
		pushPredicate( predicateFactory.getConjunctionPredicate() );
		return this;
	}

	public SingleEntityQueryBuilder<Q> pushOrPredicate() {
		pushPredicate( predicateFactory.getDisjunctionPredicate() );
		return this;
	}

	public SingleEntityQueryBuilder<Q> pushNotPredicate() {
		pushPredicate( predicateFactory.getNegationPredicate() );
		return this;
	}

	private void pushPredicate(Predicate<Q> predicate) {
		// Add as sub-predicate to the current top predicate
		predicates.peek().add( predicate );

		// push to parent predicate stack if required
		if ( predicate.getType().isParent() ) {
			@SuppressWarnings("unchecked")
			ParentPredicate<Q> parentPredicate = predicate.as( ParentPredicate.class );
			predicates.push( parentPredicate );
		}
	}

	public SingleEntityQueryBuilder<Q> popBooleanPredicate() {
		predicates.pop();
		return this;
	}

	/**
	 * Returns the query created by this builder.
	 *
	 * @return the query created by this builder
	 */
	public Q build() {
		return rootPredicate.getQuery();
	}

	@Override
	public String toString() {
		return "SingleEntityQueryBuilder [entityType=" + entityType + ", rootPredicate=" + rootPredicate + "]";
	}
}
