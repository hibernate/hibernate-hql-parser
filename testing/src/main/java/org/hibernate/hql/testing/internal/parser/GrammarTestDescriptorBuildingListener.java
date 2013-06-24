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
package org.hibernate.hql.testing.internal.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.hibernate.hql.testing.internal.GrammarTestBaseListener;
import org.hibernate.hql.testing.internal.GrammarTestParser.ExpectedAstContext;
import org.hibernate.hql.testing.internal.GrammarTestParser.GrammarTestContext;
import org.hibernate.hql.testing.internal.GrammarTestParser.PakkageContext;
import org.hibernate.hql.testing.internal.GrammarTestParser.TestContext;
import org.hibernate.hql.testing.internal.GrammarTestParser.TestGroupContext;
import org.hibernate.hql.testing.internal.GrammarTestParser.TestResultContext;
import org.hibernate.hql.testing.internal.GrammarTestParser.TestSubGroupContext;
import org.hibernate.hql.testing.internal.model.GrammarRuleTestGroupDescriptor;
import org.hibernate.hql.testing.internal.model.GrammarTestDescriptor;
import org.hibernate.hql.testing.internal.model.ParsingResult;

/**
 * A {@link ParseTreeListener} for grammar test trees which builds a
 * {@link GrammarTestDescriptor} representing the parsed document.
 *
 * @author Gunnar Morling
 */
public class GrammarTestDescriptorBuildingListener extends GrammarTestBaseListener {

	private final GrammarTestDescriptor.Builder testBuilder = new GrammarTestDescriptor.Builder();
	private GrammarRuleTestGroupDescriptor.Builder testGroupBuilder;

	@Override
	public void exitGrammarTest(GrammarTestContext ctx) {
		testBuilder.setName( ctx.ID().getText() );
	}

	@Override
	public void exitPakkage(PakkageContext ctx) {
		testBuilder.setPackageName( ctx.PACKAGE_ID().getText() );
	}

	@Override
	public void enterTestGroup(TestGroupContext ctx) {
		testGroupBuilder = testBuilder.addTestGroup();
	}

	@Override
	public void exitTestGroup(TestGroupContext ctx) {
		testGroupBuilder.setName( ctx.ID().getText() );
	}

	@Override
	public void exitTestResult(TestResultContext ctx) {
		String statement = ((TestContext) ctx.getParent()).statement().getText();
		testGroupBuilder.addTest(
				ctx.getParent().getStart().getLine(),
				withoutLiteralDelimiters( statement ),
				ParsingResult.Status.valueOf( ctx.getText() )
		);
	}

	@Override
	public void exitExpectedAst(ExpectedAstContext ctx) {
		String statement = ((TestContext) ctx.getParent()).statement().getText();

		testGroupBuilder.addAstTest(
				ctx.getStart().getLine(),
				withoutLiteralDelimiters( statement ),
				ctx.AST().getText()
		);
	}

	@Override
	public void enterTestSubGroup(TestSubGroupContext ctx) {
		testGroupBuilder.addSubGroup();
	}

	@Override
	public void exitTestSubGroup(TestSubGroupContext ctx) {
		testGroupBuilder.setSubGroupName( ctx.TEST_GROUP_NAME().getText() );
	}

	public GrammarTestDescriptor getGrammarTest() {
		return testBuilder.build();
	}

	private String withoutLiteralDelimiters(String statement) {
		//"..."
		if ( statement.startsWith( "\"" ) ) {
			return statement.substring( 1, statement.length() - 1 );
		}
		//<<...>>
		else {
			return statement.substring( 2, statement.length() - 2 );
		}
	}

}
