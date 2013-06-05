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

import static org.fest.assertions.Assertions.assertThat;

import org.hibernate.hql.testing.internal.model.GrammarRuleTestDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarRuleTestGroupDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarTestDescriptor;
import org.hibernate.hql.testing.internal.model.ParsingResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link GrammarTestParser}.
 *
 * @author Gunnar Morling
 */
public class GrammarTestParserTest {

	private GrammarTestParser parser;

	@Before
	public void setupParser() {
		parser = new GrammarTestParser();
	}

	@Test
	public void shouldRetrieveBaseTestData() {
		GrammarTestDescriptor grammarTest = parser.getGrammarTest(
				GrammarTestParserTest.class,
				"../../expr.testsuite"
		);

		assertThat( grammarTest.getName() ).isEqualTo( "Expr" );
		assertThat( grammarTest.getPackageName() ).isEqualTo( "com.example.calculator" );
		assertThat( grammarTest.getTestGroups() ).hasSize( 4 );
	}

	@Test
	public void shouldRetrieveLexerTests() throws Exception {
		GrammarTestDescriptor grammarTest = parser.getGrammarTest(
				GrammarTestParserTest.class,
				"../../expr.testsuite"
		);

		GrammarRuleTestGroupDescriptor idTests = grammarTest.getTestGroups().get( 0 );
		assertThat( idTests.getName() ).isEqualTo( "ID" );

		GrammarRuleTestDescriptor test = idTests.getTests().get( 0 );
		assertThat( test.getLineNumber() ).isEqualTo( 12 );
		assertThat( test.getExpression() ).isEqualTo( "a" );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.OK );

		test = idTests.getTests().get( 1 );
		assertThat( test.getLineNumber() ).isEqualTo( 13 );
		assertThat( test.getExpression() ).isEqualTo( "_" );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.FAIL );

		GrammarRuleTestGroupDescriptor intTests = grammarTest.getTestGroups().get( 1 );
		assertThat( intTests.getName() ).isEqualTo( "INT" );

		test = intTests.getTests().get( 0 );
		assertThat( test.getLineNumber() ).isEqualTo( 17 );
		assertThat( test.getExpression() ).isEqualTo( "1" );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.OK );

		test = intTests.getTests().get( 1 );
		assertThat( test.getLineNumber() ).isEqualTo( 18 );
		assertThat( test.getExpression() ).isEqualTo( "Pi" );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.FAIL );
	}

	@Test
	public void shouldRetrieveParserTests() throws Exception {
		GrammarTestDescriptor grammarTest = parser.getGrammarTest(
				GrammarTestParserTest.class,
				"../../expr.testsuite"
		);

		GrammarRuleTestGroupDescriptor progTests = grammarTest.getTestGroups().get( 2 );
		assertThat( progTests.getName() ).isEqualTo( "prog" );

		GrammarRuleTestDescriptor test = progTests.getTests().get( 0 );
		assertThat( test.getLineNumber() ).isEqualTo( 21 );
		assertThat( test.getExpression() ).isEqualTo( "a = 1 + 1\n" );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.OK );

		GrammarRuleTestGroupDescriptor exprTests = grammarTest.getTestGroups().get( 3 );
		assertThat( exprTests.getName() ).isEqualTo( "multExpr" );

		test = exprTests.getTests().get( 0 );
		assertThat( test.getLineNumber() ).isEqualTo( 25 );
		assertThat( test.getExpression() ).isEqualTo( "4 * 12" );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.OK );

		test = exprTests.getTests().get( 1 );
		assertThat( test.getLineNumber() ).isEqualTo( 26 );
		assertThat( test.getExpression() ).isEqualTo( "4 * " );
		assertThat( test.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.FAIL );
	}

	@Test
	public void shouldRetrieveAstTests() throws Exception {
		GrammarTestDescriptor grammarTest = parser.getGrammarTest(
				GrammarTestParserTest.class,
				"../../exprAst.testsuite"
		);

		GrammarRuleTestGroupDescriptor exprTests = grammarTest.getTestGroups().get( 0 );
		assertThat( exprTests.getName() ).isEqualTo( "expr" );

		GrammarRuleTestDescriptor exprTest = exprTests.getTests().get( 0 );
		assertThat( exprTest.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.OK );
		assertThat( exprTest.getExpectedAst() ).isEqualTo( "(+ (* 2 3) 4)" );

		GrammarRuleTestGroupDescriptor multExprTests = grammarTest.getTestGroups().get( 1 );
		assertThat( multExprTests.getName() ).isEqualTo( "multExpr" );

		GrammarRuleTestDescriptor multExprTest = multExprTests.getTests().get( 0 );
		assertThat( multExprTest.getExpectedParsingResultStatus() ).isEqualTo( ParsingResult.Status.OK );
		assertThat( multExprTest.getExpectedAst() ).isEqualTo( "(* 2 3)" );
	}

	@Test
	public void shouldRetrieveSubGroups() throws Exception {
		GrammarTestDescriptor grammarTest = parser.getGrammarTest(
				GrammarTestParserTest.class,
				"../../exprSubGroups.testsuite"
		);

		assertThat( grammarTest.getTestGroups() ).hasSize( 2 );

		GrammarRuleTestGroupDescriptor idTests = grammarTest.getTestGroups().get( 0 );
		assertThat( idTests.getTests() ).hasSize( 2 );
		assertThat( idTests.getSubGroups() ).hasSize( 2 );

		assertThat( idTests.getTests().get( 0 ).getExpression() ).isEqualTo( "a" );
		assertThat( idTests.getTests().get( 1 ).getExpression() ).isEqualTo( "b" );

		GrammarRuleTestGroupDescriptor lettersGroup = idTests.getSubGroups().get( 0 );
		assertThat( lettersGroup.getName() ).isEqualTo( "[letters]" );
		assertThat( lettersGroup.getTests() ).hasSize( 2 );
		assertThat( lettersGroup.getTests().get( 0 ).getExpression() ).isEqualTo( "c" );
		assertThat( lettersGroup.getTests().get( 1 ).getExpression() ).isEqualTo( "d" );

		GrammarRuleTestGroupDescriptor othersGroup = idTests.getSubGroups().get( 1 );
		assertThat( othersGroup.getName() ).isEqualTo( "[others]" );
		assertThat( othersGroup.getTests() ).hasSize( 1 );
		assertThat( othersGroup.getTests().get( 0 ).getExpression() ).isEqualTo( "_" );

		GrammarRuleTestGroupDescriptor progTests = grammarTest.getTestGroups().get( 1 );
		assertThat( progTests.getTests() ).isEmpty();
		assertThat( progTests.getSubGroups() ).hasSize( 1 );

		GrammarRuleTestGroupDescriptor multiLineGroup = progTests.getSubGroups().get( 0 );
		assertThat( multiLineGroup.getName() ).isEqualTo( "[multi-line]" );
		assertThat( multiLineGroup.getTests() ).hasSize( 2 );
		assertThat( multiLineGroup.getTests().get( 0 ).getExpression() ).isEqualTo( "a = 1 + 1\n" );
		assertThat( multiLineGroup.getTests().get( 1 ).getExpression() ).isEqualTo( "b = 2 * 2\n" );
	}
}
