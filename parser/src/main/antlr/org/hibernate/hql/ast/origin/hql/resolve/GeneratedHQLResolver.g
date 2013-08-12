tree grammar GeneratedHQLResolver;

options{
	output=AST;
	rewrite=true;
	tokenVocab=HQLLexer;
	ASTLabelType=CommonTree;
	TokenLabelType=CommonToken;
}

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008-2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 * Portions of SQL grammar parsing copyright (C) 2003 by Lubos Vnuk.  All rights
 * reserved.  These portions are distributed under license by Red Hat Inc. and
 * are covered by the above LGPL notice.  If you redistribute this material,
 * with or without modification, you must preserve this copyright notice in its
 * entirety.
 */
package org.hibernate.hql.ast.origin.hql.resolve;

import org.antlr.runtime.tree.CommonTree;
import org.hibernate.hql.ast.common.JoinType;
import org.hibernate.hql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;
import org.hibernate.hql.ast.spi.QueryResolverDelegate;
import org.hibernate.hql.ast.tree.PropertyPathTree;
}

@members {
  private QueryResolverDelegate delegate;

  public GeneratedHQLResolver(TreeNodeStream input, QueryResolverDelegate delegate) {
    this(input, new RecognizerSharedState());
    this.delegate = delegate;
  }
}

filterStatement[String collectionRole]
	:	^(QUERY ^(QUERY_SPEC FILTER
				selectClause? whereClause? ( groupByClause havingClause?)? orderByClause?))
	;

statement
	:	updateStatementSet
	|	deleteStatementSet
	|	insertStatementSet
	|	queryStatementSet
	;

updateStatementSet
	:	updateStatement+
	;

updateStatement
	:	^(UPDATE entityName ^(SET assignment+) whereClause?)
	;

assignment
	:	^(EQUALS propertyReference valueExpression)
	|	^(EQUALS VERSIONED_VALUE STRING_LITERAL)
	;

deleteStatementSet
	:	deleteStatement+
	;

deleteStatement
	:	^(DELETE entityName whereClause?)
	;

insertStatementSet
	:	insertStatement+
	;

insertStatement
	:	^(INSERT intoClause queryStatementSet)
	;

intoClause
	:	^(INTO entityName ^(INSERTABILITY_SPEC propertyReference+ ) )
	;

queryStatementSet
	:	queryStatement+
	;

queryStatement
	:	^(QUERY queryExpression orderByClause?)
	;

queryExpression
	:	^(UNION ALL? queryExpression queryExpression)
	|	^(INTERSECT ALL? queryExpression queryExpression)
	|	^(EXCEPT ALL? queryExpression queryExpression)
	|	querySpec
	;

querySpec
	:	^(QUERY_SPEC selectFrom whereClause? groupByClause? havingClause?)
	;

whereClause
	:	^(WHERE searchCondition)
	;

groupByClause
	:	^(GROUP_BY groupingValue+)
	;

groupingValue
	:	^(GROUPING_VALUE valueExpression COLLATE?)
	;

havingClause
	:	^(HAVING searchCondition)
	;

selectFrom
	:	^(SELECT_FROM fromClause selectClause)
	;

fromClause
	:	^(FROM persisterSpaces+)
	;

persisterSpaces
	:	^(PERSISTER_SPACE persisterSpace)
	;

persisterSpace
	:	persisterSpaceRoot joins*
	;

persisterSpaceRoot
	:	^(ENTITY_PERSISTER_REF entityName PROP_FETCH?)
	;

joins
	:	^(PROPERTY_JOIN jt=joinType ft=FETCH? an=ALIAS_NAME pf=PROP_FETCH?
		{	delegate.pushFromStrategy($jt.joinType, $ft, $pf, $an );	}
		(collectionExpression|propertyReference) withClause?)
		{	delegate.popStrategy();	}
	|	^(PERSISTER_JOIN joinType persisterSpaceRoot onClause?)
	;

withClause
	:	^(WITH searchCondition)
	;

onClause
	:	^(ON searchCondition)
	;

joinType returns [JoinType joinType]
	:	CROSS {	$joinType = JoinType.CROSS;	}
	|	INNER {	$joinType = JoinType.INNER;	}
	|	(LEFT {	$joinType = JoinType.LEFT;	} |	RIGHT {	$joinType = JoinType.RIGHT;	} | FULL {	$joinType = JoinType.FULL;	}) OUTER?
	;

selectClause
@init	{	if (state.backtracking == 0) delegate.pushSelectStrategy();	}
@after	{	delegate.popStrategy();	}
	:	^(SELECT DISTINCT? rootSelectExpression)
	;

