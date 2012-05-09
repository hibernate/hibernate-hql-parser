parser grammar OrderByParser;

options {
	tokenVocab = HQLLexer;
	output = AST;
	TokenLabelType = CommonToken;
	ASTLabelType = CommonTree;
}

@parser::header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008-2012, Red Hat Middleware LLC or third-party contributors as
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
package org.hibernate.sql.ast.origin.ordering;

import org.hibernate.sql.TemplateConstants;
}

@parser::members {
	private Stack flattenTuples = new Stack();

    protected void prepareFlattenTuplesStack() {
        flattenTuples.push( Boolean.FALSE );
    }

    /**
     * A check to see if the text represents a known function name (in addition to the
     * set of known {@link #standardFunction "standard"} functions.  This is only needed in the
     * case of seeing a dot-ident structure which is not followed by a paren; such structures
     * which are followed by a paren are explicitly assumed to be a function name.
     *
     * @param text The text to check as a  function name.
     *
     * @return True if the text is a known function name, false otherwise.
     *
     * @see #standardFunction
     */
    protected boolean isFunctionName(String text) {
        // by default, assume it is not
    	return false;
    }

    /**
     * A check to see if the text represents a mapped property name.
     *
     * @param text The text to check as a property name.
     *
     * @return True if the text is a mapped property name, false otherwise.
     */
    protected boolean isPropertyName(String text) {
        // by default, assume it is not
    	return false;
    }

    /**
     * Given a property, resolve it's {@link #COLUMN} or {@link #VECTOR_EXPR} tree.
     *
     * @param propertyTree The tree representing the property name.
     *
     * @return The column(s) tree.
     */
    protected CommonTree buildPropertyColumns(CommonTree propertyTree) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    private boolean validateSoftKeyword(String text) {
		return validateLT(1, text);
	}

	private boolean validateLT(int offset, String text) {
		String text2Validate = retrieveLT( offset );
		return text2Validate == null ? false : text2Validate.equalsIgnoreCase( text );
	}

	private String retrieveLT(int offset) {
      	if (null == input) {
      		return null;
      	}
		Token token = input.LT(offset);
		return token == null ? null : token.getText();
	}

    public Boolean shouldFlattenTuplesInOrderBy() {
        return Boolean.TRUE;
    }
}


// Parser rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


/**
 * Main recognition rule for this grammar
 */
orderByFragment
@init {
    if ( state.backtracking == 0 ) {
        flattenTuples.push( shouldFlattenTuplesInOrderBy() );
    }
}
@after {
    if ( state.backtracking == 0 ) {
        flattenTuples.pop();
    }
} : sortSpecification ( COMMA sortSpecification )*
        -> ^( ORDER_BY sortSpecification+ )
    ;


/**
 * Reconition rule for what ANSI SQL terms the <tt>sort specification</tt>.  These are the atomic elements of the
 * <tt>ORDER BY</tt> list pieces.
 * </p>
 * IMPL NOTE : The '+' on the outside of the ^( SORT_SPEC ... ) rewrite forces a duplication of the root, one
 *    for each child return from
 */
sortSpecification
    : sortKey collationSpecification? orderingSpecification?
        -> ^( SORT_SPEC sortKey collationSpecification? orderingSpecification? )+
    ;


/**
 * Reconition rule for what ANSI SQL terms the <tt>sort key</tt> which is the expression (column, function, etc) upon
 * which to base the sorting.
 */
sortKey
    : expression
    ;

/**
 * Reconition rule what this grammar recognizes as valid <tt>sort key</tt>.
 */
