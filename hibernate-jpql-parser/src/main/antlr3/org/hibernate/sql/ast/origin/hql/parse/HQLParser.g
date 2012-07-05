parser grammar HQLParser;

options {
	tokenVocab=HQLLexer;
	output=AST;
}

@parser::header {
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
package org.hibernate.sql.ast.origin.hql.parse;

import java.util.LinkedList;
import java.util.Iterator;
import org.hibernate.sql.ast.common.ParserContext;
import org.hibernate.sql.ast.tree.EntityNameTree;
}

@parser::members {
	private Stack enableParameterUsage = new Stack();
	private ParserContext context = new org.hibernate.sql.DefaultParsingContext();
	private List errorMessages = new LinkedList();

	public void setParserContext(ParserContext context){
		this.context = context;
	}

	private boolean validateSoftKeyword(String text) {
		return validateLT( 1, text );
	}

	private boolean validateLT(int offset, String text) {
		String text2Validate = retrieveLT( offset );
		return text2Validate == null ? false : text2Validate.equalsIgnoreCase( text );
	}

	private String retrieveLT(int offset) {
		if ( null == input ) {
			return null;
		}
		Token token = input.LT( offset );
		return token == null ? null : token.getText();
	}

	public boolean hasErrors() {
		return errorMessages.size() > 0;
	}

	public List getErrorMessages() {
		return errorMessages;
	}

	public void reportError(RecognitionException e) {
		errorMessages.add(
			generateError(
				getRuleInvocationStack( e, this.getClass().getName() ),
					this.getTokenNames(),
					e
				)
		);
		super.reportError( e );
	}

	private String generateError( List invocationStack, String[] tokenNames, RecognitionException e) {
			String localization = invocationStack + ": line " + e.line + ":" + e.charPositionInLine + " ";
			return generateError( localization, tokenNames, e );
	}

	private String generateError( String localization, String[] tokenNames, RecognitionException e) {
		String message = "";
		if ( e instanceof MismatchedTokenException ) {
			MismatchedTokenException mte = (MismatchedTokenException) e;
			String tokenName = "<unknown>";
			if ( mte.expecting == Token.EOF ) {
				tokenName = "EOF";
			}
			else {
				if ( tokenNames != null ) {
					tokenName = tokenNames[mte.expecting];
				}
			}
			message = localization + "mismatched token: " + e.token + "; expecting type " + tokenName;
		}
		else if (e instanceof MismatchedTreeNodeException) {
			MismatchedTreeNodeException mtne = (MismatchedTreeNodeException) e;
			String tokenName = "<unknown>";
			if ( mtne.expecting == Token.EOF ) {
				tokenName = "EOF";
			}
			else {
				tokenName = tokenNames[mtne.expecting];
			}
			message = localization + "mismatched tree node: " + mtne.node + "; expecting type " + tokenName;
		}
		else if (e instanceof NoViableAltException) {
			NoViableAltException nvae = (NoViableAltException) e;
			message = localization + "state " + nvae.stateNumber + " (decision=" + nvae.decisionNumber + ") no viable alt; token=" + e.token;
		}
		else if (e instanceof EarlyExitException) {
			EarlyExitException eee = (EarlyExitException) e;
			message = localization + "required (...)+ loop (decision=" + eee.decisionNumber + ") did not match anything; token=" + e.token;
		}
		else if (e instanceof MismatchedSetException) {
			MismatchedSetException mse = (MismatchedSetException) e;
			message = localization + "mismatched token: " + e.token + "; expecting set " + mse.expecting;
		}
		else if (e instanceof MismatchedNotSetException) {
			MismatchedNotSetException mse = (MismatchedNotSetException) e;
			message = localization + "mismatched token: " + e.token + "; expecting set " + mse.expecting;
		}
		else if (e instanceof FailedPredicateException) {
			FailedPredicateException fpe = (FailedPredicateException) e;
			message = localization + "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
		}
		return message;
	}

	private List extractEntityNames(String entityName) throws RecognitionException {
		List implementors = context.getEntityImplementors( entityName );
		if ( implementors == null ) {
			throw new RecognitionException();
		}
		return implementors;
	}

	private Tree generatePersisterSpacesTree(List persistenceSpaces) {
		List persisterSpaceList = new ArrayList();
		for ( Iterator iterator = persistenceSpaces.iterator(); iterator.hasNext(); ) {
			Tree persistenceSpaceData = (Tree) iterator.next();
			if ( persistenceSpaceData.getType() == PERSISTER_JOIN || persistenceSpaceData.getType() == PROPERTY_JOIN ) {
				adaptor.addChild( persisterSpaceList.get( persisterSpaceList.size() - 1), persistenceSpaceData );
			}
			else {
				Object persistenceSpaceTree = (Object) adaptor.nil();
				persistenceSpaceTree = adaptor.becomeRoot( (Object) adaptor.create( PERSISTER_SPACE, "PERSISTER_SPACE" ), persistenceSpaceTree );
				adaptor.addChild( persistenceSpaceTree, persistenceSpaceData );
				persisterSpaceList.add( persistenceSpaceTree );
			}
		}
		Tree resultTree = (Tree) adaptor.nil();
		for ( Iterator iterator = persisterSpaceList.iterator(); iterator.hasNext(); ) {
			Object persistenceElement = (Object) iterator.next();
			adaptor.addChild( resultTree, persistenceElement );
		}
		return resultTree;
	}

	private Tree generateUpdateStatementTree(Object updateKey, Object entityName, Object aliasClause, Object setClause, Object whereClause) {
		Tree result = new CommonTree();
		EntityNameTree entityNameTree = (EntityNameTree) entityName;
		for ( int i = 0; i < entityNameTree.getEntityCount(); i++ ) {
			Tree updateRoot = new CommonTree( (CommonTree) updateKey );
			updateRoot.addChild( new EntityNameTree( entityNameTree, entityNameTree.getEntityName( i ) ) );
			if ( aliasClause != null ) {
				updateRoot.addChild( (Tree) aliasClause );
			}
			updateRoot.addChild( (Tree) setClause );

			if ( whereClause != null ) {
				updateRoot.addChild( (Tree) whereClause );
			}
			result.addChild( updateRoot );
		}
		return result;
	}

	private Tree generateDeleteStatementTree(Object deleteKey, Object entityName, Object aliasClause, Object whereClause) {
		Tree result = new CommonTree();
		EntityNameTree entityNameTree = (EntityNameTree) entityName;
		for ( int i = 0; i < entityNameTree.getEntityCount(); i++ ) {
			Tree deleteRoot = new CommonTree( (CommonTree) deleteKey );
			deleteRoot.addChild( new EntityNameTree( entityNameTree, entityNameTree.getEntityName( i ) ) );
			if ( aliasClause != null ) {
				deleteRoot.addChild( (Tree) aliasClause );
			}
			if ( whereClause != null ) {
				deleteRoot.addChild( (Tree) whereClause );
			}
			result.addChild( deleteRoot );
		}
		return result;
	}

	private Tree generateSelecFromTree(Object selectClause, Object fromClause, List aliasList){
		Tree result = new CommonTree( new CommonToken( SELECT_FROM, "SELECT_FROM" ) );
		Tree selectTree = null;
		result.addChild( (Tree) fromClause );
		if (selectClause == null && aliasList != null && aliasList.size() > 0) {
			selectTree = new CommonTree( new CommonToken( SELECT, "SELECT") );
			Tree selectList = new CommonTree( new CommonToken( SELECT_LIST, "SELECT_LIST" ) );
			for ( Iterator iterator = aliasList.iterator(); iterator.hasNext(); ) {
				String aliasName = (String) iterator.next();
				Tree selectElement = new CommonTree( new CommonToken( SELECT_ITEM, "SELECT_ITEM" ) );
				Tree aliasElement = new CommonTree( new CommonToken( ALIAS_REF, aliasName ) );
				selectElement.addChild( aliasElement );
				selectList.addChild( selectElement );
			}
			selectTree.addChild( selectList );
		}
		else {
			selectTree = (Tree) selectClause;
		}
		result.addChild( selectTree );
		return result;
	}
}

