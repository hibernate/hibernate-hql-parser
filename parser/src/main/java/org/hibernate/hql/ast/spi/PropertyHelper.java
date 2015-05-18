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
package org.hibernate.hql.ast.spi;

import java.util.List;

/**
 * Helper dealing with entity properties.
 *
 * @author Gunnar Morling
 */
public interface PropertyHelper {

	/**
	 * Converts the given string value specified via JP-QL into the actual type of the given property.
	 *
	 * @param entityType the entity type owning the property
	 * @param propertyPath the path from the entity to the property (will only contain more than one element in case the
	 * entity is hosted on an embedded entity).
	 * @param value the value of the property
	 * @return the property value, converted into the actual type of the given entity property
	 */
	Object convertToPropertyType(String entityType, List<String> propertyPath, String value);

	/**
	 * Converts the given property value into the type expected by the query backend.
	 *
	 * @param entityType the entity type owning the property
	 * @param propertyPath the path from the entity to the property (will only contain more than one element in case the
	 * entity is hosted on an embedded entity).
	 * @param value the value of the property
	 * @return the property value, converted into the type expected by the query backend
	 */
	Object convertToBackendType(String entityType, List<String> propertyPath, Object value);
}
