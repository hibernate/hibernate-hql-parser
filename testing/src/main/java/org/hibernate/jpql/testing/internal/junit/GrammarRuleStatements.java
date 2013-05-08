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
package org.hibernate.jpql.testing.internal.junit;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.jpql.testing.internal.model.GrammarRuleTestDescriptor;
import org.hibernate.jpql.testing.internal.model.GrammarRuleTestGroupDescriptor;
import org.hibernate.jpql.testing.internal.model.GrammarTestDescriptor;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link ParentRunner} representing a group of tests for one given
 * grammar rule.
 *
 * @author Gunnar Morling
 */
public class GrammarRuleStatements extends ParentRunner<GrammarRuleStatement> {

	private final List<GrammarRuleStatement> statements;
	private final String name;

	public GrammarRuleStatements(
			Class<?> testClass,
			GrammarTestDescriptor grammarTest,
			GrammarRuleTestGroupDescriptor group) throws InitializationError {
		super( testClass );

		name = group.getName();

		statements = new ArrayList<GrammarRuleStatement>();
		for ( GrammarRuleTestDescriptor test : group.getTests() ) {
			statements.add( new GrammarRuleStatement( getTestClass(), grammarTest, group, test ) );
		}
	}

	@Override
	protected List<GrammarRuleStatement> getChildren() {
		return statements;
	}

	@Override
	protected Description describeChild(GrammarRuleStatement test) {
		return test.getDescription();
	}

	@Override
	protected void runChild(GrammarRuleStatement test, RunNotifier notifier) {
		runLeaf(
				test.getMethodBlock( getTestClass() ),
				test.getDescription(),
				notifier
		);
	}

	/**
	 * Returns the name of this group which is the name of the represented lexer
	 * or parser rule.
	 *
	 * @return the name of this group
	 */
	@Override
	protected String getName() {
		return name;
	}

	/**
	 * Overriding this to avoid execution of {@code @BeforeClass} and
	 * {@code @AfterClass} hooks which should happen only once for the root
	 * parent runner.
	 */
	@Override
	protected Statement classBlock(final RunNotifier notifier) {
		return childrenInvoker( notifier );
	}
}
