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

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.hibernate.hql.ast.render.QueryRenderer;

/**
 * An {@link AstProcessor} which "renders" a given source query into an output query, by invoking
 * {@link QueryRendererDelegate} while traversing the given query tree.
 * </p>
 * Input: Normalized parse tree as created by {@link QueryResolverProcessor}</br>
 * Output: Query object
 *
 * @author Gunnar Morling
 */
public class QueryRendererProcessor implements AstProcessor {

	private final QueryRendererDelegate<?> delegate;

	public QueryRendererProcessor(QueryRendererDelegate<?> delegate) {
		this.delegate = delegate;
	}

	@Override
	public CommonTree process(TokenStream tokens, CommonTree tree) throws RecognitionException {
		CommonTreeNodeStream treeNodeStream = new CommonTreeNodeStream( tree );
		treeNodeStream.setTokenStream( tokens );

		return (CommonTree) new QueryRenderer( treeNodeStream, delegate ).statement().getTree();
	}
}
