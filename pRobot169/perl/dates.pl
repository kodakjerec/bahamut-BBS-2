#!/usr/bin/perl
#

sub CSTtime () {
    return time+8*3600;
}

sub year_mon_day ($) {
    my $time = shift || time;
    my ($mday,$mon,$year_1900) = (gmtime($time))[3,4,5];
    my $year=$year_1900+1900;
    return sprintf("%04d %02d %02d", $year, $mon+1, $mday);
}

my $today = &year_mon_day(&CSTtime);
my $yesterday = &year_mon_day(&CSTtime - 86400);
my $P2D = &year_mon_day(&CSTtime - 2*86400);

print join(" ", $P2D, $yesterday, $today);