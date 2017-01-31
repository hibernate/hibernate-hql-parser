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

import org.hibernate.hql.lucene.spi.FieldBridgeProvider;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.NumericFieldBridge;
import org.hibernate.search.bridge.util.impl.BridgeAdaptor;
import org.hibernate.search.metadata.NumericFieldSettingsDescriptor.NumericEncodingType;

/**
 * A {@link LucenePropertyHelper} which delegates retrieval of {@link FieldBridge}s to a {@link FieldBridgeProvider}.
 *
 * @author Gunnar Morling
 */
public class FieldBridgeProviderBasedLucenePropertyHelper extends LucenePropertyHelper {

	private final FieldBridgeProvider fieldBridgeProvider;

	public FieldBridgeProviderBasedLucenePropertyHelper(FieldBridgeProvider fieldBridgeProvider) {
		this.fieldBridgeProvider = fieldBridgeProvider;
	}

	@Override
	public FieldBridge getFieldBridge(String entityType, List<String> propertyPath) {
		return fieldBridgeProvider.getFieldBridge(
				entityType,
				fieldName( propertyPath ) );
	}

	@Override
	public NumericEncodingType getNumericEncodingType(String entityType, List<String> propertyPath) {
		NumericFieldBridge numericFieldBridge = numericFieldBridge( entityType, propertyPath );
		if ( numericFieldBridge != null ) {
			return numericFieldBridge.getEncodingType();
		}
		return null;
	}

	private NumericFieldBridge numericFieldBridge(String entityType, List<String> propertyPath) {
		FieldBridge fieldBridge = getFieldBridge( entityType, propertyPath );
		if ( fieldBridge instanceof NumericFieldBridge ) {
			return (NumericFieldBridge) fieldBridge;
		}
		if ( fieldBridge instanceof BridgeAdaptor ) {
			return ( (BridgeAdaptor) fieldBridge ).unwrap( NumericFieldBridge.class );
		}
		return null;
	}
}
