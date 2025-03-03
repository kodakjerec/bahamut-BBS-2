#!/usr/bin/perl
#

$usage = <<USAGE;
Usage:
  perl $0 exeGetList [yr1 mon1 mday1 yr2 mon2 mday2 yr3 mon3 mday3]
  exeGetList:      the perl script to get article list
  yr1,mon1,mday1:  the day before yesterday
  yr2,mon2,mday2:  yesterday
  yr3,mon3,mday3:  today
    yr? is 4-char integer; mon? is 2-char; mday is 2-char. If no enough
    digits, please add leading 0. If any of yr? or mon? or mday? is not
    supplied, dates.pl will be called to get these.
USAGE
$exeGetList = shift or die $usage;
-f $exeGetList or die "$exeGetList not exists!\n$usage";

# --- get the year / month / mday ---------
my ($p2d_yr, $p2d_mon, $p2d_mday,
    $yesterday_yr, $yesterday_mon, $yesterday_mday,
    $today_yr, $today_mon, $today_mday);

if ($#ARGV == 8) {
    # get year / month / mday by argument list
    ($p2d_yr, $p2d_mon, $p2d_mday,
        $yesterday_yr, $yesterday_mon, $yesterday_mday,
        $today_yr, $today_mon, $today_mday)
    = splice(@ARGV, 0, 9);
}
else {
    # get year / month / mday by dates.pl
    ($p2d_yr, $p2d_mon, $p2d_mday,
        $yesterday_yr, $yesterday_mon, $yesterday_mday,
        $today_yr, $today_mon, $today_mday)
    = split(/ /, `perl dates.pl`);
}

$today = "$today_mon/$today_mday";
$yesterday = "$yesterday_mon/$yesterday_mday";
$yesterday8 = "$yesterday_yr$yesterday_mon$yesterday_mday";
$p2d = "$p2d_mon/$p2d_mday";

#print join("\n", $today, $yesterday, $yesterday8, $p2d, "");
# =========================================

my @fileBaha = ();
my $outfileBaha;
&refreshFileBaha;

while (1) {
    my @tail30 = (&merge(@fileBaha))[-30 .. -1];
    my @t = grep(/^.{10}$today/, @tail30);
    last if scalar(@t) == 30;
    sleep 30;
    &runGetList($exeGetList);
}

for my $i (1 .. 10) {
    my @t = &check_continue(@fileBaha);
    last if ! scalar(@t);
    &runGetList($exeGetList,1);
}

my @listYesterday8 = &merge(@fileBaha);
open YESTERDAY8, ">$yesterday8.txt";
print YESTERDAY8 join("\n", @listYesterday8, "");
close YESTERDAY8;

# --- try to find the begin number of yesterday articles ---
my $begin = substr( (grep(/^.{10}$p2d/, @listYesterday8))[-1], 1, 5);
$begin-=100;
if ($begin <=0) {
    $begin = 1;
}
elsif ($begin > 1000) {
    $begin = 1000;
}
#print "\$begin = $begin\n";
# ==========================================================

print "Generating statistics...\n";
system("perl ../statistics.pl $yesterday8.txt $yesterday $begin >stat.txt");
mkdir $yesterday8 if ! -d $yesterday8;
foreach (@fileBaha, "$yesterday8.txt", "stat.txt") {
    rename $_, "$yesterday8/$_";
}
exit;

sub refreshFileBaha {
    @fileBaha = ();
    $outfileBaha = undef;
    for my $i (1 .. 99) {
        $tFileBaha = sprintf("baha%02d.txt", $i);
        if (-f $tFileBaha) {
            push @fileBaha, $tFileBaha;
        }
        elsif (! defined($outfileBaha)) {
            $outfileBaha = $tFileBaha;
        }
    }
}

sub merge {
    my %strings;
    my @list = ();
    foreach my $file (@_) {
        open INFILE, "<$file" or die;
        while (<INFILE>) {
            s/[\r\n]//g;
            next if !/^[> ][ 0-9]{5}/;
            my $num = substr($_,1,5);
            s/^>/ /;
            $strings{$num} = $_;
        }
        close INFILE;
    }
    for $i (sort keys %strings) {
        push @list, "$strings{$i}";
    }
    return (@list);
}

sub check_continue {
    my %num;
    foreach my $file (@_) {
        open CONTINUE, "<$file" or die;
        while (<CONTINUE>) {
            next if !/^[> ][ 0-9]{5}/;
            $num{substr($_,1,5)} = 1;
        }
        close CONTINUE;
    }
    my @b;
    my @a;
    @b = @a = sort keys %num;
    unshift @a, 1;
    my @list;
    for my $i (0 .. $#b) {
        if ($b[$i] - $a[$i] > 1) {
            push @list, sprintf("%d-%d", $a[$i], $b[$i]-1);
        }
    }
    return @list;
}

sub runGetList {
    system("perl", @_);
    &refreshFileBaha;
}