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
package org.hibernate.jpql.lucene;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.antlr.runtime.tree.Tree;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.hibernate.query.ast.common.JoinType;
import org.hibernate.query.ast.origin.hql.resolve.path.PathedPropertyReference;
import org.hibernate.query.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.query.ast.spi.EntityNamesResolver;
import org.hibernate.query.ast.spi.QueryParserDelegate;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.impl.ConnectedQueryContextBuilder;

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
 *   the Hibernate Search provided {@link QueryBuilder} could do this.</li>
 *   <li>Implement more predicates</li>
 *   <li>Support multiple types being targeted by the Query</li>
 *   <li>Support positional parameters (currently only consumed named parameters)<li>
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 */
public class LuceneQueryBuilder implements QueryParserDelegate<LuceneQueryParsingResult> {

	/**
	 * Persister space: keep track of aliases and entity names.
	 */
	private final Map<String, String> aliasToEntityType = new HashMap<String, String>();

	/**
	 * Set to true when we are walking in the tree area defining a SELECT type/options
	 */
	private boolean definingSelectStrategy;

	/**
	 * We refer to a SearchFactor to build queries
	 */
	private final SearchFactoryImplementor searchFactory;

	/**
	 * Map predicate and searchConditions to the root query context
	 */
	private final ConnectedQueryContextBuilder queryBuildContext;

	/**
	 * How to resolve entity names to class instances
	 */
	private final EntityNamesResolver entityNames;

	//	private QueryBuilder queryBuilder = null;
	private Class<?> targetType = null;

	private BooleanQuery booleanQuery;
	private final Stack<BooleanQuery> booleanQueryStack = new Stack<BooleanQuery>();
	private final Stack<Occur> booleanQueryModeStack = new Stack<Occur>();

	private Occur booleanMode;

	private String propertyName;

	private Query rootQuery = new MatchAllDocsQuery();

	private final Map<String, Object> namedParameters;

	public LuceneQueryBuilder(SearchFactoryImplementor searchFactory, EntityNamesResolver entityNames) {
		this( searchFactory, entityNames, Collections.<String, Object> emptyMap() );
	}

	public LuceneQueryBuilder(SearchFactoryImplementor searchFactory,
			EntityNamesResolver entityNames, Map<String,Object> namedParameters) {
		this.searchFactory = searchFactory;
		this.entityNames = entityNames;
		this.namedParameters = namedParameters;
		this.queryBuildContext = new ConnectedQueryContextBuilder( searchFactory );
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
		//		queryBuilder = queryBuildContext.forEntity( targetedType ).get();
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
		definingSelectStrategy = true;
	}

	@Override
	public void popStrategy() {
		definingSelectStrategy = false;
	}

	@Override
	public void activateOR() {
		activateBoolean();
		booleanQuery = new BooleanQuery();
		booleanMode = Occur.SHOULD;
	}

	@Override
	public void activateAND() {
		activateBoolean();
		booleanQuery = new BooleanQuery();
		booleanMode = Occur.MUST;
	}

	@Override
	public void activateNOT() {
		activateBoolean();
		booleanQuery = new BooleanQuery();
		booleanMode = Occur.MUST_NOT;
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
		String comparisonValue = valueToString( fromNamedQuery( comparativePredicate ) );

		TermQuery predicate = new TermQuery( new Term( propertyName, comparisonValue ) );
		setOrAppendQuery( predicate );
	}

	@Override
	public void predicateBetween(String lower, String upper) {
		String lowerComparisonValue = valueToString( fromNamedQuery( lower ) );
		String upperComparisonValue = valueToString( fromNamedQuery( upper ) );

		TermRangeQuery predicate = new TermRangeQuery( propertyName, lowerComparisonValue, upperComparisonValue, true, true );
		setOrAppendQuery( predicate );
	}

	private void setOrAppendQuery(Query predicate) {
		if ( booleanQuery != null ) {
			booleanQuery.add( predicate, booleanMode );
		}
		else {
			rootQuery = predicate;
		}
	}

	private String valueToString(Object comparison) {
		return comparison.toString();
	}

	private Object fromNamedQuery(String comparativePredicate) {
		if ( comparativePredicate.startsWith( ":" ) ) {
			return namedParameters.get( comparativePredicate.substring( 1 ) );
		}
		else {
			return comparativePredicate;
		}
	}

	private void activateBoolean() {
		booleanQueryStack.push( booleanQuery );
		booleanQueryModeStack.push( booleanMode );
	}

	@Override
	public void deactivateBoolean() {
		BooleanQuery currentBoolean = booleanQuery;
		booleanQuery = booleanQueryStack.pop();
		booleanMode = booleanQueryModeStack.pop();
		if ( booleanQuery == null ) {
			this.rootQuery = currentBoolean;
		}
		else {
			booleanQuery.add( currentBoolean, booleanMode );
		}
	}

	@Override
	public String toString() {
		return rootQuery.toString();
	}

	@Override
	public LuceneQueryParsingResult getResult() {
		return new LuceneQueryParsingResult( rootQuery, targetType );
	}
}
