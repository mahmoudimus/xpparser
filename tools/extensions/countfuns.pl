#!/usr/bin/perl

require "./XPathStd.pm";

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
  if (grep { $fname eq $_ } @XPathStd::functions) {
    $sofar_std+=$table{$fname};
    printf STD "$n $table{$fname} %.2f\n",100*$sofar_std/$total;
  } else {
    print NONSTD "$n $table{$fname}\n";
  }
}
$locale_nbfuns = reverse join ',', unpack '(A3)*', reverse $nbfuns;
print TOTAL ("$locale_nbfuns");
printf COV "%.2f", 100*$sofar_std/$total;
