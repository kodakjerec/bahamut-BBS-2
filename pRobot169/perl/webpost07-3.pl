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

my $txtStat = "";
open txtStat, "<stat.txt";
$txtStat = join("","\n",<txtStat>);
close txtStat;

my ($postPHP, $brd, $viapost, $userid, $stamp);
open INFO, "<post_info.txt";
($postPHP = <INFO>) =~ s/[\r\n]//g;
($brd = <INFO>) =~ s/[\r\n]//g;
($viapost = <INFO>) =~ s/[\r\n]//g;
($userid = <INFO>) =~ s/[\r\n]//g;
($stamp = <INFO>) =~ s/[\r\n]//g;
close INFO;

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

    my $res;
    # --- post -----------------------------------
    if ($postPHP == 1 && $brd eq "$gBrdName") {
        my $req2 = POST("http://webbbs.gamer.com.tw/post.php?brd=$brd",
            [ brd => $brd,
            atype => '',
            title => "$gDate 洽文統計",
            content => "$txtStat",
            sign => 0,
            viapost => "$viapost",
            userid => "$userid",
            stamp => "$stamp" ]);
        $res = $ua->request($req2);
        print $res->as_string;
    }
    else {
        print $res->as_string;
        print "Failed to post!\n";
    }
    # ============================================

    last;
}
exit;