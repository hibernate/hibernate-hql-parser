/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
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
package org.hibernate.hql.lucene.test;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.hql.ParsingException;
import org.hibernate.hql.QueryParser;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.lucene.LuceneProcessingChain;
import org.hibernate.hql.lucene.LuceneQueryParsingResult;
import org.hibernate.hql.lucene.internal.LuceneQueryRendererDelegate;
import org.hibernate.hql.lucene.internal.LuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.test.model.IndexedEntity;
import org.hibernate.hql.lucene.testutil.MapBasedEntityNamesResolver;
import org.hibernate.search.spi.SearchFactoryIntegrator;
import org.hibernate.search.test.util.SearchFactoryHolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration test for {@link LuceneQueryResolverDelegate} and {@link LuceneQueryRendererDelegate}.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 * @author Gunnar Morling
 */
public class LuceneQueryParsingTest {

	private static boolean USE_STDOUT = true;

	@Rule
	public SearchFactoryHolder factoryHolder = new SearchFactoryHolder( IndexedEntity.class );

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private QueryParser queryParser;

	@Before
	public void setupParser() {
		queryParser = new QueryParser();
	}

	@Test
	public void shouldCreateUnrestrictedQuery() {
		assertLuceneQuery(
				"from IndexedEntity",
				"*:*" );
	}

