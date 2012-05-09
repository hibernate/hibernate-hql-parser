tree grammar OrderByRenderer;

options{
	output=template;
	tokenVocab=HQLLexer;
	ASTLabelType=CommonTree;
	TokenLabelType=CommonToken;
}

@header {
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
package org.hibernate.sql.ast.render;

import org.antlr.stringtemplate.StringTemplate;
}

@members {
    protected StringTemplate quotedIdentifier(CommonToken recognizedIdentifierToken) {
        return new StringTemplate( getTemplateLib(), recognizedIdentifierToken.getText() );
    }

    protected StringTemplate basicFunctionTemplate(String functionName, List arguments) {
        return new StringTemplate(
                getTemplateLib(),
                "<name>(<arguments; separator=\", \">)",
                new STAttrMap().put( "name", functionName ).put( "arguments", arguments )
        );
    }

    protected StringTemplate castFunctionTemplate(StringTemplate expression, String  datatype) {
        return new StringTemplate(
                getTemplateLib(),
                "cast(<expression> as <datatype>)",
                new STAttrMap().put( "expression", expression )
                        .put( "datatype", datatype )
        );
    }

    protected StringTemplate trimFunctionTemplate(StringTemplate trimSpec, StringTemplate trimCharacter, StringTemplate trimSource) {
        return new StringTemplate(
                getTemplateLib(),
                "trim(<trimSpec> <trimCharacter> from <trimSource>)",
                new STAttrMap().put( "trimSpec", trimSpec )
                        .put( "trimCharacter", trimCharacter )
                        .put( "trimSource", trimSource )
        );
    }

    protected StringTemplate extractFunctionTemplate(StringTemplate extractField, StringTemplate expression) {
        return new StringTemplate(
                getTemplateLib(),
                "extract(<extractField> from <expression>)",
                new STAttrMap().put( "extractField", extractField )
                        .put( "expression", expression )
        );
    }

    protected StringTemplate positionFunctionTemplate(StringTemplate searchString, StringTemplate sourceString) {
        return new StringTemplate(
                getTemplateLib(),
                "position(<searchString> in <sourceString>)",
                new STAttrMap().put( "searchString", searchString )
                        .put( "sourceString", sourceString )
        );
    }
}

// todo : merge with 'full sql rendering' grammar
//    this is currently just a temporary subset grammar limited to the needs of mapping-defined order-by fragments

orderByFragment
    : ^( ORDER_BY sortSpecs+=sortSpecification+ )
        ->  orderByFragment( sortSpecifications={$sortSpecs} )
    ;

sortSpecification
    : ^( SORT_SPEC sortKey collationSpecification? ORDER_SPEC? )
        ->  sortSpecification(
                    sortKey={$sortKey.st},
                    collationSpecification={$collationSpecification.st},
                    orderingSpecification={$ORDER_SPEC.text}
            )
    ;

collationSpecification
    : COLLATE -> {%{"collate " + $COLLATE.text}}
    ;

sortKey
    : expression -> {$expression.st}
    ;

expression
    : valueExpression -> {$valueExpression.st}
    ;

valueExpression
    : column -> {$column.st}
    | function -> {$function.st}
    | literal -> {$literal.st}
    | rowValueConstructor -> {$rowValueConstructor.st}
    ;

characterValueExpression
    : valueExpression -> {$valueExpression.st}
    ;

numericValueExpression
    : valueExpression -> {$valueExpression.st}
    ;

column
    : ^( COLUMN ALIAS_REF identifier )
        -> column( qualifier={$ALIAS_REF.text}, name={$identifier.st} )
    ;

identifier
    : IDENTIFIER -> {%{$IDENTIFIER.text}}
    | QUOTED_IDENTIFIER -> { quotedIdentifier( (CommonToken) $QUOTED_IDENTIFIER.getToken() ) }
    ;

rowValueConstructor
    : ^( VECTOR_EXPR expressions+=expression+ )
        -> template( expressions={$expressions} ) "(<expressions; separator=\", \">)"
    ;

function
    : functionFunction -> {$functionFunction.st}
	| castFunction -> {$castFunction.st}
	| concatFunction -> {$concatFunction.st}
	| substringFunction -> {$substringFunction.st}
	| trimFunction -> {$trimFunction.st}
	| upperFunction -> {$upperFunction.st}
	| lowerFunction -> {$lowerFunction.st}
	| lengthFunction -> {$lengthFunction.st}
	| locateFunction -> {$locateFunction.st}
	| absFunction -> {$absFunction.st}
	| sqrtFunction -> {$sqrtFunction.st}
	| modFunction -> {$modFunction.st}
	| currentDateFunction -> {$currentDateFunction.st}
	| currentTimeFunction -> {$currentTimeFunction.st}
	| currentTimestampFunction -> {$currentTimestampFunction.st}
	| extractFunction -> {$extractFunction.st}
	| positionFunction -> {$positionFunction.st}
	| charLengthFunction -> {$charLengthFunction.st}
	| octetLengthFunction -> {$octetLengthFunction.st}
	| bitLengthFunction -> {$bitLengthFunction.st}
    ;

