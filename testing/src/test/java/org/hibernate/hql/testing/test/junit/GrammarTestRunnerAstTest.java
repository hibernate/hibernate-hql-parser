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
package org.hibernate.hql.testing.test.junit;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.hql.testing.ForGrammar;
import org.hibernate.hql.testing.junit.GrammarTestRunner;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

/**
 * Integration test for {@link GrammarTestRunner}.
 *
 * @author Gunnar Morling
 */
@RunWith(GrammarTestRunner.class)
@ForGrammar("../exprAst.testsuite")
public class GrammarTestRunnerAstTest {

	private static Set<String> testedMethods = new HashSet<String>();

	@Rule
	public TestRule watcher = new TestWatcher() {

		@Override
		protected void finished(Description description) {
			testedMethods.add( description.getMethodName() );
		}
	};

	@AfterClass
	public static void assertExpressionsUnderTest() {
		assertThat( testedMethods ).containsOnly(
				"line 8: 2 * 3 + 4 -> (+ (* 2 3) 4)",
				"line 11: 2 * 3 -> (* 2 3)"
		);
	}
}
