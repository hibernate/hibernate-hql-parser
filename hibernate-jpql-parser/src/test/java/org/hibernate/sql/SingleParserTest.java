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
package org.hibernate.sql;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.hibernate.sql.ast.origin.hql.parse.HQLLexer;
import org.hibernate.sql.ast.origin.hql.parse.HQLParser;
import org.hibernate.sql.ast.origin.hql.parse.HQLParser.statement_return;

/**
 * Example of usage for the generated parser; useful to debug a specific syntax too.
 * Outputs both the lexer output and the Abstract Syntax Tree.
 * 
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 */
public class SingleParserTest {

	public static void main(String[] args) throws RecognitionException {
		String testCase = "select an.mother.id, max(an.bodyWeight) from Animal an group by an.mother.id having max(an.bodyWeight)>1.0";
		ANTLRStringStream antlrStream = new ANTLRStringStream( testCase );
		HQLLexer lexer = new HQLLexer( antlrStream );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		HQLParser parser = new HQLParser( tokens );
		statement_return statement = parser.statement();
		System.out.println( tokens.getTokens() );
		CommonTree tree = (CommonTree) statement.getTree();
		System.out.println( tree.toStringTree() );
	}

}