filterStatement[String collectionRole]
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	selectClause? from_key? whereClause? ( groupByClause havingClause?)? orderByClause?
		-> ^(QUERY ^(QUERY_SPEC["filter-query-spec"] FILTER[$collectionRole] 
				selectClause? from_key? whereClause? ( groupByClause havingClause?)? orderByClause?))
	//TODO throw an exception here when using from
	;

statement
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	updateStatement
	|	deleteStatement
	|	insertStatement
	|	selectStatement
	;

updateStatement
scope{
	boolean generateVersionedField;
}	:	update_key
		(versioned_key {$updateStatement::generateVersionedField = true;})? 
			from_key? entityName aliasClause[true] setClause whereClause?
		-> {	generateUpdateStatementTree($update_key.tree, $entityName.tree, $aliasClause.tree, $setClause.tree, $whereClause.tree )	}
	;

//TODO: check what is necessary to generate at versioned field
setClause
	:	set_key assignment (COMMA assignment)*
		-> {$updateStatement::generateVersionedField}? ^(set_key assignment+ ^(EQUALS VERSIONED_VALUE STRING_LITERAL))
		-> ^(set_key assignment+)
	;

assignment
	:	assignmentField EQUALS^ concatenation
	;

assignmentField
	:	dotIdentifierPath -> ^(PROPERTY_REFERENCE dotIdentifierPath)
	;

deleteStatement
	:	delete_key from_key? entityName aliasClause[true] whereClause?
		-> {	generateDeleteStatementTree($delete_key.tree, $entityName.tree, $aliasClause.tree, $whereClause.tree )	}
	;

insertStatement
	:	insert_key^ 
		intoClause selectStatement
	;

//TODO: Generate an exception when try to use a polimorfic entity at INTO clause
intoClause
	:	into_key entityName insertabilitySpecification
		-> ^(into_key entityName ALIAS_NAME[context.buildUniqueImplicitAlias()] insertabilitySpecification)
	;

insertabilitySpecification
	:	LEFT_PAREN insertablePropertySpecification ( COMMA insertablePropertySpecification )* RIGHT_PAREN
		-> ^(INSERTABILITY_SPEC insertablePropertySpecification+ )
	;

insertablePropertySpecification
	:	dotIdentifierPath -> ^(PROPERTY_REFERENCE dotIdentifierPath)
	;

selectStatement
	:	queryExpression orderByClause?
		-> ^(QUERY queryExpression orderByClause?)
	;

//Think about the exception generation where Polimorfic queries are used inside a Mix of results (union, intersect and except) 
queryExpression
	:	querySpec ( ( union_key^ | intersect_key^ | except_key^ ) all_key? querySpec )*
	;

querySpec
	:	selectFrom whereClause? ( groupByClause havingClause? )?
		-> ^(QUERY_SPEC selectFrom whereClause? groupByClause? havingClause?)
	;

groupByClause
	:	group_by_key^ groupingSpecification
	;

havingClause
	:	having_key^ logicalExpression
	;

groupingSpecification
	:	groupingValue ( COMMA! groupingValue )*
	;

