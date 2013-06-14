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
package org.hibernate.hql.lucene.internal.ast;

import org.hibernate.hql.ast.TypeDescriptor;
import org.hibernate.hql.lucene.internal.builder.PropertyHelper;

/**
 * A {@link TypeDescriptor} representing a Hibernate Search indexed entity.
 *
 * @author Gunnar Morling
 */
public class HSearchIndexedEntityTypeDescriptor implements HSearchTypeDescriptor {

	private final Class<?> indexedEntityType;
	private final PropertyHelper propertyHelper;

	public HSearchIndexedEntityTypeDescriptor(Class<?> indexedEntityType, PropertyHelper propertyHelper) {
		this.indexedEntityType = indexedEntityType;
		this.propertyHelper = propertyHelper;
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return propertyHelper.exists( indexedEntityType, propertyName );
	}

	@Override
	public Class<?> getIndexedEntityType() {
		return indexedEntityType;
	}

	@Override
	public String toString() {
		return indexedEntityType.getCanonicalName();
	}
}
