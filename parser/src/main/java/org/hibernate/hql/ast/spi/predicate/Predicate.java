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
package org.hibernate.hql.ast.spi.predicate;

/**
 * Represents a predicate as contained in the {@code WHERE} clause of a query. Implementations know how to create an
 * equivalent query for themselves and, in case of parent predicates, their children.
 *
 * @param <Q> the type of query objects created by this predicate
 * @author Gunnar Morling
 */
public interface Predicate<Q> {

	/**
	 * The type of a predicate.
	 *
	 * @author Gunnar Morling
	 */
	public enum Type {
		ROOT(true), CONJUNCTION(true), DISJUNCTION(true), NEGATION(true), COMPARISON(false), RANGE(false), IN(false), LIKE(false), IS_NULL(false);

		private final boolean isParent;

		private Type(boolean isParent) {
			this.isParent = isParent;
		}

		/**
		 * Whether this is a parent predicate type (i.e. predicates of this type can have sub-predicates) or not.
		 *
		 * @return {@code true} if this is a parent predicate type, {@code false} otherwise.
		 */
		public boolean isParent() {
			return isParent;
		}
	}

	/**
	 * Returns the query represented by this predicate. Contains the all sub-predicates if this predicate is a parent
	 * predicate.
	 *
	 * @return the query represented by this predicate
	 */
	Q getQuery();

	/**
	 * Returns the type of this predicate.
	 *
	 * @return the type of this predicate
	 */
	Type getType();

	/**
	 * Narrows the type of this predicate down to the given type. The type should be checked before via
	 * {@link #getType()}.
	 *
	 * @param type the type to narrow down to
	 * @return this predicate, narrowed down to the given type
	 */
	<T extends Predicate<?>> T as(Class<T> type);
}
