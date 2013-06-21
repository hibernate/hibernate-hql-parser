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
package org.hibernate.hql.testing.junit;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.hql.testing.ForGrammar;
import org.hibernate.hql.testing.internal.junit.GrammarRuleStatements;
import org.hibernate.hql.testing.internal.model.GrammarRuleTestGroupDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarTestDescriptor;
import org.hibernate.hql.testing.internal.parser.GrammarTestParser;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit test runner for executing Antlr grammar tests. The grammar test file
 * must be specified using the {@link ForGrammar} annotation. Note that this
 * runner supports {@code TestRule}s in the test classes but not the
 * {@code @Before} and {@code @After} hooks.
 *
 * @author Gunnar Morling
 */
public class GrammarTestRunner extends ParentRunner<Runner> {

	private final List<Runner> runners;

	public GrammarTestRunner(Class<?> testClass) throws InitializationError {
		super( testClass );

		String grammarFileName = getGrammarFileName( testClass );
		GrammarTestDescriptor grammarTest = new GrammarTestParser().getGrammarTest( testClass, grammarFileName );
		runners = getRunners( testClass, grammarTest );
	}

	private String getGrammarFileName(Class<?> testClass) throws InitializationError {
		ForGrammar forGrammar = testClass.getAnnotation( ForGrammar.class );
		if ( forGrammar == null ) {
			throw new InitializationError( "A grammar test file must be specified via @" + ForGrammar.class.getSimpleName() + "." );
		}

		return forGrammar.value();
	}

	private List<Runner> getRunners(Class<?> testClass, GrammarTestDescriptor grammarTest) throws InitializationError {
		List<Runner> runners = new ArrayList<Runner>();

		for ( GrammarRuleTestGroupDescriptor group : grammarTest.getTestGroups() ) {
			runners.add( new GrammarRuleStatements( testClass, grammarTest, group ) );
		}

		return runners;
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(Runner child, RunNotifier notifier) {
		child.run( notifier );
	}
}
