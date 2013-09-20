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
import org.hibernate.hql.ast.spi.PropertyHelper;
import org.hibernate.hql.ast.spi.QueryRendererProcessor;
import org.hibernate.hql.ast.spi.QueryResolverProcessor;
import org.hibernate.hql.ast.spi.SingleEntityQueryBuilder;
import org.hibernate.hql.lucene.internal.ClassBasedLuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.internal.LuceneQueryRendererDelegate;
import org.hibernate.hql.lucene.internal.UntypedLuceneQueryResolverDelegate;
import org.hibernate.hql.lucene.internal.builder.ClassBasedLucenePropertyHelper;
import org.hibernate.hql.lucene.internal.builder.FieldBridgeProviderBasedLucenePropertyHelper;
import org.hibernate.hql.lucene.internal.builder.predicate.LucenePredicateFactory;
import org.hibernate.hql.lucene.spi.FieldBridgeProvider;
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

	/**
	 * Builds new {@link LuceneProcessingChain}s.
	 *
	 * @author Gunnar Morling
	 */
	public static class Builder {

		private final SearchFactoryIntegrator searchFactory;
		private final EntityNamesResolver entityNames;
		private Map<String, Object> namedParameters;

		public Builder(SearchFactoryIntegrator searchFactory, EntityNamesResolver entityNames) {
			this.searchFactory = searchFactory;
			this.entityNames = entityNames;
		}

		public Builder namedParameters(Map<String, Object> namedParameters) {
			this.namedParameters = namedParameters;
			return this;
		}

		/**
		 * Builds a processing chain for parsing queries targeted at dynamic entities, i.e. entity types which are not
		 * backed by an actual Java class.
		 *
		 * @param fieldBridgeProvider the field bridge provider to use for querying the targeted dynamic entity type
		 * @return a Lucene processing chain for parsing queries targeted at dynamic entities
		 */
		public LuceneProcessingChain buildProcessingChainForDynamicEntities(FieldBridgeProvider fieldBridgeProvider) {
			QueryResolverProcessor resolverProcessor = new QueryResolverProcessor( new UntypedLuceneQueryResolverDelegate( ) );

			LuceneQueryRendererDelegate rendererDelegate = getRendererDelegate(
					searchFactory,
					fieldBridgeProvider,
					entityNames,
					namedParameters,
					new FieldBridgeProviderBasedLucenePropertyHelper( fieldBridgeProvider )
					);

			QueryRendererProcessor rendererProcessor = new QueryRendererProcessor( rendererDelegate );

			return new LuceneProcessingChain( resolverProcessor, rendererProcessor, rendererDelegate );
		}

		/**
		 * Builds a processing chain for parsing queries targeted at Java class-based entities.
		 *
		 * @return a Lucene processing chain for parsing queries targeted at Java class-based entities
		 */
		public LuceneProcessingChain buildProcessingChainForClassBasedEntities() {
			ClassBasedLucenePropertyHelper propertyHelper = new ClassBasedLucenePropertyHelper( searchFactory, entityNames );

			QueryResolverProcessor resolverProcessor = new QueryResolverProcessor(
					new ClassBasedLuceneQueryResolverDelegate( propertyHelper, entityNames )
					);

			LuceneQueryRendererDelegate rendererDelegate = getRendererDelegate( searchFactory, null, entityNames, namedParameters, propertyHelper );

			QueryRendererProcessor rendererProcessor = new QueryRendererProcessor( rendererDelegate );

			return new LuceneProcessingChain( resolverProcessor, rendererProcessor, rendererDelegate );
		}

		private static LuceneQueryRendererDelegate getRendererDelegate(SearchFactoryIntegrator searchFactory, FieldBridgeProvider fieldBridgeProvider, EntityNamesResolver entityNames, Map<String, Object> namedParameters, PropertyHelper propertyHelper) {
			SingleEntityQueryBuilder<Query> queryBuilder = SingleEntityQueryBuilder.getInstance(
					new LucenePredicateFactory( searchFactory.buildQueryBuilder(), entityNames, fieldBridgeProvider ),
					propertyHelper
					);

			return new LuceneQueryRendererDelegate(
					entityNames,
					queryBuilder,
					namedParameters );
		}
	}

	private LuceneProcessingChain(QueryResolverProcessor resolverProcessor, QueryRendererProcessor rendererProcessor,
			LuceneQueryRendererDelegate rendererDelegate) {
		this.resolverProcessor = resolverProcessor;
		this.rendererProcessor = rendererProcessor;
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
