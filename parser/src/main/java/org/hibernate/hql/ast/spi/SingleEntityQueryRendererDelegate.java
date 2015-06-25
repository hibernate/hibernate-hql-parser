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
package org.hibernate.hql.ast.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.Tree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.AggregationPropertyPath;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;
import org.hibernate.hql.ast.spi.predicate.ComparisonPredicate.Type;
import org.hibernate.hql.internal.logging.Log;
import org.hibernate.hql.internal.logging.LoggerFactory;

/**
 * This extends the ANTLR generated AST walker to transform a parsed tree
 * into a query object and collect the target entity types of the query.
 * <br/>
 * <b>TODO:</b>
 *   <li>It is currently human-written but should evolve into another ANTLR
 * generated tree walker, not extending GeneratedHQLResolver but using its
 * output as a generic normalization AST transformer.</li>
 *   <li>We are assembling the query directly, but this doesn't take
 *   into account parameter types which might need some transformation;
 *   the Hibernate Search provided {@code QueryBuilder} could do this.</li>
 *   <li>Implement more predicates</li>
 *   <li>Support multiple types being targeted by the Query</li>
 *   <li>Support positional parameters (currently only consumed named parameters)<li>
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 * @author Gunnar Morling
 * @author Adrian Nistor
 */
public abstract class SingleEntityQueryRendererDelegate<Q, R> implements QueryRendererDelegate<R> {

	protected static final String SORT_ASC_SPEC = "asc";

	private static final Log log = LoggerFactory.make();

	/**
	 * States which this object can have during tree walking
	 *
	 * @author Gunnar Morling
	 */
	protected enum Status {
		DEFINING_SELECT, DEFINING_FROM, DEFINING_WHERE, DEFINING_GROUP_BY, DEFINING_HAVING, DEFINING_ORDER_BY
	}

	/**
	 * The current status
	 */
	protected Status status;

	protected String targetTypeName;

	protected Class<?> targetType;

	protected PropertyPath propertyPath;

	protected AggregationPropertyPath.Type aggregationType;

	protected final SingleEntityQueryBuilder<Q> builder;

	protected final List<String> projections = new ArrayList<String>();

	/**
	 * Persister space: keep track of aliases and entity names.
	 */
	private final Map<String, String> aliasToEntityType = new HashMap<String, String>();

	private final Map<String, Object> namedParameters;

	/**
	 * How to resolve entity names to class instances
	 */
	private final EntityNamesResolver entityNames;

	public SingleEntityQueryRendererDelegate(EntityNamesResolver entityNames, SingleEntityQueryBuilder<Q> builder, Map<String, Object> namedParameters) {
		this.entityNames = entityNames;
		this.namedParameters = namedParameters != null ? namedParameters : Collections.<String, Object>emptyMap();
		this.builder = builder;
	}

	/**
	 * Return the optional HAVING clause builder. To be overridden by subclasses that wish to support the HAVING clause.
	 */
	protected SingleEntityHavingQueryBuilder<?> getHavingBuilder() {
		return null;
	}

	/**
	 * See rule entityName
	 */
	@Override
	public void registerPersisterSpace(Tree entityName, Tree alias) {
		registerAlias( entityName.getText(), alias.getText() );
		setTargetTypeName( entityName.getText() );
		setTargetType( entityName.getText() );

		builder.setEntityType( targetTypeName );
		if ( getHavingBuilder() != null ) {
			getHavingBuilder().setEntityType( targetTypeName );
		}
	}

	private void registerAlias(String entityName, String alias) {
		String put = aliasToEntityType.put( alias, entityName );
		if ( put != null && !put.equalsIgnoreCase( entityName ) ) {
			throw new UnsupportedOperationException(
					"Alias reuse currently not supported: alias " + alias
							+ " already assigned to type " + put );
		}
	}

