#!/usr/bin/perl

die "Usage: $0 <XML files>\n" unless @ARGV;

%table=();
for (@ARGV) {
  open(STARLET,"xmlstarlet sel -t -n -v \"//*[local-name()='functionName']\" $_ |");
  while(<STARLET>) {
    chomp;
    if (defined $table{$_}) {
      $table{$_}++;
    } else {
      $table{$_}=1;
    }
  }
}

print "Results in ascending number of occurrences:\n";
for (sort { $table{$a} <=> $table{$b} } keys %table) {
  print "$_: $table{$_}\n";
}