expression
@init {
    if ( state.backtracking == 0 ) {
        //enableParameterUsage.push(Boolean.TRUE); //FIXME
    }
}
@after {
    if ( state.backtracking == 0 ) {
        //enableParameterUsage.pop(); //FIXME
    }
}
   : QUOTED_IDENTIFIER -> ^( COLUMN ALIAS_REF[TemplateConstants.TEMPLATE] QUOTED_IDENTIFIER[$QUOTED_IDENTIFIER] )
    // we treat the so-called standard functions differently because they are handled differently by the HQL lexer which we also use here...
    | standardFunction
    | literal
    // not identDotIdentStructure because we dont want QUOTED_IDENTIFIERs is here
    | ( IDENTIFIER ( DOT IDENTIFIER )* LEFT_PAREN ) => generalFunction
    // otherwise we fully expect a dot-identifier series, and then we just need to decode the semantic of that structure
    | identDotIdentStructure
        -> { ( isFunctionName($identDotIdentStructure.text) ) }?
              // we have a function with parens (thus no args)
              ^( FUNCTION[$identDotIdentStructure.start,$identDotIdentStructure.text] )
        -> { ( isPropertyName($identDotIdentStructure.text) ) }?
              // we have a reference to a mapped property
              { buildPropertyColumns( $identDotIdentStructure.tree ) }
        -> { ( $identDotIdentStructure.tree.getType() == DOT ) }?
              // we have a reference to a column which is already qualified
              identDotIdentStructure
        ->
              // we have a reference to a column which is not qualified
              ^( COLUMN ALIAS_REF[TemplateConstants.TEMPLATE] IDENTIFIER[$identDotIdentStructure.start,$identDotIdentStructure.text] )
    ;

fragment
identifier
    : IDENTIFIER
    | QUOTED_IDENTIFIER
    ;

fragment
identDotIdentStructure
    : IDENTIFIER ( DOT^ identifier )*
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
	: cast_keyword LEFT_PAREN expression as_keyword dataType RIGHT_PAREN
	    -> ^( cast_keyword expression dataType )
	;

fragment
dataType
	:	IDENTIFIER
	;

concatFunction
	: concat_keyword LEFT_PAREN expression ( COMMA expression )+ RIGHT_PAREN
	    -> ^( concat_keyword expression+ )
	;

substringFunction
	: substring_keyword LEFT_PAREN expression COMMA expression ( COMMA expression)? RIGHT_PAREN
	    -> ^( substring_keyword expression+ )
	;

trimFunction
    // todo : dont like the creation of subtree(s) for the operands in the trimOperands rule
	: trim_keyword LEFT_PAREN trimOperands RIGHT_PAREN
	    -> ^( trim_keyword trimOperands )
	;

fragment
trimOperands
options{
k=2;
}
@init {boolean hasSecondExpression = false;}
	: trimSpecification from_keyword expression
	    -> ^( trimSpecification STRING_LITERAL["' '"] expression)
	| trimSpecification expression from_keyword expression
	    -> ^(trimSpecification expression+)
	| from_keyword expression
	    -> ^(BOTH STRING_LITERAL["' '"] expression)
	| cn=expression ( from_keyword expression {hasSecondExpression = true;} )?
	    -> {hasSecondExpression}? ^(BOTH expression+)
		-> ^(BOTH STRING_LITERAL["' '"] $cn)
	;

fragment
trimSpecification
	: leading_keyword
	| trailing_keyword
	| both_keyword
	;

upperFunction
	: upper_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( upper_keyword expression )
	;

lowerFunction
	: lower_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( lower_keyword expression )
	;

lengthFunction
	: length_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( length_keyword expression )
	;

locateFunction
	: locate_keyword LEFT_PAREN expression COMMA expression ( COMMA expression )? RIGHT_PAREN
	    -> ^( locate_keyword expression+ )
	;

absFunction
	: abs_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( abs_keyword expression )
	;

sqrtFunction
	: sqrt_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( sqrt_keyword expression )
	;

modFunction
	: mod_keyword LEFT_PAREN expression COMMA expression RIGHT_PAREN
	    -> ^( mod_keyword expression+ )
	;

currentDateFunction
	: current_date_keyword ( LEFT_PAREN! RIGHT_PAREN! )?
	;

currentTimeFunction
	: current_time_keyword ( LEFT_PAREN! RIGHT_PAREN! )?
	;

