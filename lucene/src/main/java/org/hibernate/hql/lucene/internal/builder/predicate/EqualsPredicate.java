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
package org.hibernate.hql.lucene.internal.builder.predicate;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * An {@code EQUALS} predicate.
 *
 * @author Gunnar Morling
 */
public class EqualsPredicate extends AbstractPredicate {

	private final QueryBuilder builder;
	private final String propertyName;
	private final Object value;

	public EqualsPredicate(QueryBuilder builder, String propertyName, Object value) {
		super( Type.EQUALS );
		this.builder = builder;
		this.propertyName = propertyName;
		this.value = value;
	}

	@Override
	public Query getQuery() {
		return builder.keyword().onField( propertyName ).matching( value ).createQuery();
	}

	@Override
	public String toString() {
		return "( EQUALS " + propertyName + " " + value + " )";
	}
}
