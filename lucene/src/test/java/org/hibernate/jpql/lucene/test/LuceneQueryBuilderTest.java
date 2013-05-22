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
package org.hibernate.jpql.lucene.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.hibernate.jpql.lucene.LuceneQueryBuilder;
import org.hibernate.jpql.lucene.LuceneQueryParsingResult;
import org.hibernate.jpql.lucene.test.model.IndexedEntity;
import org.hibernate.jpql.lucene.testutil.BaseSearchFactoryImplementor;
import org.hibernate.jpql.lucene.testutil.MapBasedEntityNamesResolver;
import org.hibernate.query.ast.common.ParserContext;
import org.hibernate.query.ast.origin.hql.parse.HQLLexer;
import org.hibernate.query.ast.origin.hql.parse.HQLParser;
import org.hibernate.query.ast.origin.hql.resolve.GeneratedHQLResolver;
import org.hibernate.query.ast.spi.EntityNamesResolver;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.junit.Test;

/**
 * Test for {@link LuceneQueryBuilder}.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 * @author Gunnar Morling
 */
public class LuceneQueryBuilderTest {

	private static boolean USE_STDOUT = true;

	@Test
	public void shouldCreateUnrestrictedQuery() {
		transformationAssert(
				"from IndexedEntity" ,
				"*:*" );
	}

	@Test
	public void shouldCreateRestrictedQueryUsingSelect() {
		transformationAssert(
				"select e from IndexedEntity e where e.name = 'same' and not e.id = 5" ,
				"+name:same +(-id:5)" );
	}

	@Test
	public void shouldCreateQueryWithNamedParameter() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "nameParameter", "Bob" );

		transformationAssert(
				"from IndexedEntity e where e.name = :nameParameter" ,
				"name:Bob",
				namedParameters);
	}

	@Test
	public void shouldCreateBooleanQuery() {
		transformationAssert(
				"from IndexedEntity e where e.name = 'same' or ( e.id = 4 and e.name = 'booh')" ,
				"name:same (+id:4 +name:booh)" );
	}

	@Test
	public void shouldCreateBooleanQueryUsingSelect() {
		transformationAssert(
				"select e from IndexedEntity e where e.name = 'same' or ( e.id = 4 and e.name = 'booh')" ,
				"name:same (+id:4 +name:booh)" );
	}

	@Test
	public void shouldCreateBetweenQuery() {
		transformationAssert(
				"select e from IndexedEntity e where e.name between 'aaa' and 'zzz'" ,
				"name:[aaa TO zzz]" );
	}

	@Test
	public void shouldCreateBetweenQueryForCharacterLiterals() {
		transformationAssert( "select e from IndexedEntity e where e.name between 'a' and 'z'", "name:[a TO z]" );
	}

	@Test
	public void shouldCreateBetweenQueryWithNamedParameters() {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put( "lower", "aaa" );
		namedParameters.put( "upper", "zzz" );

		transformationAssert(
				"select e from IndexedEntity e where e.name between :lower and :upper" ,
				"name:[aaa TO zzz]",
				namedParameters);
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

	private void transformationAssert(String jpaql, String expectedLuceneQuery) {
		transformationAssert( jpaql, expectedLuceneQuery, null );
	}

	private void transformationAssert(String jpaql, String expectedLuceneQuery, Map<String, Object> namedParameters) {
		if ( USE_STDOUT ) {
			System.out.println( jpaql );
		}
		SearchFactoryMock searchFactory = new SearchFactoryMock();
		HashMap<String, Class<?>> entityNames = new HashMap<String, Class<?>>();
		entityNames.put( "com.acme.IndexedEntity", IndexedEntity.class );
		entityNames.put( "IndexedEntity", IndexedEntity.class );
		//generated alias:
		LuceneQueryParsingResult parsingResult = assertTreeParsed( null, jpaql, searchFactory, entityNames, namedParameters );
		Assert.assertTrue( IndexedEntity.class.equals( parsingResult.getTargetEntity() ) );
		Assert.assertEquals( expectedLuceneQuery, parsingResult.getQuery().toString() );
		if ( USE_STDOUT ) {
			System.out.println( expectedLuceneQuery );
			System.out.println();
		}
	}

	private LuceneQueryParsingResult assertTreeParsed(ParserContext context, String input, SearchFactoryImplementor searchFactory,
			HashMap<String, Class<?>> entityNames, Map<String, Object> namedParameters) {
		HQLLexer lexed = new HQLLexer( new ANTLRStringStream( input ) );
		Assert.assertEquals( 0, lexed.getNumberOfSyntaxErrors() );
		CommonTokenStream tokens = new CommonTokenStream( lexed );

		CommonTree tree = null;
		HQLParser parser = new HQLParser( tokens );
		if ( context != null ) {
			parser.setParserContext( context );
		}
		try {
			HQLParser.statement_return r = parser.statement();
			Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
			tree = (CommonTree) r.getTree();
		}
		catch (RecognitionException e) {
			Assert.fail( e.getMessage() );
		}

		if ( tree != null ) {
			if ( USE_STDOUT ) {
				System.out.println( tree.toStringTree() );
			}
			// To walk the resulting tree we need a treenode stream:
			CommonTreeNodeStream treeStream = new CommonTreeNodeStream( tree );

			// AST nodes have payloads referring to the tokens from the Lexer:
			treeStream.setTokenStream( tokens );

			EntityNamesResolver nameResolver = new MapBasedEntityNamesResolver( entityNames );
			// Finally create the treewalker:
			LuceneQueryBuilder delegate = new LuceneQueryBuilder( searchFactory, nameResolver, namedParameters );
			GeneratedHQLResolver walker = new GeneratedHQLResolver( treeStream, delegate );
			try {
				walker.statement();
				Assert.assertEquals( 0, walker.getNumberOfSyntaxErrors() );
				return delegate.getResult();
			}
			catch (RecognitionException e) {
				Assert.fail( e.getMessage() );
			}
		}
		return null; // failed
	}

	private class SearchFactoryMock extends BaseSearchFactoryImplementor {
		// For now keep it simple.
		// We might want to add enough testing context to use the org.hibernate.search.query.dsl.QueryBuilder,
		// and so take advantage of the well-known analyzers, fieldbridges and actual field names.
	}
}
