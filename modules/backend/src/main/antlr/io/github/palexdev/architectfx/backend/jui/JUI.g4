grammar JUI;

// Entry Point
document:
  dependencies?
  imports?
  controller?
  root = uiObj
  EOF
;

// Document metadata
dependencies:
  DEPENDENCIES LBRACE
    STRING (COMMA STRING)*
  RBRACE
;

imports:
  IMPORTS LBRACE
    STRING (COMMA STRING)*
  RBRACE
;

controller:
  CONTROLLER uiObj
;

uiObj:
  IDENTIFIER (DOT IDENTIFIER)* constructor? LBRACE
    factory? cid? (methodsChain | property | uiObj)*
  RBRACE
;

constructor:
  COLON COLON LPAREN args RPAREN
;

factory:
  FACTORY methodsChain
;

/* Controller ID*/
cid:
  CONTROLLER_ID STRING
;

/* Written like this we enfore standard properties with ':' and collections with any of ': = +='*/
property: IDENTIFIER (COLON type | (EQUALS | PLUSEQUALS) collection);

field:
  (THIS | IDENTIFIER) (DOT IDENTIFIER)+
;

method:
  IDENTIFIER LPAREN args RPAREN
;

methodsChain:
   (THIS | (IDENTIFIER (DOT IDENTIFIER)*)) (DOT method)+
;

array:
  (IDENTIFIER (DOT IDENTIFIER)*)? LBRACK args RBRACK
;

collection:
  ctype = COLLECTION_TYPE LPAREN
    args
  RPAREN
;

keywords:
  THIS | NULL | INJECTION
;

type:
    uiObj
  | keywords
  | field
  | methodsChain
  | array
  | collection
  | URL
  | BOOLEAN
  | CHAR
  | STRING
  | INTEGER
  | HEXADECIMAL
  | BINARY
  | OCTAL
  | FLOAT
  | DOUBLE
  | NAN
  | INFINITY
;

args: (type COMMA?)*;

//================================================================================
// LEXER
//================================================================================

// Symbols
EQUALS    : '=';
PLUSEQUALS: '+=';
LPAREN    : '(';
RPAREN    : ')';
LBRACE    : '{';
RBRACE    : '}';
LBRACK    : '[';
RBRACK    : ']';
RARROW    : '->';
COLON     : ':';
SEMI      : ';';
COMMA     : ',';
DOT       : '.';
APOSTROPHE: '\'';

// Comments and Whitespace
COMMENTS: '#' ~[\r\n]* -> channel(HIDDEN); // Ignore comments, ~[\r\n]* matches any sequence of characters excluding newlines
WS: [ \t\r\n\u000C]+ -> channel(HIDDEN); // Ignore whitespaces

// Java Keywords
THIS: 'this';
BOOLEAN: 'true' | 'false';
INFINITY: ('Infinity' | '-Infinity');                                    // Matches positive and negative infinity
NAN: 'NaN';                                                              // Matches NaN
NULL: 'null';
INJECTION: '$'~('\r' | '\n')+'$';

// Types - String and Character Literals
CHAR: '\'' ( ~['\\\r\n] | '\\' . ) '\'';                                 // Single-quoted character literals, including escape sequences.
STRING: ('\'' (~[\r\n'] | '\\' .)* '\'' | '"' (~[\r\n"] | '\\' .)* '"'); // Matches both single-quoted and double-quoted strings with escape sequences.


// Types - Integers
INTEGER: Digit+;                                                         // Matches decimal integers
HEXADECIMAL: '0x' [0-9a-fA-F]+;                                          // Matches hexadecimal integers (e.g., 0x1F)
BINARY: '0b' [01]+;                                                      // Matches binary integers (e.g., 0b1011)
OCTAL: '0' [0-7]+;                                                       // Matches octal integers (e.g., 0755)

// Types - Floating-point
FLOAT: Digit+ '.' Digit+ ([eE] Digit+)? [fF];                      // Explicit floating-point (e.g., 3.14f, 1.23e10f)
DOUBLE: Digit+ '.' Digit+ ([eE] Digit+)?;                          // Regular double (e.g., 3.14)

// Types - Resources
URL: 'url' LPAREN STRING RPAREN;

// Identifiers
IDENTIFIER: Letter LetterOrDigit*;

// Metadata and UILang keywords
DEPENDENCIES: ('.deps' | '.dependencies') ~[()];
IMPORTS: '.imports' ~[()];
CONTROLLER: '.controller' COLON;
CONTROLLER_ID: ('.cid' | '.id') COLON;
FACTORY: ('.factory' | '.builder') COLON;
COLLECTION_TYPE: ~[.] ('listOf' | 'mapOf' | 'setOf');

// Fragments
fragment Letter:
    [a-zA-Z$_]                         // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF]    // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF]; // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF

fragment Digit: [+-]?[0-9];

fragment LetterOrDigit: Letter | Digit;