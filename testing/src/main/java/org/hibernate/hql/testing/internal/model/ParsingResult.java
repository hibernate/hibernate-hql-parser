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

/**
 * The result of invoking a parsing method on a lexer or parser. Has a status
 * and an description in case the status is {@link Status#FAIL}. May contain the
 * resulting AST if this result represents the outcome of a parser rule.
 *
 * @author Gunnar Morling
 */
public class ParsingResult {

	public enum Status {
		OK, FAIL;
	}

	private final Status status;
	private final String description;
	private final String ast;

	private ParsingResult(Status status, String description, String ast) {
		this.status = status;
		this.description = description;
		this.ast = ast;
	}

	public static ParsingResult ok() {
		return new ParsingResult( Status.OK, null, null );
	}

	public static ParsingResult ok(String ast) {
		return new ParsingResult( Status.OK, null, ast );
	}

	public static ParsingResult fail(String description) {
		return new ParsingResult( Status.FAIL, description, null );
	}

	public static ParsingResult fail(String description, String ast) {
		return new ParsingResult( Status.FAIL, description, ast );
	}

	public Status getStatus() {
		return status;
	}

	public String getDescription() {
		return description;
	}

	public String getAst() {
		return ast;
	}
}
