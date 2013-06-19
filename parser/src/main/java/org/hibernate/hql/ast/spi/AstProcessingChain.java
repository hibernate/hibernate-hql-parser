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
package org.hibernate.hql.ast.spi;

import java.util.Iterator;

/**
 * A chain of {@link AstProcessor}s which are applied sequentially on an AST. Implementations must ensure that the chain
 * is consistent, i.e. each processor understands the AST as created or modified by the previous processor.
 *
 * @author Gunnar Morling
 * @param <T> The result type of this chain as obtained from the last processor
 */
public interface AstProcessingChain<T> extends Iterable<AstProcessor> {

	/**
	 * Returns an iterator with the processors of this chain.
	 *
	 * @return an iterator with the processors of this chain
	 */
	@Override
	Iterator<AstProcessor> iterator();

	/**
	 * The processing result of this chain, as usually retrieved from the last processor.
	 *
	 * @return the processing result of this chain
	 */
	T getResult();
}
