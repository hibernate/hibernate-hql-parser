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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import org.hibernate.hql.testing.internal.model.GrammarRuleTestDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarRuleTestGroupDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarTestDescriptor;
import org.hibernate.hql.testing.internal.model.ParsingResult;
import org.hibernate.hql.testing.internal.model.RuleType;

/**
 * Runs a single {@link GrammarRuleTestDescriptor grammar rule test}.
 *
 * @author Gunnar Morling
 */
public class GrammarRuleTestRunner {

	private final GrammarTestDescriptor grammarTest;
	private final GrammarRuleTestGroupDescriptor grammarRuleTestGroup;
	private final GrammarRuleTestDescriptor grammarRuleTest;

	public GrammarRuleTestRunner(
			GrammarTestDescriptor grammarTest,
			GrammarRuleTestGroupDescriptor grammarRuleTestGroup,
			GrammarRuleTestDescriptor grammarRuleTest) {

		this.grammarTest = grammarTest;
		this.grammarRuleTestGroup = grammarRuleTestGroup;
		this.grammarRuleTest = grammarRuleTest;
	}

	/**
	 * Runs the given grammar rule test by creating lexer and parser for the
	 * grammar under test and invoking the appropriate methods on them.
	 *
	 * @return a descriptor representing the outcoming of this operation
	 *
	 * @throws Throwable in case of any errors
	 */
	public ParsingResult run() throws Throwable {
		Lexer lexer = getLexer();
		ParsingResult parsingResult = null;

		if ( grammarRuleTestGroup.getRuleType() == RuleType.LEXER ) {
			Method rule = lexer.getClass().getMethod( "m" + grammarRuleTestGroup.getName() );

			parsingResult = invokeRule( lexer, rule );
		}
		else {
			CommonTokenStream tokens = new CommonTokenStream( lexer );
			Parser parser = getParser( tokens );
			Method rule = parser.getClass().getMethod( grammarRuleTestGroup.getName() );

			parsingResult = invokeRule( parser, rule );

			//consider unconsumed tokens as FAIL
			String unconsumedTokens = getUnconsumedTokens( tokens );
			if ( unconsumedTokens != null ) {
				parsingResult = ParsingResult.fail( "Found unconsumed tokens: \"" + unconsumedTokens + "\"." );
			}
		}

		return parsingResult;
	}

	/**
	 * Invokes the given lexer or parser rule on the given target object.
	 *
	 * @param target the lexer or parser to invoke the given method on
	 * @param rule the rule to invoke
	 *
	 * @return the outcome of the lexer or parser rule. The status will be
	 *         {@link ParsingResult.Status#FAIL} in case anything is written to
	 *         {@link System#err} during parsing or in case a
	 *         {@link RecognitionException} is thrown, otherwise
	 *         {@link ParsingResult.Status#OK}.
	 *
	 * @throws Throwable In case of any unexpected error.
	 */
	private ParsingResult invokeRule(Object target, Method rule) throws Throwable {
		PrintStream originalErrorStream = System.err;

		try {
			// have to catch any output written to System.err in Antlr3 since
			// there is no way to register an error handler
			ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
			System.setErr( new PrintStream( errorStream ) );

			Object result = rule.invoke( target );
			String ast = getAst( result );

			return getParsingResult( errorStream, ast );
		}
		catch (InvocationTargetException ite) {
			try {
				if ( ite.getCause() != null ) {
					throw ite.getCause();
				}
				else {
					throw ite;
				}
			}
			catch (RecognitionException re) {
				return ParsingResult.fail( re.getMessage() );
			}
		}
		finally {
			System.setErr( originalErrorStream );
		}
	}

	private ParsingResult getParsingResult(OutputStream errorStream, String ast) {
		return errorStream.toString().isEmpty() ? ParsingResult.ok( ast ) : ParsingResult.fail(
				errorStream.toString(),
				ast
		);
	}

	private String getAst(Object result) {
		if ( result instanceof RuleReturnScope ) {
			return ((CommonTree) ((RuleReturnScope) result).getTree()).toStringTree();
		}

		return null;
	}

	private Lexer getLexer() throws Exception {
		return grammarTest
				.getLexerClass()
				.getConstructor( CharStream.class )
				.newInstance( new ANTLRStringStream( grammarRuleTest.getExpression() ) );
	}

	private Parser getParser(CommonTokenStream tokens) throws Exception {
		return grammarTest
				.getParserClass()
				.getConstructor( TokenStream.class )
				.newInstance( tokens );
	}

	private String getUnconsumedTokens(CommonTokenStream tokens) {
		if ( tokens.index() == tokens.size() - 1 ) {
			return null;
		}

		StringBuilder nonEofEndingTokens = new StringBuilder();

		for ( Token endToken : tokens.getTokens( tokens.index(), tokens.size() - 1 ) ) {
			// Ignore <EOF> tokens as they might be inserted by the parser
			if ( !"<EOF>".equals( endToken.getText() ) ) {
				nonEofEndingTokens.append( endToken.getText() );
			}
		}

		return nonEofEndingTokens.length() > 0 ? nonEofEndingTokens.toString() : null;
	}
}