groupingValue
	:	concatenation collationSpecification?
		-> ^(GROUPING_VALUE concatenation collationSpecification?)
	;

whereClause
	:	where_key^ logicalExpression
	;

selectFrom
	:	sc=selectClause? fc=fromClause
		-> { generateSelecFromTree($sc.tree, $fc.tree, $fc.aliasList)}
	;

subQuery
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	queryExpression
		-> ^(SUB_QUERY ^(QUERY queryExpression))
	;

fromClause returns [List aliasList]
scope{
	List aliases;
}
@init	{	$fromClause::aliases = new ArrayList();	}
@after	{	$aliasList = $fromClause::aliases;	}
	:	from_key^ 
			persisterSpaces
	;

persisterSpaces
	:	ps+=persisterSpace ( COMMA ps+=persisterSpace )*
		-> {generatePersisterSpacesTree($ps)}
	;

persisterSpace
	:	persisterSpaceRoot ( qualifiedJoin | crossJoin )*
	;

crossJoin
	:	cross_key join_key mainEntityPersisterReference
		-> ^(PERSISTER_JOIN[$join_key.start,"persister-join"] cross_key mainEntityPersisterReference) 
	;

qualifiedJoin
@init	{ boolean isEntityReference = false; boolean hasFetch = false; List entityNames = null; }
@after	{ if (!hasFetch) $fromClause::aliases.add(((Tree)$ac.tree).getText()); }
	:	nonCrossJoinType join_key (fetch_key {hasFetch = true;})? path ac=aliasClause[true]
	(	on_key 
	{	isEntityReference = true;
		entityNames = extractEntityNames($path.text);	} 
		logicalExpression 
	|	propertyFetch? withClause?
	)
	-> {isEntityReference}? ^(PERSISTER_JOIN[$join_key.start,"persister-join"] nonCrossJoinType ^(ENTITY_PERSISTER_REF ENTITY_NAME<EntityNameTree>[$path.start, $path.text, entityNames] aliasClause?) ^(on_key logicalExpression))
	-> ^(PROPERTY_JOIN[$join_key.start, "property-join"] nonCrossJoinType fetch_key? aliasClause? propertyFetch? ^(PROPERTY_REFERENCE path) withClause?)
	;

withClause
	:	with_key^ logicalExpression
	;

nonCrossJoinType
	:	inner_key
	|	outerJoinType outer_key?
	|	-> INNER
	;

outerJoinType
	:	left_key
	|	right_key
	|	full_key
	;

persisterSpaceRoot
options{
backtrack=true;
}	:	mainEntityPersisterReference
	|	jpaCollectionReference
	|	hibernateLegacySyntax
	;

mainEntityPersisterReference
@after	{ $fromClause::aliases.add(((Tree)$ac.tree).getText()); }
	:	entityName ac=aliasClause[true] propertyFetch?
		-> ^(ENTITY_PERSISTER_REF entityName aliasClause? propertyFetch?)
	;

propertyFetch
	:	fetch_key all_key properties_key
		-> PROP_FETCH[$fetch_key.start, "property-fetch"]
	;

hibernateLegacySyntax returns [boolean isPropertyJoin]
@init {$isPropertyJoin = false;}
@after	{ $fromClause::aliases.add(((Tree)$ad.tree).getText()); }
	:	ad=aliasDeclaration in_key
	(	class_key entityName -> ^(ENTITY_PERSISTER_REF entityName aliasDeclaration) 
	|	collectionExpression {$isPropertyJoin = true;} -> ^(PROPERTY_JOIN INNER[$in_key.start, "inner legacy"] aliasDeclaration collectionExpression)
	)
	;

jpaCollectionReference
@after	{ $fromClause::aliases.add(((Tree)$ac.tree).getText()); }
	:	in_key LEFT_PAREN propertyReference RIGHT_PAREN ac=aliasClause[true]
		-> ^(PROPERTY_JOIN INNER[$in_key.start, "inner"] aliasClause? propertyReference) 
	;

selectClause
	:	select_key^ distinct_key? rootSelectExpression 
	;

rootSelectExpression
	:	rootDynamicInstantiation
	|	jpaSelectObjectSyntax
	|	explicitSelectList
	;

explicitSelectList
	:	explicitSelectItem ( COMMA explicitSelectItem )*
		-> ^(SELECT_LIST explicitSelectItem+)
	;

explicitSelectItem
	:	selectExpression
	;

selectExpression
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.FALSE); }
@after	{ enableParameterUsage.pop(); }
//PARAMETERS CAN'T BE USED -> This verification should be scoped
	:	expression aliasClause[false]
		-> ^(SELECT_ITEM expression aliasClause?)
	;

aliasClause[boolean generateAlias]
options{
    k=2;
}	:	-> {$generateAlias}? ALIAS_NAME[context.buildUniqueImplicitAlias()]
		->
	|	aliasDeclaration
	|	as_key! aliasDeclaration
	;

aliasDeclaration
	:	IDENTIFIER -> ALIAS_NAME[$IDENTIFIER]
	;

aliasReference
	:	IDENTIFIER -> ALIAS_REF[$IDENTIFIER] 
	;

rootDynamicInstantiation
	:	new_key dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
		-> ^(SELECT_ITEM ^(DYNAMIC_INSTANTIATION[$dynamicInstantiationTarget.start, $dynamicInstantiationTarget.text] dynamicInstantiationArgs))
	;

dynamicInstantiationTarget
	:	dotIdentifierPath
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA! dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	selectExpression -> ^(DYNAMIC_INSTANTIATION_ARG selectExpression)
	|	rootDynamicInstantiation -> ^(DYNAMIC_INSTANTIATION_ARG rootDynamicInstantiation)
	;

