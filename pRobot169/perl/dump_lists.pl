#!/usr/bin/perl
#
use strict;
use IO::Handle;
my $fAppend = shift || exit;
my $hh = shift || exit;
@ARGV > 0 or exit;

my @document = @ARGV;
sub dump1list;
my $hdmp;
open $hdmp, ">>$fAppend" or die;
binmode $hdmp;
$hdmp->autoflush(1);


foreach my $i (0 .. $#document) {
    my $document = $document[$i];
    my $list;
    $list = "";
    print "http://$hh/$document";
    $list = dump1list($hh, $document);
    if ($list ne "") {
        print " -> get\n";
        print $hdmp $list;
    }
    else {
        print " -> fail\n";
    }
}

close $hdmp;
exit;

sub dump1list ($$) {
    my ($host, $fileRelative) = @_;
    my $timeInit = time;
    my $port = 80;

    # --- try to find if the system has gzip --------
    my $EOL = "\015\012";
    my $msgGetGzip = 
"GET /$fileRelative HTTP/1.0".$EOL.
"Host: $host".$EOL.
"Accept: text/html, text/plain, text/sgml, */*;q=0.01".$EOL.
"Accept-Encoding: gzip, compress".$EOL.
"Accept-Language: en".$EOL.
"User-Agent: Perl".$EOL.
"";

    my $msgGet = $msgGetGzip;
    # ===============================================

    # --- send the request ----------------------------
    use IO::Socket;
    # http://webbbs.gamer.com.tw/board.php?brd=Chat
    my $sock = IO::Socket::INET->new("${host}:${port}") or die "socket create error\n";
    $sock->print($msgGet.$EOL);
    # =================================================

    # --- setup some variables for select(...) ------
    my ($rin, $win, $ein);
    $rin = $win = $ein = '';
    vec($rin, fileno($sock), 1) = 1;
    vec($win, fileno($sock), 1) = 1;
    $ein = $rin | $win;
    # ===============================================

    print "getting...";
    # --- get the response ------------------
    my $response = "";
    while (! $sock->eof) {
        my ($rout, $wout, $eout) = ($rin, $win, $ein);
        my ($nfound, $timeleft) = select($rout, $wout, $eout, 0.05);
        if ($nfound > 0) {
            my $buf;
            $sock->read($buf, 10000);
            $response.=$buf;
        }
        last if (time - $timeInit > 10);
    }
    $sock->close;
    select(undef, undef, undef, 0.05) while (time == $timeInit);
    # =======================================

    # --- parse the response -------------------
    my $idx_rnrn = index($response, "\r\n\r\n");
    return "" if ($idx_rnrn < 0);

    my @header = split(/\r\n/, substr($response, 0, $idx_rnrn+1));
    my $content = substr($response, $idx_rnrn+4);

    my $isVaryAcceptEncoding = 0;
    my $contentLength = 0;
    my $contentEncoding = "text";
    foreach (@header) {
        s/[\r\n]//g;
        if (/^Vary:/ && /Accept-Encoding/) {
            $isVaryAcceptEncoding = 1;
        }
        elsif (/^Content-Length: (\d+)$/) {
            $contentLength = $1 + 0;
        }
        elsif (/^Content-Encoding: (.+)$/) {
            $contentEncoding = $1;
        }
    }
    # ==========================================

    # --- exit if the content is inconsistent ---------
    if ((length($content) != $contentLength && $contentLength > 0)
        || length($content) == 0) {
        # print STDERR "length unmatch\n";
        return "";
    }
    # =================================================

    # --- print the content -------------
    if ($contentEncoding =~ /gzip/) {
        use Compress::Zlib;
        $content = Compress::Zlib::memGunzip($content);
    }
    return &htm2list($content);
    # ===================================
}

exit;

sub htm2list ($) {
#    my $content = shift;
#    my @list = grep(/./,split(/\n/,$content));
    my @lineBuf;
    for my $i (0 .. 11) {
        $lineBuf[$i] = "";
    }
    my ($num, $mark, $gy, $date, $author, $title);

    my $outlist = "";
    for ( grep(/./, split(/\n/, shift)) ) {
        chomp;
        shift @lineBuf;
        push @lineBuf, $_;

        if ($lineBuf[9] =~ /readPost/ || ($lineBuf[5] =~ /queryUser/ && 
                $lineBuf[9] =~ /^<font.*>/)) {
            $lineBuf[1] =~ /^<.*>(\d+)<\/.*>/;
            $num = $1;

            $lineBuf[3] =~ m#^(.*)</td>#;
            $mark = lc(substr($1." ",0,1));

            $lineBuf[4] =~ m#^<td.*>../(../..)</td>#;
            $date = $1;

            $lineBuf[5] =~ m#^<td><a .*>(.*)</a></td>#;
            $author = $1;

            if ($lineBuf[7] =~ /\"javascript:thread\(\'(.*)\'\)\"/) {
                $title = $1;
            } elsif ($lineBuf[9] =~ m#<font.*>(.*)</font></td>#) {
                $title = $1;
            }

            $title = "◇ ".$title if $title !~ /^Re: /;
            $title = "Re ".substr($title,4) if $title =~ /^Re: /;
            $title =~ s/&gt;/>/g;
            $title =~ s/&lt;/</g;
            $title =~ s/&nbsp;/ /g;
            $title =~ s/&quot;/\"/g;
            $title =~ s/&amp;/\&/g;

            if ($lineBuf[10] =~ m#<td><em>(\d+)</em></td>#) {
                $gy = $1;
                $gy = 99 if $gy>99;
                $gy = sprintf("%02d",$gy);
            }
            else {
                $gy = "";
            }

            $outlist .= sprintf(" %5d %1s%2s%5s %-12s %s\015\012",
                $num, $mark, $gy, $date, $author, $title);
        }
    }
    return $outlist;
}