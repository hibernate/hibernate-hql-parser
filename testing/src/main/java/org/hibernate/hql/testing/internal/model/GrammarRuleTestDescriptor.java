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

import org.hibernate.hql.testing.internal.model.ParsingResult.Status;

/**
 * Describes one test for a rule of a given grammar, comprising an expression
 * and the expected parsing result or the resulting AST, e.g. {@code "Pi" FAIL}.
 *
 * @author Gunnar Morling
 */
public class GrammarRuleTestDescriptor {

	private final int lineNumber;
	private final String expression;
	private final Status expectedParsingResultStatus;
	private final String expectedAst;

	public GrammarRuleTestDescriptor(
			int lineNumber,
			String expression,
			ParsingResult.Status expectedParsingResultStatus) {
		this.lineNumber = lineNumber;
		this.expression = expression;
		this.expectedParsingResultStatus = expectedParsingResultStatus;
		this.expectedAst = null;
	}

	public GrammarRuleTestDescriptor(int lineNumber, String expression, String expectedAst) {
		this.lineNumber = lineNumber;
		this.expression = expression;
		this.expectedParsingResultStatus = Status.OK;
		this.expectedAst = expectedAst;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getExpression() {
		return expression;
	}

	public Status getExpectedParsingResultStatus() {
		return expectedParsingResultStatus;
	}

	public String getExpectedAst() {
		return expectedAst;
	}

	@Override
	public String toString() {
		return "GrammarRuleTestDescriptor [lineNumber=" + lineNumber
				+ ", expression=" + expression
				+ ", expectedParsingResultStatus="
				+ expectedParsingResultStatus + ", expectedAst=" + expectedAst
				+ "]";
	}
}
