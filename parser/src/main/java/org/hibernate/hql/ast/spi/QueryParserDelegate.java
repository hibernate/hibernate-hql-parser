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

import org.antlr.runtime.tree.Tree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;

/**
 * Defines hooks for implementing custom logic when walking the parse tree of a JPQL query.
 *
 * @author Gunnar Morling
 */
public interface QueryParserDelegate<T> {

	void registerPersisterSpace(Tree entityName, Tree alias);

	boolean isUnqualifiedPropertyReference();

	PathedPropertyReferenceSource normalizeUnqualifiedPropertyReference(Tree property);

	boolean isPersisterReferenceAlias();

	PathedPropertyReferenceSource normalizeUnqualifiedRoot(Tree identifier382);

	PathedPropertyReferenceSource normalizeQualifiedRoot(Tree identifier381);

	PathedPropertyReferenceSource normalizePropertyPathIntermediary(PathedPropertyReferenceSource source, Tree propertyName);

	PathedPropertyReferenceSource normalizeIntermediateIndexOperation(PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty,
			Tree selector);

	void normalizeTerminalIndexOperation(PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty, Tree selector);

	PathedPropertyReferenceSource normalizeUnqualifiedPropertyReferenceSource(Tree identifier394);

	Tree normalizePropertyPathTerminus(PathedPropertyReferenceSource source, Tree propertyNameNode);

	void pushFromStrategy(JoinType joinType, Tree assosiationFetchTree, Tree propertyFetchTree, Tree alias);

	void pushSelectStrategy();

	void popStrategy();

	void activateOR();

	void activateAND();

	void activateNOT();

	void deactivateBoolean();

	void predicateEquals(String comparativePredicate);

	void predicateBetween(String lower, String upper);

	/**
	 * Returns the result created by this delegate after the tree processing has been finished.
	 *
	 * @return the result created by this delegate after the tree processing has been finished
	 */
	T getResult();
}
