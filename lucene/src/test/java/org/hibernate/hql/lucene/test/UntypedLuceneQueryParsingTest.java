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

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.lucene.LuceneProcessingChain;
import org.hibernate.hql.lucene.LuceneQueryParsingResult;
import org.hibernate.hql.lucene.internal.LuceneQueryRendererDelegate;
import org.hibernate.hql.lucene.internal.UntypedLuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.spi.FieldBridgeProvider;
import org.hibernate.hql.lucene.test.model.GenericValueHolder;
import org.hibernate.hql.lucene.test.model.IndexedEntity;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DefaultStringBridge;
import org.hibernate.search.bridge.builtin.NumericFieldBridge;
import org.hibernate.search.bridge.builtin.StringBridge;
import org.hibernate.search.bridge.builtin.impl.NullEncodingFieldBridge;
import org.hibernate.search.bridge.builtin.impl.NullEncodingTwoWayFieldBridge;
import org.hibernate.search.bridge.builtin.impl.String2FieldBridgeAdaptor;
import org.hibernate.search.bridge.builtin.impl.TwoWayString2FieldBridgeAdaptor;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.testsupport.junit.SearchFactoryHolder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration test for {@link UntypedLuceneQueryResolverDelegate} and {@link LuceneQueryRendererDelegate}.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 * @author Gunnar Morling
 */
public class UntypedLuceneQueryParsingTest extends LuceneQueryParsingTestBase {

	@Rule
	public SearchFactoryHolder factoryHolder = new SearchFactoryHolder( GenericValueHolder.class );

	@Override
	protected LuceneProcessingChain setUpLuceneProcessingChain(Map<String, Object> namedParameters) {
		SearchIntegrator searchFactory = factoryHolder.getSearchFactory();
		EntityNamesResolver nameResolver = new ConstantEntityNamesResolver();

		return new LuceneProcessingChain.Builder( searchFactory, nameResolver )
			.namedParameters( namedParameters )
			.buildProcessingChainForDynamicEntities( new TestFieldBridgeProvider() );
	}

	@Test
	public void shouldDetermineTargetEntityType() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e from IndexedEntity e where e.name = 'same' and not e.id = 5" );
		assertThat( parsingResult.getTargetEntityName() ).isEqualTo( "IndexedEntity" );
		assertThat( parsingResult.getTargetEntity() ).isSameAs( GenericValueHolder.class );
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
	public void shouldBuildSortForNullEncoding() {
		LuceneQueryParsingResult parsingResult = parseQuery( "select e from IndexedEntity e order by e.code DESC" );
		Sort sort = parsingResult.getSort();
		assertThat( sort ).isNotNull();
		assertThat( sort.getSort().length ).isEqualTo( 1 );
		assertThat( sort.getSort()[0].getField() ).isEqualTo( "code" );
		assertThat( sort.getSort()[0].getType() ).isEqualTo( SortField.Type.LONG );
	}

	/**
	 * A {@link FieldBridgeProvider} which returns fields for a dynamic entity equivalent to {@link IndexedEntity}.
	 *
	 * @author Gunnar Morling
	 */
	private static class TestFieldBridgeProvider implements FieldBridgeProvider {

		private final Map<String, Map<String, FieldBridge>> bridgesByType = new HashMap<String, Map<String, FieldBridge>>();

		private TestFieldBridgeProvider() {
			Map<String, FieldBridge> indexedEntityBridges = new HashMap<String, FieldBridge>();

			indexedEntityBridges.put( "id", new TwoWayString2FieldBridgeAdaptor( new StringBridge() ) );
			indexedEntityBridges.put( "name", new NullEncodingTwoWayFieldBridge( new TwoWayString2FieldBridgeAdaptor( new StringBridge() ), "_null_" ) );
			indexedEntityBridges.put( "position", NumericFieldBridge.LONG_FIELD_BRIDGE );
			indexedEntityBridges.put( "code", new NullEncodingTwoWayFieldBridge( NumericFieldBridge.LONG_FIELD_BRIDGE, "_null_" ) );
			indexedEntityBridges.put( "title", new TwoWayString2FieldBridgeAdaptor( new StringBridge() ) );
			indexedEntityBridges.put( "author", new NullEncodingFieldBridge( new String2FieldBridgeAdaptor( DefaultStringBridge.INSTANCE ), "_null_" ) );
			indexedEntityBridges.put( "author.name", new TwoWayString2FieldBridgeAdaptor( new StringBridge() ) );
			indexedEntityBridges.put( "contactDetails.email", new TwoWayString2FieldBridgeAdaptor( new StringBridge() ) );
			indexedEntityBridges.put( "contactDetails.address.alternatives.postCode", new TwoWayString2FieldBridgeAdaptor( new StringBridge() ) );

			bridgesByType.put( "IndexedEntity", indexedEntityBridges );
		}

		@Override
		public FieldBridge getFieldBridge(String type, String propertyName) {
			Map<String, FieldBridge> bridgesOfType = bridgesByType.get( type );

			if ( bridgesOfType != null ) {
				FieldBridge bridge = bridgesOfType.get( propertyName );
				if ( bridge != null ) {
					return bridge;
				}
			}

			throw new IllegalArgumentException( String.format( "Unknown property: %s.%s", type, propertyName ) );
		}
	}

	private static class ConstantEntityNamesResolver implements EntityNamesResolver {

		@Override
		public Class<?> getClassFromName(String entityName) {
			return GenericValueHolder.class;
		}
	}
}
