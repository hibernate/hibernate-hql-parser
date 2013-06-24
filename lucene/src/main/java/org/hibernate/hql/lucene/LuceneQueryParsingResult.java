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
package org.hibernate.hql.lucene;

import java.util.Collections;
import java.util.List;

import org.apache.lucene.search.Query;
import org.hibernate.hql.lucene.internal.LuceneQueryResolverDelegate;

/**
 * The result of walking a query parse tree using a {@link LuceneQueryResolverDelegate}.
 *
 * @author Gunnar Morling
 */
public class LuceneQueryParsingResult {

	private final Query query;
	private final Class<?> targetEntity;
	private final List<String> projections;

	public LuceneQueryParsingResult(Query query, Class<?> targetEntity, List<String> projections) {
		this.query = query;
		this.targetEntity = targetEntity;
		this.projections = projections != null ? projections : Collections.<String>emptyList();
	}

	/**
	 * Returns the Lucene create created while walking the parse tree.
	 *
	 * @return the Lucene create created while walking the parse tree
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * Returns the entity type of the parsed query.
	 *
	 * @return the entity type of the parsed query
	 */
	public Class<?> getTargetEntity() {
		return targetEntity;
	}

	/**
	 * Returns the projections of the parsed query, represented as dot paths in case of references to fields of embedded
	 * entities, e.g. {@code ["foo", "bar.qaz"]}.
	 *
	 * @return a list with the projections of the parsed query; an empty list will be returned if no the query has no
	 * projections
	 */
	public List<String> getProjections() {
		return projections;
	}

	@Override
	public String toString() {
		return "LuceneQueryParsingResult [query=" + query + ", targetEntity=" + targetEntity + ", projections=" + projections + "]";
	}
}
