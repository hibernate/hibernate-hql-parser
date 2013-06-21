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
package org.hibernate.query;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.hibernate.query.ast.origin.hql.parse.HQLLexer;
import org.hibernate.query.ast.origin.hql.parse.HQLParser;
import org.hibernate.query.ast.origin.hql.resolve.GeneratedHQLResolver;
import org.hibernate.query.ast.spi.QueryParserDelegate;
import org.hibernate.query.logging.Log;
import org.hibernate.query.logging.LoggerFactory;

/**
 * A parser for JPQL queries. Parsing comprises these steps:
 * <ul>
 * <li>lexing the query</li>
 * <li>parsing the query, building up an AST while doing so</li>
 * <li>parsing the resulting parse tree, applying logic defined by a {@link QueryParserDelegate}
 * </ul>
 * The given query parser delegate can be used to implement custom logic based on the parsed tree such
 * as building up an object model representing the parsed query.
 *
 * @author Gunnar Morling
 */
public class QueryParser {

	private static final Log log = LoggerFactory.make();

	/**
	 * Parses the given query string.
	 *
	 * @param queryString the query string to parse
	 * @param delegate the delegate to be invoked during parsing the query AST
	 * @return the result of this parsing as created by the given delegate
	 * @throws ParsingException in case any exception occurs during parsing
	 */
	public <T> T parseQuery(String queryString, QueryParserDelegate<T> delegate) throws ParsingException {
		HQLLexer lexer = new HQLLexer( new ANTLRStringStream( queryString ) );
		TokenStream tokens = new CommonTokenStream( lexer );
		HQLParser parser = new HQLParser( tokens );

		try {
			// parser#statement() is the entry point for evaluation of any kind of statement
			HQLParser.statement_return r = parser.statement();

			if ( parser.hasErrors() ) {
				throw log.getInvalidQuerySyntaxException( queryString, parser.getErrorMessages() );
			}

			CommonTree tree = (CommonTree) r.getTree();

			if ( log.isDebugEnabled() ) {
				log.debug( "Parse tree: " + tree.toStringTree() );
			}

			// To walk the resulting tree we need a treenode stream:
			CommonTreeNodeStream treeStream = new CommonTreeNodeStream( tree );
			// AST nodes have payloads referring to the tokens from the Lexer:
			treeStream.setTokenStream( tokens );

			GeneratedHQLResolver walker = new GeneratedHQLResolver( treeStream, delegate );
			walker.statement();

			return delegate.getResult();
		}
		catch (RecognitionException e) {
			throw log.getInvalidQuerySyntaxException( queryString, e );
		}
	}
}
