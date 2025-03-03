#!/usr/bin/perl
# In version 06, all operations are put in one file. But it has
# problem when run with ActivePerl on my machine (OS: win2k SP3):
# LWP::UserAgent can only run once in one process. So I split them
# into different files, and store cookie and some information for
# posting an article.

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

system("perl", "-w", "webpost07-1.pl", $gBrdName, $gDate, $file_uid_pass);
sleep 5;
system("perl", "-w", "webpost07-2.pl", $gBrdName, $gDate, $file_uid_pass);
sleep 5;
system("perl", "-w", "webpost07-3.pl", $gBrdName, $gDate, $file_uid_pass);
sleep 5;
system("perl", "-w", "webpost07-4.pl", $gBrdName, $gDate, $file_uid_pass);

unlink "baha_cookie.txt";
unlink "post_info.txt";
