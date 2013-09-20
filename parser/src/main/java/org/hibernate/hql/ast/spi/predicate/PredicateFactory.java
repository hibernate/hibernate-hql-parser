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
package org.hibernate.hql.ast.spi.predicate;

import java.util.List;

import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate.Type;

/**
 * Factory for creating predicate instances. Used by the query builder to create a stack of predicates representing the
 * processed query.
 *
 * @author Gunnar Morling
 */
public interface PredicateFactory<Q> {

	RootPredicate<Q> getRootPredicate(String entityType);

	ComparisonPredicate<Q> getComparisonPredicate(String entityType, Type comparisonType, List<String> propertyPath, Object value);

	InPredicate<Q> getInPredicate(String entityType, List<String> propertyPath, List<Object> typedElements);

	RangePredicate<Q> getRangePredicate(String entityType, List<String> propertyPath, Object lowerValue, Object upperValue);

	NegationPredicate<Q> getNegationPredicate();

	DisjunctionPredicate<Q> getDisjunctionPredicate();

	ConjunctionPredicate<Q> getConjunctionPredicate();

	LikePredicate<Q> getLikePredicate(String entityType, List<String> propertyPath, String patternValue, Character escapeCharacter);

	IsNullPredicate<Q> getIsNullPredicate(String entityType, List<String> propertyPath);
}
