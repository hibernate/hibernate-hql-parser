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
package org.hibernate.sql.ast.origin.hql.resolve;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.impl.ConnectedQueryContextBuilder;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.sql.ast.origin.hql.resolve.path.PathedPropertyReference;
import org.hibernate.sql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;

/**
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 */
public class LuceneJPQLWalker extends GeneratedHQLResolver {

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
	private final HashMap<String, Class> entityNames;

	private QueryBuilder queryBuilder = null;
	private Class targetType = null;

	private BooleanQuery booleanQuery;
	private Stack<BooleanQuery> booleanQueryStack = new Stack<BooleanQuery>();
	private Stack<Occur> booleanQueryModeStack = new Stack<Occur>();

	private Occur booleanMode;

	private String propertyName;

	private Query rootQuery = new MatchAllDocsQuery();

	public LuceneJPQLWalker(TreeNodeStream input, SearchFactoryImplementor searchFactory, HashMap<String, Class> entityNames) {
		super( input );
		this.searchFactory = searchFactory;
		this.entityNames = entityNames;
		this.queryBuildContext = new ConnectedQueryContextBuilder( searchFactory );
	}

	/**
	 * See rule entityName
	 */
	protected void registerPersisterSpace(Tree entityName, Tree alias) {
		String put = aliasToEntityType.put( alias.getText(), entityName.getText() );
		if ( put != null && !put.equalsIgnoreCase( entityName.getText() ) ) {
			throw new UnsupportedOperationException(
					"Alias reuse currently not supported: alias " + alias.getText()
							+ " already assigned to type " + put );
		}
		Class targetedType = entityNames.get( entityName.getText() );
		if ( targetedType == null ) {
			throw new IllegalStateException( "Unknown entity name " + entityName.getText() );
		}
		if ( targetType != null ) {
			throw new IllegalStateException( "Can't target multiple types: " + targetType + " already selected before " + targetedType );
		}
		targetType = targetedType;
		queryBuilder = queryBuildContext.forEntity( targetedType ).get();
	}

	protected boolean isUnqualifiedPropertyReference() {
		return true; // TODO - very likely always true for our supported use cases
	}

	protected PathedPropertyReferenceSource normalizeUnqualifiedPropertyReference(Tree property) {
		// TODO
		return null;// return value is ignored anyway
	}

	protected boolean isPersisterReferenceAlias() {
		if ( aliasToEntityType.size() == 1 ) {
			return true; // should be safe
		}
		else {
			throw new UnsupportedOperationException( "Unexpected use case: not implemented yet?" );
		}
	}

	protected PathedPropertyReferenceSource normalizeUnqualifiedRoot(Tree identifier382) {
		throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected PathedPropertyReferenceSource normalizeQualifiedRoot(Tree identifier381) {
		return new PathedPropertyReference( identifier381.getText(), aliasToEntityType );
	}

	protected PathedPropertyReferenceSource normalizePropertyPathIntermediary(
			PathedPropertyReferenceSource source, Tree propertyName) {
		throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected PathedPropertyReferenceSource normalizeIntermediateIndexOperation(
			PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty, Tree selector) {
		throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected void normalizeTerminalIndexOperation(
			PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty, Tree selector) {
		throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected PathedPropertyReferenceSource normalizeUnqualifiedPropertyReferenceSource(Tree identifier394) {
		throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected Tree normalizePropertyPathTerminus(PathedPropertyReferenceSource source, Tree propertyNameNode) {
		// receives the property name on a specific entity reference _source_
		this.propertyName = propertyNameNode.toString();
		return null;
	}

	protected void pushFromStrategy(
			JoinType joinType,
			Tree assosiationFetchTree,
			Tree propertyFetchTree,
			Tree alias) {
		throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected void pushSelectStrategy() {
		definingSelectStrategy = true;
	}

	protected void popStrategy() {
		definingSelectStrategy = false;
	}

	public Class getTargetEntity() {
		return targetType;
	}

	protected void activateOR() {
		activateBoolean();
		booleanQuery = new BooleanQuery();
		booleanMode = Occur.SHOULD;
	}

	protected void activateAND() {
		activateBoolean();
		booleanQuery = new BooleanQuery();
		booleanMode = Occur.MUST;
	}

	protected void activateNOT() {
		activateBoolean();
		booleanQuery = new BooleanQuery();
		booleanMode = Occur.MUST_NOT;
	}

	protected void predicateEquals(String comparativePredicate) {
		//TODO apply appropriate bridge to comparativePredicate
		booleanQuery.add( new TermQuery( new Term(propertyName, comparativePredicate)), booleanMode );
	}

	private void activateBoolean() {
		booleanQueryStack.push( booleanQuery );
		booleanQueryModeStack.push( booleanMode );
	}

	protected void deactivateBoolean() {
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

	public String toString() {
		return rootQuery.toString();
	}

	public Query getLuceneQuery() {
		return rootQuery;
	}
}