	@Test
	public void shouldCreateRestrictedQueryUsingSelect() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name = 'same' and not e.id = 5",
				"+name:same -id:5" );
	}

	@Test
	public void shouldCreateProjectionQuery() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e.id, e.name from IndexedEntity e" );

		assertThat( parsingResult.getQuery().toString() ).isEqualTo( "*:*" );
		assertThat( parsingResult.getProjections() ).containsExactly( "id", "name" );
	}

	@Test
	public void shouldCreateEmbeddedProjectionQuery() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e.author.name from IndexedEntity e" );

		assertThat( parsingResult.getQuery().toString() ).isEqualTo( "*:*" );
		assertThat( parsingResult.getProjections() ).containsExactly( "author.name" );
	}

	@Test
	public void shouldCreateNestedEmbeddedProjectionQuery() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e.author.address.street from IndexedEntity e" );

		assertThat( parsingResult.getQuery().toString() ).isEqualTo( "*:*" );
		assertThat( parsingResult.getProjections() ).containsExactly( "author.address.street" );
	}

	@Test
	public void shouldCreateQueryWithUnqualifiedPropertyReferences() {
		assertLuceneQuery(
				"from IndexedEntity e where name = 'same' and not id = 5",
				"+name:same -id:5" );
	}

	@Test
	public void shouldRaiseExceptionDueToUnknownAlias() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000004" );

		parseQuery( "from IndexedEntity e where a.name = 'same'" );
	}

	@Test
	public void shouldRaiseExceptionDueToUnknownQualifiedProperty() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000002" );

		parseQuery( "from IndexedEntity e where e.foobar = 'same'" );
	}

	@Test
	public void shouldRaiseExceptionDueToUnknownUnqualifiedProperty() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000002" );

		parseQuery( "from IndexedEntity e where foobar = 'same'" );
	}

	@Test
	public void shouldRaiseExceptionDueToAnalyzedPropertyInFromClause() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000003" );

		parseQuery( "from IndexedEntity e where e.size = 10" );
	}

	@Test
	public void shouldRaiseExceptionDueToUnknownPropertyInSelectClause() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000002" );

		parseQuery( "select e.foobar from IndexedEntity e" );
	}

	@Test
	public void shouldRaiseExceptionDueToUnknownPropertyInEmbeddedSelectClause() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000002" );

		parseQuery( "select e.author.foo from IndexedEntity e" );
	}

	@Test
	public void shouldRaiseExceptionDueToSelectionOfCompleteEmbeddedEntity() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000005" );

		parseQuery( "select e.author from IndexedEntity e" );
	}

	@Test
	public void shouldRaiseExceptionDueToUnqualifiedSelectionOfCompleteEmbeddedEntity() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLLUCN000005" );

		parseQuery( "select author from IndexedEntity e" );
	}

	@Test
	public void shouldCreateNegatedQuery() {
		assertLuceneQuery(
				"from IndexedEntity e where NOT e.name = 'same'",
				"-name:same *:*" );

		// JPQL syntax
		assertLuceneQuery(
				"from IndexedEntity e where e.name <> 'same'",
				"-name:same *:*" );

		// HQL syntax
		assertLuceneQuery(
				"from IndexedEntity e where e.name != 'same'",
				"-name:same *:*" );
	}

	@Test
	public void shouldCreateNegatedQueryOnNumericProperty() {
		assertLuceneQuery(
				"from IndexedEntity e where e.position <> 3",
				"-position:[3 TO 3] *:*" );
	}

	@Test
	public void shouldCreateNegatedRangeQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name = 'Bob' and not e.position between 1 and 3",
				"+name:Bob -position:[1 TO 3]" );
	}

	@Test
	public void shouldCreateQueryWithNamedParameter() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "nameParameter", "Bob" );

		assertLuceneQuery(
				"from IndexedEntity e where e.name = :nameParameter",
				namedParameters,
				"name:Bob" );
	}

	@Test
	public void shouldCreateBooleanQuery() {
		assertLuceneQuery(
				"from IndexedEntity e where e.name = 'same' or ( e.id = 4 and e.name = 'booh')",
				"name:same (+id:4 +name:booh)" );
	}

	@Test
	public void shouldCreateBooleanQueryUsingSelect() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name = 'same' or ( e.id = 4 and e.name = 'booh')",
				"name:same (+id:4 +name:booh)" );
	}

	@Test
	public void shouldCreateBetweenQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name between 'aaa' and 'zzz'",
				"name:[aaa TO zzz]" );
	}

	@Test
	public void shouldCreateNotBetweenQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name not between 'aaa' and 'zzz'",
				"-name:[aaa TO zzz] *:*" );
	}

	@Test
	public void shouldCreateBetweenQueryForCharacterLiterals() {
		assertLuceneQuery( "select e from IndexedEntity e where e.name between 'a' and 'z'", "name:[a TO z]" );
	}

	@Test
	public void shouldCreateBetweenQueryWithNamedParameters() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "lower", "aaa" );
		namedParameters.put( "upper", "zzz" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.name between :lower and :upper",
				namedParameters,
				"name:[aaa TO zzz]" );
	}

	@Test
	public void shouldCreateNumericBetweenQuery() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "lower", 10L );
		namedParameters.put( "upper", 20L );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.position between :lower and :upper",
				namedParameters,
				"position:[10 TO 20]" );
	}

	@Test
	public void shouldCreateQueryWithEmbeddedPropertyInFromClause() {
		assertLuceneQuery(
				"from IndexedEntity e where e.author.name = 'Bob'",
				"author.name:Bob" );
	}

	@Test
	public void shouldCreateLessThanQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.position < 100",
				"position:[* TO 100}" );
	}

	@Test
	public void shouldCreateLessThanOrEqualsToQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.position <= 100",
				"position:[* TO 100]" );
	}

	@Test
	public void shouldCreateGreaterThanOrEqualsToQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.position >= 100",
				"position:[100 TO *]" );
	}

	@Test
	public void shouldCreateGreaterThanQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.position > 100",
				"position:{100 TO *]" );
	}

	@Test
	public void shouldCreateInQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name in ('Bob', 'Alice')",
				"name:Bob name:Alice" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.position in (10, 20, 30, 40)",
				"position:[10 TO 10] position:[20 TO 20] position:[30 TO 30] position:[40 TO 40]" );
	}

	@Test
	public void shouldCreateInQueryWithNamedParameters() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "name1", "Bob" );
		namedParameters.put( "name2", "Alice" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.name in (:name1, :name2)",
				namedParameters,
				"name:Bob name:Alice" );

		namedParameters = new HashMap<String, Object>();
		namedParameters.put( "pos1", 10 );
		namedParameters.put( "pos2", 20 );
		namedParameters.put( "pos3", 30 );
		namedParameters.put( "pos4", 40 );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.position in (:pos1, :pos2, :pos3, :pos4)",
				namedParameters,
				"position:[10 TO 10] position:[20 TO 20] position:[30 TO 30] position:[40 TO 40]" );
	}

	@Test
	public void shouldCreateNotInQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name not in ('Bob', 'Alice')",
				"-(name:Bob name:Alice) *:*" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.position not in (10, 20, 30, 40)",
				"-(position:[10 TO 10] position:[20 TO 20] position:[30 TO 30] position:[40 TO 40]) *:*" );
	}

	@Test
	public void shouldCreateLikeQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name LIKE 'Al_ce'",
				"name:Al?ce" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.name LIKE 'Ali%'",
				"name:Ali*" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.name LIKE '_l_ce'",
				"name:?l?ce" );
	}

	@Test
	public void shouldCreateNotLikeQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name NOT LIKE 'Al_ce'",
				"-name:Al?ce *:*" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.name NOT LIKE 'Ali%'",
				"-name:Ali* *:*" );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.name NOT LIKE '_l_ce'",
				"-name:?l?ce *:*" );
	}

	@Test
	public void shouldCreateIsNullQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name IS null",
				"name:_null_" );
	}

	@Test
	public void shouldCreateIsNullQueryForEmbeddedEntity() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.author IS null",
				"author:_null_" );
	}

	@Test
	public void shouldCreateIsNotNullQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name IS NOT null",
				"-name:_null_ *:*" );
	}

	private void assertLuceneQuery(String queryString, String expectedLuceneQuery) {
		assertLuceneQuery( queryString, null, expectedLuceneQuery );
	}

	private void assertLuceneQuery(String queryString, Map<String, Object> namedParameters, String expectedLuceneQuery) {
		LuceneQueryParsingResult parsingResult = parseQuery( queryString, namedParameters );

		assertThat( parsingResult.getTargetEntity() ).isSameAs( IndexedEntity.class );
		assertThat( parsingResult.getQuery().toString() ).isEqualTo( expectedLuceneQuery );

		if ( USE_STDOUT ) {
			System.out.println( expectedLuceneQuery );
			System.out.println();
		}
	}

	private LuceneQueryParsingResult parseQuery(String queryString) {
		return parseQuery( queryString, null );
	}

	private LuceneQueryParsingResult parseQuery(String queryString, Map<String, Object> namedParameters) {
		if ( USE_STDOUT ) {
			System.out.println( queryString );
		}

		return queryParser.parseQuery(
				queryString,
				setUpLuceneProcessingChain( namedParameters )
				);
	}

	private LuceneProcessingChain setUpLuceneProcessingChain(Map<String, Object> namedParameters) {
		SearchFactoryIntegrator searchFactory = factoryHolder.getSearchFactory();

		Map<String, Class<?>> entityNames = new HashMap<String, Class<?>>();
		entityNames.put( "com.acme.IndexedEntity", IndexedEntity.class );
		entityNames.put( "IndexedEntity", IndexedEntity.class );
		EntityNamesResolver nameResolver = new MapBasedEntityNamesResolver( entityNames );

		return new LuceneProcessingChain( searchFactory, nameResolver, namedParameters );
	}
}
