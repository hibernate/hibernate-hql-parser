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

import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;

/**
 * Defines hooks for implementing custom logic when walking the parse tree of a JPQL query.
 *
 * @author Gunnar Morling
 */
public interface QueryRendererDelegate<T> {

	void registerPersisterSpace(Tree entityName, Tree alias);

	boolean isUnqualifiedPropertyReference();

	boolean isPersisterReferenceAlias();

	void pushFromStrategy(JoinType joinType, Tree assosiationFetchTree, Tree propertyFetchTree, Tree alias);

	void pushSelectStrategy();

	void popStrategy();

	void activateOR();

	void activateAND();

	void activateNOT();

	void deactivateBoolean();

	void predicateLess(String comparativePredicate);

	void predicateLessOrEqual(String comparativePredicate);

	void predicateEquals(String comparativePredicate);

	void predicateGreaterOrEqual(String comparativePredicate);

	void predicateGreater(String comparativePredicate);

	void predicateBetween(String lower, String upper);

	void predicateIn(List<String> list);

	void predicateLike(String patternValue, Character escapeCharacter);

	/**
	 * Returns the result created by this delegate after the tree processing has been finished.
	 *
	 * @return the result created by this delegate after the tree processing has been finished
	 */
	T getResult();

	/**
	 * Sets a property path representing one property in the SELECT or WHERE clause of a given query.
	 *
	 * @param propertyPath the property path to set
	 */
	void setPropertyPath(PropertyPath propertyPath);
}
