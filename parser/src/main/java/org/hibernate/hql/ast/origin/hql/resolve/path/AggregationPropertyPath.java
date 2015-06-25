/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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
package org.hibernate.hql.ast.origin.hql.resolve.path;

/**
 * An aggregated property path (e.g. {@code SUM(foo.bar.baz)}) represented by {@link PathedPropertyReferenceSource}s used
 * with an aggregation function in the SELECT, HAVING or ORDER BY clause.
 *
 * @author Adrian Nistor
 */
public final class AggregationPropertyPath extends PropertyPath {

	/**
	 * The aggregation function.
	 */
	public enum Type {
		SUM, AVG, MIN, MAX, COUNT, COUNT_DISTINCT
	}

	private final Type type;

	public AggregationPropertyPath(Type type, PropertyPath propertyPath) {
		super( propertyPath );
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "AggregationPropertyPath [type=" + type + ", path=" + path + "]";
	}
}
