grammar Policy;

policy: decision EOF;

decision
    : operation operations*;

function: functionName arguments;

conversion: conversionName conversionArguments;

operation: quantifier operations | function | conversion;

conversionArguments: '(' ((function | conversion | operation) (',' argument)*) ')';

arguments: '(' (argument (',' argument)*)* ')';

operations: '(' (operation (',' operation)*)* ')';

argument: variable | constant;

constant: STRING;

variable: OP_CHAR+;

functionName: OP_CHAR+;

conversionName: OP_CHAR+;

quantifier: ALL_OF | ANY_OF | NONE_OF;

ALL_OF  : 'ALL_OF';
ANY_OF  : 'ANY_OF';
NONE_OF : 'NONE_OF';

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

STRING
    	:  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    	|  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    	;

OP_CHAR: ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'.');

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ -> skip;