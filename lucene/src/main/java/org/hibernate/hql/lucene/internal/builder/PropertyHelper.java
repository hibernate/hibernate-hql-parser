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

import org.apache.lucene.document.Field;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DoubleNumericFieldBridge;
import org.hibernate.search.bridge.builtin.FloatNumericFieldBridge;
import org.hibernate.search.bridge.builtin.IntegerNumericFieldBridge;
import org.hibernate.search.bridge.builtin.LongNumericFieldBridge;
import org.hibernate.search.bridge.builtin.impl.TwoWayString2FieldBridgeAdaptor;
import org.hibernate.search.engine.spi.AbstractDocumentBuilder;
import org.hibernate.search.engine.spi.EntityIndexBinder;
import org.hibernate.search.spi.SearchFactoryIntegrator;

/**
 * Provides functionality for dealing with Lucene-mapped properties.
 *
 * @author Gunnar Morling
 */
public class PropertyHelper {

	private static final Log log = LoggerFactory.make();

	private final SearchFactoryIntegrator searchFactory;

	public PropertyHelper(SearchFactoryIntegrator searchFactory) {
		this.searchFactory = searchFactory;
	}

	/**
	 * Returns the given value converted into the type of the given property as determined via the field bridge of the
	 * property.
	 *
	 * @param value the value to convert
	 * @param entityType the type hosting the property
	 * @param propertyName the name of the property
	 * @return the given value converted into the type of the given property
	 */
	public Object convertToPropertyType(Object value, Class<?> entityType, String propertyName) {
		FieldBridge bridge = getFieldBridge( entityType, propertyName );

		// For non-string fields we're assuming they have the correct type already; If that's not
		// the case, the Lucene query creation will fail later on
		if ( !( value instanceof String ) ) {
			return value;
		}

		String stringValue = (String) value;

		if ( bridge instanceof TwoWayString2FieldBridgeAdaptor ) {
			return ( (TwoWayString2FieldBridgeAdaptor) bridge ).unwrap().stringToObject( stringValue );
		}
		else if ( bridge instanceof IntegerNumericFieldBridge ) {
			return Integer.parseInt( stringValue );
		}
		else if ( bridge instanceof LongNumericFieldBridge ) {
			return Long.parseLong( stringValue );
		}
		else if ( bridge instanceof FloatNumericFieldBridge ) {
			return Float.parseFloat( stringValue );
		}
		else if ( bridge instanceof DoubleNumericFieldBridge ) {
			return Double.parseDouble( stringValue );
		}
		else {
			return value;
		}

		// See DocumentBuilderHelper.processFieldsForProjection for a more complete logic esp around embedded objects
	}

	private FieldBridge getFieldBridge(Class<?> type, String propertyName) {
		EntityIndexBinder entityIndexBinding = searchFactory.getIndexBindingForEntity( type );
		if ( entityIndexBinding == null ) {
			throw log.getNoIndexedEntityException( type.getCanonicalName() );
		}

		if ( propertyName.equals( entityIndexBinding.getDocumentBuilder().getIdentifierName() ) ) {
			return entityIndexBinding.getDocumentBuilder().getIdBridge();
		}
		else {
			AbstractDocumentBuilder.PropertiesMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();
			int index = metadata.fieldNames.indexOf( propertyName );
			if ( index == -1 ) {
				throw log.getNoSuchPropertyException( type.getCanonicalName(), propertyName );
			}

			return metadata.fieldBridges.get( index );
		}
	}

	public boolean exists(Class<?> type, String propertyName) {
		EntityIndexBinder entityIndexBinding = searchFactory.getIndexBindingForEntity( type );
		if ( entityIndexBinding == null ) {
			throw log.getNoIndexedEntityException( type.getCanonicalName() );
		}

		if ( propertyName.equals( entityIndexBinding.getDocumentBuilder().getIdentifierName() ) ) {
			return true;
		}
		else {
			AbstractDocumentBuilder.PropertiesMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();
			if ( metadata.fieldNames.indexOf( propertyName ) != -1 ) {
				return true;
			}
			else {
				return metadata.embeddedFieldNames.indexOf( propertyName )  != -1;
			}
		}
	}

	public boolean isAnalyzed(Class<?> type, String propertyName) {
		EntityIndexBinder entityIndexBinding = searchFactory.getIndexBindingForEntity( type );
		if ( entityIndexBinding == null ) {
			throw log.getNoIndexedEntityException( type.getCanonicalName() );
		}

		if ( propertyName.equals( entityIndexBinding.getDocumentBuilder().getIdentifierName() ) ) {
			return false;
		}
		else {
			AbstractDocumentBuilder.PropertiesMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();
			int index = metadata.fieldNames.indexOf( propertyName );
			if ( index == -1 ) {
				throw log.getNoSuchPropertyException( type.getCanonicalName(), propertyName );
			}

			return EnumSet.of( Field.Index.ANALYZED, Field.Index.ANALYZED_NO_NORMS ).contains( metadata.fieldIndex.get( index ) );
		}
	}
}
