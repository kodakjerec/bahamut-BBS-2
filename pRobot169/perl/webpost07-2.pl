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
open IDPASS, "<".$file_uid_pass;
my ($gUid, $gPasswd) = <IDPASS>;
close IDPASS;
$gUid =~ s/[\r\n]//g;
$gPasswd =~ s/[\r\n]//g;
$gPasswd = substr($gPasswd, 0, 8);

my $txtStat = "";
open txtStat, "<stat.txt";
$txtStat = join("","\n",<txtStat>);
close txtStat;

use LWP::UserAgent;
use HTTP::Cookies;
use HTTP::Request::Common qw(GET POST);

my $ua = LWP::UserAgent->new;
my $cookie_jar = HTTP::Cookies->new(
    ignore_discard => 1, file => "./baha_cookie.txt");
$cookie_jar->load;
$ua->cookie_jar($cookie_jar);
$ua->protocols_allowed(undef);

# --- get some information when doing post -----------
my $req2 = HTTP::Request->new(GET =>
    "http://webbbs.gamer.com.tw/post.php?brd=$gBrdName");
my $res = $ua->request($req2);
unless ($res->is_success) {
    print "Failed: ", $res->status_line, "\n";
    print $res->as_string, "\n";
    exit;
}
my ($postPHP, $brd, $viapost, $userid,
    $stamp) = &getInfoToPost($res->as_string);

open INFO, ">post_info.txt";
print INFO join("\n", $postPHP, $brd, $viapost, $userid, $stamp);
close INFO;
# ====================================================
exit;


sub getInfoToPost ($) {
    my @html = split('\n', shift);
    my ($postPHP, $brd, $viapost, $userid, $stamp);
    $postPHP = 0;
    foreach (@html) {
        if (/^<form\s+name=postform\s+method=post\s+action=\"post\.php\">/) {
            $postPHP = 1;
        }
        if (/<input\s+type=hidden\s+name=brd\s+value=\"(\w+)\">/) {
            $brd = $1;
        }
        if (/<input\s+.*\s+name=\"viapost\"\s+value=\"(\w+)\">/) {
            $viapost = $1;
        }
        if (/<input\s+.*\s+name=\"userid\"\s+value=\"(\w+)\">/) {
            $userid = $1;
        }
        if (/<input\s+.*\s+name=\"stamp\"\s+value=(\w+)>/) {
            $stamp = $1;
        }
    }
    return ($postPHP, $brd, $viapost, $userid, $stamp);
}