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
package org.hibernate.hql.lucene.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.Tree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReference;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;
import org.hibernate.hql.ast.spi.QueryResolverDelegate;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;

/**
 * A resolver delegate which builds up {@code PropertyPath}s for any referenced properties but doesn't perform any kind
 * of validation. Intended to be used for queries targeted at dynamic, non Java class based entities.
 *
 * @author Gunnar Morling
 */
public class UntypedLuceneQueryResolverDelegate implements QueryResolverDelegate {

	private static final Log log = LoggerFactory.make();

	/**
	 * Persister space: keep track of aliases and entity names.
	 */
	private final Map<String, String> aliasToEntityType = new HashMap<String, String>();
	private final Map<String, PropertyPath> aliasToPropertyPath = new HashMap<String, PropertyPath>();

	private String alias;

	@Override
	public void registerPersisterSpace(Tree entityName, Tree alias) {
		String put = aliasToEntityType.put( alias.getText(), entityName.getText() );
		if ( put != null && !put.equalsIgnoreCase( entityName.getText() ) ) {
			throw new UnsupportedOperationException(
					"Alias reuse currently not supported: alias " + alias.getText()
					+ " already assigned to type " + put );
		}
	}

	@Override
	public boolean isUnqualifiedPropertyReference() {
		return true;
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedPropertyReference(Tree property) {
		if ( aliasToEntityType.containsKey( property.getText() ) ) {
			return new PathedPropertyReference( property.getText(), null, true );
		}
		else {
			return new PathedPropertyReference( property.getText(), null, false );
		}
	}

	@Override
	public boolean isPersisterReferenceAlias() {
		return true;
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedRoot(Tree identifier382) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public PathedPropertyReferenceSource normalizeQualifiedRoot(Tree root) {
		String entityNameForAlias = aliasToEntityType.get( root.getText() );

		if ( entityNameForAlias == null ) {
		PropertyPath propertyPath = aliasToPropertyPath.get( root.getText() );
			if ( propertyPath == null ) {
				throw log.getUnknownAliasException( root.getText() );
			}
			List<String> nodeNamesWithoutAlias = propertyPath.getNodeNamesWithoutAlias();
			StringBuilder builder = new StringBuilder();
			for ( String name : nodeNamesWithoutAlias ) {
				builder.append( "." );
				builder.append( name );
			}
			return new PathedPropertyReference( builder.substring( 1 ), null, false );
		}

		return new PathedPropertyReference( root.getText(), null, true );
	}

	@Override
	public PathedPropertyReferenceSource normalizePropertyPathIntermediary(PropertyPath path, Tree propertyName) {
		return new PathedPropertyReference( propertyName.getText(), null, false );
	}

	@Override
	public PathedPropertyReferenceSource normalizeIntermediateIndexOperation(PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty,
			Tree selector) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public void normalizeTerminalIndexOperation(PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty, Tree selector) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedPropertyReferenceSource(Tree identifier394) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public PathedPropertyReferenceSource normalizePropertyPathTerminus(PropertyPath path, Tree propertyNameNode) {
		return new PathedPropertyReference( propertyNameNode.getText(), null, false );
	}

	@Override
	public void pushFromStrategy(JoinType joinType, Tree assosiationFetchTree, Tree propertyFetchTree, Tree alias) {
		this.alias = alias.getText();
	}

	@Override
	public void pushSelectStrategy() {
		//nothing to do
	}

	@Override
	public void popStrategy() {
		alias = null;
	}

	@Override
	public void propertyPathCompleted(PropertyPath path) {
		//nothing to do
	}

	@Override
	public void registerJoinAlias(Tree alias, PropertyPath path) {
		if ( !path.getNodes().isEmpty() && !aliasToPropertyPath.containsKey( alias ) ) {
			aliasToPropertyPath.put( alias.getText(), path );
		}
	}
}
