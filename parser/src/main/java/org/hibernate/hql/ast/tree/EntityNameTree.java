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
package org.hibernate.hql.ast.tree;

import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.hibernate.hql.ast.common.HibernateTree;

public class EntityNameTree extends HibernateTree {

	private List entityNames = null;
	private String outputText = null;

	public EntityNameTree(EntityNameTree entityNameTree, String outputText) {
		super( entityNameTree.getToken() );
		this.outputText = outputText;
	}

	public EntityNameTree(int tokenType, Token token, String tokenText,
			List entityNames) {
		super( token );
		Token newToken = createToken( token );
		newToken.setType( tokenType );
		newToken.setText( tokenText );
		this.token = newToken;
		this.entityNames = entityNames;
	}

	private Token createToken(Token fromToken) {
		return new CommonToken(fromToken);
	}

	public int getEntityCount() {
		return entityNames.size();
	}

	public String getEntityName(int index) {
		return (String)entityNames.get( index );
	}

	@Override
	public String toString() {
		if (outputText == null) {
			outputText = entityNames.get( 0 ).toString();
		}
		return outputText;
	}
}