rootSelectExpression
	:	^(SELECT_LIST rootSelectExpression+)
	|	^(SELECT_ITEM rootSelectExpression)
	|	^(DYNAMIC_INSTANTIATION rootSelectExpression+)
	|	^(DYNAMIC_INSTANTIATION_ARG rootSelectExpression)
	|	valueExpression ALIAS_NAME?
	;

orderByClause
	:	^(ORDER_BY sortSpecification+)
	;

sortSpecification
	:	^(SORT_SPEC valueExpression COLLATE? ORDER_SPEC)
	;

searchCondition
	:	^( OR searchCondition searchCondition )
	|	^( AND searchCondition searchCondition )
	|	^( NOT searchCondition )
	|	predicate
	;

predicate
	:	^( EQUALS rowValueConstructor comparativePredicateValue )
	|	^( NOT_EQUAL rowValueConstructor comparativePredicateValue )
	|	^( LESS rowValueConstructor comparativePredicateValue )
	|	^( LESS_EQUAL rowValueConstructor comparativePredicateValue )
	|	^( GREATER rowValueConstructor comparativePredicateValue )
	|	^( GREATER_EQUAL rowValueConstructor comparativePredicateValue )
	|	^( IS_NULL rowValueConstructor )
	|	^( IS_NOT_NULL rowValueConstructor ) -> ^( NOT ^( IS_NULL rowValueConstructor ) )
	|	^( LIKE valueExpression valueExpression escapeSpecification? )
	|	^( NOT_LIKE valueExpression valueExpression escapeSpecification? ) -> ^( NOT ^( LIKE valueExpression valueExpression escapeSpecification? ) )
	|	^( BETWEEN rowValueConstructor betweenList )
	|	^( NOT_BETWEEN rowValueConstructor betweenList ) -> ^( NOT ^( BETWEEN rowValueConstructor betweenList ) )
	|	^( IN rowValueConstructor inPredicateValue )
	|	^( NOT_IN rowValueConstructor inPredicateValue ) -> ^( NOT ^( IN rowValueConstructor inPredicateValue ) )
	|	^( MEMBER_OF rowValueConstructor rowValueConstructor )
	|	^( NOT_MEMBER_OF rowValueConstructor rowValueConstructor  )
	|	^( IS_EMPTY rowValueConstructor )
	|	^( IS_NOT_EMPTY rowValueConstructor )
	|	rowValueConstructor
	;

betweenList
	:	^( BETWEEN_LIST lower=rowValueConstructor upper=rowValueConstructor )
	;

comparativePredicateValue
	:	rowValueConstructor
	;

rowValueConstructor
	:	valueExpression
	;

escapeSpecification
	:	^(ESCAPE characterValueExpression)
	;

inPredicateValue
	:	^(IN_LIST valueExpression+)
	;

numericValueExpression
	:	valueExpression
	;

characterValueExpression
	:	valueExpression
	;

datetimeValueExpression
	:	valueExpression
	;

valueExpression
	:	^( DOUBLE_PIPE characterValueExpression+ )
	|	^( UNARY_MINUS numericValueExpression )
	|	^( UNARY_PLUS numericValueExpression )
	|	^( PLUS valueExpression valueExpression )
	|	^( MINUS valueExpression valueExpression )
	|	^( ASTERISK numericValueExpression numericValueExpression )
	|	^( SOLIDUS numericValueExpression numericValueExpression )
	|	^( EXISTS rowValueConstructor)
	|	^( SOME valueExpression )
	|	^( ALL valueExpression )
	|	^( ANY valueExpression )
	|	^( VECTOR_EXPR valueExpression+) // or a tuples or ^(AND or IN statement
	|	valueExpressionPrimary
	;

valueExpressionPrimary
	:	caseExpression
	|	function
	|	collectionFunction
	|	collectionExpression
	|	constant
	|	parameter
	|	propertyReference
	|	^(SUB_QUERY queryStatementSet)
	|	ALIAS_REF //ID COLUMN, full property column list
	|	^(DOT_CLASS path) // crazy
	|	^(JAVA_CONSTANT path) //It will generate at SQL a parameter element (?) -> 'cos we do not need to care about char escaping
	|	^(PATH ret=propertyReferencePath) -> ^(PATH<node=PropertyPathTree>[$PATH, $ret.retPath] propertyReferencePath)
	;

