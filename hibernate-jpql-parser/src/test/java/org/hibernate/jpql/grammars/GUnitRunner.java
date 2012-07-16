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
package org.hibernate.jpql.grammars;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.antlr.gunit.GrammarInfo;
import org.antlr.gunit.Interp;
import org.antlr.gunit.gUnitExecutor;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.RecognitionException;


/**
 * Utility to debug GUnit tests: run the tests as GUnit would run them, but not as a unit test.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 */
public class GUnitRunner {

	private static String[] GUniTests = {
		"org/hibernate/sql/ast/origin/hql/parse/gUnitGeneratedAST.testsuite",
		"org/hibernate/sql/ast/origin/hql/parse/gUnitHQLGrammar.testsuite",
		"org/hibernate/sql/ast/origin/hql/parse/gUnitHQLTokens.testsuite",
		};

	public static void main(String[] args) throws IOException, RecognitionException {
		for (String resourceName : GUniTests) {
			executeGUnit( resourceName );
		}
	}

	private static void executeGUnit(String resourceName) throws IOException, RecognitionException {
		ClassLoader classLoader = GUnitRunner.class.getClassLoader();
		URL resource = classLoader.getResource( resourceName );
		InputStream resourceAsStream = classLoader.getResourceAsStream( resourceName );
		try {
			if ( resourceAsStream != null ) {
				ANTLRInputStream antlrStream = new ANTLRInputStream( resourceAsStream );
				GrammarInfo grammarInfo = Interp.parse( antlrStream );
				gUnitExecutor executor = new gUnitExecutor(
						grammarInfo,
						classLoader,
						new File( resource.getPath() ).getAbsolutePath()
				);
				String report = executor.execTest();
				System.out.println( report );
			}
		}
		finally {
			if ( resourceAsStream != null) {
				resourceAsStream.close();
			}
			else {
				System.out.println( "Resource not found: " + resourceName );
			}
		}
	}

}
