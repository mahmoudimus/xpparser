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
      $table{$_}=0;
    }
  }
}

$n=100;
print "Results above $n:\n";
for (keys %table) {
  print "$_: $table{$_}\n" if $table{$_}>$n;
}
