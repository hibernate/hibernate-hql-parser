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
package org.hibernate.hql.lucene.internal.builder.predicate;

import org.apache.lucene.search.Query;
import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Lucene-based comparison predicate.
 *
 * @author Gunnar Morling
 */
public class LuceneComparisonPredicate extends ComparisonPredicate<Query> {

	private final MatchingContextSupport matchingContextSupport;

	public LuceneComparisonPredicate(QueryBuilder builder, FieldBridge fieldBridge, String propertyName, Type comparisonType, Object value) {
		super( propertyName, comparisonType, value );
		this.matchingContextSupport = new MatchingContextSupport( builder, fieldBridge, propertyName );
	}

	@Override
	protected Query getStrictlyLessQuery() {
		return matchingContextSupport.rangeMatchingContext().below( value ).excludeLimit().createQuery();
	}

	@Override
	protected Query getLessOrEqualsQuery() {
		return matchingContextSupport.rangeMatchingContext().below( value ).createQuery();
	}

	@Override
	protected Query getEqualsQuery() {
		return matchingContextSupport.keyWordTermMatchingContext().matching( value ).createQuery();
	}

	@Override
	protected Query getGreaterOrEqualsQuery() {
		return matchingContextSupport.rangeMatchingContext().above( value ).createQuery();
	}

	@Override
	protected Query getStrictlyGreaterQuery() {
		return matchingContextSupport.rangeMatchingContext().above( value ).excludeLimit().createQuery();
	}
}
