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
package org.hibernate.query.lucene.internal.builder;

import org.hibernate.query.lucene.internal.logging.Log;
import org.hibernate.query.lucene.internal.logging.LoggerFactory;
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

	private static Log log = LoggerFactory.make();

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
	 *
	 * @return the given value converted into the type of the given property
	 */
	public Object convertToPropertyType(String value, Class<?> entityType, String propertyName) {
		FieldBridge bridge = getFieldBridge( entityType, propertyName );

		if (bridge instanceof TwoWayString2FieldBridgeAdaptor) {
			return ( (TwoWayString2FieldBridgeAdaptor)bridge ).unwrap().stringToObject( value );
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
}
