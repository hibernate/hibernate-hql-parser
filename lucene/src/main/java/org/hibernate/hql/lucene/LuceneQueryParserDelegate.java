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
package org.hibernate.hql.lucene;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.tree.Tree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReference;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.ast.spi.QueryParserDelegate;
import org.hibernate.hql.lucene.internal.builder.LuceneQueryBuilder;
import org.hibernate.hql.lucene.internal.builder.PropertyHelper;
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
 */
public class LuceneQueryParserDelegate implements QueryParserDelegate<LuceneQueryParsingResult> {

	/**
	 * Persister space: keep track of aliases and entity names.
	 */
	private final Map<String, String> aliasToEntityType = new HashMap<String, String>();

	/**
	 * How to resolve entity names to class instances
	 */
	private final EntityNamesResolver entityNames;

	private Class<?> targetType = null;

	private String propertyName;

	private final Map<String, Object> namedParameters;

	private final LuceneQueryBuilder builder;

	public LuceneQueryParserDelegate(SearchFactoryIntegrator searchFactory, EntityNamesResolver entityNames) {
		this( searchFactory, entityNames, Collections.<String, Object>emptyMap() );
	}

	public LuceneQueryParserDelegate(SearchFactoryIntegrator searchFactory,
			EntityNamesResolver entityNames, Map<String,Object> namedParameters) {
		this.entityNames = entityNames;
		this.namedParameters = namedParameters;
		this.builder = new LuceneQueryBuilder(searchFactory.buildQueryBuilder(), new PropertyHelper( searchFactory ) );
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
		builder.setEntityType( targetedType );
	}

	@Override
	public boolean isUnqualifiedPropertyReference() {
		return true; // TODO - very likely always true for our supported use cases
	}

	@Override
	public PathedPropertyReferenceSource normalizeUnqualifiedPropertyReference(Tree property) {
		this.propertyName = property.getText();
		return null;// return value is ignored anyway
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
	public PathedPropertyReferenceSource normalizeQualifiedRoot(Tree identifier381) {
		return new PathedPropertyReference( identifier381.getText(), aliasToEntityType );
	}

	@Override
	public PathedPropertyReferenceSource normalizePropertyPathIntermediary(
			PathedPropertyReferenceSource source, Tree propertyName) {
		throw new UnsupportedOperationException( "Not yet implemented" );
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
	public Tree normalizePropertyPathTerminus(PathedPropertyReferenceSource source, Tree propertyNameNode) {
		// receives the property name on a specific entity reference _source_
		this.propertyName = propertyNameNode.toString();
		return null;
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
	}

	@Override
	public void popStrategy() {
	}

	@Override
	public void activateOR() {
		builder.pushOrPredicate();
	}

	@Override
	public void activateAND() {
		builder.pushAndPredicate();
	}

	@Override
	public void activateNOT() {
		builder.pushNotPredicate();
	}

	/**
	 * This implements the equality predicate; the comparison
	 * predicate could be a constant, a subfunction or
	 * some random type parameter.
	 * The tree node has all details but with current tree rendering
	 * it just passes it's text so we have to figure out the options again.
	 */
	@Override
	public void predicateEquals(final String comparativePredicate) {
		Object comparisonValue = fromNamedQuery( comparativePredicate );
		builder.addEqualsPredicate( propertyName, comparisonValue );
	}

	@Override
	public void predicateBetween(String lower, String upper) {
		Object lowerComparisonValue = fromNamedQuery( lower );
		Object upperComparisonValue = fromNamedQuery( upper );

		builder.addRangePredicate( propertyName, lowerComparisonValue, upperComparisonValue );
	}

	private Object fromNamedQuery(String comparativePredicate) {
		if ( comparativePredicate.startsWith( ":" ) ) {
			return namedParameters.get( comparativePredicate.substring( 1 ) );
		}
		else {
			return comparativePredicate;
		}
	}

	@Override
	public void deactivateBoolean() {
		builder.popBooleanPredicate();
	}

	@Override
	public LuceneQueryParsingResult getResult() {
		return new LuceneQueryParsingResult( builder.build(), targetType );
	}
}
