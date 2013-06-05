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
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * A logical {@code OR} predicate.
 *
 * @author Gunnar Morling
 */
public class DisjunctionPredicate extends ParentPredicate {

	private final QueryBuilder builder;

	public DisjunctionPredicate(QueryBuilder builder) {
		super( Type.DISJUNCTION );
		this.builder = builder;
	}

	@Override
	public Query getQuery() {
		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> booleanJunction = builder.bool();

		for ( Predicate predicate : children ) {
			booleanJunction.should( predicate.getQuery() );
		}

		return booleanJunction.createQuery();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( "( OR " );

		for ( Predicate child : children ) {
			sb.append( child.toString() ).append( " " );
		}

		sb.append( " )" );

		return sb.toString();
	}
}
