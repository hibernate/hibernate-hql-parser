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
package org.hibernate.hql.lucene.test.internal.builder;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.lucene.search.Query;
import org.hibernate.hql.ast.spi.SingleEntityQueryBuilder;
import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate.Type;
import org.hibernate.hql.lucene.internal.builder.LucenePropertyHelper;
import org.hibernate.hql.lucene.internal.builder.predicate.LucenePredicateFactory;
import org.hibernate.hql.lucene.test.internal.builder.model.IndexedEntity;
import org.hibernate.search.query.dsl.QueryContextBuilder;
import org.hibernate.search.spi.SearchFactoryIntegrator;
import org.hibernate.search.test.util.SearchFactoryHolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link SingleEntityQueryBuilder}.
 *
 * @author Gunnar Morling
 */
public class LuceneQueryBuilderTest {

	@Rule
	public SearchFactoryHolder factoryHolder = new SearchFactoryHolder( IndexedEntity.class );

	private SingleEntityQueryBuilder<Query> queryBuilder;

	@Before
	public void setupQueryBuilder() {
		SearchFactoryIntegrator searchFactory = factoryHolder.getSearchFactory();
		QueryContextBuilder queryContextBuilder = searchFactory.buildQueryBuilder();

		queryBuilder = SingleEntityQueryBuilder.getInstance(
				new LucenePredicateFactory( queryContextBuilder ),
				new LucenePropertyHelper( searchFactory )
		);
	}

	@Test
	public void shouldBuildEqualsQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.addComparisonPredicate( Arrays.asList( "name" ), Type.EQUALS, "foobar" )
			.build();

		assertThat( query.toString() ).isEqualTo( "name:foobar" );
	}

	@Test
	public void shouldBuildLongEqualsQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.addComparisonPredicate( Arrays.asList( "l" ), Type.EQUALS, "10" )
			.build();

		assertThat( query.toString() ).isEqualTo( "l:[10 TO 10]" );
	}

	@Test
	public void shouldBuildDoubleEqualsQuery() {
		Query query = queryBuilder
				.setEntityType( IndexedEntity.class )
				.addComparisonPredicate( Arrays.asList( "d" ), Type.EQUALS, "10.0" )
				.build();

		assertThat( query.toString() ).isEqualTo( "d:[10.0 TO 10.0]" );
	}

	@Test
	public void shouldBuildDateEqualsQuery() {
		Query query = queryBuilder
				.setEntityType( IndexedEntity.class )
				.addComparisonPredicate( Arrays.asList( "date" ), Type.EQUALS, "201209251130" )
				.build();

		//Only "day" resolution expected as per the field's configuration
		assertThat( query.toString() ).isEqualTo( "date:20120925" );
	}

	@Test
	public void shouldBuildDateEqualsQueryForTypedDate() {
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
		calendar.set( 2012, 8, 25 );

		Query query = queryBuilder
				.setEntityType( IndexedEntity.class )
				.addComparisonPredicate( Arrays.asList( "date" ), Type.EQUALS, calendar.getTime() )
				.build();

		//Only "day" resolution expected as per the field's configuration
		assertThat( query.toString() ).isEqualTo( "date:20120925" );
	}

	@Test
	public void shouldBuildRangeQueryForTypedDates() {
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
		calendar.set( 2012, 8, 25 );
		Date start = calendar.getTime();

		calendar.set( 2012, 10, 25 );
		Date end = calendar.getTime();

		Query query = queryBuilder
				.setEntityType( IndexedEntity.class )
				.addRangePredicate( "date", start, end )
				.build();

		//Only "day" resolution expected as per the field's configuration
		assertThat( query.toString() ).isEqualTo( "date:[20120925 TO 20121125]" );
	}

	@Test
	public void shouldBuildRangeQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.addRangePredicate( "i", "1", "10" )
			.build();

		assertThat( query.toString() ).isEqualTo( "i:[1 TO 10]" );
	}

	@Test
	public void shouldBuildConjunctionQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.pushAndPredicate()
				.addComparisonPredicate( Arrays.asList( "name" ), Type.EQUALS, "foobar" )
				.addComparisonPredicate( Arrays.asList( "i" ), Type.EQUALS, "1" )
			.build();

		assertThat( query.toString() ).isEqualTo( "+name:foobar +i:[1 TO 1]" );
	}

	@Test
	public void shouldBuildDisjunctionQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.pushOrPredicate()
				.addComparisonPredicate( Arrays.asList( "name" ), Type.EQUALS, "foobar" )
				.addComparisonPredicate( Arrays.asList( "i" ), Type.EQUALS, "1" )
			.build();

		assertThat( query.toString() ).isEqualTo( "name:foobar i:[1 TO 1]" );
	}

	@Test
	public void shouldBuildNegationQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.pushNotPredicate()
				.addComparisonPredicate( Arrays.asList( "name" ), Type.EQUALS, "foobar" )
			.build();

		assertThat( query.toString() ).isEqualTo( "-name:foobar *:*" );
	}

	@Test
	public void shouldBuildNestedLogicalPredicatesQuery() {
		Query query = queryBuilder
			.setEntityType( IndexedEntity.class )
			.pushAndPredicate()
				.pushOrPredicate()
					.addComparisonPredicate( Arrays.asList( "name" ), Type.EQUALS, "foobar" )
					.addComparisonPredicate( Arrays.asList( "i" ), Type.EQUALS, "1" )
					.popBooleanPredicate()
				.addComparisonPredicate( Arrays.asList( "l" ), Type.EQUALS, "10" )
			.build();

		assertThat( query.toString() ).isEqualTo( "+(name:foobar i:[1 TO 1]) +l:[10 TO 10]" );
	}
}
