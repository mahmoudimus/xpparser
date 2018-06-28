#!/usr/bin/perl

require "./XPathStd.pm";

die "Usage: $0 <XML files>\n" unless @ARGV;
@files = @ARGV;

@unsupported = (
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
  "boolean-equal",
  "boolean-greater-than",
  "boolean-less-than",
  "ceiling",
  "codepoint-equal",
  "codepoints-to-string",
  "collection",
  "compare",
  "concatenate",
  "cos",
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
  "environment-variable",
  "error",
  "escape-html-uri",
  "exactly-one",
  "except",
  "exists",
  "exp",
  "exp10",
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
  "head",
  "hexBinary-equal",
  "hours-from-dateTime",
  "hours-from-duration",
  "hours-from-time",
  "implicit-timezone",
  "index-of",
  "innermost",
  "in-scope-prefixes",
  "insert-before",
  "intersect",
  "iri-to-uri",
  "is-same-node",
  "lang",
  "local-name-from-QName",
  "log",
  "log10",
  "lower-case",
  "matches",
  "minutes-from-dateTime",
  "minutes-from-duration",
  "minutes-from-time",
  "month-from-date",
  "month-from-dateTime",
  "months-from-duration",
  "multiply-dayTimeDuration",
  "multiply-yearMonthDuration",
  "namespace-uri-for-prefix",
  "namespace-uri-from-QName",
  "nilled",
  "node-after",
  "node-before",
  "normalize-unicode",
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
  "pow",
  "prefix-from-QName",
  "QName",
  "QName-equal",
  "remove",
  "replace",
  "resolve-QName",
  "resolve-uri",
  "reverse",
  "round",
  "round-half-to-even",
  "seconds-from-dateTime",
  "seconds-from-duration",
  "seconds-from-time",
  "serialize",
  "sin",
  "sqrt",
  "static-base-uri",
  "string",
  "string-to-codepoints",
  "subsequence",
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

$query = '(not(@xqx:prefix != \'fn\' and @xqx:prefix != \'math\')) and (false()';
foreach (@unsupported) {
  $query = "$query or text() = '$_'";
}
$query = "$query)";
$nonsupportedquery = "ast//xqx:functionName[$query]";

$query = '(@xqx:prefix != \'fn\') or (true()';
foreach (@XPathStd::functions) {
  $query = "$query and text() != '$_'";
}
$query = "$query)";
$nonstandardquery = "ast//xqx:functionName[$query]";

$inextras = 'schemas/validation[@schema=\'xpath-efo-extra.rnc\'][@valid=\'yes\']';

$total=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -v \"count(//xpath[schemas])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $total+=$_;
  }
  close STARLET;
}
print STDERR "Counting only validated queries...\n";
print STDERR "$total queries in total\n";

$nonstd=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -v \"count(//xpath[schemas][$nonstandardquery])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $nonstd+=$_;
  }
  close STARLET;
}
print STDERR "$nonstd queries with non-standard functions\n";

$nonsup=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -v \"count(//xpath[schemas][not($nonstandardquery) and not($inextras) and ($nonsupportedquery)])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $nonsup+=$_;
  }
  close STARLET;
}
print STDERR "$nonsup queries with unsupported standard functions but no non-standard ones\n";

$extras=0;
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -v \"count(//xpath[schemas][$inextras])\" -n $file |");
  while(<STARLET>) {
    chomp;
    $extras+=$_;
  }
  close STARLET;
}
print STDERR "$extras queries captured in extra fragments\n";

$remaining = $total-$extras-$nonstd-$nonsup;
print STDERR "$remaining remaining queries\n";
print "<?xml version=\"1.0\"?>\n<benchmark>\n";
for my $file (@files) {
  open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -c \"//xpath[schemas][not($nonstandardquery) and not($inextras) and not($nonsupportedquery)]\" -n $file |");
  while(<STARLET>) {
    print "$_";
  }
  close STARLET;
}
print "</benchmark>\n";

# for my $file (@files) {
#   open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -c \"//xpath[schemas][not($nonstandardquery) and ($inextras) and ($nonsupportedquery)]\" -n $file |");
#   while(<STARLET>) {
#     print "$_";
#   }
#   close STARLET;
# }
