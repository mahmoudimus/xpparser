/* Generated By:JJTree&JavaCC: Do not edit this line. XParserConstants.java */
package fr.lsv.xpparser;

public interface XParserConstants {

  int EOF = 0;
  int DirCommentContentDoubleDashError = 148;
  int RbraceError = 149;
  int LeftAngleBracketError = 150;
  int AmpersandError = 151;
  int PITargetError = 152;
  int NumericLiteralError = 153;
  int Slash = 154;
  int SlashSlash = 155;
  int Greatest = 156;
  int Least = 157;
  int External = 158;
  int Lbrace = 159;
  int Rbrace = 160;
  int Ascending = 161;
  int Descending = 162;
  int LeftAngleBracket = 163;
  int Plus = 164;
  int Minus = 165;
  int PragmaOpen = 166;
  int PragmaClose = 167;
  int URIQualifiedStar = 168;
  int NCNameColonStar = 169;
  int StarColonNCName = 170;
  int TagQName = 171;
  int StartTagClose = 172;
  int EmptyTagClose = 173;
  int EndTagOpen = 174;
  int EndTagQName = 175;
  int EndTagClose = 176;
  int ValueIndicator = 177;
  int OpenQuot = 178;
  int CloseQuot = 179;
  int OpenApos = 180;
  int CloseApos = 181;
  int LCurlyBraceEscape = 182;
  int RCurlyBraceEscape = 183;
  int DirCommentStart = 184;
  int DirCommentEnd = 185;
  int DirCommentContentChar = 186;
  int DirCommentContentDashChar = 187;
  int ProcessingInstructionStart = 188;
  int ProcessingInstructionEnd = 189;
  int CdataSectionStart = 190;
  int CdataSectionEnd = 191;
  int IntegerLiteral = 192;
  int DecimalLiteral = 193;
  int DoubleLiteral = 194;
  int StringLiteral = 195;
  int URIQualifiedName = 196;
  int BracedURILiteral = 197;
  int PredefinedEntityRef = 198;
  int EscapeQuot = 199;
  int EscapeApos = 200;
  int ElementContentChar = 201;
  int QuotAttrContentChar = 202;
  int AposAttrContentChar = 203;
  int PITarget = 204;
  int CharRef = 205;
  int QNameToken = 206;
  int NCNameTok = 207;
  int S = 208;
  int Char = 209;
  int Digits = 210;
  int HexDigits = 211;
  int WhitespaceChar = 212;
  int LocalPart = 213;
  int Nmstart = 214;
  int Nmchar = 215;
  int Letter = 216;
  int BaseChar = 217;
  int Ideographic = 218;
  int CombiningChar = 219;
  int Digit = 220;
  int Extender = 221;
  int CommentStart = 222;
  int CommentEnd = 223;
  int CommentContent = 224;
  int ImplicitlyAllowedWhitespace = 225;

  int DEFAULT = 0;
  int XML_COMMENT = 1;
  int QUOT_ATTRIBUTE_CONTENT = 2;
  int APOS_ATTRIBUTE_CONTENT = 3;
  int ELEMENT_CONTENT = 4;
  int PROCESSING_INSTRUCTION = 5;
  int PRAGMA_2 = 6;
  int PRAGMA_3 = 7;
  int START_TAG = 8;
  int END_TAG = 9;
  int PROCESSING_INSTRUCTION_CONTENT = 10;
  int CDATA_SECTION = 11;
  int PRAGMA_1 = 12;
  int EXPR_COMMENT = 13;

