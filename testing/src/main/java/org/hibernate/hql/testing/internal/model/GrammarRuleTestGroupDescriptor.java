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
package org.hibernate.hql.testing.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the group of tests for one rule of the grammar under test. Can
 * contain tests directly and/or sub-groups with tests.
 *
 * @author Gunnar Morling
 */
public class GrammarRuleTestGroupDescriptor {

	private final String name;
	private final RuleType ruleType;
	private final List<GrammarRuleTestDescriptor> tests;
	private final List<GrammarRuleTestGroupDescriptor> subGroups;

	private GrammarRuleTestGroupDescriptor(
			String name,
			RuleType ruleType,
			List<GrammarRuleTestDescriptor> tests,
			List<GrammarRuleTestGroupDescriptor> subGroups) {
		this.name = name;
		this.ruleType = ruleType;
		this.tests = tests;
		this.subGroups = subGroups;
	}

	public String getName() {
		return name;
	}

	public List<GrammarRuleTestDescriptor> getTests() {
		return tests;
	}

	public List<GrammarRuleTestGroupDescriptor> getSubGroups() {
		return subGroups;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public static class Builder {

		private String name;
		private final boolean isSubGroup;
		private RuleType ruleType;
		private final List<Builder> subGroupBuilders;
		private final List<GrammarRuleTestDescriptor> tests;
		private Builder currentSubGroup;

		public Builder() {
			this( false );
		}

		private Builder(boolean isSubGroup) {
			this.isSubGroup = isSubGroup;
			this.subGroupBuilders = new ArrayList<GrammarRuleTestGroupDescriptor.Builder>();
			this.tests = new ArrayList<GrammarRuleTestDescriptor>();
		}

		public void addTest(int lineNumber, String expression, ParsingResult.Status expectedTestStatus) {
			if ( currentSubGroup != null ) {
				currentSubGroup.addTest( lineNumber, expression, expectedTestStatus );
			}
			else {
				tests.add( new GrammarRuleTestDescriptor( lineNumber, expression, expectedTestStatus ) );
			}
		}

		public void addAstTest(int lineNumber, String expression, String expectedAst) {
			if ( currentSubGroup != null ) {
				currentSubGroup.addAstTest( lineNumber, expression, expectedAst );
			}
			else {
				tests.add( new GrammarRuleTestDescriptor( lineNumber, expression, expectedAst ) );
			}
		}

		public void setName(String name) {
			this.name = name;
			if ( !isSubGroup ) {
				ruleType = startsLowerCase( name ) ? RuleType.PARSER : RuleType.LEXER;
			}
		}

		public void setRuleType(RuleType ruleType) {
			this.ruleType = ruleType;
		}

		public void addSubGroup() {
			currentSubGroup = new Builder( true );
			subGroupBuilders.add( currentSubGroup );
		}

		public void setSubGroupName(String name) {
			currentSubGroup.setName( name );
		}

		public GrammarRuleTestGroupDescriptor build() {
			return new GrammarRuleTestGroupDescriptor( name, ruleType, tests, buildSubGroups() );
		}

		private List<GrammarRuleTestGroupDescriptor> buildSubGroups() {
			List<GrammarRuleTestGroupDescriptor> subGroups = new ArrayList<GrammarRuleTestGroupDescriptor>();

			for ( Builder builder : subGroupBuilders ) {
				builder.setRuleType( ruleType );
				subGroups.add( builder.build() );
			}

			return subGroups;
		}

		private boolean startsLowerCase(String string) {
			return string.startsWith( string.substring( 0, 1 ).toLowerCase() );
		}
	}
}