caseExpression
	:	^(NULLIF valueExpression valueExpression)
	|	^(COALESCE valueExpression valueExpression*)
	|	^(SIMPLE_CASE valueExpression simpleCaseWhenClause+ elseClause?)
	|	^(SEARCHED_CASE searchedWhenClause+ elseClause?)
	;

simpleCaseWhenClause
	:	^(WHEN valueExpression valueExpression)
	;

searchedWhenClause
	:	^(WHEN searchCondition valueExpression)
	;

elseClause
	:	^(ELSE valueExpression)
	;

function
	: setFunction
	| standardFunction
	;

setFunction
	:	^(SUM numericValueExpression)
	|	^(AVG numericValueExpression)
	|	^(MAX numericValueExpression)
	|	^(MIN numericValueExpression)
	|	^(COUNT (ASTERISK | (DISTINCT|ALL) countFunctionArguments))
	;

standardFunction
	: functionFunction
	| castFunction
	|	concatFunction
	|	substringFunction
	|	trimFunction
	|	upperFunction
	|	lowerFunction
	|	lengthFunction
	|	locateFunction
	|	absFunction
	|	sqrtFunction
	|	modFunction
	|	sizeFunction
	|	indexFunction
	|	currentDateFunction
	|	currentTimeFunction
	|	currentTimestampFunction
	|	extractFunction
	|	positionFunction
	|	charLengthFunction
	|	octetLengthFunction
	|	bitLengthFunction
	;

functionFunction
	: ^( FUNCTION valueExpression* )
	;

castFunction
	: ^(CAST valueExpression IDENTIFIER)
	// todo : -> ^( FUNCTION[$CAST.start,"cast"] valueExpression "as" IDENTIFIER )
	;

concatFunction
	:	^(CONCAT valueExpression+)
	;

substringFunction
	:	^(SUBSTRING characterValueExpression numericValueExpression numericValueExpression?)
	;

trimFunction
	:	^(TRIM trimOperands)
	;

trimOperands
	:	^((LEADING|TRAILING|BOTH) characterValueExpression characterValueExpression)
	;

upperFunction
	:	^(UPPER characterValueExpression)
	;

lowerFunction
	:	^(LOWER characterValueExpression)
	;

lengthFunction
	:	^(LENGTH characterValueExpression)
	;

locateFunction
	:	^(LOCATE characterValueExpression characterValueExpression numericValueExpression?)
	;

absFunction
	:	^(ABS numericValueExpression)
	;

sqrtFunction
	:	^(SQRT numericValueExpression)
	;

modFunction
	:	^(MOD numericValueExpression numericValueExpression)
	;

sizeFunction
	:	^(SIZE propertyReference)
	;

indexFunction
	:	^(INDEX ALIAS_REF)
	;

currentDateFunction
	:	CURRENT_DATE
	;

currentTimeFunction
	:	CURRENT_TIME
	;

currentTimestampFunction
	:	CURRENT_TIMESTAMP
	;

extractFunction
	:	^(EXTRACT extractField datetimeValueExpression)
	;

extractField
	:	datetimeField
	|	timeZoneField
	;

datetimeField
	:	YEAR
	|	MONTH
	|	DAY
	|	HOUR
	|	MINUTE
	|	SECOND
	;

timeZoneField
	:	TIMEZONE_HOUR
	|	TIMEZONE_MINUTE
	;

positionFunction
	:	^(POSITION characterValueExpression characterValueExpression)
	;

charLengthFunction
	:	^(CHARACTER_LENGTH characterValueExpression)
	;

octetLengthFunction
	:	^(OCTET_LENGTH characterValueExpression)
	;

bitLengthFunction
	:	^(BIT_LENGTH characterValueExpression)
	;


countFunctionArguments
	:	collectionExpression
	|	propertyReference
	|	numeric_literal
	;

collectionFunction
	:	^((MAXELEMENT|MAXINDEX|MINELEMENT|MININDEX) collectionPropertyReference)
		//it will generate a SELECT MAX (m.column) form Table xxx -> it is realted to Hibernate mappings to Table->Map
	;

collectionPropertyReference
	:	propertyReference
	;

collectionExpression
	:	^(ELEMENTS propertyReference) //it will generate a SELECT m.column form Table xxx -> it is realted to Hibernate mappings to Table->Map
	|	^(INDICES propertyReference)
	;

parameter
	:	NAMED_PARAM
	|	JPA_PARAM
	|	PARAM
	;

constant
	:	literal
	|	NULL
	|	TRUE
	|	FALSE
	;

