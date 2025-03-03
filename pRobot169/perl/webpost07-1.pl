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

use LWP::UserAgent;
use HTTP::Cookies;
use HTTP::Request::Common qw(GET POST);

my $ua = LWP::UserAgent->new;
my $cookie_jar = HTTP::Cookies->new(
    ignore_discard => 1, file => "./baha_cookie.txt");
$ua->cookie_jar($cookie_jar);
$ua->protocols_allowed(undef);

# --- login ---------------------------------------
my $req = HTTP::Request->new(
    POST => 'https://user.gamer.com.tw/doLogin.php') or die;
$req->content_type('application/x-www-form-urlencoded');
$req->content("uid=$gUid&passwd=$gPasswd");
my $res = $ua->request($req);   # 須有這行才會登入
if ($res->is_success) {
    print $res->as_string;
}
else {
    print "Failed: ", $res->status_line, "\n";
}
# =================================================

$cookie_jar->save;

exit;