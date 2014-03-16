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
package org.hibernate.hql.test.ast;

import java.util.Collections;
import java.util.Iterator;

import org.hibernate.hql.ParsingException;
import org.hibernate.hql.QueryParser;
import org.hibernate.hql.ast.spi.AstProcessingChain;
import org.hibernate.hql.ast.spi.AstProcessor;
import org.hibernate.hql.testutil.TestForIssue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link QueryParser}.
 *
 * @author Gunnar Morling
 */
public class QueryParserTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	@TestForIssue(jiraKey = "HQLPARSER-26")
	public void shouldRaiseExceptionDueToUnconsumedTokens() {
		expectedException.expect( ParsingException.class );
		expectedException.expectMessage( "HQLPARSER000006" );

		QueryParser queryParser = new QueryParser();
		queryParser.parseQuery( "FROM IndexedEntity u WHERE u.name = 'John' blah blah blah", new NoOpProcessingChain() );
	}

	private static class NoOpProcessingChain implements AstProcessingChain<Void> {

		@Override
		public Iterator<AstProcessor> iterator() {
			return Collections.<AstProcessor>emptyList().iterator();
		}

		@Override
		public Void getResult() {
			return null;
		}
	}
}
