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

import java.util.List;

import org.hibernate.hql.ast.spi.PropertyHelper;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DoubleNumericFieldBridge;
import org.hibernate.search.bridge.builtin.FloatNumericFieldBridge;
import org.hibernate.search.bridge.builtin.IntegerNumericFieldBridge;
import org.hibernate.search.bridge.builtin.LongNumericFieldBridge;
import org.hibernate.search.bridge.builtin.impl.TwoWayString2FieldBridgeAdaptor;

/**
 * Provides functionality for dealing with Lucene-mapped properties.
 *
 * @author Gunnar Morling
 */
public abstract class LucenePropertyHelper implements PropertyHelper {

	/**
	 * Returns the given value converted into the type of the given property as determined via the field bridge of the
	 * property.
	 *
	 * @param value the value to convert
	 * @param entityType the type hosting the property
	 * @param propertyPath the name of the property
	 * @return the given value converted into the type of the given property
	 */
	@Override
	public Object convertToPropertyType(String entityType, List<String> propertyPath, String value) {
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

	protected abstract FieldBridge getFieldBridge(String entityType, List<String> propertyPath);
}
