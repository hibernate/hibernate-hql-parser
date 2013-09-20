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

import java.util.List;

import org.apache.lucene.search.Query;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate;
import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate.Type;
import org.hibernate.hql.ast.spi.predicate.ConjunctionPredicate;
import org.hibernate.hql.ast.spi.predicate.DisjunctionPredicate;
import org.hibernate.hql.ast.spi.predicate.InPredicate;
import org.hibernate.hql.ast.spi.predicate.IsNullPredicate;
import org.hibernate.hql.ast.spi.predicate.LikePredicate;
import org.hibernate.hql.ast.spi.predicate.NegationPredicate;
import org.hibernate.hql.ast.spi.predicate.PredicateFactory;
import org.hibernate.hql.ast.spi.predicate.RangePredicate;
import org.hibernate.hql.ast.spi.predicate.RootPredicate;
import org.hibernate.hql.internal.util.Strings;
import org.hibernate.hql.lucene.spi.FieldBridgeProvider;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.QueryContextBuilder;

/**
 * Factory creating predicate instances based on Lucene.
 * <p>
 * Depending on whether the factory is created with or without a {@link FieldBridgeProvider}, the generated predicates
 * will either make use of the default field bridges configured for the addressed fields or obtain field bridges from
 * that provider.
 *
 * @author Gunnar Morling
 */
public class LucenePredicateFactory implements PredicateFactory<Query> {

	private final QueryContextBuilder queryContextBuilder;
	private final EntityNamesResolver entityNames;
	private final FieldBridgeProvider fieldBridgeProvider;
	private QueryBuilder queryBuilder;

	public LucenePredicateFactory(QueryContextBuilder queryContextBuilder, EntityNamesResolver entityNames) {
		this( queryContextBuilder, entityNames, null );
	}

	public LucenePredicateFactory(QueryContextBuilder queryContextBuilder, EntityNamesResolver entityNames, FieldBridgeProvider fieldBridgeProvider) {
		this.queryContextBuilder = queryContextBuilder;
		this.entityNames = entityNames;
		this.fieldBridgeProvider = fieldBridgeProvider;
	}

	@Override
	public RootPredicate<Query> getRootPredicate(String entityType) {
		Class<?> targetedType = entityNames.getClassFromName( entityType );
		if ( targetedType == null ) {
			throw new IllegalStateException( "Unknown entity name " + entityType );
		}

		queryBuilder = queryContextBuilder.forEntity( targetedType ).get();
		return new LuceneRootPredicate( queryBuilder );
	}

	@Override
	public ComparisonPredicate<Query> getComparisonPredicate(String entityType, Type comparisonType, List<String> propertyPath, Object value) {
		String pathAsString = getPathAsString( propertyPath );
		FieldBridge fieldBridge = getFieldBridge( entityType, pathAsString );

		return new LuceneComparisonPredicate( queryBuilder, fieldBridge, pathAsString, comparisonType, value );
	}

	@Override
	public InPredicate<Query> getInPredicate(String entityType, List<String> propertyPath, List<Object> values) {
		String pathAsString = getPathAsString( propertyPath );
		FieldBridge fieldBridge = getFieldBridge( entityType, pathAsString );

		return new LuceneInPredicate( queryBuilder, fieldBridge, pathAsString, values );
	}

	@Override
	public RangePredicate<Query> getRangePredicate(String entityType, List<String> propertyPath, Object lowerValue, Object upperValue) {
		String pathAsString = getPathAsString( propertyPath );
		FieldBridge fieldBridge = getFieldBridge( entityType, pathAsString );

		return new LuceneRangePredicate( queryBuilder, fieldBridge, pathAsString, lowerValue, upperValue );
	}

	@Override
	public NegationPredicate<Query> getNegationPredicate() {
		return new LuceneNegationPredicate( queryBuilder );
	}

	@Override
	public DisjunctionPredicate<Query> getDisjunctionPredicate() {
		return new LuceneDisjunctionPredicate( queryBuilder );
	}

	@Override
	public ConjunctionPredicate<Query> getConjunctionPredicate() {
		return new LuceneConjunctionPredicate( queryBuilder );
	}

	@Override
	public LikePredicate<Query> getLikePredicate(String entityType, List<String> propertyPath, String patternValue, Character escapeCharacter) {
		String pathAsString = getPathAsString( propertyPath );
		FieldBridge fieldBridge = getFieldBridge( entityType, pathAsString );

		return new LuceneLikePredicate( queryBuilder, fieldBridge, pathAsString, patternValue );
	}

	@Override
	public IsNullPredicate<Query> getIsNullPredicate(String entityType, List<String> propertyPath) {
		String pathAsString = getPathAsString( propertyPath );
		FieldBridge fieldBridge = getFieldBridge( entityType, pathAsString );

		return new LuceneIsNullPredicate( queryBuilder, fieldBridge, pathAsString );
	}

	private String getPathAsString(List<String> propertyPath) {
		return Strings.join( propertyPath, "." );
	}

	private FieldBridge getFieldBridge(String entityType, String pathAsString) {
		return fieldBridgeProvider != null ? fieldBridgeProvider.getFieldBridge( entityType, pathAsString ) : null;
	}
}