jpaSelectObjectSyntax
	:	object_key LEFT_PAREN aliasReference RIGHT_PAREN
		-> ^(SELECT_ITEM aliasReference) 
	;

orderByClause
	:	order_by_key^ sortSpecification ( COMMA! sortSpecification )*
	;

sortSpecification
@init{boolean generateOmmitedElement = true;}
	:	sortKey collationSpecification? (orderingSpecification {generateOmmitedElement = false;})?
		-> {generateOmmitedElement}? ^(SORT_SPEC sortKey collationSpecification? ORDER_SPEC["asc"])
		-> ^(SORT_SPEC sortKey collationSpecification? orderingSpecification?)
	;

sortKey
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.FALSE); }
@after	{ enableParameterUsage.pop(); }
//PARAMETERS CAN'T BE USED -> This verification should be scoped
	:	concatenation
	;

collationSpecification
	:	collate_key collateName
	->	COLLATE[$collateName.start, $collateName.text]
	;

collateName
	:	dotIdentifierPath
	;

orderingSpecification
	:	ascending_key -> ORDER_SPEC[$ascending_key.start, "asc"]
	|	descending_key -> ORDER_SPEC[$descending_key.start, "desc"]
	;

logicalExpression
	:	expression
	;

expression
	:	logicalOrExpression
	;

logicalOrExpression
	:	logicalAndExpression ( or_key^ logicalAndExpression )*
	;

logicalAndExpression
	:	negatedExpression ( and_key^ negatedExpression )*
	;

negatedExpression
	:	not_key^ negatedExpression
	|	equalityExpression
	;

equalityExpression
@init{ boolean isNull = false; boolean isNegated = false;}
	:	(relationalExpression -> relationalExpression) 
	(	is_key (not_key {isNegated = true;})? (NULL {isNull = true;}|empty_key)
		-> {isNull && isNegated}? ^(IS_NOT_NULL[$not_key.start, "is not null"] $equalityExpression)
		-> {isNull && !isNegated}? ^(IS_NULL[$NULL, "is null"] $equalityExpression)
		-> {!isNull && isNegated}? ^(IS_NOT_EMPTY $equalityExpression)
		-> ^(IS_EMPTY $equalityExpression)
	|	( op=EQUALS | op=NOT_EQUAL ) relationalExpression
		-> ^($op $equalityExpression relationalExpression)
	)*
	;

relationalExpression
@init {boolean isNegated = false;} 
	:	(concatenation -> concatenation)
	( 
	(	( op=LESS | op=GREATER | op=LESS_EQUAL | op=GREATER_EQUAL ) additiveExpression
			-> ^($op $relationalExpression additiveExpression) 
		)+
	|  (not_key {isNegated = true;} )?
		(	in_key inList
			-> {isNegated}? ^(NOT_IN[$not_key.start, "not in"] $relationalExpression inList)
			-> ^(in_key $relationalExpression inList) 
		|	between_key betweenList
			-> {isNegated}? ^(NOT_BETWEEN[$not_key.start, "not between"] $relationalExpression betweenList)
			-> ^(between_key $relationalExpression betweenList)
		|	like_key concatenation likeEscape?
			-> {isNegated}? ^(NOT_LIKE[$not_key.start, "not like"] $relationalExpression concatenation likeEscape?) 
			-> ^(like_key $relationalExpression concatenation likeEscape?)
		|	member_of_key path
			-> {isNegated}? ^(NOT_MEMBER_OF[$not_key.start, "not member of"] $relationalExpression ^(PATH path))
			-> ^(member_of_key $relationalExpression ^(PATH path))
		)
	)?
	;

likeEscape
	:	escape_key^ concatenation
	;

inList
	:	collectionExpression
		-> ^(IN_LIST collectionExpression)
	|	LEFT_PAREN ( {((validateSoftKeyword("select")|validateSoftKeyword("from")))}?=> subQuery | concatenation (COMMA concatenation)* ) RIGHT_PAREN
		-> ^(IN_LIST concatenation* subQuery?)
	;

betweenList
	:	concatenation and_key concatenation
		-> ^(BETWEEN_LIST concatenation+)
	;

concatenation
	:	additiveExpression (DOUBLE_PIPE^ {enableParameterUsage.push(Boolean.TRUE);} additiveExpression { enableParameterUsage.pop(); })*
	;

additiveExpression
	:	multiplyExpression ( ( PLUS^ | MINUS^ ) {enableParameterUsage.push(Boolean.TRUE);} multiplyExpression { enableParameterUsage.pop(); })*
	;

multiplyExpression
	:	unaryExpression ( ( ASTERISK^ | SOLIDUS^ ) {enableParameterUsage.push(Boolean.TRUE);} unaryExpression { enableParameterUsage.pop(); })*
	;

unaryExpression
	:	MINUS unaryExpression -> ^(UNARY_MINUS[$MINUS] unaryExpression)
	|	PLUS unaryExpression -> ^(UNARY_PLUS[$PLUS] unaryExpression)
	|	caseExpression
	|	quantifiedExpression
	|	standardFunction
	|	setFunction
	|	collectionFunction
	|	collectionExpression
	|	atom
	;

caseExpression
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	caseAbbreviation
	|	caseSpecification
	;

caseAbbreviation
	:	nullif_key^ LEFT_PAREN! concatenation COMMA! concatenation RIGHT_PAREN!
	|	coalesce_key^ LEFT_PAREN! concatenation (COMMA! concatenation)* RIGHT_PAREN!
	;

