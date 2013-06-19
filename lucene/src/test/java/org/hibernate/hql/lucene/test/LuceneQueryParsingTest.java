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

import org.hibernate.hql.QueryParser;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.lucene.LuceneProcessingChain;
import org.hibernate.hql.lucene.LuceneQueryParsingResult;
import org.hibernate.hql.lucene.internal.LuceneQueryRendererDelegate;
import org.hibernate.hql.lucene.internal.LuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.test.model.IndexedEntity;
import org.hibernate.hql.lucene.testutil.MapBasedEntityNamesResolver;
import org.hibernate.search.spi.SearchFactoryIntegrator;
import org.hibernate.search.test.programmaticmapping.TestingSearchFactoryHolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration test for {@link LuceneQueryResolverDelegate} and {@link LuceneQueryRendererDelegate}.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 * @author Gunnar Morling
 */
public class LuceneQueryParsingTest {

	private static boolean USE_STDOUT = true;

	@Rule
	public TestingSearchFactoryHolder factoryHolder = new TestingSearchFactoryHolder( IndexedEntity.class );

	private QueryParser queryParser;

	@Before
	public void setupParser() {
		queryParser = new QueryParser();
	}

	@Test
	public void shouldCreateUnrestrictedQuery() {
		assertLuceneQuery(
				"from IndexedEntity" ,
				"*:*" );
	}

	@Test
	public void shouldCreateRestrictedQueryUsingSelect() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name = 'same' and not e.id = 5" ,
				"+name:same -id:5" );
	}

	@Test
	public void shouldCreateQueryWithUnqualifiedPropertyReferences() {
		assertLuceneQuery(
				"from IndexedEntity e where name = 'same' and not id = 5" ,
				"+name:same -id:5" );
	}

	@Test
	public void shouldCreateQueryWithNamedParameter() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "nameParameter", "Bob" );

		assertLuceneQuery(
				"from IndexedEntity e where e.name = :nameParameter" ,
				namedParameters,
				"name:Bob");
	}

	@Test
	public void shouldCreateBooleanQuery() {
		assertLuceneQuery(
				"from IndexedEntity e where e.name = 'same' or ( e.id = 4 and e.name = 'booh')" ,
				"name:same (+id:4 +name:booh)" );
	}

	@Test
	public void shouldCreateBooleanQueryUsingSelect() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name = 'same' or ( e.id = 4 and e.name = 'booh')" ,
				"name:same (+id:4 +name:booh)" );
	}

	@Test
	public void shouldCreateBetweenQuery() {
		assertLuceneQuery(
				"select e from IndexedEntity e where e.name between 'aaa' and 'zzz'" ,
				"name:[aaa TO zzz]" );
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
				"select e from IndexedEntity e where e.name between :lower and :upper" ,
				namedParameters,
				"name:[aaa TO zzz]");
	}

	@Test
	public void shouldCreateNumericBetweenQuery() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "lower", 10L );
		namedParameters.put( "upper", 20L );

		assertLuceneQuery(
				"select e from IndexedEntity e where e.position between :lower and :upper" ,
				namedParameters,
				"position:[10 TO 20]");
	}

	@Test
	public void walkTest5() {
		//TODO, we have several options:
		// - Add explicit support for NOT_EQUAL like we did for EQUALS
		// - Have the AST rewrite such cases into a unique form: [a != b] --> [NOT a = b]
		// - Have ANTLR generate the Walker embedding Lucene Queries as return types for each predicate
		//		transformationAssert(
		//				"select e from IndexedEntity e where e.name = 'same' and e.id != 5" ,
		//				"+name:'same' +(-id:5)" );
	}

	private void assertLuceneQuery(String queryString, String expectedLuceneQuery) {
		assertLuceneQuery( queryString, null, expectedLuceneQuery );
	}

	private void assertLuceneQuery(String queryString, Map<String, Object> namedParameters, String expectedLuceneQuery) {
		if ( USE_STDOUT ) {
			System.out.println( queryString );
		}

		LuceneQueryParsingResult parsingResult = queryParser.parseQuery( queryString, setUpLuceneProcessingChain( namedParameters ) );

		assertThat( parsingResult.getTargetEntity() ).isSameAs( IndexedEntity.class );
		assertThat( parsingResult.getQuery().toString() ).isEqualTo( expectedLuceneQuery );

		if ( USE_STDOUT ) {
			System.out.println( expectedLuceneQuery );
			System.out.println();
		}
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
