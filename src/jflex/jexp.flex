package ranttu.rapid.jexp.compile.jflex;

import ranttu.rapid.jexp.compile.parse.Token;
import ranttu.rapid.jexp.compile.parse.TokenType;

/**
 * ### WARNING ###
 * this file is generated by src/jflex/jexp.flex, don't modify this directly
 *
 * the lexer generated by jflex
 * @author rapidhere@gmail.com
 */
@SuppressWarnings("all")
%%

%public
%class Lexer
%type Token
%unicode
%line
%column

%{
  private StringBuffer string = new StringBuffer();

  private Token token(TokenType type, Object val) {
      return new Token(type, yyline, yycolumn, val);
  }
%}

InputCharacter = [^\r\n]
WhiteSpace     = [ \t\f]

DecIntegerLiteral = 0 | [1-9][0-9]*
HexIntegerLiteral = 0[xX][0-9]+
OctIntegerLiteral = 0[0-9]+

%state STRING_DOUBLE_QUOTE
%state STRING_SINGLE_QUOTE

%%

<YYINITIAL> {
  /* operators */
  "+"                            { return token(TokenType.PLUS, yytext()); }
  "-"                            { return token(TokenType.SUBTRACT, yytext()); }
  "*"                            { return token(TokenType.MULTIPLY, yytext()); }
  "/"                            { return token(TokenType.DIVIDE, yytext()); }
  "%"                            { return token(TokenType.MODULAR, yytext()); }
  "("                            { return token(TokenType.LEFT_PARENTHESIS, yytext()); }
  "||"                           { return token(TokenType.OR, yytext()); }
  "or"                           { return token(TokenType.OR, yytext()); }
  "&&"                           { return token(TokenType.AND, yytext()); }
  "and"                          { return token(TokenType.AND, yytext()); }
  ")"                            { return token(TokenType.RIGHT_PARENTHESIS, yytext()); }
  ","                            { return token(TokenType.COMMA, yytext()); }
  "."                            { return token(TokenType.DOT, yytext()); }
  "["                            { return token(TokenType.LEFT_BRACKET, yytext()); }
  "]"                            { return token(TokenType.RIGHT_BRACKET, yytext()); }
  "=>"                           { return token(TokenType.POINTER, yytext()); }
  "{"                            { return token(TokenType.LEFT_BRACE, yytext()); }
  "}"                            { return token(TokenType.RIGHT_BRACE, yytext()); }
  "from"                         { return token(TokenType.FROM, yytext()); }
  "in"                           { return token(TokenType.IN, yytext()); }
  "select"                       { return token(TokenType.SELECT, yytext()); }
  "let"                          { return token(TokenType.LET, yytext()); }
  "="                            { return token(TokenType.EQ, yytext()); }
  "where"                        { return token(TokenType.WHERE, yytext()); }
  "orderby"                      { return token(TokenType.ORDERBY, yytext()); }
  "ascending"                    { return token(TokenType.ASCENDING, yytext()); }
  "descending"                   { return token(TokenType.DESCENDING, yytext()); }
  "join"                         { return token(TokenType.JOIN, yytext()); }
  "on"                           { return token(TokenType.ON, yytext()); }
  "equals"                       { return token(TokenType.EQUALS, yytext()); }
  "=="                           { return token(TokenType.EQEQ, yytext()); }
  "!="                           { return token(TokenType.NOT_EQ, yytext()); }
  ">"                            { return token(TokenType.GREATER, yytext()); }
  ">="                           { return token(TokenType.GREATER_EQ, yytext()); }
  "<"                            { return token(TokenType.SMALLER, yytext()); }
  "<="                           { return token(TokenType.SMALLER_EQ, yytext()); }
  "!"                            { return token(TokenType.NOT, yytext()); }

  /* identifiers */
  [a-zA-Z\_][a-zA-Z0-9\_]*       { return token(TokenType.IDENTIFIER, yytext()); }

  /* literals */
  (0 | [1-9][0-9]*)\.[0-9]+      { return token(TokenType.FLOAT, Double.valueOf(yytext())); }
  {DecIntegerLiteral}            { return token(TokenType.INTEGER, Integer.valueOf(yytext())); }
  {HexIntegerLiteral}            { return token(TokenType.INTEGER, Integer.parseInt(yytext().substring(2), 16)); }
  {OctIntegerLiteral}            { return token(TokenType.INTEGER, Integer.parseInt(yytext(), 8)); }

  \"                             { string.setLength(0); yybegin(STRING_DOUBLE_QUOTE); }
  \'                             { string.setLength(0); yybegin(STRING_SINGLE_QUOTE); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<STRING_DOUBLE_QUOTE> {
  \"                             {
                                   yybegin(YYINITIAL);
                                   return token(TokenType.STRING, string.toString());
                                 }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}

<STRING_SINGLE_QUOTE> {
  \'                             {
                                   yybegin(YYINITIAL);
                                   return token(TokenType.STRING, string.toString());
                                 }
  [^\n\r\'\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\\'                           { string.append('\''); }
  \\                             { string.append('\\'); }
}

/* error fallback */
[^]                              { throw new Error("unexpected character at line "
                                            + yyline + ", column " + yycolumn + ": " + yytext());
                                 }