#!/usr/bin/perl

# Standard XPATH functions
@stdfuns = (
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

unless (@ARGV) {
  print <<HELP;
Usage: $0 <XML files>

Produces gnuplot data files countfuns_<XPPSUF>{,_std,_nonstd}.dat,
where XPPSUF is set as an env variable.

Issues a detailed output with function names if env variable VERBOSE is set.
HELP
  exit 1;
}

$suffix = $ENV{'XPPSUF'};
$verbose = $ENV{'VERBOSE'};

%table=(); # maps function names to occurrence count
$total=0;  # total nb of occurrences of functions
$nbfuns=0; # nb of functions i.e. size of %table
for (@ARGV) {
  open(STARLET,"xmlstarlet sel -t -n -v \"//*[local-name()='functionName']\" $_ |");
  while(<STARLET>) {
    chomp;
    next if $_ eq "not";
    $total++;
    if (defined $table{$_}) {
      $table{$_}++;
    } else {
      $nbfuns++;
      $table{$_}=1;
    }
  }
}

if ($verbose ne "") {
  print "Results in ascending number of occurrences:\n";
  for (sort { $table{$a} <=> $table{$b} } keys %table) {
	print "$_: $table{$_}\n";
  }
}

print "Generating countfuns_${suffix}{,_std,_nonstd}.dat...\n";
open DAT,">","countfuns_${suffix}.dat"
  or die "Cannot open countfuns_${suffix}.dat!\n";
open STD,">","countfuns_${suffix}_std.dat"
  or die "Cannot open countfuns_${suffix}_std.dat!\n";
open NONSTD,">","countfuns_${suffix}_nonstd.dat"
  or die "Cannot open countfuns_${suffix}_nonstd.dat!\n";
open TOTAL,">","countfuns_${suffix}_total.tex"
  or die "Cannot open countfuns_${suffix}_total.tex!\n";
open COV,">","countfuns_${suffix}_std_cov.tex"
  or die "Cannot open countfuns_${suffix}_std_cov.tex!\n";
$n=0;             # rank of current function (decr. order)
$sofar=0;         # total nb of occ. so far
$sofar_std=0;     # total nb of occ. of std funs so far
$target=0.7;      # display nb fun. needed to reach this % of total
$threshold=100;   # display stats about fun. with >$threshold occ.
for $fname (sort { $table{$b} <=> $table{$a} } keys %table) {
  if ($table{$fname}<$threshold) {
    print "* There are $n functions with >=$threshold occurrences,\n";
    printf("  together they account for %.2f%% of occurrences.\n",
      100*($sofar/$total));
	open THR,">","countfuns_${suffix}_${threshold}nb.tex";
	printf THR "$n";
	close THR;
	open THR,">","countfuns_${suffix}_${threshold}pc.tex";
	printf THR "%.2f\\%%", 100*$sofar/$total;
	close THR;
    $threshold=0;
  }
  $n++;
  $sofar+=$table{$fname};
  if ($sofar >= $target*$total) {
    $target=2;
    print
      "* $n functions (out of $nbfuns) needed to cover 70% of occurrences.\n";
  }
  printf DAT "$n $table{$fname} %.2f\n", 100*$sofar/$total;
  if (grep { $fname eq $_ } @stdfuns) {
    $sofar_std+=$table{$fname};
    printf STD "$n $table{$fname} %.2f\n",100*$sofar_std/$total;
  } else {
    print NONSTD "$n $table{$fname}\n";
  }
}
$locale_nbfuns = reverse join ',', unpack '(A3)*', reverse $nbfuns;
print TOTAL ("$locale_nbfuns");
printf COV "%.2f", 100*$sofar_std/$total;