functionFunction
    : ^( FUNCTION args+=functionArgument* )
        -> { basicFunctionTemplate( $FUNCTION.text, $args ) }
    ;

functionArgument
    : expression -> {$expression.st}
;

castFunction
    : ^( CAST valueExpression IDENTIFIER )
        -> { castFunctionTemplate( $valueExpression.st, $IDENTIFIER.text ) }
    ;

concatFunction
	: ^( CONCAT args+=valueExpression+ )
        -> { basicFunctionTemplate( $CONCAT.text, $args ) }
	;

substringFunction
    : ^( SUBSTRING args+=characterValueExpression args+=numericValueExpression args+=numericValueExpression? )
        -> { basicFunctionTemplate( $SUBSTRING.text, $args ) }
    ;

trimFunction
    : ^( TRIM ^( trimSpec trimChar trimSource ) )
        -> { trimFunctionTemplate( $trimSpec.st, $trimChar.st, $trimSource.st ) }
    ;

trimSpec
    : LEADING   -> {%{"leading"}}
    | TRAILING  -> {%{"trailing"}}
    | BOTH      -> {%{"both"}}
    ;

trimChar
    : characterValueExpression -> { $characterValueExpression.st }
    ;

trimSource
    : characterValueExpression -> { $characterValueExpression.st }
    ;

upperFunction
    : ^( UPPER args+=characterValueExpression )
        -> { basicFunctionTemplate( $UPPER.text, $args ) }
    ;

lowerFunction
    : ^( LOWER args+=characterValueExpression )
        -> { basicFunctionTemplate( $LOWER.text, $args ) }
    ;

lengthFunction
    : ^( LENGTH args+=characterValueExpression )
        -> { basicFunctionTemplate( $LENGTH.text, $args ) }
    ;

locateFunction
    : ^( LOCATE args+=characterValueExpression args+=characterValueExpression args+=numericValueExpression? )
        -> { basicFunctionTemplate( $LOCATE.text, $args ) }
    ;

absFunction
    : ^( ABS args+=expression )
        -> { basicFunctionTemplate( $ABS.text, $args ) }
    ;

sqrtFunction
    : ^( SQRT args+=expression )
        -> { basicFunctionTemplate( $SQRT.text, $args ) }
    ;

modFunction
    : ^( MOD args+=expression args+=expression )
        -> { basicFunctionTemplate( $MOD.text, $args ) }
    ;

currentDateFunction
    : CURRENT_DATE -> { basicFunctionTemplate( $CURRENT_DATE.text, null ) }
    ;

currentTimeFunction
    : CURRENT_TIME -> { basicFunctionTemplate( $CURRENT_TIME.text, null ) }
    ;

currentTimestampFunction
    : CURRENT_TIMESTAMP -> { basicFunctionTemplate( $CURRENT_TIMESTAMP.text, null ) }
    ;

extractFunction
    : ^( EXTRACT extractField expression )
        -> { extractFunctionTemplate( $extractField.st, $expression.st ) }
    ;

extractField
	:	datetimeField
	|	timeZoneField
	;

datetimeField
	:	YEAR    -> {%{"year"}}
	|	MONTH   -> {%{"month"}}
	|	DAY     -> {%{"day"}}
	|	HOUR    -> {%{"hour"}}
	|	MINUTE  -> {%{"minute"}}
	|	SECOND  -> {%{"second"}}
	;

timeZoneField
	:	TIMEZONE_HOUR   -> {%{"timezone_hour"}}
	|	TIMEZONE_MINUTE -> {%{"timezone_minute"}}
	;

positionFunction
    : ^( POSITION s1=expression s2=expression )
        -> { positionFunctionTemplate( $s1.st, $s2.st ) }
    ;

charLengthFunction
    : ^( CHARACTER_LENGTH args+=expression )
         -> { basicFunctionTemplate( $CHARACTER_LENGTH.text, $args ) }
    ;

octetLengthFunction
    : ^( OCTET_LENGTH args+=expression )
         -> { basicFunctionTemplate( $OCTET_LENGTH.text, $args ) }
    ;

bitLengthFunction
    : ^( BIT_LENGTH args+=expression )
         -> { basicFunctionTemplate( $BIT_LENGTH.text, $args ) }
    ;

literal
	:	numeric_literal -> {$numeric_literal.st}
	|	HEX_LITERAL -> {%{$HEX_LITERAL.text}}
	|	OCTAL_LITERAL -> {%{$OCTAL_LITERAL.text}}
	|	CHARACTER_LITERAL -> {%{$CHARACTER_LITERAL.text}}
	|	STRING_LITERAL -> {%{$STRING_LITERAL.text}}
	;

numeric_literal
	:	INTEGER_LITERAL -> {%{$INTEGER_LITERAL.text}}
	|	DECIMAL_LITERAL -> {%{$DECIMAL_LITERAL.text}}
	|	FLOATING_POINT_LITERAL -> {%{$FLOATING_POINT_LITERAL.text}}
	;

