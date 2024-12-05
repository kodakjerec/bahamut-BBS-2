#!/usr/bin/perl
#

use strict;
my $gBrdName = shift;
my $gDate = shift;
my $file_uid_pass = shift;

if (!($gBrdName =~ /^\w+$/ && $gDate =~ m#^\d\d/\d\d$#
        && -f $file_uid_pass && -f "stat.txt" )) {
    print <<USAGE;
Usage: perl $0 boardname date uid_pass_file
    The form of parameter 'date' is 'mm/dd'.
    Please execute this program in a directory with 'stat.txt'.
USAGE
    exit;
}

use LWP::UserAgent;
use HTTP::Cookies;
use HTTP::Request::Common qw(GET POST);

my $ua = LWP::UserAgent->new;
my $cookie_jar = HTTP::Cookies->new(
    ignore_discard => 1, file => "./baha_cookie.txt");
$cookie_jar->load;
$ua->cookie_jar($cookie_jar);
$ua->protocols_allowed(undef);

foreach (1) {
    # --- logout ----------------------------------------
    my $req = HTTP::Request->new(
        HEADER => 'https://user.gamer.com.tw/logout.php');
    print $ua->request($req)->as_string;
    # ===================================================
    last;
}
exit;