#!/usr/bin/perl
#

use Fcntl;

# --- define some literals -------------------------
$chat = "http://webbbs.gamer.com.tw/board.php?brd=Chat";
$page = "&p";
# ==================================================

# --- get the last of current articles ------------------------
my ($last, $end) = &endOfList($chat);
print "$chat -> $last\n";
print "\$end = $end\n";
# ============================================================

# --- get baha files --------
my @fileBaha = ();
my $outfileBaha = undef;
for my $i (1 .. 99) {
    my $tFileBaha = sprintf("baha%02d.txt", $i);
    if (-f $tFileBaha) {
        push @fileBaha, $tFileBaha;
    }
    elsif (! defined($outfileBaha)) {
        $outfileBaha = $tFileBaha;
    }
}
# ===========================

# --- the ranges to dump ------------------------
if (scalar(@fileBaha)) {
    my $preEnd = 1;
    push @ARGV, $_ foreach ( &check_continue(@fileBaha) );
    $preEnd = &preEnd(@fileBaha);
    print "preEnd = $preEnd\n";
    push @ARGV, "$preEnd-$end";
}
else {
    unshift @ARGV, "1-$end";
}
# =============================================

print join(",", @ARGV), "\n";
$outfileBaha = $outfileBaha;
print "output file -> $outfileBaha\n";

foreach my $range (@ARGV) {
    next if ! ($range =~ /^(\d+)-(\d+)$/);
    print STDERR "$1-$2\n";
    my $start = $1;
    my $end = $2;
    next if $start > $end;

    my @listDocs;
    my ($host, $port, $fileRelative);
    for my $i ( ($start+29)/30-1 .. ($end+29)/30+1 ) {
        next if $i<=0;
        my $url = "$chat$page=$i";
        if ($url =~ m#^http://([\w\.-]+)(:(\d+))?/(.*)$#) {
            $host = $1;
            $port = $3 if $3 ne "";
            $fileRelative = $4;
            push @listDocs, $fileRelative;
        }
    }
    if (@listDocs > 0) {
        system("perl", "-w", "dump_lists.pl", $outfileBaha, $host, @listDocs);
    }
#       &readPage($url, $outfileBaha);
}
exit;

sub readPage ($$) {
    my $urlA;
    my $outfileBahaA;
    $urlA = $_[0];
    $outfileBahaA = $_[1];

    print "$urlA\n";
    my $listArticle = &dump1list($urlA);

    open OUTPUT, ">>$outfileBahaA";
    print OUTPUT $listArticle;
    close OUTPUT;
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

sub preEnd {
    my $preEnd = 1;
    foreach my $file (@_) {
        open INFILE, "<$file" or die;
        while (<INFILE>) {
            next if !/^[> ][ 0-9]{5}/;
            my $num = substr($_,1,5);
            $num = $num + 0;
            $preEnd = $num if $num > $preEnd;
        }
        close INFILE;
    }
    return $preEnd;
}

sub endOfList ($) {
    my $chat = shift;
    my @a = split(/\n/,&dump1list($chat));
    my $last = substr($a[-1], 1, 5);
    chomp $last;
    my $end = 0;
    if ($#ARGV==0 && $ARGV[0] =~ /^\d+$/) {
        $end = shift(@ARGV);
        $end = $last if $last < $end;
    }
    elsif ($#ARGV == -1) {
        $end = $last;
    }
    return ($last+0, $end+0);
}

sub dump1list ($) {
    my ($host, $port, $fileRelative);
    while ($_ = shift) {
        if (m#^http://([\w\.-]+)(:(\d+))?/(.*)$#) {
            $host = $1;
            $port = $3 if $3 ne "";
            $fileRelative = $4;
            last;
        }
    }

    my $content = "";
    system("perl", "-w", "dump_lists.pl", "dump1list.temp.txt", $host,
$fileRelative);
    open DMPFILE, "<dump1list.temp.txt" or die;
    $content .= $_ while (<DMPFILE>);
    close DMPFILE;
    unlink "dump1list.temp.txt";
    return $content;
}