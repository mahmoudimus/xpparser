#!/usr/bin/perl

%table=();
@ops=split(/ /,"andOp orOp unionOp eqOp neOp equalOp notEqualOp ltOp leOp gtOp geOp lessThanOp lessThanOrEqualOp greaterThanOp greaterThanOrEqualOp isOp nodeBeforeOp nodeAfterOp addOp subtractOp multiplyOp divOp idivOp modOp intersectOp exceptOp stringConcatenateOp");
$files=join(" ",@ARGV);
die "Usage: $0 <XML benchmark files>\n" unless @ARGV;
for $i (0..$#ops) {
  $op = $ops[$i];
  print "\r",$i+1,"/",$#ops+1,"...";
  open(STARLET,"xmlstarlet sel -t -n -c \"count(//*[local-name()='$op'])\" $files |");
  while(<STARLET>) {
    chomp;
    if (defined $table{$op}) {
      $table{$op}+=$_;
    } else {
      $table{$op}=$_;
    }
  }
}
print " all done.\n";
print "Results in ascending number of occurrences:\n";
for (sort { $table{$a} <=> $table{$b} } keys %table) {
  print "$_: $table{$_}\n";
}
