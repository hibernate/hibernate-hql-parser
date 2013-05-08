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
package org.hibernate.jpql.testing.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the group of tests for one rule of the grammar under test.
 *
 * @author Gunnar Morling
 */
public class GrammarRuleTestGroupDescriptor {

	private final String name;
	private final List<GrammarRuleTestDescriptor> tests;

	public GrammarRuleTestGroupDescriptor(String name, List<GrammarRuleTestDescriptor> tests) {
		this.name = name;
		this.tests = tests;
	}

	public String getName() {
		return name;
	}

	public List<GrammarRuleTestDescriptor> getTests() {
		return tests;
	}

	public RuleType getRuleType() {
		return startsLowerCase( name ) ? RuleType.PARSER : RuleType.LEXER;
	}

	private boolean startsLowerCase(String string) {
		return string.startsWith( string.substring( 0, 1 ).toLowerCase() );
	}

	public static class Builder {

		private String name;
		private final List<GrammarRuleTestDescriptor> tests;

		public Builder() {
			this.tests = new ArrayList<GrammarRuleTestDescriptor>();
		}

		public void addTest(int lineNumber, String expression, ParsingResult.Status expectedTestStatus) {
			tests.add( new GrammarRuleTestDescriptor( lineNumber, expression, expectedTestStatus ) );
		}

		public void addAstTest(int lineNumber, String expression, String expectedAst) {
			tests.add( new GrammarRuleTestDescriptor( lineNumber, expression, expectedAst ) );
		}

		public void setName(String name) {
			this.name = name;
		}

		public GrammarRuleTestGroupDescriptor build() {
			return new GrammarRuleTestGroupDescriptor( name, tests );
		}
	}
}
