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
package org.hibernate.sql.ast.common;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a canonical join type.
 * <p/>
 * Note that currently HQL really only supports inner and left outer joins
 * (though cross joins can also be achieved).  This is because joins in HQL
 * are always defined in relation to a mapped association.  However, when we
 * start allowing users to specify ad-hoc joins this may need to change to
 * allow the full spectrum of join types.  Thus the others are provided here
 * currently just for completeness and for future expansion.
 *
 * @author Steve Ebersole
 */
public class JoinType implements Serializable {
	/**
	 * Represents an inner join.
	 */
	public static final JoinType INNER = new JoinType( "inner" );
	/**
	 * Represents a left outer join.
	 */
	public static final JoinType LEFT = new JoinType( "left outer" );
	/**
	 * Represents a right outer join.
	 */
	public static final JoinType RIGHT = new JoinType( "right outer" );
	/**
	 * Represents a cross join (aka a cartesian product).
	 */
	public static final JoinType CROSS = new JoinType( "cross" );
	/**
	 * Represents a full join.
	 */
	public static final JoinType FULL = new JoinType( "full" );

	private static final HashMap INSTANCES = new HashMap();
	static {
		INSTANCES.put( INNER.name, INNER );
		INSTANCES.put( LEFT.name, LEFT );
		INSTANCES.put( RIGHT.name, RIGHT );
		INSTANCES.put( CROSS.name, CROSS );
		INSTANCES.put( FULL.name, FULL );
	}

	private final String name;

	private JoinType(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	private Object readResolve() {
		return INSTANCES.get( name );
	}

//	public static JoinType resolve(Node node) {
//		switch ( node.getType() ) {
//			case Sql92TokenTypes.INNER :
//				return JoinType.INNER;
//			case Sql92TokenTypes.LEFT :
//				return JoinType.LEFT;
//			case Sql92TokenTypes.RIGHT :
//				return JoinType.RIGHT;
//			case Sql92TokenTypes.CROSS :
//				return JoinType.CROSS;
//			case Sql92TokenTypes.FULL :
//				return JoinType.FULL;
//			default :
//				throw new IllegalArgumentException(
//						"Cannot resolve join-type node [type=" +
//								ASTUtil.getTokenTypeName( Sql92TokenTypes.class, node.getType() ) +
//								", text=" + node.getText() +
//								"]"
//				);
//		}
//	}
}
