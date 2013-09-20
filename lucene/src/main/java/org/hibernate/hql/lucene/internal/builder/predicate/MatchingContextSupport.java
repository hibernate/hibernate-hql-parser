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

import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.impl.ConnectedRangeMatchingContext;
import org.hibernate.search.query.dsl.impl.ConnectedTermMatchingContext;

/**
 * Helper for creating DSL matching contexts, taking explicitly provided field bridges into account.
 *
 * @author Gunnar Morling
 */
/* package private */class MatchingContextSupport {

	QueryBuilder builder;
	FieldBridge fieldBridge;
	String propertyName;

	MatchingContextSupport(QueryBuilder builder, FieldBridge fieldBridge, String propertyName) {
		this.builder = builder;
		this.fieldBridge = fieldBridge;
		this.propertyName = propertyName;
	}

	TermMatchingContext keyWordTermMatchingContext() {
		if ( fieldBridge != null ) {
			return ( (ConnectedTermMatchingContext) builder.keyword().onField( propertyName ) ).withFieldBridge( fieldBridge ).ignoreAnalyzer();
		}
		else {
			return builder.keyword().onField( propertyName );
		}
	}

	TermMatchingContext wildcardTermMatchingContext() {
		if ( fieldBridge != null ) {
			return ( (ConnectedTermMatchingContext) builder.keyword().wildcard().onField( propertyName ) ).withFieldBridge( fieldBridge ).ignoreAnalyzer();
		}
		else {
			return builder.keyword().wildcard().onField( propertyName );
		}
	}

	RangeMatchingContext rangeMatchingContext() {
		if ( fieldBridge != null ) {
			return ( (ConnectedRangeMatchingContext) builder.range().onField( propertyName ) ).withFieldBridge( fieldBridge ).ignoreAnalyzer();
		}
		else {
			return builder.range().onField( propertyName );
		}
	}
}
