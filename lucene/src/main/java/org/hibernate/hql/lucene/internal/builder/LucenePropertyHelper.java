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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.DateTools;
import org.hibernate.hql.ParsingException;
import org.hibernate.hql.ast.spi.PropertyHelper;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.TwoWayStringBridge;
import org.hibernate.search.bridge.builtin.NumericEncodingCalendarBridge;
import org.hibernate.search.bridge.builtin.NumericEncodingDateBridge;
import org.hibernate.search.bridge.builtin.NumericFieldBridge;
import org.hibernate.search.bridge.builtin.StringEncodingCalendarBridge;
import org.hibernate.search.bridge.builtin.StringEncodingDateBridge;
import org.hibernate.search.bridge.builtin.impl.NullEncodingTwoWayFieldBridge;
import org.hibernate.search.bridge.util.impl.TwoWayString2FieldBridgeAdaptor;

/**
 * Provides functionality for dealing with Lucene-mapped properties.
 *
 * @author Gunnar Morling
 * @author Sanne Grinovero
 */
public abstract class LucenePropertyHelper implements PropertyHelper {

	@Override
	public Object convertToBackendType(String entityType, List<String> propertyPath, Object value) {
		return value;
	}

	// TODO It does not really make sense to use the field bridge here as it'd e.g. apply encoding options geared
	// towards storing the value in the index; Rather the value should be converted applying the JP-QL literal
	// conversion rules
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
		final FieldBridge bridge = getFieldBridge( entityType, propertyPath);
		return convertToPropertyType( entityType, propertyPath, value, bridge );
	}

	private Object convertToPropertyType(String entityType, List<String> propertyPath, String value, FieldBridge bridge) {
		//Order matters! Some types are subclasses of others
		//TODO expose something in Hibernate Search so that we can avoid this horrible code
		if ( bridge instanceof NullEncodingTwoWayFieldBridge ) {
			return convertToPropertyType( entityType, propertyPath, value, ( (NullEncodingTwoWayFieldBridge) bridge ).unwrap( NullEncodingTwoWayFieldBridge.class ) );
		}
		else if ( bridge instanceof TwoWayString2FieldBridgeAdaptor ) {
			return ( (TwoWayString2FieldBridgeAdaptor) bridge ).unwrap( TwoWayStringBridge.class ).stringToObject( value );
		}
		else if ( bridge instanceof TwoWayStringBridge ) {
			return ( (TwoWayStringBridge) bridge ).stringToObject( value );
		}
		else if ( bridge instanceof NumericFieldBridge ) {
			NumericFieldBridge numericBridge = (NumericFieldBridge) bridge;
			switch ( numericBridge ) {
				case INT_FIELD_BRIDGE : return Integer.valueOf( value );
				case LONG_FIELD_BRIDGE : return Long.valueOf( value );
				case FLOAT_FIELD_BRIDGE : return Float.valueOf( value );
				case DOUBLE_FIELD_BRIDGE : return Double.valueOf( value );
				default: return value;
			}
		}
		else if ( bridge instanceof StringEncodingCalendarBridge || bridge instanceof NumericEncodingCalendarBridge ) {
			Date date = parseDate( value );
			Calendar calendar = Calendar.getInstance();
			calendar.setTime( date );
			return calendar;
		}
		else if ( bridge instanceof StringEncodingDateBridge || bridge instanceof NumericEncodingDateBridge ) {
			return parseDate( value );
		}

		else {
			return value;
		}
	}

	private Date parseDate(String value) {
		try {
			return DateTools.stringToDate( value );
		}
		catch (ParseException e) {
			throw new ParsingException( e );
		}
	}

	public abstract FieldBridge getFieldBridge(String entityType, List<String> propertyPath);
}
