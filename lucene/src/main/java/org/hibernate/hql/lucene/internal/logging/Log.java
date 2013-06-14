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
package org.hibernate.hql.lucene.internal.logging;

import org.hibernate.hql.ParsingException;
import org.hibernate.hql.lucene.internal.builder.predicate.Predicate;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * Logging methods for the Lucene query parser component.
 *
 * @author Gunnar Morling
 */
@MessageLogger(projectCode = "HQLLUCN")
public interface Log extends BasicLogger {

	@Message(id = 1, value = "The type %s is not an indexed entity.")
	IllegalArgumentException getNoIndexedEntityException(String typeName);

	@Message(id = 2, value = "The type %s has no indexed property named %s.")
	ParsingException getNoSuchPropertyException(String typeName, String propertyName);

	@Message(id = 3, value = "The predicate %s is not of type %s.")
	IllegalArgumentException getUnsupportedPredicateTypeException(Object predicate, String targetTypeName);

	@Message(id = 4, value = "The predicate %s can not be added since there may be only one root predicate.")
	IllegalStateException getNotMoreThanOnePredicateInRootOfWhereClauseAllowedException(Predicate predicate);

	@Message(id = 5, value = "The predicate %s can not be added since there may be only one sub-predicate in a NOT predicate.")
	IllegalStateException getNotMoreThanOnePredicateInNegationAllowedException(Predicate predicate);

	@Message(id = 6, value = "No queries can be applied to property %2$s in type %1$s since the property is analyzed.")
	ParsingException getQueryOnAnalyzedPropertyNotSupportedException(String typeName, String propertyName);

	@Message(id = 7, value = "Unknown alias: %s.")
	ParsingException getUnknownAliasException(String unknownAlias);
}
