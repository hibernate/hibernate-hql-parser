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

import java.util.regex.Pattern;

import org.apache.lucene.search.Query;
import org.hibernate.hql.ast.spi.predicate.LikePredicate;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Lucene-based {@code LIKE} predicate.
 *
 * @author Gunnar Morling
 */
public class LuceneLikePredicate extends LikePredicate<Query> {

	private static final String LUCENE_SINGLE_CHARACTER_WILDCARD = "?";
	private static final String LUCENE_MULTIPLE_CHARACTERS_WILDCARD = "*";

	private static final Pattern MULTIPLE_CHARACTERS_WILDCARD_PATTERN = Pattern.compile( "%" );
	private static final Pattern SINGLE_CHARACTER_WILDCARD_PATTERN = Pattern.compile( "_" );

	private final MatchingContextSupport matchingContextSupport;

	public LuceneLikePredicate(QueryBuilder builder, FieldBridge fieldBridge, String propertyName, String patternValue) {
		super( propertyName, patternValue, null );
		this.matchingContextSupport = new MatchingContextSupport( builder, fieldBridge, propertyName );
	}

	@Override
	public Query getQuery() {
		String patternValue = MULTIPLE_CHARACTERS_WILDCARD_PATTERN.matcher( this.patternValue ).replaceAll( LUCENE_MULTIPLE_CHARACTERS_WILDCARD );
		patternValue = SINGLE_CHARACTER_WILDCARD_PATTERN.matcher( patternValue ).replaceAll( LUCENE_SINGLE_CHARACTER_WILDCARD );

		return matchingContextSupport.wildcardTermMatchingContext().matching( patternValue ).createQuery();
	}
}
