/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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

import org.hibernate.hql.ast.origin.hql.resolve.path.AggregationPropertyPath;
import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate.Type;

import java.util.List;

/**
 * Builder for the creation of HAVING clause filters.
 *
 * @param <Q> the type of query created by this builder
 * @author Adrian Nistor
 */
public interface SingleEntityHavingQueryBuilder<Q> {

	void setEntityType(String entityType);

	void addComparisonPredicate(AggregationPropertyPath.Type aggregationType, List<String> propertyPath, Type comparisonType, Object value);

	void addRangePredicate(AggregationPropertyPath.Type aggregationType, List<String> propertyPath, Object lower, Object upper);

	void addInPredicate(AggregationPropertyPath.Type aggregationType, List<String> propertyPath, List<Object> elements);

	void addLikePredicate(AggregationPropertyPath.Type aggregationType, List<String> propertyPath, String patternValue, Character escapeCharacter);

	void addIsNullPredicate(AggregationPropertyPath.Type aggregationType, List<String> propertyPath);

	void pushAndPredicate();

	void pushOrPredicate();

	void pushNotPredicate();

	void popBooleanPredicate();

	Q build();
}
