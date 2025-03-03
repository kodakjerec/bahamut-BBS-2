#!/usr/bin/perl
#

$usage = <<USAGE;
Usage:
  perl $0 exeGetList uid_pass.txt
  exeGetList:      the perl script to get article list
  uid_pass.txt:    uid & password file, select non-existent file means
                   not to webpost
USAGE

$exeGetList = shift or die $usage;
-f $exeGetList or die "$exeGetList not exists!\n$usage";
$fileUidPasswd = shift or die $usage;
-f $fileUidPasswd or print "$fileUidPasswd not found -> will not post\n";

my @fileBaha = ();
my $outfileBaha;
&refreshFileBaha;

my %do_at;
while (1) {
    my $hrmin = &date("+%H%M");
    my $hr = substr($hrmin,0,2);
    my $min = substr($hrmin,2,2);
    last if $hrmin le "0300";

    if (($hr =~ /^(06|18|22)$/) && ! defined $do_at{$hr."00"}) {
        &runGetList($exeGetList);
        $do_at{$hr."00"} = 1;
    }
    elsif (($hrmin gt "2330") && ! defined $do_at{"2330"}) {
        &runGetList($exeGetList);
        $do_at{"2330"} = 1;
    }
    elsif ($hrmin gt "0700") {
        &refreshFileBaha;
        if (0 == scalar(@fileBaha)) {
            &runGetList($exeGetList);
        }
    }

    my $hrmin = &date("+%H%M");
    $hr = substr($hrmin,0,2);
    $min = substr($hrmin,2,2);
    my $minWait = 10;

    last if $hrmin le "0300";
    if ($hrmin le "0600") {
        $minWait = 60;
    }
    elsif ($hrmin gt "2330") {
        $minWait = 1;
    }

    my $secWait = $minWait*60;
    printf("%s - sleep for %d secs\n", &date, $secWait);
    sleep $secWait;
}

# --- get the year / month / mday ------------------
($p2d_yr, $p2d_mon, $p2d_mday,
    $yesterday_yr, $yesterday_mon, $yesterday_mday,
    $today_yr, $today_mon, $today_mday) = split(/ /, `perl dates.pl`);

$yesterday = "$yesterday_mon/$yesterday_mday";
$yesterday8 = "$yesterday_yr$yesterday_mon$yesterday_mday";
-d $yesterday8 && exit;
# ==================================================

print "Call last1.pl...\n";
system("perl", "last1.pl", $exeGetList,
    $p2d_yr, $p2d_mon, $p2d_mday,
    $yesterday_yr, $yesterday_mon, $yesterday_mday,
    $today_yr, $today_mon, $today_mday);

if (-f $fileUidPasswd) { 
    print "Posting...\n";
    &cp("$yesterday8/stat.txt", "stat.txt");
    system("perl webpost07.pl Chat $yesterday $fileUidPasswd");
    unlink "stat.txt";
}
exit;

sub refreshFileBaha {
    @fileBaha = ();
    $outfileBaha = undef;
    for my $i (1 .. 99) {
        my $tFileBaha = sprintf("baha%02d.txt", $i);
        if (-f $tFileBaha) {
            push @fileBaha, $tFileBaha;
        }
        elsif (! defined($outfileBaha)) {
            $outfileBaha = $tFileBaha;
        }
    }
}

sub runGetList {
    system("perl", @_);
    &refreshFileBaha;
}

sub CSTtime () {
    return time+8*3600;
}

use POSIX qw(strftime);
sub date ($) {
    my $strFormat = shift || "%a %b %e %H:%M:%S %Y";
    $strFormat =~ s/^\+//g;
    return strftime($strFormat, gmtime(&CSTtime));
}

sub cp ($$) {
    my ($from, $to) = @_;
    my ($buf);
    
    open(FROM, "$from") || die "Can't open $from\n";
    open(TO, ">$to")    || die "Can't open $to\n";

    while(read (FROM, $buf, 16384)) {
        print TO $buf;
    }
    close(FROM);
    close(TO);
}