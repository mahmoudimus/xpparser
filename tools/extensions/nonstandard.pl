#!/usr/bin/perl

die "Usage: $0 <XML files>\n" unless @ARGV;
@files = @ARGV;

@functions = (
  "abs",
  "acos",
  "add-dayTimeDurations",
  "add-dayTimeDuration-to-date",
  "add-dayTimeDuration-to-dateTime",
  "add-dayTimeDuration-to-time",
  "add-yearMonthDurations",
  "add-yearMonthDuration-to-date",
  "add-yearMonthDuration-to-dateTime",
  "adjust-dateTime-to-timezone",
  "adjust-date-to-timezone",
  "adjust-time-to-timezone",
  "analyze-string",
  "asin",
  "atan",
  "atan2",
  "available-environment-variables",
  "avg",
  "base64Binary-equal",
  "base-uri",
  "boolean",
  "boolean-equal",
  "boolean-greater-than",
  "boolean-less-than",
  "ceiling",
  "codepoint-equal",
  "codepoints-to-string",
  "collection",
  "compare",
  "concat",
  "concatenate",
  "contains",
  "cos",
  "count",
  "current-date",
  "current-dateTime",
  "current-time",
  "data",
  "date-equal",
  "date-greater-than",
  "date-less-than",
  "dateTime",
  "dateTime-equal",
  "dateTime-greater-than",
  "dateTime-less-than",
  "day-from-date",
  "day-from-dateTime",
  "days-from-duration",
  "dayTimeDuration-greater-than",
  "dayTimeDuration-less-than",
  "deep-equal",
  "default-collation",
  "distinct-values",
  "divide-dayTimeDuration",
  "divide-dayTimeDuration-by-dayTimeDuration",
  "divide-yearMonthDuration",
  "divide-yearMonthDuration-by-yearMonthDuration",
  "doc",
  "doc-available",
  "document-uri",
  "duration-equal",
  "element-with-id",
  "empty",
  "encode-for-uri",
  "ends-with",
  "environment-variable",
  "error",
  "escape-html-uri",
  "exactly-one",
  "except",
  "exists",
  "exp",
  "exp10",
  "false",
  "filter",
  "floor",
  "fold-left",
  "fold-right",
  "for-each",
  "for-each-pair",
  "format-date",
  "format-dateTime",
  "format-integer",
  "format-number",
  "format-time",
  "function-arity",
  "function-lookup",
  "function-name",
  "gDay-equal",
  "generate-id",
  "gMonthDay-equal",
  "gMonth-equal",
  "gYear-equal",
  "gYearMonth-equal",
  "has-children",
  "head",
  "hexBinary-equal",
  "hours-from-dateTime",
  "hours-from-duration",
  "hours-from-time",
  "id",
  "idref",
  "implicit-timezone",
  "index-of",
  "innermost",
  "in-scope-prefixes",
  "insert-before",
  "intersect",
  "iri-to-uri",
  "is-same-node",
  "lang",
  "last",
  "local-name",
  "local-name-from-QName",
  "log",
  "log10",
  "lower-case",
  "matches",
  "max",
  "min",
  "minutes-from-dateTime",
  "minutes-from-duration",
  "minutes-from-time",
  "month-from-date",
  "month-from-dateTime",
  "months-from-duration",
  "multiply-dayTimeDuration",
  "multiply-yearMonthDuration",
  "name",
  "namespace-uri",
  "namespace-uri-for-prefix",
  "namespace-uri-from-QName",
  "nilled",
  "node-after",
  "node-before",
  "node-name",
  "normalize-space",
  "normalize-unicode",
  "not",
  "NOTATION-equal",
  "number",
  "numeric-add",
  "numeric-divide",
  "numeric-equal",
  "numeric-greater-than",
  "numeric-integer-divide",
  "numeric-less-than",
  "numeric-mod",
  "numeric-multiply",
  "numeric-subtract",
  "numeric-unary-minus",
  "numeric-unary-plus",
  "one-or-more",
  "outermost",
  "parse-xml",
  "parse-xml-fragment",
  "path",
  "pi",
  "position",
  "pow",
  "prefix-from-QName",
  "QName",
  "QName-equal",
  "remove",
  "replace",
  "resolve-QName",
  "resolve-uri",
  "reverse",
  "root",
  "round",
  "round-half-to-even",
  "seconds-from-dateTime",
  "seconds-from-duration",
  "seconds-from-time",
  "serialize",
  "sin",
  "sqrt",
  "starts-with",
  "static-base-uri",
  "string",
  "string-join",
  "string-length",
  "string-to-codepoints",
  "subsequence",
  "substring",
  "substring-after",
  "substring-before",
  "subtract-dates",
  "subtract-dateTimes",
  "subtract-dayTimeDuration-from-date",
  "subtract-dayTimeDuration-from-dateTime",
  "subtract-dayTimeDuration-from-time",
  "subtract-dayTimeDurations",
  "subtract-times",
  "subtract-yearMonthDuration-from-date",
  "subtract-yearMonthDuration-from-dateTime",
  "subtract-yearMonthDurations",
  "sum",
  "tail",
  "tan",
  "time-equal",
  "time-greater-than",
  "time-less-than",
  "timezone-from-date",
  "timezone-from-dateTime",
  "timezone-from-time",
  "to",
  "tokenize",
  "trace",
  "translate",
  "true",
  "union",
  "unordered",
  "unparsed-text",
  "unparsed-text-available",
  "unparsed-text-lines",
  "upper-case",
  "uri-collection",
  "year-from-date",
  "year-from-dateTime",
  "yearMonthDuration-greater-than",
  "yearMonthDuration-less-than",
  "years-from-duration",
  "zero-or-one"
);

$query = "";
foreach (@functions) {
  $query = "$query and text() != '$_'";
}

$nonstandard = "ast/descendant::*[local-name()='functionName' $query]";
$inextras = 'schemas/validation[@schema=\'xpath-1.0-core-extra.rnc\' or @schema=\'xpath-efo-extra.rnc\'][@valid=\'yes\']';

$total=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -t -v \"count(//xpath[schemas])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $total+=$_;
  }
  close STARLET;
}
print "Counting only validated queries...\n";
print "$total queries in total\n";

$nonstd=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -t -v \"count(//xpath[schemas][$nonstandard])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $nonstd+=$_;
  }
  close STARLET;
}
print "$nonstd queries with non-standard functions\n";

$extras=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -t -v \"count(//xpath[schemas][$inextras])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $extras+=$_;
  }
  close STARLET;
}
print "$extras queries captured in extra fragments\n";

$remaining = $total-$extras-$nonstandard;
print "$remaining remaining queries:\n\n";
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -t -v \"//xpath[schemas][not($nonstandard) and not($inextras)]/query\" -n $file |");
  while(<STARLET>) {
    print " * $_\n";
  }
  close STARLET;
}