literal
	:	numeric_literal
	|	HEX_LITERAL
	|	OCTAL_LITERAL
	|	CHARACTER_LITERAL
	|	STRING_LITERAL
	|	^(CONST_STRING_VALUE CHARACTER_LITERAL)
	|	^(CONST_STRING_VALUE STRING_LITERAL)
	;

numeric_literal
	:	INTEGER_LITERAL
	|	DECIMAL_LITERAL
	|	FLOATING_POINT_LITERAL
	;

entityName
	:	ENTITY_NAME ALIAS_NAME
	{	delegate.registerPersisterSpace($ENTITY_NAME, $ALIAS_NAME);	}
	;

propertyReference
	:	^(PROPERTY_REFERENCE propertyReferencePath)
	;

propertyReferencePath returns [PropertyPath retPath]
	scope {
		PropertyPath path;
	}
	@init {
		$propertyReferencePath::path = new PropertyPath();
	}
	@after { $retPath = $propertyReferencePath::path; delegate.propertyPathCompleted( $propertyReferencePath::path ); }
	: 	{delegate.isUnqualifiedPropertyReference()}? unqualifiedPropertyReference
	|	pathedPropertyReference
	|	terminalIndexOperation
	;

unqualifiedPropertyReference returns [PathedPropertyReferenceSource propertyReferenceSource]
	@after { $propertyReferencePath::path.appendNode( $propertyReferenceSource ); }
	:	IDENTIFIER
	{	$propertyReferenceSource = delegate.normalizeUnqualifiedPropertyReference( $IDENTIFIER ); }
	;

pathedPropertyReference
	:	^(DOT pathedPropertyReferenceSource IDENTIFIER)
	{
		$propertyReferencePath::path.appendNode( delegate.normalizePropertyPathTerminus( $propertyReferencePath::path, $IDENTIFIER ) );
	}
	;

pathedPropertyReferenceSource returns [PathedPropertyReferenceSource propertyReferenceSource]
	@after { $propertyReferencePath::path.appendNode( $propertyReferenceSource ); }
	:	{(delegate.isPersisterReferenceAlias())}?=> IDENTIFIER { $propertyReferenceSource = delegate.normalizeQualifiedRoot( $IDENTIFIER ); }
	|	{(delegate.isUnqualifiedPropertyReference())}?=> IDENTIFIER { $propertyReferenceSource = delegate.normalizeUnqualifiedRoot( $IDENTIFIER ); }
	|	intermediatePathedPropertyReference { $propertyReferenceSource = $intermediatePathedPropertyReference.propertyReferenceSource; }
	|	intermediateIndexOperation { $propertyReferenceSource = $intermediateIndexOperation.propertyReferenceSource; }
	;

intermediatePathedPropertyReference returns [PathedPropertyReferenceSource propertyReferenceSource]
	:	^(DOT source=pathedPropertyReferenceSource IDENTIFIER )
	{	$propertyReferenceSource = delegate.normalizePropertyPathIntermediary( $propertyReferencePath::path, $IDENTIFIER );	}
	;

intermediateIndexOperation returns [PathedPropertyReferenceSource propertyReferenceSource]
	:	^( LEFT_SQUARE indexOperationSource indexSelector )
	{	$propertyReferenceSource = delegate.normalizeIntermediateIndexOperation( $indexOperationSource.propertyReferenceSource, $indexOperationSource.collectionProperty, $indexSelector.tree );	}
	;

terminalIndexOperation
	:	^( LEFT_SQUARE indexOperationSource indexSelector )
	{	delegate.normalizeTerminalIndexOperation( $indexOperationSource.propertyReferenceSource, $indexOperationSource.collectionProperty, $indexSelector.tree );	}
	;

indexOperationSource returns [PathedPropertyReferenceSource propertyReferenceSource, Tree collectionProperty]
	:	^(DOT pathedPropertyReferenceSource IDENTIFIER )
	{	$propertyReferenceSource = $pathedPropertyReferenceSource.propertyReferenceSource;
		$collectionProperty = $IDENTIFIER;	}
		|	{(delegate.isUnqualifiedPropertyReference())}?=> IDENTIFIER
		{	$propertyReferenceSource = delegate.normalizeUnqualifiedPropertyReferenceSource( $IDENTIFIER );
			$collectionProperty = $IDENTIFIER;	}
	;

indexSelector
	:	valueExpression
	;

path
	: 	IDENTIFIER
	|	^(DOT path path )
	|	^(LEFT_SQUARE path valueExpression* )
	|	^(LEFT_PAREN path valueExpression* )
	;
