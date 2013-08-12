/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.Tree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReference;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.ast.spi.QueryResolverDelegate;
import org.hibernate.hql.lucene.internal.ast.HSearchEmbeddedEntityTypeDescriptor;
import org.hibernate.hql.lucene.internal.ast.HSearchIndexedEntityTypeDescriptor;
import org.hibernate.hql.lucene.internal.ast.HSearchPropertyTypeDescriptor;
import org.hibernate.hql.lucene.internal.ast.HSearchTypeDescriptor;
import org.hibernate.hql.lucene.internal.builder.LucenePropertyHelper;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.spi.SearchFactoryIntegrator;

/**
 * This extends the ANTLR generated AST walker to transform a parsed tree
 * into a Lucene Query and collect the target entity types of the query.
 * <br/>
 * <b>TODO:</b>
 *   <li>It is currently human-written but should evolve into another ANTLR
 * generated tree walker, not extending GeneratedHQLResolver but using its
 * output as a generic normalization AST transformer.</li>
 *   <li>We are assembling the Lucene Query directly, but this doesn't take
 *   into account parameter types which might need some transformation;
 *   the Hibernate Search provided {@code QueryBuilder} could do this.</li>
 *   <li>Implement more predicates</li>
 *   <li>Support multiple types being targeted by the Query</li>
 *   <li>Support positional parameters (currently only consumed named parameters)<li>
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 * @author Gunnar Morling
 *
 */
public class LuceneQueryResolverDelegate implements QueryResolverDelegate {

	private enum Status {
		DEFINING_SELECT, DEFINING_FROM;
	}

	private static final Log log = LoggerFactory.make();
	/**
	 * Persister space: keep track of aliases and entity names.
	 */
	private final Map<String, String> aliasToEntityType = new HashMap<String, String>();

	private Status status;

	/**
	 * How to resolve entity names to class instances
	 */
	private final EntityNamesResolver entityNames;

	private final LucenePropertyHelper propertyHelper;

	private Class<?> targetType = null;

	public LuceneQueryResolverDelegate(SearchFactoryIntegrator searchFactory,
			EntityNamesResolver entityNames, Map<String,Object> namedParameters) {
		this.entityNames = entityNames;
		this.propertyHelper = new LucenePropertyHelper( searchFactory );
	}

	/**
	 * See rule entityName
	 */
	@Override
	public void registerPersisterSpace(Tree entityName, Tree alias) {
		String put = aliasToEntityType.put( alias.getText(), entityName.getText() );
		if ( put != null && !put.equalsIgnoreCase( entityName.getText() ) ) {
			throw new UnsupportedOperationException(
					"Alias reuse currently not supported: alias " + alias.getText()
					+ " already assigned to type " + put );
		}
		Class<?> targetedType = entityNames.getClassFromName( entityName.getText() );
		if ( targetedType == null ) {
			throw new IllegalStateException( "Unknown entity name " + entityName.getText() );
		}
		if ( targetType != null ) {
			throw new IllegalStateException( "Can't target multiple types: " + targetType + " already selected before " + targetedType );
		}
		targetType = targetedType;
	}

