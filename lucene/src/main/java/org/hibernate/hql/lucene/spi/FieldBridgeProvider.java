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
package org.hibernate.hql.lucene.spi;

import org.hibernate.search.bridge.FieldBridge;

/**
 * Implementations provide the field bridges to be applied for queries on the properties of an entity.
 *
 * @author Gunnar Morling
 */
public interface FieldBridgeProvider {

	/**
	 * Returns the field bridge to be applied when executing queries on the given property of the given entity type.
	 *
	 * @param type the entity type hosting the given property; may either identify an actual Java type or a virtual type
	 * managed by the given implementation; never {@code null}
	 * @param propertyPath a dot separated path denoting the property of interest, e.g. "foo" or "foo.bar" (in case this
	 * is an embedded property); never {@code null}
	 * @return the field bridge to be used for querying the given property; may be {@code null}
	 */
	FieldBridge getFieldBridge(String type, String propertyPath);
}