currentTimestampFunction
	: current_timestamp_keyword ( LEFT_PAREN! RIGHT_PAREN! )?
	;

extractFunction
	: extract_keyword LEFT_PAREN extractField from_keyword expression RIGHT_PAREN
	    -> ^( extract_keyword extractField expression )
	;

extractField
	: datetimeField
	| timeZoneField
	;

datetimeField
	: nonSecondDatetimeField
	| second_keyword
	;

nonSecondDatetimeField
	: year_keyword
	| month_keyword
	| day_keyword
	| hour_keyword
	| minute_keyword
	;

timeZoneField
	: timezone_hour_keyword
	| timezone_minute_keyword
	;

positionFunction
	: position_keyword LEFT_PAREN expression in_keyword expression RIGHT_PAREN
	    -> ^( position_keyword expression+ )
	;

charLengthFunction
	: character_length_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( character_length_keyword expression )
	;

octetLengthFunction
	: octet_length_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( octet_length_keyword expression )
	;

bitLengthFunction
	: bit_length_keyword LEFT_PAREN expression RIGHT_PAREN
	    -> ^( bit_length_keyword expression )
	;


generalFunction
    : generalFunctionName LEFT_PAREN functionArgument ( COMMA functionArgument )* RIGHT_PAREN
          -> ^( FUNCTION[$generalFunctionName.start,$generalFunctionName.text] functionArgument+ )
    ;

generalFunctionName :
    IDENTIFIER ( DOT IDENTIFIER )+
;

/**
 * Recognized function parameters.
 */
functionArgument
    : expression
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

/**
 * Reconition rule for what ANSI SQL terms the <tt>collation specification</tt> used to allow specifying that sorting for
 * the given {@link #sortSpecification} be treated within a specific character-set.
 */
collationSpecification!
    : collateKeyword collationName
        -> COLLATE[$collateKeyword.start,$collationName.text]
    ;

collateKeyword
    : {(validateSoftKeyword("collate"))}?=>  id=IDENTIFIER
        -> COLLATE[$id]
    ;

/**
 * The collation name wrt {@link #collationSpecification}.  Namely, the character-set.
 */
collationName
    : IDENTIFIER
    ;

/**
 * Reconition rule for what ANSI SQL terms the <tt>ordering specification</tt>; <tt>ASCENDING</tt> or
 * <tt>DESCENDING</tt>.
 */
orderingSpecification!
    : ascending_keyword
        -> ORDER_SPEC[$ascending_keyword.start,$ascending_keyword.text]
    | descending_keyword
        -> ORDER_SPEC[$descending_keyword.start,$descending_keyword.text]
    ;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Soft-keyword handling rules
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

abs_keyword
	:	{(validateSoftKeyword("abs"))}?=>  id=IDENTIFIER
		->	ABS[$id]
	;

as_keyword
	:	{(validateSoftKeyword("as"))}?=>  id=IDENTIFIER
		->	AS[$id]
	;

ascending_keyword :
    {(validateSoftKeyword("ascending") || validateSoftKeyword("asc"))}?=>  IDENTIFIER
;

bit_length_keyword
	:	{(validateSoftKeyword("bit_length"))}?=>  id=IDENTIFIER
		->	BIT_LENGTH[$id]
	;

both_keyword
	:	{(validateSoftKeyword("both"))}?=>  id=IDENTIFIER
		->	BOTH[$id]
	;

cast_keyword
	:	{(validateSoftKeyword("cast"))}?=>  id=IDENTIFIER
		->	CAST[$id]
	;

character_length_keyword
	:	{(validateSoftKeyword("character_length") || validateSoftKeyword("char_length"))}?=>  id=IDENTIFIER
		->	CHARACTER_LENGTH[$id]
	;

concat_keyword
	:	{(validateSoftKeyword("concat"))}?=>  id=IDENTIFIER
		->	CONCAT[$id]
	;

