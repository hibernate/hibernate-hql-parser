tree grammar TestHQLTreeWalker;

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
 * Copyright (c) 2012, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
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
 * reserved.  These portions are distributed under license by Red Hat Middleware
 * LLC and are covered by the above LGPL notice.  If you redistribute this material,
 * with or without modification, you must preserve this copyright notice in its
 * entirety.
 */
package org.hibernate.sql.ast.origin.hql.parse;
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
	:	^(PROPERTY_JOIN joinType FETCH? ALIAS_NAME PROP_FETCH? (collectionExpression|propertyReference) withClause?)
	|	^(PERSISTER_JOIN joinType persisterSpaceRoot onClause?)
	;

withClause
	:	^(WITH searchCondition)
	;

onClause
	:	^(ON searchCondition)
	;

joinType
	:	CROSS
	|	INNER
	|	(LEFT |	RIGHT | FULL) OUTER?
	;

selectClause
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
	|	^( IS_NOT_NULL rowValueConstructor )
	|	^( LIKE valueExpression valueExpression escapeSpecification? )
	|	^( NOT_LIKE valueExpression valueExpression escapeSpecification? )
	|	^( BETWEEN rowValueConstructor betweenList )
	|	^( NOT_BETWEEN rowValueConstructor betweenList )
	|	^( IN rowValueConstructor inPredicateValue )
	|	^( NOT_IN rowValueConstructor inPredicateValue )
	|	^( MEMBER_OF rowValueConstructor rowValueConstructor )
	|	^( NOT_MEMBER_OF rowValueConstructor rowValueConstructor  )
	|	^( IS_EMPTY rowValueConstructor )
	|	^( IS_NOT_EMPTY rowValueConstructor )
	|	rowValueConstructor
	;

betweenList
	:	^( BETWEEN_LIST rowValueConstructor rowValueConstructor )
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
	|	^(DOT_CLASS identPrimary) // crazy 
	|	^(GENERAL_FUNCTION_CALL identPrimary)
	|	^(JAVA_CONSTANT identPrimary) //It will generate at SQL a parameter element (?) -> 'cos we do not need to care about char escaping
	|	^(PATH identPrimary)
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
	:	standardFunction
	|	setFunction
	;

standardFunction
	:	castFunction
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

castFunction
	:	^(CAST valueExpression IDENTIFIER)
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

setFunction
	:	^(SUM numericValueExpression)
	|	^(AVG numericValueExpression)
	|	^(MAX numericValueExpression)
	|	^(MIN numericValueExpression)
	|	^(COUNT (ASTERISK | (DISTINCT|ALL) countFunctionArguments))
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
	;

numeric_literal
	:	INTEGER_LITERAL
	|	DECIMAL_LITERAL
	|	FLOATING_POINT_LITERAL
	;

entityName
	:	ENTITY_NAME ALIAS_NAME
	;

propertyReference
	:	^(PROPERTY_REFERENCE identPrimary)
	;

identPrimary
	: 	IDENTIFIER
	|	^(DOT identPrimary identPrimary )
	|	^(LEFT_SQUARE identPrimary valueExpression* )
	|	^(LEFT_PAREN identPrimary valueExpression* )
	;