/**
 * A grammar for describing tests of Antlr grammars. Accepts a sub-set of the
 * constructs supported by the GUnit testing tool.
 */
grammar GrammarTest;

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat, Inc. or third-party contributors as
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
 */
package org.hibernate.hql.testing.internal;
}

/* Parser rules */

grammarTest : 'gunit' ID ';' header (testGroup)+ ;
header : '@header' '{' pakkage '}' ;
pakkage : 'package' PACKAGE_ID ';' ;
testGroup : ID ':' (test)* (testSubGroup)* ;
testSubGroup : TEST_GROUP_NAME (test)+ ;
test : statement outcome ;
statement : (STRING_LITERAL|MULTI_LINE) ;

outcome
	: TEST_RESULT # testResult
	| '->' AST    # expectedAst
	;

/* Lexer rules */

TEST_RESULT : ('OK'|'FAIL') ;
ID : ('a'..'z'|'A'..'Z'|'_')+ ;
PACKAGE_ID : ID('.'ID)* ;
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

TEST_GROUP_NAME : '[' ~(']')* ']' ;
STRING_LITERAL : '"' ( ESC | ~('\\'|'"') )* '"' ;
AST : '(' ( ESC | ~('\\'|'"') )* ')' ;
MULTI_LINE : '<<' .*? '>>' ;
COMMENT
	: ( '//' ~[\r\n]* '\r'? '\n'
	| '/*' .*? '*/'
	) -> skip
	;

/* Fragments */

fragment
ESC : '\\' ( 'n' | 'r' | 't' | 'b' | 'f' | '"' | '\'' | '\\' | '>' | 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT ) ;

fragment
HEX_DIGIT : '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ;