  String[] tokenImage = {
    "<EOF>",
    "\"%%%\"",
    "\"xquery\"",
    "\"encoding\"",
    "\"version\"",
    "\"module\"",
    "\"namespace\"",
    "\"=\"",
    "\";\"",
    "\"declare\"",
    "\"boundary-space\"",
    "\"preserve\"",
    "\"strip\"",
    "\"default\"",
    "\"collation\"",
    "\"base-uri\"",
    "\"construction\"",
    "\"ordering\"",
    "\"ordered\"",
    "\"unordered\"",
    "\"order\"",
    "\"empty\"",
    "\"copy-namespaces\"",
    "\",\"",
    "\"no-preserve\"",
    "\"inherit\"",
    "\"no-inherit\"",
    "\"decimal-format\"",
    "\"decimal-separator\"",
    "\"grouping-separator\"",
    "\"infinity\"",
    "\"minus-sign\"",
    "\"NaN\"",
    "\"percent\"",
    "\"per-mille\"",
    "\"zero-digit\"",
    "\"digit\"",
    "\"pattern-separator\"",
    "\"import\"",
    "\"schema\"",
    "\"at\"",
    "\"element\"",
    "\"function\"",
    "\"%\"",
    "\"(\"",
    "\")\"",
    "\"variable\"",
    "\"$\"",
    "\":=\"",
    "\"context\"",
    "\"item\"",
    "\"as\"",
    "\"option\"",
    "\"for\"",
    "\"in\"",
    "\"allowing\"",
    "\"let\"",
    "\"tumbling\"",
    "\"window\"",
    "\"sliding\"",
    "\"start\"",
    "\"when\"",
    "\"only\"",
    "\"end\"",
    "\"previous\"",
    "\"next\"",
    "\"count\"",
    "\"where\"",
    "\"group\"",
    "\"by\"",
    "\"stable\"",
    "\"return\"",
    "\"some\"",
    "\"every\"",
    "\"satisfies\"",
    "\"switch\"",
    "\"case\"",
    "\"typeswitch\"",
    "\"|\"",
    "\"if\"",
    "\"then\"",
    "\"else\"",
    "\"try\"",
    "\"catch\"",
    "\"or\"",
    "\"and\"",
    "\"||\"",
    "\"to\"",
    "\"*\"",
    "\"div\"",
    "\"idiv\"",
    "\"mod\"",
    "\"union\"",
    "\"intersect\"",
    "\"except\"",
    "\"instance\"",
    "\"of\"",
    "\"treat\"",
    "\"castable\"",
    "\"cast\"",
    "\"!=\"",
    "\"<=\"",
    "\">\"",
    "\">=\"",
    "\"eq\"",
    "\"ne\"",
    "\"lt\"",
    "\"le\"",
    "\"gt\"",
    "\"ge\"",
    "\"is\"",
    "\"<<\"",
    "\">>\"",
    "\"validate\"",
    "\"type\"",
    "\"lax\"",
    "\"strict\"",
    "\"!\"",
    "\"child\"",
    "\"::\"",
    "\"descendant\"",
    "\"attribute\"",
    "\"self\"",
    "\"descendant-or-self\"",
    "\"following-sibling\"",
    "\"following\"",
    "\"@\"",
    "\"parent\"",
    "\"ancestor\"",
    "\"preceding-sibling\"",
    "\"preceding\"",
    "\"ancestor-or-self\"",
    "\"..\"",
    "\"[\"",
    "\"]\"",
    "\".\"",
    "\"?\"",
    "\"document\"",
    "\"text\"",
    "\"comment\"",
    "\"processing-instruction\"",
    "\"#\"",
    "\"empty-sequence\"",
    "\"node\"",
    "\"document-node\"",
    "\"namespace-node\"",
    "\"schema-attribute\"",
    "\"schema-element\"",
    "<DirCommentContentDoubleDashError>",
    "\"}\"",
    "\"<\"",
    "\"&\"",
    "<PITargetError>",
    "<NumericLiteralError>",
    "\"/\"",
    "\"//\"",
    "\"greatest\"",
    "\"least\"",
    "\"external\"",
    "\"{\"",
    "\"}\"",
    "\"ascending\"",
    "\"descending\"",
    "\"<\"",
    "\"+\"",
    "\"-\"",
    "\"(#\"",
    "\"#)\"",
    "<URIQualifiedStar>",
    "<NCNameColonStar>",
    "<StarColonNCName>",
    "<TagQName>",
    "\">\"",
    "\"/>\"",
    "\"</\"",
    "<EndTagQName>",
    "\">\"",
    "\"=\"",
    "\"\\\"\"",
    "\"\\\"\"",
    "\"\\\'\"",
    "\"\\\'\"",
    "\"{{\"",
    "\"}}\"",
    "\"<!--\"",
    "\"-->\"",
    "<DirCommentContentChar>",
    "<DirCommentContentDashChar>",
    "\"<?\"",
    "\"?>\"",
    "\"<![CDATA[\"",
    "<CdataSectionEnd>",
    "<IntegerLiteral>",
    "<DecimalLiteral>",
    "<DoubleLiteral>",
    "<StringLiteral>",
    "<URIQualifiedName>",
    "<BracedURILiteral>",
    "<PredefinedEntityRef>",
    "\"\\\"\\\"\"",
    "\"\\\'\\\'\"",
    "<ElementContentChar>",
    "<QuotAttrContentChar>",
    "<AposAttrContentChar>",
    "<PITarget>",
    "<CharRef>",
    "<QNameToken>",
    "<NCNameTok>",
    "<S>",
    "<Char>",
    "<Digits>",
    "<HexDigits>",
    "<WhitespaceChar>",
    "<LocalPart>",
    "<Nmstart>",
    "<Nmchar>",
    "<Letter>",
    "<BaseChar>",
    "<Ideographic>",
    "<CombiningChar>",
    "<Digit>",
    "<Extender>",
    "\"(:\"",
    "\":)\"",
    "<CommentContent>",
    "<ImplicitlyAllowedWhitespace>",
  };

}
