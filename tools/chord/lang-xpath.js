/**
 * Based on `lang-xq.js' Copyright (C) 2011 Patrick Wied
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * @fileoverview
 * Registers a language handler for XQuery.
 *
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 *      <pre class="prettyprint lang-xq"></pre>
 *
 *
 * @author Patrick Wied ( patpa7p@live.de )
 * @version 2010-09-28
 */

(function () {
// Falls back to plain for stylesheets that don't style fun.
var PR_FUNCTION = 'fun pln';
// Falls back to plaiin for stylesheets that don't style var.
var PR_VARIABLE = 'var pln';

PR['registerLangHandler'](
    PR['createSimpleLexer'](
        [
         // Matching $var-ia_bles
         [PR_VARIABLE, /^\$[A-Za-z0-9_\-]+/, null, "$"]
        ],
        [
         // Matching lt and gt operators
         // Not the best matching solution but you have to differentiate between the gt operator and the tag closing char
         [PR['PR_PLAIN'], /^[\s=][<>][\s=]/],
         // Matching @Attributes
         [PR['PR_LITERAL'], /^[0-9\.]+/],
         // Matching xml tags
         [PR['PR_TAG'], /^<\/?[a-z](?:[\w.:-]*\w)?|\/?>$/i],
         // Tokenizing /{}=;*,[]() as plain
         [PR['PR_PLAIN'], /^[\/\{\};,\[\]\(\)\:]$/],
         // Matching a double or single quoted, possibly multi-line, string.
         // with the special condition that a { in a string changes to xquery context 
         [PR['PR_STRING'], /^(?:\"(?:[^\"\\\{]|\\[\s\S])*(?:\"|$)|\'(?:[^\'\\\{]|\\[\s\S])*(?:\'|$))/, null, '"\''],
         // Matching standard xpath keywords
         [PR['PR_KEYWORD'], /^(?:ancestor::|ancestor-or-self::|attribute::|child::|descendant::|following::|following-sibling::|namespace::|parent::|preceding::|preceding-sibling::|self::)|(?:any|and|as|cast|castable|else|eq|every|for|ge|gt|idiv|in|instance|intersect|is|le|let|lt|satisfies|ne|of|or|some|then|to|treat|union)\b/],
         // Matching functions
         [PR_FUNCTION, /^(?:(?:fn|math):[a-zA-Z\-]+|zero-or-one|years-from-duration|year-from-dateTime|year-from-date|upper-case|unordered|true|translate|trace|tokenize|timezone-from-time|timezone-from-dateTime|timezone-from-date|sum|subtract-dateTimes-yielding-yearMonthDuration|subtract-dateTimes-yielding-dayTimeDuration|substring-before|substring-after|substring|subsequence|string-to-codepoints|string-pad|string-length|string-join|string|static-base-uri|starts-with|seconds-from-time|seconds-from-duration|seconds-from-dateTime|round-half-to-even|round|reverse|resolve-uri|resolve-QName|replace|remove|QName|prefix-from-QName|position|one-or-more|number|not|normalize-unicode|normalize-space|node-name|node-kind|nilled|namespace-uri-from-QName|namespace-uri-for-prefix|namespace-uri|months-from-duration|month-from-dateTime|month-from-date|minutes-from-time|minutes-from-duration|minutes-from-dateTime|min|max|matches|lower-case|local-name-from-QName|local-name|last|lang|iri-to-uri|insert-before|index-of|in-scope-prefixes|implicit-timezone|idref|id|hours-from-time|hours-from-duration|hours-from-dateTime|floor|false|expanded-QName|exists|exactly-one|escape-uri|escape-html-uri|error|ends-with|encode-for-uri|document-uri|doc-available|doc|distinct-values|distinct-nodes|default-collation|deep-equal|days-from-duration|day-from-dateTime|day-from-date|data|current-time|current-dateTime|current-date|count|contains|concat|compare|collection|codepoints-to-string|codepoint-equal|ceiling|boolean|base-uri|avg|adjust-time-to-timezone|adjust-dateTime-to-timezone|adjust-date-to-timezone|abs|format-number)\b/],
         // Matching normal words if none of the previous regular expressions matched
         [PR['PR_PLAIN'], /^[A-Za-z_][A-Za-z0-9_-]+/],
         // Matching whitespaces
         [PR['PR_PLAIN'], /^[\t\n\r \xA0]+/]
         ]),
    ['xpath']);
})();
