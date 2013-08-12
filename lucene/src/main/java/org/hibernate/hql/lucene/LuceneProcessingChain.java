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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.hibernate.hql.ast.spi.AstProcessingChain;
import org.hibernate.hql.ast.spi.AstProcessor;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.ast.spi.QueryRendererProcessor;
import org.hibernate.hql.ast.spi.QueryResolverProcessor;
import org.hibernate.hql.ast.spi.SingleEntityQueryBuilder;
import org.hibernate.hql.lucene.internal.LuceneQueryRendererDelegate;
import org.hibernate.hql.lucene.internal.LuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.internal.builder.LucenePropertyHelper;
import org.hibernate.hql.lucene.internal.builder.predicate.LucenePredicateFactory;
import org.hibernate.search.spi.SearchFactoryIntegrator;

/**
 * AST processing chain for creating Lucene queries from HQL queries.
 *
 * @author Gunnar Morling
 */
public class LuceneProcessingChain implements AstProcessingChain<LuceneQueryParsingResult> {

	private final QueryResolverProcessor resolverProcessor;
	private final QueryRendererProcessor rendererProcessor;
	private final LuceneQueryRendererDelegate rendererDelegate;

	public LuceneProcessingChain(SearchFactoryIntegrator searchFactory, EntityNamesResolver entityNames, Map<String, Object> namedParameters) {
		this.resolverProcessor = new QueryResolverProcessor( new LuceneQueryResolverDelegate( searchFactory, entityNames, namedParameters ) );

		LucenePropertyHelper propertyHelper = new LucenePropertyHelper( searchFactory );
		SingleEntityQueryBuilder<Query> queryBuilder = SingleEntityQueryBuilder.getInstance(
				new LucenePredicateFactory( searchFactory.buildQueryBuilder(), propertyHelper ),
				propertyHelper
		);

		LuceneQueryRendererDelegate rendererDelegate = new LuceneQueryRendererDelegate(
				entityNames,
				queryBuilder,
				namedParameters );
		this.rendererProcessor = new QueryRendererProcessor( rendererDelegate );
		this.rendererDelegate = rendererDelegate;
	}

	@Override
	public Iterator<AstProcessor> iterator() {
		return Arrays.asList( resolverProcessor, rendererProcessor ).iterator();
	}

	@Override
	public LuceneQueryParsingResult getResult() {
		return rendererDelegate.getResult();
	}
}
