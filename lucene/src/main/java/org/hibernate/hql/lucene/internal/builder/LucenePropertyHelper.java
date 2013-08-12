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
package org.hibernate.hql.lucene.internal.builder;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.hibernate.hql.ast.spi.PropertyHelper;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DoubleNumericFieldBridge;
import org.hibernate.search.bridge.builtin.FloatNumericFieldBridge;
import org.hibernate.search.bridge.builtin.IntegerNumericFieldBridge;
import org.hibernate.search.bridge.builtin.LongNumericFieldBridge;
import org.hibernate.search.bridge.builtin.impl.TwoWayString2FieldBridgeAdaptor;
import org.hibernate.search.engine.metadata.impl.EmbeddedTypeMetadata;
import org.hibernate.search.engine.metadata.impl.PropertyMetadata;
import org.hibernate.search.engine.metadata.impl.TypeMetadata;
import org.hibernate.search.engine.spi.EntityIndexBinding;
import org.hibernate.search.spi.SearchFactoryIntegrator;

/**
 * Provides functionality for dealing with Lucene-mapped properties.
 *
 * @author Gunnar Morling
 */
public class LucenePropertyHelper implements PropertyHelper {

	private static final Log log = LoggerFactory.make();

	private final SearchFactoryIntegrator searchFactory;

	public LucenePropertyHelper(SearchFactoryIntegrator searchFactory) {
		this.searchFactory = searchFactory;
	}

	@Override
	public Object convertToPropertyType(Class<?> entityType, List<String> propertyPath, String value) {
		return convertToPropertyType( value, entityType, propertyPath );
	}

	public Object convertToPropertyType(String value, Class<?> entityType, List<String> propertyPath) {
		return convertToPropertyType( value, entityType, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	/**
	 * Returns the given value converted into the type of the given property as determined via the field bridge of the
	 * property.
	 *
	 * @param value the value to convert
	 * @param entityType the type hosting the property
	 * @param propertyPath the name of the property
	 * @return the given value converted into the type of the given property
	 */
	public Object convertToPropertyType(String value, Class<?> entityType, String... propertyPath) {
		FieldBridge bridge = getFieldBridge( entityType, propertyPath );

		if ( bridge instanceof TwoWayString2FieldBridgeAdaptor ) {
			return ( (TwoWayString2FieldBridgeAdaptor) bridge ).unwrap().stringToObject( value );
		}
		else if ( bridge instanceof IntegerNumericFieldBridge ) {
			return Integer.parseInt( value );
		}
		else if ( bridge instanceof LongNumericFieldBridge ) {
			return Long.parseLong( value );
		}
		else if ( bridge instanceof FloatNumericFieldBridge ) {
			return Float.parseFloat( value );
		}
		else if ( bridge instanceof DoubleNumericFieldBridge ) {
			return Double.parseDouble( value );
		}
		else {
			return value;
		}

		// See DocumentBuilderHelper.processFieldsForProjection for a more complete logic esp around embedded objects
	}

	private FieldBridge getFieldBridge(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return entityIndexBinding.getDocumentBuilder().getIdBridge();
		}

		PropertyMetadata metadata = getLeafTypeMetadata( type, propertyPath ).getPropertyMetadataForProperty( propertyPath[propertyPath.length - 1] );

		// TODO Consider properties with several fields
		return metadata.getFieldMetadata().iterator().next().getFieldBridge();
	}

	public boolean exists(Class<?> type, List<String> propertyPath) {
		return exists( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	public boolean exists(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return true;
		}

		TypeMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( int i = 0; i < propertyPath.length - 1; i++ ) {
			Set<EmbeddedTypeMetadata> embeddedTypeMetadata = metadata.getEmbeddedTypeMetadata();
			metadata = getEmbeddedTypeMetadata( embeddedTypeMetadata, propertyPath[i] );
			if ( metadata == null ) {
				return false;
			}
		}

		return metadata.getPropertyMetadataForProperty( propertyPath[propertyPath.length - 1] ) != null
				|| getEmbeddedTypeMetadata( metadata.getEmbeddedTypeMetadata(), propertyPath[propertyPath.length - 1] ) != null;
	}

	public TypeMetadata getLeafTypeMetadata(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( type );
		TypeMetadata leafTypeMetadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( int i = 0; i < propertyPath.length; i++ ) {
			Set<EmbeddedTypeMetadata> embeddedTypeMetadata = leafTypeMetadata.getEmbeddedTypeMetadata();
			TypeMetadata metadata = getEmbeddedTypeMetadata( embeddedTypeMetadata, propertyPath[i] );
			if ( metadata != null ) {
				leafTypeMetadata = metadata;
			}
		}

		return leafTypeMetadata;
	}

	private EmbeddedTypeMetadata getEmbeddedTypeMetadata(Set<EmbeddedTypeMetadata> embeddedTypeMetadata, String name) {
		for ( EmbeddedTypeMetadata metadata : embeddedTypeMetadata ) {
			if ( metadata.getEmbeddedFieldName().equals( name ) ) {
				return metadata;
			}
		}

		return null;
	}

	public boolean isAnalyzed(Class<?> type, List<String> propertyPath) {
		return isAnalyzed( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	public boolean isAnalyzed(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return false;
		}

		TypeMetadata metadata = getLeafTypeMetadata( type, propertyPath );

		Index index = metadata.getPropertyMetadataForProperty( propertyPath[propertyPath.length - 1] ).getFieldMetadata().iterator().next().getIndex();
		return EnumSet.of( Field.Index.ANALYZED, Field.Index.ANALYZED_NO_NORMS ).contains( index );
	}

	public boolean isEmbedded(Class<?> type, List<String> propertyPath) {
		return isEmbedded( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	/**
	 * Determines whether the given property path denotes an embedded entity (not a property of such entity).
	 *
	 * @param type the indexed type
	 * @param propertyPath the path of interest
	 * @return {@code true} if the given path denotes an embedded entity of the given indexed type, {@code false}
	 * otherwise.
	 */
	public boolean isEmbedded(Class<?> type, String... propertyPath) {
		if ( propertyPath.length == 0 ) {
			return false;
		}

		EntityIndexBinding entityIndexBinding = getIndexBinding( type );
		TypeMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( int i = 0; i < propertyPath.length; i++ ) {
			Set<EmbeddedTypeMetadata> embeddedTypeMetadata = metadata.getEmbeddedTypeMetadata();
			metadata = getEmbeddedTypeMetadata( embeddedTypeMetadata, propertyPath[i] );
			if ( metadata == null ) {
				break;
			}
		}

		return metadata != null;
	}

	private boolean isIdentifierProperty(EntityIndexBinding entityIndexBinding, String... propertyPath) {
		return propertyPath.length == 1 && propertyPath[0].equals( entityIndexBinding.getDocumentBuilder().getIdentifierName() );
	}

	private EntityIndexBinding getIndexBinding(Class<?> type) {
		EntityIndexBinding entityIndexBinding = searchFactory.getIndexBinding( type );

		if ( entityIndexBinding == null ) {
			throw log.getNoIndexedEntityException( type.getCanonicalName() );
		}

		return entityIndexBinding;
	}
}
