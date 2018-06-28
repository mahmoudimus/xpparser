#!/usr/bin/perl

use strict;
require "./XPathStd.pm";

die "Usage: $0 <type> <XML files>\n" unless @ARGV;
my $type = @ARGV[0];
shift;

# XML sources
my @files = @ARGV;

# Standard XPath functions which are well supported in decidable fragments
my @wellsupported = (
  "boolean",
  "false",
  "has-children",
  "local-name",
  "name",
  "namespace-uri",
  "node-name",
  "not",
  "root",
  "true"
);

# Standard XPath functions for which there is some amount of support
# in the decidable fragments
my @supported = (
  "boolean",
  "concat",
  "contains",
  # "count",
  "ends-with",
  "false",
  "has-children",
  "id",
  "idref",
  "last",
  "local-name",
  # "max",
  # "min",
  "name",
  "namespace-uri",
  "node-name",
  # "normalize-space",
  "not",
  "position",
  "root",
  "starts-with",
  "string-join",
  "string-length",
  "substring",
  "substring-after",
  "substring-before",
  "translate",
  "true"
);  

# =============================================================================

# Standard functions

my $standardfun = 'not(@xqx:prefix != \'fn\' and @xqx:prefix != \'math\') and (false()';
foreach (@XPathStd::functions) {
  $standardfun = "$standardfun or text() = '$_'";
}
$standardfun = "$standardfun)";

# Well-supported standard functions

my $wellsupported = 'not(@xqx:prefix != \'fn\' and @xqx:prefix != \'math\') and (false()';
foreach (@wellsupported) {
  $wellsupported = "$wellsupported or text() = '$_'";
}
$wellsupported = "$wellsupported)";

# Unsupported standard functions

my $unsupportedstdfun = "($standardfun and (true()";
foreach (@supported) {
  $unsupportedstdfun = "$unsupportedstdfun and text() != '$_'";
}
$unsupportedstdfun = "$unsupportedstdfun))";

# =============================================================================

# AST featuring at least one non-standard function

my $nonstandardAST = "ast//xqx:functionName[not($standardfun)]";

# AST featuring at least one unsupported standard function

my $nonsupportedAST = "ast//xqx:functionName[$unsupportedstdfun]";

# AST featuring only well supported functions

my $wellsupportedAST = "not(ast//xqx:functionName[not($wellsupported)])";

# Captured in Positive +extra

my $inextras = 'schemas/validation[@schema=\'xpath-efo-extra.rnc\'][@valid=\'yes\']';

# Captured in one of the full fragments
my $infulls = '(schemas/validation[@valid=\'yes\'][false()';
for my $rnc (split /\n/, `grep file ../../relaxng/fragments-full.xml  | sed -e 's/.*="\\(.*\\)".*/\\1/'`) {
  $infulls="$infulls or \@schema='$rnc'";
}
$infulls = "$infulls])";

# =============================================================================

sub count {
  my $c=0;
  my $query=$_[0];
  for my $file (@files) {
    open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -v \"count($query)\" -n $file |");
    while(<STARLET>) {
      chomp;
      $c+=$_;
    }
    close STARLET;
  }
  return $c;
}

sub extract_and_print {
  my $query=$_[0];
  print "<?xml version=\"1.0\"?>\n<benchmark>\n";
  for my $file (@files) {
    open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -c \"$query\" -n $file |");
    while(<STARLET>) {
      print "$_";
    }
    close STARLET;
  }
  print "</benchmark>\n";
}

sub count_show {
  my $n = count($_[0]);
  print STDERR "$n queries $_[1]\n";
  return $n;
}

sub count_showcov {
  open(CF, '>', "non-standard-$type-$_[2].tex") or die $!;
  open(CFfull, '>', "non-standard-$type-$_[2]-full.tex") or die $!;
  open(CFextra, '>', "non-standard-$type-$_[2]-extra.tex") or die $!;
  
  my $n = count_show($_[0],$_[1]);
  my $d = reverse join ',', unpack '(A3)*', reverse $n;
  printf CF "$d";
  my $captured = count($_[0]."[$infulls]");
  printf STDERR "  among which %d (%.2f%%) are captured in full\n",
    $captured, 100*$captured/$n;
  printf CFfull "%.2f%%", 100*$captured/$n;
  my $captured=count($_[0]."[$infulls or $inextras]");
  printf STDERR "  among which %d (%.2f%%) are captured in full+extra\n",
    $captured, 100*$captured/$n;
  printf CFextra "%.2f%%", 100*$captured/$n;
  
  close CF;
  close CFfull;
  close CFextra;
  return $n;
}

# =============================================================================

print STDERR "Counting only validated queries...\n";

my $total=count_showcov("//xpath[schemas]","in total","tot");

count_showcov("//xpath[schemas][ast//xqx:functionName]","with functions","fun");

count_showcov("//xpath[schemas][not($nonstandardAST)]","with only standard functions","std");

count_showcov("//xpath[schemas/validation[\@schema='xpath-3.0.rnc'][\@valid='yes']][not($nonstandardAST)]",
              "that are fully XP3.0 std","std3");

# This one is irrelevant, as it keeps unsupported operators
# count_showcov("//xpath[schemas][$wellsupportedAST]","with only well-supported functions");

my $nonstd=count_show("//xpath[schemas][$nonstandardAST]","with non-std functions");

my $nonsup=count_show("//xpath[schemas][not($nonstandardAST) and ($nonsupportedAST)]",
                      "with std but unsupported functions");

count_showcov("//xpath[schemas][not($nonstandardAST) and not($nonsupportedAST)]","without unsupported functions","wuns");

my $extras=count_show("//xpath[schemas][$inextras]","captured in positive+extra alone");

# Outdated:
# my $remaining = $total-$extras-$nonstd-$nonsup;
# print STDERR "$remaining remaining queries\n";

extract_and_print("//xpath[schemas][not($nonstandardAST) and not($inextras) and not($infulls) and not($nonsupportedAST)]");

# for my $file (@files) {
#   open(STARLET,"xmlstarlet sel -N xqx=\"http://www.w3.org/2005/XQueryX\" -t -c \"//xpath[schemas][not($nonstandardAST) and ($inextras) and ($nonsupportedAST)]\" -n $file |");
#   while(<STARLET>) {
#     print "$_";
#   }
#   close STARLET;
# }
