/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009, Red Hat Inc. or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.sql.ast.common;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.Token;

/**
 * todo : javadocs
 * 
 * @author Steve Ebersole
 * @author Alexandre Porcelli
 */
public class HibernateTree extends CommonTree {
	/**
	 * start char offset
	 */
	int startCharOffset = -1;

	/**
	 * end char offset
	 */
	int endCharOffset = -1;

	public HibernateTree() {
	}

	public HibernateTree(HibernateTree node) {
		super(node);
		this.token = node.token;
	}

	public HibernateTree(CommonTree node) {
		super(node);
		this.token = node.token;
	}

	public HibernateTree(Token token) {
		super(token);
	}

	public HibernateTree(int type, String text) {
		this(new HibernateToken(type, text));
	}

	public HibernateTree(int type) {
		this(new HibernateToken(type));
	}

	public Tree dupNode() {
		return new HibernateTree(this);
	}

	/**
	 * getter for start char offset
	 * 
	 * @return start char offset
	 */
	public int getStartCharOffset() {
		return startCharOffset;
	}

	/**
	 * setter for start char offset
	 * 
	 * @param startCharOffset
	 *            start char offset
	 */
	public void setStartCharOffset(int startCharOffset) {
		this.startCharOffset = startCharOffset;
	}

	/**
	 * getter of end char offset
	 * 
	 * @return end char offset
	 */
	public int getEndCharOffset() {
		return endCharOffset;
	}

	/**
	 * setter of end char offset
	 * 
	 * @param endCharOffset
	 *            end char offset
	 */
	public void setEndCharOffset(int endCharOffset) {
		this.endCharOffset = endCharOffset;
	}
}
