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

import java.util.Calendar;
import java.util.TimeZone;

import org.hibernate.hql.lucene.internal.builder.LucenePropertyHelper;
import org.hibernate.hql.lucene.test.internal.builder.model.IndexedEntity;
import org.hibernate.search.test.util.SearchFactoryHolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link LucenePropertyHelper}.
 *
 * @author Gunnar Morling
 */
public class LucenePropertyHelperTest {

	@Rule
	public SearchFactoryHolder factoryHolder = new SearchFactoryHolder( IndexedEntity.class );

	private LucenePropertyHelper propertyHelper;

	@Before
	public void setupPropertyTypeHelper() {
		propertyHelper = new LucenePropertyHelper( factoryHolder.getSearchFactory() );
	}

	@Test
	public void shouldConvertIdProperty() {
		assertThat( propertyHelper.convertToPropertyType( "42", IndexedEntity.class, "id" ) ).isEqualTo( "42" );
	}

	@Test
	public void shouldConvertStringProperty() {
		assertThat( propertyHelper.convertToPropertyType( "42", IndexedEntity.class, "name" ) ).isEqualTo( "42" );
	}

	@Test
	public void shouldConvertIntProperty() {
		assertThat( propertyHelper.convertToPropertyType( "42", IndexedEntity.class, "i" ) ).isEqualTo( 42 );
	}

	@Test
	public void shouldConvertLongProperty() {
		assertThat( propertyHelper.convertToPropertyType( "42", IndexedEntity.class, "l" ) ).isEqualTo( 42L );
	}

	@Test
	public void shouldConvertFloatProperty() {
		assertThat( propertyHelper.convertToPropertyType( "42.0", IndexedEntity.class, "f" ) ).isEqualTo( 42.0F );
	}

	@Test
	public void shouldConvertDoubleProperty() {
		assertThat( propertyHelper.convertToPropertyType( "42.0", IndexedEntity.class, "d" ) ).isEqualTo( 42.0D );
	}

	@Test
	public void shouldConvertDateProperty() {
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
		calendar.clear();
		calendar.set( 2012, 8, 25 );
		assertThat( propertyHelper.convertToPropertyType( "20120925", IndexedEntity.class, "date" ) ).isEqualTo( calendar.getTime() );
	}

	@Test
	public void shouldRecognizeAnalyzedField() {
		assertThat( propertyHelper.isAnalyzed( IndexedEntity.class, "description" ) ).isTrue();
	}

	@Test
	public void shouldRecognizeUnanalyzedField() {
		assertThat( propertyHelper.isAnalyzed( IndexedEntity.class, "i" ) ).isFalse();
	}
}
