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
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * The root predicate of the {@code WHERE} clause of a query. Allows for a uniform handling of clauses containing only a
 * single predicate as well as clauses containing several logically joined predicates.
 *
 * @author Gunnar Morling
 */
public class RootPredicate extends AbstractPredicate implements ParentPredicate {

	private static final Log log = LoggerFactory.make();

	private final QueryBuilder builder;
	private Predicate child;

	public RootPredicate(QueryBuilder builder) {
		super( Type.ROOT );
		this.builder = builder;
	}

	@Override
	public void add(Predicate predicate) {
		if ( child != null ) {
			throw log.getNotMoreThanOnePredicateInRootOfWhereClauseAllowedException( predicate );
		}

		child = predicate;
	}

	@Override
	public Query getQuery() {
		return child == null ? builder.all().createQuery() : child.getQuery();
	}

	@Override
	public String toString() {
		return child == null ? "( * )" : child.toString();
	}
}
