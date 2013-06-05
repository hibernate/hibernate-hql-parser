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
package org.hibernate.hql.testing;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

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
@ForGrammar("expr.testsuite")
public class GrammarTestRunnerTest {

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
				"line 12: a - OK",
				"line 13: _ - FAIL",
				"line 17: 1 - OK",
				"line 18: Pi - FAIL",
				"line 21: a = 1 + 1 - OK",
				"line 25: 4 * 12 - OK",
				"line 26: 4 *  - FAIL"
		);
	}
}
