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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.hibernate.hql.ast.spi.PropertyHelper;
import org.hibernate.hql.internal.util.Strings;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DoubleNumericFieldBridge;
import org.hibernate.search.bridge.builtin.FloatNumericFieldBridge;
import org.hibernate.search.bridge.builtin.IntegerNumericFieldBridge;
import org.hibernate.search.bridge.builtin.LongNumericFieldBridge;
import org.hibernate.search.bridge.builtin.impl.TwoWayString2FieldBridgeAdaptor;
import org.hibernate.search.engine.spi.AbstractDocumentBuilder.PropertiesMetadata;
import org.hibernate.search.engine.spi.EntityIndexBinder;
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
		EntityIndexBinder entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return entityIndexBinding.getDocumentBuilder().getIdBridge();
		}

		PropertiesMetadata metadata = getPropertyMetadata( entityIndexBinding, propertyPath );
		String fullPropertyName = Strings.join( propertyPath, "." );

		return metadata.fieldBridges.get( metadata.fieldNames.indexOf( fullPropertyName ) );
	}

	public boolean exists(Class<?> type, List<String> propertyPath) {
		return exists( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	public boolean exists(Class<?> type, String... propertyPath) {
		EntityIndexBinder entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return true;
		}

		PropertiesMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();
		String fullPropertyName = Strings.join( propertyPath, "." );

		for ( String property : propertyPath ) {
			int embeddedPropertyIndex = metadata.embeddedFieldNames.indexOf( property );

			boolean isEmbedded = embeddedPropertyIndex != -1;
			boolean isField = metadata.fieldNames.indexOf( fullPropertyName ) != -1;

			if ( !isEmbedded && !isField ) {
				return false;
			}
			else if ( isEmbedded ) {
				metadata = metadata.embeddedPropertiesMetadata.get( embeddedPropertyIndex );
			}
		}

		return true;
	}

	public boolean isAnalyzed(Class<?> type, List<String> propertyPath) {
		return isAnalyzed( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	public boolean isAnalyzed(Class<?> type, String... propertyPath) {
		EntityIndexBinder entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return false;
		}

		PropertiesMetadata metadata = getPropertyMetadata( entityIndexBinding, propertyPath );
		String fullPropertyName = Strings.join( propertyPath, "." );
		Index index = metadata.fieldIndex.get( metadata.fieldNames.indexOf( fullPropertyName ) );

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
		EntityIndexBinder entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return false;
		}

		String fullPropertyName = Strings.join( propertyPath, "." );

		PropertiesMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( String property : propertyPath ) {
			if ( metadata.embeddedFieldNames.contains( fullPropertyName ) ) {
				return true;
			}

			int embeddedPropertyIndex = metadata.embeddedFieldNames.indexOf( property );
			if ( embeddedPropertyIndex == -1 ) {
				break;
			}
			else {
				metadata = metadata.embeddedPropertiesMetadata.get( embeddedPropertyIndex );
			}
		}

		return false;
	}

	private PropertiesMetadata getPropertyMetadata(EntityIndexBinder entityIndexBinding, String... propertyPath) {
		PropertiesMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( String property : propertyPath ) {
			int embeddedPropertyIndex = metadata.embeddedFieldNames.indexOf( property );

			if ( embeddedPropertyIndex == -1 ) {
				break;
			}
			else {
				metadata = metadata.embeddedPropertiesMetadata.get( embeddedPropertyIndex );
			}
		}

		return metadata;
	}

	private boolean isIdentifierProperty(EntityIndexBinder entityIndexBinding, String... propertyPath) {
		return propertyPath.length == 1 && propertyPath[0].equals( entityIndexBinding.getDocumentBuilder().getIdentifierName() );
	}

	private EntityIndexBinder getIndexBinding(Class<?> type) {
		EntityIndexBinder entityIndexBinding = searchFactory.getIndexBindingForEntity( type );

		if ( entityIndexBinding == null ) {
			throw log.getNoIndexedEntityException( type.getCanonicalName() );
		}

		return entityIndexBinding;
	}
}
