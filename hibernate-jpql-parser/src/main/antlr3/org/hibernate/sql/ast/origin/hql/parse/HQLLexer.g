lexer grammar HQLLexer;

tokens {
//GENERIC SQL TOKENS
	TABLE;
	COLUMN;
	COLUMN_LIST;

//VIRTUAL TOKENS
	ALIAS_NAME;
	ALIAS_REF;
	BETWEEN_LIST;
	COLLATE;
	COLLECTION_EXPRESSION;
	DOT_CLASS;
	DYNAMIC_INSTANTIATION_ARG;
	DYNAMIC_INSTANTIATION;
	ENTITY_NAME;
	ENTITY_PERSISTER_REF;
	FILTER;
	FUNCTION;
//	GENERAL_FUNCTION_CALL;
//	GENERAL_FUNCTION_ARGUMENTS;
	GROUPING_VALUE;
	IN_LIST;
	INSERTABILITY_SPEC;
	IS_NOT_EMPTY;
	IS_NOT_NULL;
	IS_NULL;
	JAVA_CONSTANT;
	JPA_PARAM;
	NAMED_PARAM;
	NOT_BETWEEN;
	NOT_IN;
	NOT_LIKE;
	NOT_MEMBER_OF;
	ORDER_SPEC;
	PATH;
	PERSISTER_JOIN;
	PERSISTER_SPACE;
	PROP_FETCH;
	PROPERTY_JOIN;
	PROPERTY_REFERENCE;
	QUALIFIED_JOIN;
	QUERY_SPEC;
	QUERY;
	SEARCHED_CASE;
	SELECT_FROM;
	SELECT_ITEM;
	SELECT_LIST;
	SIMPLE_CASE;
	SORT_SPEC;
	SUB_QUERY;
	UNARY_MINUS;
	UNARY_PLUS;
	VECTOR_EXPR;
	VERSIONED_VALUE;
	CONST_STRING_VALUE;

//SOFT KEYWORDS
	ABS;
	ALL;
	AND;
	ANY;
	AS;
	AVG;
	BETWEEN;
	BIT_LENGTH;
	BOTH;
	CAST;
	CHARACTER_LENGTH;
	CLASS;
	COALESCE;
	CONCAT;
	COUNT;
	CROSS;
	CURRENT_DATE;
	CURRENT_TIME;
	CURRENT_TIMESTAMP;
	DAY;
	DELETE;
	DISTINCT;
	ELEMENTS;
	ELSE;
	END;
	ESCAPE;
	EXCEPT;
	EXISTS;
	EXTRACT;
	FETCH;
	FROM;
	FULL;
	GROUP_BY;
	HAVING;
	HOUR;
	IN;
	INDEX;
	INDICES;
	INNER;
	INSERT;
	INTERSECT;
	INTO;
	IS_EMPTY;
	IS;
	JOIN;
	LEADING;
	LEFT;
	LENGTH;
	LIKE;
	LOCATE;
	LOWER;
	MAX;
	MAXELEMENT;
	MAXINDEX;
	MEMBER_OF;
	MIN;
	MINELEMENT;
	MININDEX;
	MINUTE;
	MOD;
	MONTH;
	NEW;
	NOT;
	NULLIF;
	OCTET_LENGTH;
	ON;
	OR;
	ORDER_BY;
	OUTER;
	POSITION;
	PROPERTIES;
	RIGHT;
	SECOND;
	SELECT;
	SET;
	SIZE;
	SOME;
	SQRT;
	SUBSTRING;
	SUM;
	THEN;
	TIMEZONE_HOUR;
	TIMEZONE_MINUTE;
	TRAILING;
	TRIM;
	UNION;
	UPDATE;
	UPPER;
	VERSIONED;
	WHEN;
	WHERE;
	WITH;
	YEAR;
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
package org.hibernate.sql.ast.origin.hql.parse;
}

WS	:	(	' '
	|	'\t'
	|	'\f'
	|	EOL
)+
{ $channel=HIDDEN; }
;

fragment
EOL	:
	(	( '\r\n' )=> '\r\n'	// Evil DOS
		|	'\r'	// Macintosh
		|	'\n'	// Unix (the right way)
	)
	;

HEX_LITERAL : '0' ('x'|'X') HEX_DIGIT+ INTEGER_TYPE_SUFFIX? ;

INTEGER_LITERAL : ('0' | '1'..'9' '0'..'9'*) ;

DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) INTEGER_TYPE_SUFFIX ;

OCTAL_LITERAL : '0' ('0'..'7')+ INTEGER_TYPE_SUFFIX? ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
INTEGER_TYPE_SUFFIX : ('l'|'L') ;

FLOATING_POINT_LITERAL
	:	('0'..'9')+ '.' ('0'..'9')* EXPONENT? FLOAT_TYPE_SUFFIX?
	|	'.' ('0'..'9')+ EXPONENT? FLOAT_TYPE_SUFFIX?
	|	('0'..'9')+ EXPONENT FLOAT_TYPE_SUFFIX?
	|	('0'..'9')+ FLOAT_TYPE_SUFFIX
	;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
FLOAT_TYPE_SUFFIX : ('f'|'F'|'d'|'D') ;

CHARACTER_LITERAL
	:	'\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') ) '\''
	;

STRING_LITERAL
	:	'"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"'
	|	('\'' ( ESCAPE_SEQUENCE | ~('\\'|'\'') )* '\'')+
	;

fragment
ESCAPE_SEQUENCE
	:	'\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
	|	UNICODE_ESCAPE
	|	OCTAL_ESCAPE
	;

fragment
OCTAL_ESCAPE
	:	'\\' ('0'..'3') ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7')
	;

fragment
UNICODE_ESCAPE
	:	'\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

TRUE
	:	'true'
	;	

FALSE
	:	'false'
	;

NULL
	:	'null'
	;

EQUALS
	:	'='
	;

SEMICOLON
	:	';'
	;

COLON
	:	':'
	;

NOT_EQUAL
	:	'!='
	|	'^='
	|	'<>'
	;

PIPE
	:	'|'
	;

DOUBLE_PIPE
	:	'||'
	;

PARAM	:	'?'
	;

GREATER
	:	'>'
	;

GREATER_EQUAL
	:	'>='
	;

LESS
	:	'<'
	;

LESS_EQUAL
	:	'<='
	;

ARROW
	:	'->'
	;

IDENTIFIER
	:	('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe')('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9'|'\u0080'..'\ufffe')*
	;

QUOTED_IDENTIFIER
	: '`' ( ESCAPE_SEQUENCE | ~('\\'|'`') )* '`'
	;

LEFT_PAREN
	:	'('
	;

RIGHT_PAREN
	:	')'
	;

LEFT_SQUARE
	:	'['
	;

RIGHT_SQUARE
	:	']'
	;

COMMA	:	','
	;
	
DOT	:	'.'
	;

PLUS	:	'+'
	;

MINUS	:	'-'
	;

ASTERISK
	:	'*'
	;

SOLIDUS	:	'/'
	;

PERCENT	:	'%'
	;

AMPERSAND
	:	'&'
	;