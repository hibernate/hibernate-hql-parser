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
package org.hibernate.hql.testing.internal.junit;

import java.util.Collection;
import java.util.List;

import org.hibernate.hql.testing.internal.model.GrammarRuleTestDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarRuleTestGroupDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarTestDescriptor;
import org.hibernate.hql.testing.internal.model.ParsingResult;
import org.hibernate.hql.testing.internal.model.ParsingResult.Status;
import org.hibernate.hql.testing.internal.parser.GrammarRuleTestRunner;

import org.junit.Rule;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import static org.junit.Assert.assertEquals;

/**
 * A JUnit {@link Statement} representing one test of a rule of the grammar
 * under test. Equivalent to one test method of a normal JUnit test class.
 *
 * @author Gunnar Morling
 */
class GrammarRuleStatement extends Statement implements GrammarRuleTest {

	private final TestClass testClass;
	private final GrammarRuleTestDescriptor grammarRuleTest;
	private final Description description;
	private final GrammarRuleTestRunner grammarRuleTestRunner;

	public GrammarRuleStatement(
			TestClass testClass,
			GrammarTestDescriptor grammarTest,
			GrammarRuleTestGroupDescriptor grammarRuleTestGroup,
			GrammarRuleTestDescriptor grammarRuleTest) {

		this.testClass = testClass;
		this.grammarRuleTest = grammarRuleTest;
		this.description = createDescription( testClass, grammarRuleTest );
		this.grammarRuleTestRunner = new GrammarRuleTestRunner( grammarTest, grammarRuleTestGroup, grammarRuleTest );
	}

	private Description createDescription(TestClass testClass, GrammarRuleTestDescriptor test) {
		String name = null;

		if ( test.getExpectedAst() != null ) {
			name = "line " + test.getLineNumber() + ": " +
					withoutLineBreaks( test.getExpression() ) + " -> " +
					test.getExpectedAst();
		}
		else {
			name = "line " + test.getLineNumber() + ": " +
					withoutLineBreaks( test.getExpression() ) + " - " +
					test.getExpectedParsingResultStatus();
		}

		return Description.createTestDescription( testClass.getJavaClass(), name );
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public void evaluate() throws Throwable {
		ParsingResult parsingResult = grammarRuleTestRunner.run();

		if ( grammarRuleTest.getExpectedParsingResultStatus() == Status.OK ) {
			assertEquals(
					"Unexpected parsing result (parser output: " + parsingResult.getDescription() + ")",
					Status.OK,
					parsingResult.getStatus()
			);
		}
		else {
			assertEquals(
					"Unexpected parsing result,",
					Status.FAIL,
					parsingResult.getStatus()
			);
		}

		if ( grammarRuleTest.getExpectedAst() != null ) {
			assertEquals( "Unexpected AST: ", grammarRuleTest.getExpectedAst(), parsingResult.getAst() );
		}
	}

	@Override
	public void run(GrammarRuleStatements parent, RunNotifier notifier) {
		parent.runLeafNode( getMethodBlock( testClass ), getDescription(), notifier );
	}

	/**
	 * Returns this statement, amended with any test rule statements if
	 * existent.
	 *
	 * @param testClass the class from which to retrieve the test rules
	 *
	 * @return this, amended with any test rule statements if existent
	 */
	public Statement getMethodBlock(final TestClass testClass) {
		Object testInstance;
		try {
			testInstance = new ReflectiveCallable() {
				@Override
				protected Object runReflectiveCall() throws Throwable {
					return testClass.getOnlyConstructor().newInstance();
				}
			}
			.run();
		}
		catch (Throwable e) {
			return new Fail( e );
		}

		List<TestRule> testRules = testClass.getAnnotatedFieldValues( testInstance, Rule.class, TestRule.class );

		return withTestRules( testRules );
	}

	private Statement withTestRules(Collection<TestRule> testRules) {
		return testRules.isEmpty() ? this :
				new RunRules(
						this,
						testRules,
						getDescription()
				);
	}

	private String withoutLineBreaks(String string) {
		String withoutLineBreaks = string.replaceAll( "\\s+", " " );

		//remove trailing whitespace if we added it ourselves
		if ( !string.endsWith( " " ) && withoutLineBreaks.endsWith( " " ) ) {
			withoutLineBreaks = withoutLineBreaks.substring( 0, withoutLineBreaks.length() - 1 );
		}

		return withoutLineBreaks;
	}
}
