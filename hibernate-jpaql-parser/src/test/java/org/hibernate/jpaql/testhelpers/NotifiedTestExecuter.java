/* 
 * Hibernate, Relational Persistence for Idiomatic Java
 * 
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
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
package org.hibernate.jpaql.testhelpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.antlr.gunit.BooleanTest;
import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.ITestCase;
import org.antlr.gunit.OutputTest;
import org.antlr.gunit.ReturnTest;
import org.antlr.gunit.gUnitExecutor;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * Extends the gUnitExecutor to allow us to detect which tests failed,
 * and record failures to expose them to JUnit test listeners.
 * 
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 */
public class NotifiedTestExecuter extends gUnitExecutor {

	private final Map<ITestCase, Description> testDescriptions;
	private final Map<Description, Failure> testFailures = new HashMap<Description, Failure>();
	private final Set<Description> testSuccesses = new HashSet<Description>();

	public NotifiedTestExecuter(GrammarInfo grammarInfo, ClassLoader grammarClassLoader, String testsuiteDir,
			Map<ITestCase, Description> testDescriptions) {
		super( grammarInfo, grammarClassLoader, testsuiteDir );
		this.testDescriptions = testDescriptions;
	}

	public Failure getTestResult(Description testDescription) {
		if ( testSuccesses.contains( testDescription ) ) {
			if ( testFailures.containsKey( testDescription ) ) {
				throw new IllegalStateException( "Test descriptor found registered both as failing and succeeding" );
			}
			else {
				return null;//Test OK
			}
		}
		else {
			Failure failure = testFailures.get( testDescription );
			if ( failure == null ) {
				throw new IllegalStateException( "Test Description not found for either failed or ok" );
			}
			return failure;
		}
	}

	@Override
	public void onPass(ITestCase passTest) {
		Description testDescription = getTestDescription( passTest );
		boolean added = testSuccesses.add( testDescription );
		if (! added ) {
			throw new IllegalStateException( "Test passed but was already executed. Duplicate description?" );
		}
	}

	private Description getTestDescription(ITestCase test) {
		Description description = testDescriptions.get( test );
		if ( description == null && GUnitTestRunner.FORCE_LIMIT == null ) {
			throw new IllegalStateException( "Executing a test whose Description was not defined" );
		}
		return description;
	}

	@Override
	public void onFail(ITestCase failingTest) {
		Description description = getTestDescription( failingTest );
		Throwable error = null;
		//Not very happy about this dependency on specific types
		if ( failingTest instanceof BooleanTest ) {
			error = failed((BooleanTest) failingTest, description);
		}
		else if ( failingTest instanceof OutputTest ) {
			error = failed((OutputTest) failingTest);
		}
		else if ( failingTest instanceof ReturnTest ) {
			error = failed((ReturnTest) failingTest);
		}
		if ( error == null ) {
			throw new AssertionFailedError( "" );
		}
		Failure f = new Failure( description, error );
		Failure existing = testFailures.put( description, f );
		if ( existing != null ) {
			throw new IllegalStateException( "Test failed but was already executed. Duplicate description?" );
		}
	}

	private Throwable format(String header, String expectedResult, String actualResult) {
		//Header is broken in ANTLR 3.4 : only shows test number and line number.
		//Still, better than nothing. Fixed in 3.4.next
		return new AntlrAssertionFailure(
				 "\n\t" + header +
				 "\n\tExpected: '" + expectedResult +
				"'\n\tGot:      '" + actualResult + "'");}

	private Throwable failed(ReturnTest failingTest) {
		return format(
				failingTest.getHeader(),
				failingTest.getExpectedResult(),
				failingTest.getActualResult()
				);
	}

	private Throwable failed(OutputTest failingTest) {
		return format(
				failingTest.getHeader(),
				failingTest.getExpectedResult(),
				failingTest.getActualResult()
				);
	}

	private Throwable failed(BooleanTest failingTest, Description description) {
		return format(
				failingTest.getHeader(),
				failingTest.getExpectedResult(),
				failingTest.getActualResult()
				);
	}

	private static class AntlrAssertionFailure extends Exception {
		public AntlrAssertionFailure(String message) {
			super(message);
		}
		public synchronized Throwable fillInStackTrace() {
			//turn the stack silent, as it's unrelated noise in this case.
			return this;
		}
	}

}
