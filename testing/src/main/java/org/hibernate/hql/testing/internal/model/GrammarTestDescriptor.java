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
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;

/**
 * Describes an Antlr grammar to be tested. Has a name and package as well as
 * one or more tests for the grammar's rules.
 *
 * @author Gunnar Morling
 */
public class GrammarTestDescriptor {

	private final String name;
	private final String packageName;
	private final List<GrammarRuleTestGroupDescriptor> testGroups;

	private GrammarTestDescriptor(String name, String packageName, List<GrammarRuleTestGroupDescriptor> testGroups) {
		this.name = name;
		this.packageName = packageName;
		this.testGroups = Collections.unmodifiableList( testGroups );
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public List<GrammarRuleTestGroupDescriptor> getTestGroups() {
		return testGroups;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Lexer> getLexerClass() {
		return (Class<? extends Lexer>) loadClass( packageName + "." + name + "Lexer" );
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Parser> getParserClass() {
		return (Class<? extends Parser>) loadClass( packageName + "." + name + "Parser" );
	}

	private Class<?> loadClass(String fqn) {
		try {
			return GrammarTestDescriptor.class.getClassLoader().loadClass( fqn );
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException( e );
		}
	}

	public static class Builder {

		private String name;
		private String packageName;
		private final List<GrammarRuleTestGroupDescriptor.Builder> testGroupBuilders = new ArrayList<GrammarRuleTestGroupDescriptor.Builder>();

		public Builder() {
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public GrammarRuleTestGroupDescriptor.Builder addTestGroup() {
			GrammarRuleTestGroupDescriptor.Builder testGroup = new GrammarRuleTestGroupDescriptor.Builder();
			testGroupBuilders.add( testGroup );
			return testGroup;
		}

		public GrammarTestDescriptor build() {
			List<GrammarRuleTestGroupDescriptor> testGroups = new ArrayList<GrammarRuleTestGroupDescriptor>(
					testGroupBuilders.size()
			);
			for ( GrammarRuleTestGroupDescriptor.Builder builder : testGroupBuilders ) {
				testGroups.add( builder.build() );
			}

			return new GrammarTestDescriptor( name, packageName, testGroups );
		}
	}
}