caseSpecification
options{
backtrack=true;
}	:	simpleCase
	|	searchedCase
	;

simpleCase
	:	case_key concatenation simpleCaseWhenClause+ elseClause? end_key
	->	^(SIMPLE_CASE[$case_key.start, $case_key.text] concatenation simpleCaseWhenClause+ elseClause?)
	;

simpleCaseWhenClause
	:	when_key^ concatenation then_key! concatenation
	;

elseClause
	:	else_key^ concatenation
	;

searchedCase
	:	case_key searchedWhenClause+ elseClause? end_key
	->	^(SEARCHED_CASE[$case_key.start, $case_key.text] searchedWhenClause+ elseClause?)
	;

searchedWhenClause
	:	when_key^ logicalExpression then_key! concatenation
	;

quantifiedExpression
	:	( some_key^ | exists_key^ | all_key^ | any_key^ ) 
	(	collectionExpression
	|	aliasReference
	|	LEFT_PAREN! subQuery RIGHT_PAREN!
	)
	;

standardFunction
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
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
	:	cast_key^ LEFT_PAREN! concatenation as_key! dataType RIGHT_PAREN!
	;

concatFunction
	:	concat_key^ LEFT_PAREN! concatenation ( COMMA! concatenation )+ RIGHT_PAREN!
	;

substringFunction
	:	substring_key^ LEFT_PAREN! concatenation COMMA! concatenation ( COMMA! concatenation)? RIGHT_PAREN!
	;

trimFunction
	:	trim_key LEFT_PAREN trimOperands RIGHT_PAREN
		-> ^(trim_key trimOperands)
	;

trimOperands
options{
k=2;
}
@init {boolean hasSecondExpression = false;}
	:	trimSpecification from_key concatenation -> ^(trimSpecification STRING_LITERAL[" "] concatenation)
	|	trimSpecification concatenation from_key concatenation -> ^(trimSpecification concatenation+)
	|	from_key concatenation -> ^(BOTH STRING_LITERAL[" "] concatenation)
	|	cn=concatenation ( from_key concatenation {hasSecondExpression = true;} )?
		-> {hasSecondExpression}? ^(BOTH concatenation+)
		-> ^(BOTH STRING_LITERAL[" "] $cn)
	;

trimSpecification
	:	leading_key
	|	trailing_key
	|	both_key
	;

upperFunction
	:	upper_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

lowerFunction
	:	lower_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

lengthFunction
	:	length_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

locateFunction
	:	locate_key^ LEFT_PAREN! concatenation COMMA! concatenation ( COMMA! concatenation )? RIGHT_PAREN!
	;

absFunction
	:	abs_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

sqrtFunction
	:	sqrt_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

modFunction
	:	mod_key^ LEFT_PAREN! concatenation COMMA! concatenation RIGHT_PAREN!
	;

sizeFunction
	:	size_key^ LEFT_PAREN! propertyReference RIGHT_PAREN!
	;

indexFunction
	:	index_key^ LEFT_PAREN! aliasReference RIGHT_PAREN!
	;

currentDateFunction
	:	current_date_key ( LEFT_PAREN! RIGHT_PAREN! )?
	;

currentTimeFunction
	:	current_time_key ( LEFT_PAREN! RIGHT_PAREN! )?
	;

currentTimestampFunction
	:	current_timestamp_key ( LEFT_PAREN! RIGHT_PAREN! )?
	;

extractFunction
	:	extract_key^ LEFT_PAREN! extractField from_key! concatenation RIGHT_PAREN!
	;

extractField
	:	datetimeField
	|	timeZoneField
	;

datetimeField
	:	nonSecondDatetimeField
	|	second_key
	;

nonSecondDatetimeField
	:	year_key
	|	month_key
	|	day_key
	|	hour_key
	|	minute_key
	;

timeZoneField
	:	timezone_hour_key
	|	timezone_minute_key
	;

positionFunction
	:	position_key^ LEFT_PAREN! concatenation in_key! concatenation RIGHT_PAREN!
	;

charLengthFunction
	:	character_length_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

octetLengthFunction
	:	octet_length_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

bitLengthFunction
	:	bit_length_key^ LEFT_PAREN! concatenation RIGHT_PAREN!
	;

