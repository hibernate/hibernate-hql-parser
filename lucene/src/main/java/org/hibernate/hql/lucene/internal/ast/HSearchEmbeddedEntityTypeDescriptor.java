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

import java.util.LinkedList;
import java.util.List;

import org.hibernate.hql.ast.TypeDescriptor;
import org.hibernate.hql.lucene.internal.builder.LucenePropertyHelper;

/**
 * A {@link TypeDescriptor} representing an embedded entity of a Hibernate Search indexed entity.
 *
 * @author Gunnar Morling
 */
public class HSearchEmbeddedEntityTypeDescriptor implements HSearchTypeDescriptor {

	private final Class<?> indexedEntityType;
	private final List<String> propertyPath;
	private final LucenePropertyHelper propertyHelper;

	/**
	 * Creates a new {@link HSearchEmbeddedEntityTypeDescriptor}.
	 *
	 * @param indexedEntityType the indexed entity into which this entity is embedded
	 * @param path the property path from the embedding indexed entity to this entity
	 * @param propertyHelper a helper for dealing with properties
	 */
	public HSearchEmbeddedEntityTypeDescriptor(Class<?> indexedEntityType, List<String> path, LucenePropertyHelper propertyHelper) {
		this.indexedEntityType = indexedEntityType;
		this.propertyPath = path;
		this.propertyHelper = propertyHelper;
	}

	@Override
	public boolean hasProperty(String propertyName) {
		List<String> newPath = new LinkedList<String>( propertyPath );
		newPath.add( propertyName );
		return propertyHelper.exists( indexedEntityType, newPath );
	}

	@Override
	public boolean isAnalyzed(String propertyName) {
		List<String> newPath = new LinkedList<String>( propertyPath );
		newPath.add( propertyName );
		return propertyHelper.isAnalyzed( indexedEntityType, newPath );
	}

	@Override
	public boolean isEmbedded(String propertyName) {
		List<String> newPath = new LinkedList<String>( propertyPath );
		newPath.add( propertyName );
		return propertyHelper.isEmbedded( indexedEntityType, newPath );
	}

	@Override
	public Class<?> getIndexedEntityType() {
		return indexedEntityType;
	}

	@Override
	public String toString() {
		return propertyPath.toString();
	}
}
