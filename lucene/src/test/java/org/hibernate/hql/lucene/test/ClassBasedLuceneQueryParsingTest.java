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

import java.util.Map;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.hql.ParsingException;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.lucene.LuceneProcessingChain;
import org.hibernate.hql.lucene.LuceneQueryParsingResult;
import org.hibernate.hql.lucene.internal.ClassBasedLuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.internal.LuceneQueryRendererDelegate;
import org.hibernate.hql.lucene.test.model.IndexedEntity;
import org.hibernate.hql.lucene.testutil.MapBasedEntityNamesResolver;
import org.hibernate.search.spi.SearchFactoryIntegrator;
import org.hibernate.search.testsupport.junit.SearchFactoryHolder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration test for {@link ClassBasedLuceneQueryResolverDelegate} and {@link LuceneQueryRendererDelegate}.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 * @author Gunnar Morling
 */
public class ClassBasedLuceneQueryParsingTest extends LuceneQueryParsingTestBase {

	@Rule
	public SearchFactoryHolder factoryHolder = new SearchFactoryHolder( IndexedEntity.class );

	@Override
	protected LuceneProcessingChain setUpLuceneProcessingChain(Map<String, Object> namedParameters) {
		SearchFactoryIntegrator searchFactory = factoryHolder.getSearchFactory();
		EntityNamesResolver nameResolver = MapBasedEntityNamesResolver.forClasses( IndexedEntity.class );

		return new LuceneProcessingChain.Builder( searchFactory, nameResolver )
			.namedParameters( namedParameters )
			.buildProcessingChainForClassBasedEntities();
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
	public void shouldDetermineTargetEntityType() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e from IndexedEntity e where e.name = 'same' and not e.id = 5" );
		assertThat( parsingResult.getTargetEntity() ).isSameAs( IndexedEntity.class );
		assertThat( parsingResult.getTargetEntityName() ).isEqualTo( "IndexedEntity" );

		parsingResult = parseQuery( "select e from org.hibernate.hql.lucene.test.model.IndexedEntity e" );
		assertThat( parsingResult.getTargetEntity() ).isSameAs( IndexedEntity.class );
		assertThat( parsingResult.getTargetEntityName() ).isEqualTo( "org.hibernate.hql.lucene.test.model.IndexedEntity" );
	}

	@Test
	public void shouldBuildOneFieldSort() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e from IndexedEntity e where e.name = 'same' order by e.title" );
		Sort sort = parsingResult.getSort();
		assertThat( sort ).isNotNull();
		assertThat( sort.getSort().length ).isEqualTo( 1 );
		assertThat( sort.getSort()[0].getField() ).isEqualTo( "title" );
		assertThat( sort.getSort()[0].getReverse() ).isEqualTo( false );
		assertThat( sort.getSort()[0].getType() ).isEqualTo( SortField.Type.STRING );
	}

	@Test
	public void shouldBuildTwoFieldsSort() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e from IndexedEntity e where e.name = 'same' order by e.title, e.position DESC" );
		Sort sort = parsingResult.getSort();
		assertThat( sort ).isNotNull();
		assertThat( sort.getSort().length ).isEqualTo( 2 );
		assertThat( sort.getSort()[0].getField() ).isEqualTo( "title" );
		assertThat( sort.getSort()[0].getReverse() ).isEqualTo( false );
		assertThat( sort.getSort()[0].getType() ).isEqualTo( SortField.Type.STRING );
		assertThat( sort.getSort()[1].getField() ).isEqualTo( "position" );
		assertThat( sort.getSort()[1].getReverse() ).isEqualTo( true );
		assertThat( sort.getSort()[1].getType() ).isEqualTo( SortField.Type.LONG );
	}

	@Test
	public void shouldRaiseExceptionDueToUnrecognizedSortDirection() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLPARSER000006" );

		parseQuery( "select e from IndexedEntity e where e.name = 'same' order by e.title DESblah, e.name ASC" );
	}
}