current_date_keyword
	:	{(validateSoftKeyword("current_date"))}?=>  id=IDENTIFIER
		->	CURRENT_DATE[$id]
	;

current_time_keyword
	:	{(validateSoftKeyword("current_time"))}?=>  id=IDENTIFIER
		->	CURRENT_TIME[$id]
	;

current_timestamp_keyword
	:	{(validateSoftKeyword("current_timestamp"))}?=>  id=IDENTIFIER
		->	CURRENT_TIMESTAMP[$id]
	;

day_keyword
	:	{(validateSoftKeyword("day"))}?=>  id=IDENTIFIER
		->	DAY[$id]
	;

descending_keyword :
    {(validateSoftKeyword("descending") || validateSoftKeyword("desc"))}?=>  IDENTIFIER
;

extract_keyword
	:	{(validateSoftKeyword("extract"))}?=>  id=IDENTIFIER
		->	EXTRACT[$id]
	;

from_keyword
	:	{(validateSoftKeyword("from"))}?=>  id=IDENTIFIER
		->	FROM[$id]
	;

hour_keyword
	:	{(validateSoftKeyword("hour"))}?=>  id=IDENTIFIER
		->	HOUR[$id]
	;

in_keyword
	:	{(validateSoftKeyword("in"))}?=>  id=IDENTIFIER
		->	IN[$id]
	;
leading_keyword
	:	{(validateSoftKeyword("leading"))}?=>  id=IDENTIFIER
		->	LEADING[$id]
	;

length_keyword
	:	{(validateSoftKeyword("length"))}?=>  id=IDENTIFIER
		->	LENGTH[$id]
	;

locate_keyword
	:	{(validateSoftKeyword("locate"))}?=>  id=IDENTIFIER
		->	LOCATE[$id]
	;

lower_keyword
	:	{(validateSoftKeyword("lower"))}?=>  id=IDENTIFIER
		->	LOWER[$id]
	;

minute_keyword
	:	{(validateSoftKeyword("minute"))}?=>  id=IDENTIFIER
		->	MINUTE[$id]
	;

mod_keyword
	:	{(validateSoftKeyword("mod"))}?=>  id=IDENTIFIER
		->	MOD[$id]
	;

month_keyword
	:	{(validateSoftKeyword("month"))}?=>  id=IDENTIFIER
		->	MONTH[$id]
	;

octet_length_keyword
	:	{(validateSoftKeyword("octet_length"))}?=>  id=IDENTIFIER
		->	OCTET_LENGTH[$id]
	;

position_keyword
	:	{(validateSoftKeyword("position"))}?=>  id=IDENTIFIER
		->	POSITION[$id]
	;

second_keyword
	:	{(validateSoftKeyword("second"))}?=>  id=IDENTIFIER
		->	SECOND[$id]
	;

sqrt_keyword
	:	{(validateSoftKeyword("sqrt"))}?=>  id=IDENTIFIER
		->	SQRT[$id]
	;

substring_keyword
	:	{(validateSoftKeyword("substring"))}?=>  id=IDENTIFIER
		->	SUBSTRING[$id]
	;

timezone_hour_keyword
	:	{(validateSoftKeyword("timezone_hour"))}?=>  id=IDENTIFIER
		->	TIMEZONE_HOUR[$id]
	;

timezone_minute_keyword
	:	{(validateSoftKeyword("timezone_minute"))}?=>  id=IDENTIFIER
		->	TIMEZONE_MINUTE[$id]
	;

trailing_keyword
	:	{(validateSoftKeyword("trailing"))}?=>  id=IDENTIFIER
		->	TRAILING[$id]
	;

trim_keyword
	:	{(validateSoftKeyword("trim"))}?=>  id=IDENTIFIER
		->	TRIM[$id]
	;

upper_keyword
	:	{(validateSoftKeyword("upper"))}?=>  id=IDENTIFIER
		->	UPPER[$id]
	;

year_keyword
	:	{(validateSoftKeyword("year"))}?=>  id=IDENTIFIER
		->	YEAR[$id]
	;

