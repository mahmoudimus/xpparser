#!/usr/bin/perl

die "Usage: $0 <XML files>\n" unless @ARGV;

%table=();
$total=0;
$nbfuns=0;
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

print "Results in ascending number of occurrences:\n";
for (sort { $table{$a} <=> $table{$b} } keys %table) {
  print "$_: $table{$_}\n";
}

print "Generating countfuns_dist.dat...\n";
open DAT,">","countfuns_dist.dat" or die "Cannot open countfuns_dist.dat!\n";
$n=0;
$sofar=0;
$threshold=100;
for (sort { $table{$b} <=> $table{$a} } keys %table) {
  if ($table{$_}<$threshold) {
    print "* There are $n functions with >=$threshold occurrences,\n";
    printf("  together they account for %.2f%% of occurrences.\n",
      100*($sofar/$total));
    $threshold=0;
  }
  $n++;
  $sofar+=$table{$_};
  if ($sofar >= 0.7*$total) {
    $sofar=0;
    print
      "* $n functions (out of $nbfuns) needed to cover 70% of occurrences.\n";
  }
  print DAT "$n $table{$_}\n";
}
