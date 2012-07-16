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

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.CharStream;

/**
 * Models the token-type/text portion of an Antlr tree for a specific node in said tree.
 *
 * @author Steve Ebersole
 * @author Alexandre Porcelli
 */
public class HibernateToken extends CommonToken {
	public HibernateToken(int type) {
		super( type );
	}

	public HibernateToken(CharStream input, int type, int channel, int start, int stop) {
		super( input, type, channel, start, stop );
	}

	public HibernateToken(int type, String text) {
		super( type, text );
	}

	/**
	 * Constructor that preserves the char offset
	 *
	 * @param oldToken A token to use to template the creation of this new one.
	 */
	public HibernateToken(Token oldToken) {
		super( oldToken );
		if ( null != oldToken && CommonToken.class.isInstance( oldToken ) ) {
			setStartIndex( ( ( CommonToken ) oldToken ).getStartIndex() );
			setStopIndex( ( ( CommonToken ) oldToken ).getStopIndex() );
		}
	}

	/**
	 * Constructor form used to track origination position information via the passed
	 * 'oldToken' param, but to utilize a new token type and text.
	 *
	 * @param oldToken The original token type (used for position tracking info).
	 * @param type The type of the new (this) token
	 * @param text The test of the new (this) token.
	 */
	public HibernateToken(Token oldToken, int type, String text) {
		this( oldToken );
		setType( type );
		setText( text );
	}
}
