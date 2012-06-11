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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.gunit.AbstractTest;
import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.ITestCase;
import org.antlr.gunit.Interp;
import org.antlr.gunit.gUnitTestInput;
import org.antlr.gunit.gUnitTestSuite;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.RecognitionException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;


/**
 * Runs GUnit tests as standard JUnit tests: much easier
 * to setup debugging in the IDE and integrate with build tools.
 * 
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 */
public class GUnitTestRunner extends Runner {

	/**
	 * Eclipse's integration for JUnit can't deal with too many atomic tests in the same test.
	 * Use this variable to limit tests extracted from the gunit test.
	 */
	static final Integer FORCE_LIMIT = Integer.getInteger( "JUnitgUnitLimitNumberTests" );

	private final Class<?> testClass;
	private String gunitSource;
	private GrammarInfo testInfo;
	private Description parentTestDescription;
	private Map<ITestCase,Description> testDescriptions = new HashMap<ITestCase,Description>();

	/**
	 * @param testClass The class defining the tests
	 * @throws InitializationError
	 */
	public GUnitTestRunner(Class<?> testClass) throws InitializationError {
		this.testClass = testClass;
		GUnitTest gUnitTestAnnotation = testClass.getAnnotation( GUnitTest.class );
		if ( gUnitTestAnnotation == null ) {
			throw new IllegalStateException( "Tests using the GUnitTestRunner require annotation " + GUnitTest.class );
		}
		else {
			gunitSource = gUnitTestAnnotation.value();
		}
		testInfo = loadTestGrammarInfo();
	}

	@Override
	public Description getDescription() {
		if ( parentTestDescription == null ) {
			int totalTestsDefined = 0;
			String grammarName = testInfo.getGrammarName();
			parentTestDescription = Description.createSuiteDescription( testClass + ":" + grammarName );
			for ( gUnitTestSuite ruleTestSuite: testInfo.getRuleTestSuites() ) {
				int suiteSubSection = 0;
				Description ruleSuiteDecription = defineSection( parentTestDescription, ruleTestSuite, suiteSubSection );
				for ( Entry<gUnitTestInput,AbstractTest> inputEntry: extractTestLines( ruleTestSuite ) ) {
					gUnitTestInput input = inputEntry.getKey();
					AbstractTest test = inputEntry.getValue();
					String inputName = "Line " + input.line + ": \"" + input.input + "\"";
					Description testDescription = Description.createTestDescription( testClass, inputName );
					ruleSuiteDecription.addChild( testDescription );
					testDescriptions.put( test, testDescription );
					totalTestsDefined++;
					if ( FORCE_LIMIT != null ) {
						if ( FORCE_LIMIT.intValue() == totalTestsDefined ) {
							return parentTestDescription;
						}
					}
				}
			}
		}
		return parentTestDescription;
	}

	private Description defineSection(Description parentTestDescription, gUnitTestSuite ruleTestSuite, int suiteSubSection) {
		Description ruleSuiteDecription = Description.createSuiteDescription( ruleTestSuite.getRuleName() + ":" + suiteSubSection );
		parentTestDescription.addChild( ruleSuiteDecription );
		return ruleSuiteDecription;
	}

	private Set<Entry<gUnitTestInput,AbstractTest>> extractTestLines(gUnitTestSuite ruleTestSuite) {
		//To read that crazy code we need reflection:
		try {
			Field field = gUnitTestSuite.class.getDeclaredField( "testSuites" );
			field.setAccessible( true );
			Map<gUnitTestInput, AbstractTest> testSuites = (Map<gUnitTestInput, AbstractTest>) field.get( ruleTestSuite );
			return testSuites.entrySet();
		}
		catch ( Exception e ) {
			throw new Error( e );
		}
	}

	private GrammarInfo loadTestGrammarInfo() throws InitializationError {
		ClassLoader classLoader = testClass.getClassLoader();
		InputStream resourceAsStream = classLoader.getResourceAsStream( gunitSource );
		if ( resourceAsStream == null ) {
			throw new InitializationError( "Resource could not be opened: " + gunitSource );
		}
		try {
			ANTLRInputStream antlrStream = new ANTLRInputStream( resourceAsStream );
			return Interp.parse( antlrStream );
		}
		catch ( IOException e ) {
			throw new InitializationError( e );
		}
		catch ( RecognitionException e ) {
			throw new InitializationError( e );
		}
		finally {
			try {
				resourceAsStream.close();
			}
			catch ( IOException e ) {
				throw new InitializationError( "Resource could not be closed: " + gunitSource );
			}
		}
	}
	@Override
	public void run(RunNotifier notifier) {
		Description desc = getDescription();
		try {
			NotifiedTestExecuter gUnitTests = executeGUnitTests();
			recursiveTestRunner( desc, notifier, gUnitTests );
		}
		catch ( IOException e ) {
			notifier.fireTestStarted( desc );
			notifier.fireTestFailure( new Failure( desc, e ) );
		}
	}

	private NotifiedTestExecuter executeGUnitTests() throws IOException {
		ClassLoader classLoader = testClass.getClassLoader();
		URL resource = classLoader.getResource( gunitSource );
		NotifiedTestExecuter executor = new NotifiedTestExecuter(
				testInfo,
				testClass.getClassLoader(),
				new File( resource.getPath() ).getAbsolutePath(),
				testDescriptions
		);
		executor.execTest();
		return executor;//contains collected results 
	}

	private void recursiveTestRunner(Description desc, RunNotifier notifier, NotifiedTestExecuter gUnitTests) {
		if ( desc.getChildren().size() == 0 ) {
			notifier.fireTestStarted( desc );
			try {
				Failure testResult = gUnitTests.getTestResult( desc );
				if ( testResult != null ) {
					notifier.fireTestFailure( testResult );
				}
			}
			finally {
				notifier.fireTestFinished( desc );
			}
		}
		else {
			for ( Description test : desc.getChildren() ) {
				recursiveTestRunner( test, notifier, gUnitTests );
			}
		}
	}

}