setFunction
@init	{ boolean generateOmmitedElement = true; if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	( sum_key^ | avg_key^ | max_key^ | min_key^ ) LEFT_PAREN! additiveExpression RIGHT_PAREN!
	|	count_key LEFT_PAREN ( ASTERISK {generateOmmitedElement = false;} | ( ( (distinct_key | all_key) {generateOmmitedElement = false;} )? countFunctionArguments ) ) RIGHT_PAREN
		-> {generateOmmitedElement}? ^(count_key ASTERISK? ALL countFunctionArguments?)
		-> ^(count_key ASTERISK? distinct_key? all_key? countFunctionArguments?)
	;

countFunctionArguments
@init { int type = -1;}
	:	propertyReference
	|	collectionExpression
	|	numeric_literal
	;

collectionFunction
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	( maxelement_key^ | maxindex_key^ | minelement_key^ | minindex_key^ ) LEFT_PAREN! propertyReference RIGHT_PAREN!
	;

collectionExpression
	:	(elements_key^|indices_key^) LEFT_PAREN! propertyReference RIGHT_PAREN!
	;

atom
@init { int type = -1;}
	:	identPrimary
	    //TODO  if ends with:
	    //  .class -> class type
	    //  if contains "()" it is a function call 
	    //  if it is constantReference (using context)
	    //  otherwise it will be a generic element to be resolved on the next phase (1st tree walker)
	    -> {type == 0}? ^(DOT_CLASS identPrimary)
	    -> {type == 3}? ^(FUNCTION[$identPrimary.start,$identPrimary.text])
	    -> {type == 4}? ^(JAVA_CONSTANT identPrimary) //-> here will have 2 strutctures element and the constant
	    -> ^(PATH identPrimary)
	|	constant
	|	parameterSpecification { if (enableParameterUsage.peek().equals(Boolean.FALSE)) throw new RecognitionException( ); }
	//validate using Scopes if it is enabled or not to use parameterSpecification.. if not generate an exception 
	|	LEFT_PAREN! ({((validateSoftKeyword("select")|validateSoftKeyword("from")))}?=> subQuery|expressionOrVector) RIGHT_PAREN!
	;

parameterSpecification
@init {boolean isJpaParam = false;}
	:	COLON IDENTIFIER -> NAMED_PARAM[$IDENTIFIER]
	|	PARAM (INTEGER_LITERAL {isJpaParam = true;})?
		-> {isJpaParam}? JPA_PARAM[$INTEGER_LITERAL]
		-> PARAM	
	;

expressionOrVector
@init {boolean isVectorExp = false;}
	:	expression (vectorExpr {isVectorExp = true;})?
		-> {isVectorExp}? ^(VECTOR_EXPR expression vectorExpr) 
		-> expression
	;

vectorExpr
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	COMMA! expression (COMMA! expression)*
	;

identPrimary
	: 	IDENTIFIER
		(	DOT^ IDENTIFIER
		|	LEFT_SQUARE^ expression RIGHT_SQUARE!
		|	LEFT_SQUARE^ RIGHT_SQUARE!
		|	LEFT_PAREN^ exprList RIGHT_PAREN!
		)*
	;

exprList
@init	{ if (state.backtracking == 0) enableParameterUsage.push(Boolean.TRUE); }
@after	{ enableParameterUsage.pop(); }
	:	expression? (COMMA! expression)*
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
@init	{ List entityNames = null; }
	:	dotIdentifierPath
	{	entityNames = extractEntityNames($dotIdentifierPath.text);	}
	//here the polimorfic entities should be resolved... to:
	   // 1. to place inside the ENTITY_NAME Token all its possible values, otherwise it would be much difficult to return to the place that should explit the sentence 
	   // 2. enable exception geration when not supported (union, insert)
		-> ENTITY_NAME<EntityNameTree>[$dotIdentifierPath.start, $dotIdentifierPath.text, entityNames]
	;

propertyReference
	:	path
		-> ^(PROPERTY_REFERENCE path)
	;

dataType
	:	IDENTIFIER
	;

dotIdentifierPath
	:	IDENTIFIER 
		(	DOT^ IDENTIFIER		)*
	;

path
	:	IDENTIFIER 
		(	DOT^ IDENTIFIER
		|	LEFT_SQUARE^ expression RIGHT_SQUARE!
		|	LEFT_SQUARE^ RIGHT_SQUARE!
		)*
	;

class_key
	:	{(validateSoftKeyword("class"))}?=>  id=IDENTIFIER
		->	CLASS[$id]
	;

new_key
	:	{(validateSoftKeyword("new"))}?=>  id=IDENTIFIER
		->	NEW[$id]
	;

else_key
	:	{(validateSoftKeyword("else"))}?=>  id=IDENTIFIER
		->	ELSE[$id]
	;

object_key
	:	{(validateSoftKeyword("object"))}?=>  id=IDENTIFIER
	;

case_key
	:	{(validateSoftKeyword("case"))}?=>  IDENTIFIER
	;

current_date_key
	:	{(validateSoftKeyword("current_date"))}?=>  id=IDENTIFIER
		->	CURRENT_DATE[$id]
	;

current_time_key
	:	{(validateSoftKeyword("current_time"))}?=>  id=IDENTIFIER
		->	CURRENT_TIME[$id]
	;

current_timestamp_key
	:	{(validateSoftKeyword("current_timestamp"))}?=>  id=IDENTIFIER
		->	CURRENT_TIMESTAMP[$id]
	;

timezone_hour_key
	:	{(validateSoftKeyword("timezone_hour"))}?=>  id=IDENTIFIER
		->	TIMEZONE_HOUR[$id]
	;

timezone_minute_key
	:	{(validateSoftKeyword("timezone_minute"))}?=>  id=IDENTIFIER
		->	TIMEZONE_MINUTE[$id]
	;

character_length_key
	:	{(validateSoftKeyword("character_length") || validateSoftKeyword("char_length"))}?=>  id=IDENTIFIER
		->	CHARACTER_LENGTH[$id]
	;

octet_length_key
	:	{(validateSoftKeyword("octet_length"))}?=>  id=IDENTIFIER
		->	OCTET_LENGTH[$id]
	;

bit_length_key
	:	{(validateSoftKeyword("bit_length"))}?=>  id=IDENTIFIER
		->	BIT_LENGTH[$id]
	;

extract_key
	:	{(validateSoftKeyword("extract"))}?=>  id=IDENTIFIER
		->	EXTRACT[$id]
	;

second_key
	:	{(validateSoftKeyword("second"))}?=>  id=IDENTIFIER
		->	SECOND[$id]
	;

year_key
	:	{(validateSoftKeyword("year"))}?=>  id=IDENTIFIER
		->	YEAR[$id]
	;

month_key
	:	{(validateSoftKeyword("month"))}?=>  id=IDENTIFIER
		->	MONTH[$id]
	;

day_key
	:	{(validateSoftKeyword("day"))}?=>  id=IDENTIFIER
		->	DAY[$id]
	;

hour_key
	:	{(validateSoftKeyword("hour"))}?=>  id=IDENTIFIER
		->	HOUR[$id]
	;

minute_key
	:	{(validateSoftKeyword("minute"))}?=>  id=IDENTIFIER
		->	MINUTE[$id]
	;

position_key
	:	{(validateSoftKeyword("position"))}?=>  id=IDENTIFIER
		->	POSITION[$id]
	;

sum_key
	:	{(validateSoftKeyword("sum"))}?=>  id=IDENTIFIER
		->	SUM[$id]
	;

avg_key
	:	{(validateSoftKeyword("avg"))}?=>  id=IDENTIFIER
		->	AVG[$id]
	;

max_key
	:	{(validateSoftKeyword("max"))}?=>  id=IDENTIFIER
		->	MAX[$id]
	;

min_key
	:	{(validateSoftKeyword("min"))}?=>  id=IDENTIFIER
		->	MIN[$id]
	;

count_key
	:	{(validateSoftKeyword("count"))}?=>  id=IDENTIFIER
		->	COUNT[$id]
	;

maxelement_key
	:	{(validateSoftKeyword("maxelement"))}?=>  id=IDENTIFIER
		->	MAXELEMENT[$id]
	;

maxindex_key
	:	{(validateSoftKeyword("maxindex"))}?=>  id=IDENTIFIER
		->	MAXINDEX[$id]
	;

minelement_key
	:	{(validateSoftKeyword("minelement"))}?=>  id=IDENTIFIER
		->	MINELEMENT[$id]
	;

minindex_key
	:	{(validateSoftKeyword("minindex"))}?=>  id=IDENTIFIER
		->	MININDEX[$id]
	;

locate_key
	:	{(validateSoftKeyword("locate"))}?=>  id=IDENTIFIER
		->	LOCATE[$id]
	;

abs_key
	:	{(validateSoftKeyword("abs"))}?=>  id=IDENTIFIER
		->	ABS[$id]
	;

sqrt_key
	:	{(validateSoftKeyword("sqrt"))}?=>  id=IDENTIFIER
		->	SQRT[$id]
	;

mod_key
	:	{(validateSoftKeyword("mod"))}?=>  id=IDENTIFIER
		->	MOD[$id]
	;

size_key
	:	{(validateSoftKeyword("size"))}?=>  id=IDENTIFIER
		->	SIZE[$id]
	;

index_key
	:	{(validateSoftKeyword("index"))}?=>  id=IDENTIFIER
		->	INDEX[$id]
	;

leading_key
	:	{(validateSoftKeyword("leading"))}?=>  id=IDENTIFIER
		->	LEADING[$id]
	;

trailing_key
	:	{(validateSoftKeyword("trailing"))}?=>  id=IDENTIFIER
		->	TRAILING[$id]
	;

upper_key
	:	{(validateSoftKeyword("upper"))}?=>  id=IDENTIFIER
		->	UPPER[$id]
	;

lower_key
	:	{(validateSoftKeyword("lower"))}?=>  id=IDENTIFIER
		->	LOWER[$id]
	;

length_key
	:	{(validateSoftKeyword("length"))}?=>  id=IDENTIFIER
		->	LENGTH[$id]
	;

both_key
	:	{(validateSoftKeyword("both"))}?=>  id=IDENTIFIER
		->	BOTH[$id]
	;

trim_key
	:	{(validateSoftKeyword("trim"))}?=>  id=IDENTIFIER
		->	TRIM[$id]
	;
	
substring_key
	:	{(validateSoftKeyword("substring"))}?=>  id=IDENTIFIER
		->	SUBSTRING[$id]
	;

concat_key
	:	{(validateSoftKeyword("concat"))}?=>  id=IDENTIFIER
		->	CONCAT[$id]
	;

cast_key
	:	{(validateSoftKeyword("cast"))}?=>  id=IDENTIFIER
		->	CAST[$id]
	;

any_key
	:	{(validateSoftKeyword("any"))}?=>  id=IDENTIFIER
		->	ANY[$id]
	;

exists_key
	:	{(validateSoftKeyword("exists"))}?=>  id=IDENTIFIER
		->	EXISTS[$id]
	;

some_key
	:	{(validateSoftKeyword("some"))}?=>  id=IDENTIFIER
		->	SOME[$id]
	;

then_key
	:	{(validateSoftKeyword("then"))}?=>  id=IDENTIFIER
		->	THEN[$id]
	;

end_key
	:	{(validateSoftKeyword("end"))}?=>  id=IDENTIFIER
		->	END[$id]
	;


when_key
	:	{(validateSoftKeyword("when"))}?=>  id=IDENTIFIER
		->	WHEN[$id]
	;

nullif_key
	:	{(validateSoftKeyword("nullif"))}?=>  id=IDENTIFIER
		->	NULLIF[$id]
	;

coalesce_key
	:	{(validateSoftKeyword("coalesce"))}?=>  id=IDENTIFIER
		->	COALESCE[$id]
	;

escape_key
	:	{(validateSoftKeyword("escape"))}?=>  id=IDENTIFIER
		->	ESCAPE[$id]
	;

like_key
	:	{(validateSoftKeyword("like"))}?=>  id=IDENTIFIER
		->	LIKE[$id]
	;

between_key
	:	{(validateSoftKeyword("between"))}?=>  id=IDENTIFIER
		->	BETWEEN[$id]
	;

member_of_key
@init{
	String text = "";
}	:	{(validateSoftKeyword("member") && validateLT(2, "of"))}?=>  id=IDENTIFIER IDENTIFIER {text = $text;}
		->	MEMBER_OF[$id]
	;

empty_key
	:	{(validateSoftKeyword("empty"))}?=>  id=IDENTIFIER
	;

is_key	:	{(validateSoftKeyword("is"))}?=>  id=IDENTIFIER
		->	IS[$id]
	;

or_key	:	{(validateSoftKeyword("or"))}?=>  id=IDENTIFIER
		->	OR[$id]
	;

and_key	:	{(validateSoftKeyword("and"))}?=>  id=IDENTIFIER
		->	AND[$id]
	;

not_key	:	{(validateSoftKeyword("not"))}?=>  id=IDENTIFIER
		->	NOT[$id]
	;

set_key
	:	{(validateSoftKeyword("set"))}?=>  id=IDENTIFIER
		->	SET[$id]
	;

versioned_key
	:	{(validateSoftKeyword("versioned"))}?=>  id=IDENTIFIER
		->	VERSIONED[$id]
	;

update_key
	:	{(validateSoftKeyword("update"))}?=>  id=IDENTIFIER
		->	UPDATE[$id]
	;

delete_key
	:	{(validateSoftKeyword("delete"))}?=>  id=IDENTIFIER
		->	DELETE[$id]
	;

insert_key
	:	{(validateSoftKeyword("insert"))}?=>  id=IDENTIFIER
		->	INSERT[$id]
	;

into_key
	:	{(validateSoftKeyword("into"))}?=>  id=IDENTIFIER
		->	INTO[$id]
	;

having_key
	:	{(validateSoftKeyword("having"))}?=>  id=IDENTIFIER
		->	HAVING[$id]
	;

with_key
	:	{(validateSoftKeyword("with"))}?=>  id=IDENTIFIER
		->	WITH[$id]
	;

on_key
	:	{(validateSoftKeyword("on"))}?=>  id=IDENTIFIER
		->	ON[$id]
	;

indices_key
	:	{(validateSoftKeyword("indices"))}?=>  id=IDENTIFIER
		->	INDICES[$id]
	;

cross_key
	:	{(validateSoftKeyword("cross"))}?=>  id=IDENTIFIER
		->	CROSS[$id]
	;

join_key
	:	{(validateSoftKeyword("join"))}?=>  id=IDENTIFIER
		->	JOIN[$id]
	;

inner_key
	:	{(validateSoftKeyword("inner"))}?=>  id=IDENTIFIER
		->	INNER[$id]
	;

outer_key
	:	{(validateSoftKeyword("outer"))}?=>  id=IDENTIFIER
		->	OUTER[$id]
	;

left_key
	:	{(validateSoftKeyword("left"))}?=>  id=IDENTIFIER
		->	LEFT[$id]
	;

right_key
	:	{(validateSoftKeyword("right"))}?=>  id=IDENTIFIER
		->	RIGHT[$id]
	;

full_key
	:	{(validateSoftKeyword("full"))}?=>  id=IDENTIFIER
		->	FULL[$id]
	;

elements_key
	:	{(validateSoftKeyword("elements"))}?=>  id=IDENTIFIER
		->	ELEMENTS[$id]
	;

properties_key
	:	{(validateSoftKeyword("properties"))}?=>  id=IDENTIFIER
		->	PROPERTIES[$id]
	;

fetch_key
	:	{(validateSoftKeyword("fetch"))}?=>  id=IDENTIFIER
		->	FETCH[$id]
	;

in_key
	:	{(validateSoftKeyword("in"))}?=>  id=IDENTIFIER
		->	IN[$id]
	;

as_key
	:	{(validateSoftKeyword("as"))}?=>  id=IDENTIFIER
		->	AS[$id]
	;

where_key
	:	{(validateSoftKeyword("where"))}?=>  id=IDENTIFIER
		->	WHERE[$id]
	;

select_key
	:	{(validateSoftKeyword("select"))}?=>  id=IDENTIFIER
		->	SELECT[$id]
	;

distinct_key
	:	{(validateSoftKeyword("distinct"))}?=>  id=IDENTIFIER
		->	DISTINCT[$id]
	;

union_key
	:	{(validateSoftKeyword("union"))}?=>  id=IDENTIFIER
		->	UNION[$id]
	;

intersect_key
	:	{(validateSoftKeyword("intersect"))}?=>  id=IDENTIFIER
		->	INTERSECT[$id]
	;

except_key
	:	{(validateSoftKeyword("except"))}?=>  id=IDENTIFIER
		->	EXCEPT[$id]
	;

all_key
	:	{(validateSoftKeyword("all"))}?=>  id=IDENTIFIER
		->	ALL[$id]
	;

ascending_key
	:	{(validateSoftKeyword("ascending") || validateSoftKeyword("asc"))}?=>  IDENTIFIER
	;

descending_key
	:	{(validateSoftKeyword("descending") || validateSoftKeyword("desc"))}?=>  IDENTIFIER
	;

collate_key
	:	{(validateSoftKeyword("collate"))}?=>  IDENTIFIER
	;

order_by_key
@init{
	String text = "";
}	:	{(validateSoftKeyword("order") && validateLT(2, "by"))}?=>  id=IDENTIFIER IDENTIFIER {text = $text;}
		->	ORDER_BY[$id]
	;

group_by_key
@init{
	String text = "";
}	:	{(validateSoftKeyword("group") && validateLT(2, "by"))}?=>  id=IDENTIFIER IDENTIFIER {text = $text;}
		->	GROUP_BY[$id]
	;

from_key
	:	{(validateSoftKeyword("from"))}?=>  id=IDENTIFIER
		->	FROM[$id]
	;