	private void setTargetType(String entityName) {
		Class<?> targetedType = entityNames.getClassFromName( entityName );
		if ( targetedType == null ) {
			throw new IllegalStateException( "Unknown entity name " + entityName );
		}

		targetType = targetedType;
	}

	private void setTargetTypeName(String entityName) {
		if ( targetTypeName != null ) {
			throw new IllegalStateException( "Can't target multiple types: " + targetTypeName + " already selected before " + entityName );
		}

		targetTypeName = entityName;
	}

	@Override
	public boolean isUnqualifiedPropertyReference() {
		return true; // TODO - very likely always true for our supported use cases
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
	public void pushWhereStrategy() {
		status = Status.DEFINING_WHERE;
	}

	@Override
	public void pushGroupByStrategy() {
		status = Status.DEFINING_GROUP_BY;
		if ( getHavingBuilder() == null ) {
			throw new UnsupportedOperationException( "The GROUP BY clause is not supported" );
		}
	}

	@Override
	public void pushHavingStrategy() {
		status = Status.DEFINING_HAVING;
		if ( getHavingBuilder() == null ) {
			throw new UnsupportedOperationException( "The HAVING clause is not supported" );
		}
	}

	@Override
	public void pushOrderByStrategy() {
		status = Status.DEFINING_ORDER_BY;
	}

	@Override
	public void popStrategy() {
		status = null;
		propertyPath = null;
		aggregationType = null;
	}

	@Override
	public void activateOR() {
		if ( status == Status.DEFINING_WHERE ) {
			builder.pushOrPredicate();
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().pushOrPredicate();
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void activateAND() {
		if ( status == Status.DEFINING_WHERE ) {
			builder.pushAndPredicate();
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().pushAndPredicate();
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void activateNOT() {
		if ( status == Status.DEFINING_WHERE ) {
			builder.pushNotPredicate();
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().pushNotPredicate();
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void predicateLess(String comparativePredicate) {
		addComparisonPredicate( comparativePredicate, Type.LESS );
	}

	@Override
	public void predicateLessOrEqual(String comparativePredicate) {
		addComparisonPredicate( comparativePredicate, Type.LESS_OR_EQUAL );
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
		addComparisonPredicate( comparativePredicate, Type.EQUALS );
	}

	@Override
	public void predicateNotEquals(String comparativePredicate) {
		activateNOT();
		addComparisonPredicate( comparativePredicate, Type.EQUALS );
		deactivateBoolean();
	}

	@Override
	public void predicateGreaterOrEqual(String comparativePredicate) {
		addComparisonPredicate( comparativePredicate, Type.GREATER_OR_EQUAL );
	}

	@Override
	public void predicateGreater(String comparativePredicate) {
		addComparisonPredicate( comparativePredicate, Type.GREATER );
	}

	private void addComparisonPredicate(String comparativePredicate, Type comparisonType) {
		Object comparisonValue = fromNamedQuery( comparativePredicate );
		List<String> property = propertyPath.getNodeNamesWithoutAlias();
		if ( status == Status.DEFINING_WHERE ) {
			builder.addComparisonPredicate( property, comparisonType, comparisonValue );
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().addComparisonPredicate( getAggregation(), property, comparisonType, comparisonValue );
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void predicateIn(List<String> list) {
		List<Object> values = fromNamedQuery( list );
		List<String> property = propertyPath.getNodeNamesWithoutAlias();
		if ( status == Status.DEFINING_WHERE ) {
			builder.addInPredicate( property, values );
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().addInPredicate( getAggregation(), property, values );
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void predicateBetween(String lower, String upper) {
		Object lowerComparisonValue = fromNamedQuery( lower );
		Object upperComparisonValue = fromNamedQuery( upper );

		List<String> property = propertyPath.getNodeNamesWithoutAlias();
		if ( status == Status.DEFINING_WHERE ) {
			builder.addRangePredicate( property, lowerComparisonValue, upperComparisonValue );
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().addRangePredicate( getAggregation(), property, lowerComparisonValue, upperComparisonValue );
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void predicateLike(String patternValue, Character escapeCharacter) {
		Object pattern = fromNamedQuery( patternValue );
		List<String> property = propertyPath.getNodeNamesWithoutAlias();
		if ( status == Status.DEFINING_WHERE ) {
			builder.addLikePredicate( property, (String) pattern, escapeCharacter );
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().addLikePredicate( getAggregation(), property, (String) pattern, escapeCharacter );
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void predicateIsNull() {
		List<String> property = propertyPath.getNodeNamesWithoutAlias();
		if ( status == Status.DEFINING_WHERE ) {
			builder.addIsNullPredicate( property );
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().addIsNullPredicate( getAggregation(), property );
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void setPropertyReferencePath(PropertyPath propertyPath) {
		if (aggregationType != null) {
			if ( propertyPath == null && aggregationType != AggregationPropertyPath.Type.COUNT
					&& aggregationType != AggregationPropertyPath.Type.COUNT_DISTINCT ) {
				throw log.getAggregationCanOnlyBeAppliedToPropertyReferencesException( aggregationType.name() );
			}
			propertyPath = new AggregationPropertyPath( aggregationType, propertyPath );
		}
		setPropertyPath( propertyPath );
	}

	@Override
	public void activateAggregation(AggregationPropertyPath.Type aggregationType) {
		if ( status == Status.DEFINING_WHERE ) {
			throw log.getNoAggregationsInWhereClauseException( aggregationType.name() );
		}
		if ( status == Status.DEFINING_GROUP_BY ) {
			throw log.getNoAggregationsInGroupByClauseException( aggregationType.name() );
		}
		this.aggregationType = aggregationType;
		propertyPath = null;
	}

	@Override
	public void deactivateAggregation() {
		aggregationType = null;
	}

	private AggregationPropertyPath.Type getAggregation() {
		if ( propertyPath instanceof AggregationPropertyPath ) {
			return ((AggregationPropertyPath) propertyPath).getType();
		}
		return null;
	}

	@Override
	public void sortSpecification(String collateName, String orderSpec) {
		// orderSpec is already normalized to be lowercase and non-null
		addSortField( propertyPath, collateName, SORT_ASC_SPEC.equals( orderSpec ) );
	}

	@Override
	public void groupingValue(String collateName) {
		addGrouping( propertyPath, collateName );
	}

	/**
    * Add field sort criteria. To be overridden by subclasses.
    *
    * @param propertyPath the path of the field being sorted
    * @param collateName optional collation name
    * @param isAscending sort direction
    */
	protected void addSortField(PropertyPath propertyPath, String collateName, boolean isAscending) {
		throw new UnsupportedOperationException( "Sorting is not supported : " + propertyPath.asStringPathWithoutAlias() );
	}

	/**
	 * Add 'group by' criteria. To be overridden by subclasses.
	 *
	 * @param propertyPath the path of the field used for grouping
	 * @param collateName optional collation name
	 */
	protected void addGrouping(PropertyPath propertyPath, String collateName) {
		throw new UnsupportedOperationException( "Grouping is not supported : " + propertyPath.asStringPathWithoutAlias() );
	}

	private Object fromNamedQuery(String comparativePredicate) {
		if ( comparativePredicate.startsWith( ":" ) ) {
			return namedParameters.get( comparativePredicate.substring( 1 ) );
		}
		else {
			return comparativePredicate;
		}
	}

	private List<Object> fromNamedQuery(List<String> list) {
		List<Object> elements = new ArrayList<Object>( list.size() );

		for ( String string : list ) {
			elements.add( fromNamedQuery( string ) );
		}

		return elements;
	}

	@Override
	public void deactivateBoolean() {
		if ( status == Status.DEFINING_WHERE ) {
			builder.popBooleanPredicate();
		}
		else if ( status == Status.DEFINING_HAVING ) {
			getHavingBuilder().popBooleanPredicate();
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public abstract R getResult();
}
