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

import org.hibernate.hql.internal.util.Strings;
import org.hibernate.hql.lucene.spi.FieldBridgeProvider;
import org.hibernate.search.bridge.FieldBridge;

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
	protected FieldBridge getFieldBridge(String entityType, List<String> propertyPath) {
		return fieldBridgeProvider.getFieldBridge(
				entityType,
				Strings.join( propertyPath.toArray( new String[propertyPath.size()] ), "." )
		);
	}
}
