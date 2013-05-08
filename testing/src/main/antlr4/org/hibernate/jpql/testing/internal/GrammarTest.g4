/**
 * A grammar for describing tests of Antlr grammars. Accepts a sub-set of the
 * constructs supported by the GUnit testing tool.
 */
grammar GrammarTest;

/* Parser rules */

grammarTest : 'gunit' ID ';' header (testGroup)+ ;
header : '@header' '{' pakkage '}' ;
pakkage : 'package' PACKAGE_ID ';' ;
testGroup : ID ':' (test)+ ;
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
