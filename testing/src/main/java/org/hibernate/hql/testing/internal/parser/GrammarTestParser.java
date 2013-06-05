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
package org.hibernate.hql.testing.internal.parser;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Nullable;
import org.hibernate.hql.testing.internal.GrammarTestLexer;
import org.hibernate.hql.testing.internal.model.GrammarTestDescriptor;

/**
 * A parser for grammar test files.
 *
 * @author Gunnar Morling
 */
public class GrammarTestParser {

	/**
	 * Parses the given grammar test file and returns a descriptor representing
	 * the file's contents.
	 *
	 * @param clazz the class via which to load the given file
	 * @param grammarFileName the grammar test file
	 *
	 * @return a descriptor representing the given file's contents
	 */
	public GrammarTestDescriptor getGrammarTest(Class<?> clazz, String grammarFileName) {
		InputStream inputStream = null;

		try {
			inputStream = clazz.getResourceAsStream( grammarFileName );

			if ( inputStream == null ) {
				throw new IllegalArgumentException( "The grammar test file " + grammarFileName + " couldn't be found." );
			}
			GrammarTestLexer lexer = new GrammarTestLexer(
					new ANTLRInputStream(
							inputStream
					)
			);
			TokenStream tokens = new CommonTokenStream( lexer );
			org.hibernate.hql.testing.internal.GrammarTestParser parser = new org.hibernate.hql.testing.internal.GrammarTestParser(
					tokens
			);

			parser.setBuildParseTree( true );
			GrammarTestDescriptorBuildingListener buildingListener = new GrammarTestDescriptorBuildingListener();
			parser.addParseListener( buildingListener );

			SimpleErrorListener errorListener = new SimpleErrorListener();
			parser.addErrorListener( errorListener );

			parser.grammarTest();

			if ( errorListener.isSyntaxErrorOccured() ) {
				throw new IllegalArgumentException( "The grammar test file " + grammarFileName + " contains syntax errors. See System.err." );
			}

			return buildingListener.getGrammarTest();
		}
		catch (IOException e) {
			throw new RuntimeException( e );
		}
		finally {
			if ( inputStream != null ) {
				try {
					inputStream.close();
				}
				catch (IOException e) { /*ignore*/ }
			}
		}
	}

	private static class SimpleErrorListener extends BaseErrorListener {

		private boolean syntaxErrorOccured = false;

		@Override
		public void syntaxError(
				Recognizer<?, ?> recognizer,
				@Nullable Object offendingSymbol, int line, int charPositionInLine,
				String msg, @Nullable RecognitionException e) {

			syntaxErrorOccured = true;
		}

		public boolean isSyntaxErrorOccured() {
			return syntaxErrorOccured;
		}
	}
}