	@Override
	public boolean isUnqualifiedPropertyReference() {
		return true; // TODO - very likely always true for our supported use cases
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedPropertyReference(Tree property) {
		if ( aliasToEntityType.containsKey( property.getText() ) ) {
			return normalizeQualifiedRoot( property );
		}

		return normalizeProperty(
				new HSearchIndexedEntityTypeDescriptor( targetType, propertyHelper ),
				Collections.<String>emptyList(),
				property.getText()
		);
	}

	@Override
	public boolean isPersisterReferenceAlias() {
		if ( aliasToEntityType.size() == 1 ) {
			return true; // should be safe
		}
		else {
			throw new UnsupportedOperationException( "Unexpected use case: not implemented yet?" );
		}
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedRoot(Tree identifier382) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public PathedPropertyReferenceSource normalizeQualifiedRoot(Tree root) {
		String entityNameForAlias = aliasToEntityType.get( root.getText() );

		if ( entityNameForAlias == null ) {
			throw log.getUnknownAliasException( root.getText() );
		}

		Class<?> entityType = entityNames.getClassFromName( entityNameForAlias );

		return new PathedPropertyReference(
				root.getText(),
				new HSearchIndexedEntityTypeDescriptor( entityType, propertyHelper ),
				true
		);
	}

	@Override
	public PathedPropertyReferenceSource normalizePropertyPathIntermediary(
			PropertyPath path, Tree propertyName) {

		HSearchTypeDescriptor sourceType = (HSearchTypeDescriptor) path.getLastNode().getType();

		if ( !sourceType.hasProperty( propertyName.getText() ) ) {
			throw log.getNoSuchPropertyException( sourceType.toString(), propertyName.getText() );
		}

		List<String> newPath = new LinkedList<String>( path.getNodeNamesWithoutAlias() );
		newPath.add( propertyName.getText() );

		PathedPropertyReference property = new PathedPropertyReference(
				propertyName.getText(),
				new HSearchEmbeddedEntityTypeDescriptor(
						sourceType.getIndexedEntityType(),
						newPath,
						propertyHelper
				),
				false
		);

		return property;
	}

	@Override
	public PathedPropertyReferenceSource normalizeIntermediateIndexOperation(
			PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty, Tree selector) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public void normalizeTerminalIndexOperation(
			PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty, Tree selector) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedPropertyReferenceSource(Tree identifier394) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public PathedPropertyReferenceSource normalizePropertyPathTerminus(PropertyPath path, Tree propertyNameNode) {
		// receives the property name on a specific entity reference _source_
		return normalizeProperty( (HSearchTypeDescriptor) path.getLastNode().getType(), path.getNodeNamesWithoutAlias(), propertyNameNode.getText() );
	}

	private PathedPropertyReferenceSource normalizeProperty(HSearchTypeDescriptor type, List<String> path, String propertyName) {

		if ( !type.hasProperty( propertyName ) ) {
			throw log.getNoSuchPropertyException( type.toString(), propertyName );
		}

		if ( status != Status.DEFINING_SELECT && !type.isEmbedded( propertyName ) && type.isAnalyzed( propertyName ) ) {
			throw log.getQueryOnAnalyzedPropertyNotSupportedException( type.getIndexedEntityType().getCanonicalName(), propertyName );
		}

		if ( type.isEmbedded( propertyName ) ) {
			List<String> newPath = new LinkedList<String>( path );
			newPath.add( propertyName );
			return new PathedPropertyReference(
					propertyName,
					new HSearchEmbeddedEntityTypeDescriptor( type.getIndexedEntityType(), newPath, propertyHelper ),
					false)
			;
		}
		else {
			return new PathedPropertyReference(
					propertyName,
					new HSearchPropertyTypeDescriptor(),
					false
			);
		}
	}

	@Override
	public void pushFromStrategy(
			JoinType joinType,
			Tree assosiationFetchTree,
			Tree propertyFetchTree,
			Tree alias) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public void pushSelectStrategy() {
		status = Status.DEFINING_SELECT;
	}

	@Override
	public void popStrategy() {
		status = null;
	}

	@Override
	public void propertyPathCompleted(PropertyPath path) {
		if ( status == Status.DEFINING_SELECT && path.getLastNode().getType() instanceof HSearchEmbeddedEntityTypeDescriptor ) {
			HSearchEmbeddedEntityTypeDescriptor type = (HSearchEmbeddedEntityTypeDescriptor) path.getLastNode().getType();

			throw log.getProjectionOfCompleteEmbeddedEntitiesNotSupportedException(
					type.getIndexedEntityType().getCanonicalName(),
					path.asStringPathWithoutAlias()
			);
		}
	}
}
