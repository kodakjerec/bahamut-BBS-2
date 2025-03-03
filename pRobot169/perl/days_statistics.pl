#!/usr/bin/perl
#

my (%first, %last);
my (%authors, %titles);
my (%selfErase);
my ($articles, $deleted);

while (my $dir = shift) {
    next if ! -d $dir;
    my $date = "";
    # --- get date -----------------
    1 while $dir =~ s#/$##;
    if ($dir =~ m#.*/([^/]+)$#) { $date = $1; }
    else { $date = $dir; }
    $date = substr($date, 0, 8);
    # ==============================
    next if (! $date =~ /^\d{8}$/);
    next if ! -f "$dir/stat.txt";
    next if ! -f "$dir/$date.txt";

    my ($start, $last) = &getStartLast("$dir/stat.txt");
    my $slashdate = substr($date,4,2)."/".substr($date,6,2);
    open LIST, "<$dir/$date.txt";
    while (<LIST>) {
        next if (substr($_, 10, 5) ne $slashdate);
        next if (substr($_, 1, 5) lt $start);
        last if (substr($_, 1, 5) gt $last);
        chomp;
        s/\r//g; s/\n//g;
        my $author = substr($_, 16, 12);
        $author =~ s/ +$//;
        my $title = substr($_, 32);
        $first{$date} = $_ if (substr($_, 1, 5) eq $start);
        $last{$date} = $_ if (substr($_, 1, 5) eq $last);

        $authors{$author}++;
        if (lc(substr($_, 7, 1)) eq "d") {
            $deleted++;
            if ($title =~ /\($author\)/) {
                $selfErase{$author}++;
            }
            next;
        }
        $articles++;
        $titles{$title}++;
    }
    close LIST;

    print "$date $slashdate $start-$last\n";
}

#&printAuthors;
&printTitles;

sub topN ($$$) {
    my ($pList, $nTop, $nChar) = @_;
    my $result;

    my $n = 0;
    my $nLast = 0;
    foreach my $i (sort { $b cmp $a } @$pList) {
        $result .= "$i\n" if $n++ < $nTop;
        $nLast = substr($i, 1, $nChar) if $n == $nTop;
        if (substr($i, 1, $nChar) < $nLast) {
            last;
        }
        elsif ($n > $nTop) {
            $result .= "$i\n";
        }
    }
    return $result;
}

sub printTitles {
    foreach my $i (keys %titles) {
        push @sortTitles, sprintf("%6s | %s", $titles{$i}, $i);
    }

    my @titlesNormal;
    my @titlesPopular;

    foreach my $i (@sortTitles) {
        if ($i =~ /[0-9]/) {
            if ($i =~ /(推廣|日目|航目)/) {
                push @titlesPopular, $i;
            }
            else {
                push @titlesNormal, $i;
            }
        }
    }

    my @sumTitlesPop = ("111111 | \xFF\xFF");
    my %mainTitlesPop;
    for my $i (sort { substr($a, index($a,"|")+2) cmp
            substr($b, index($b,"|")+2) } @titlesPopular) {
        $sumTitlesPop[$#sumTitlesPop] =~ /^\s*(\d+)\s\|\s(.+)$/;
        my ($nPrev, $titlePrev) = ($1, $2);
        $i =~ /^\s*(\d+)\s\|\s(.+)$/;
        my ($nCur, $titleCur) = ($1, $2);
        my ($ox, $titleMain) = &cmpSimilar($titlePrev, $titleCur);
        if ($ox eq "O") {
            pop @sumTitlesPop;
            push @sumTitlesPop, sprintf("%6s | %s", $nPrev+$nCur, $titlePrev);
            $mainTitlesPop{$titlePrev} = $titleMain;
        }
        else {
            push @sumTitlesPop, $i;
        }
    }
    shift @sumTitlesPop;

    for my $i (@sumTitlesPop) {
        $i =~ /^\s*(\d+)\s\|\s(.+)$/;
        my ($nCur, $titleCur) = ($1, $2);
        if (defined $mainTitlesPop{$titleCur}) {
            $i = sprintf("%6s | %s", $nCur, $mainTitlesPop{$titleCur});
        }
    }

    # --- print normal titles -----------------------------------
    print <<EOF;
     # |   Titles
-------+---------------------------------------------
EOF
    print &topN(\@titlesNormal, 40, 6);
    print "\n";
    # ===========================================================

    # --- print popularized titles ------------------------
    print <<EOF;
推廣成果:
     # |   Titles
-------+---------------------------------------------
EOF
    print &topN(\@sumTitlesPop, 20, 6);
    print "\n";
    # =====================================================
    print "\n";

}

sub printAuthors {
    print <<EOF;
     # |  Authors
-------+---------------------------------------------
EOF
    foreach my $i (keys %authors) {
        my $name = $i;
        $name = substr($i,0,1).("*" x (length($i)-2)).substr($i,-1,1);
        substr($name,0,1) = "*" if length($name)==2;
        push @sortAuthors, sprintf("%6s | %1s", $authors{$i}, $name);
    }
    print &topN(\@sortAuthors, 30, 6);
    print "\n";
}

sub getStartLast ($) {
    my $stat = shift;
    my ($start, $last) = undef;

    open STAT, "<$stat";
    while (<STAT>) {
        $start = sprintf("%5d",$1) if /^start = (\d+)/;
        $last = sprintf("%5d",$1) if /^last = (\d+)/;
    }
    close STAT;
    return ($start,$last);
}

sub cmpSimilar ($$) {
    my $a = shift;
    my $b = shift;
    my ($maina, $mainb) = ($a, $b);
    my ($main1, $main2) = ($maina, $mainb);
    ($main1, $main2) = ($main2, $main1) if (length($main1)>length($main2));
    if (substr($main2,0,length($main1)) eq $main1
        && substr($main2,length($main1)) =~ /^[a-zA-Z ]{0,3}$/) {
        return ("O", $maina);
    }

    my @a = grep /./, split(/([\x80-\xFF].|.)/,$a);
    my @b = grep /./, split(/([\x80-\xFF].|.)/,$b);
    my $i = 0;
    sub isNumChi;
    for $i (0 .. $#a) {
        last if $i > $#b;
        if ($a[$i] ne $b[$i]) {
            if (&isNumChi($a[$i]) || &isNumChi($b[$i])) {
                my $ja = $i;
                my $jb = $i;
                my $ka = $i;
                my $kb = $i;
                --$ja if (defined $a[$i-1] && &isNumChi($a[$i-1])
                    && ! &isNumChi($a[$i]));
                --$jb if (defined $b[$i-1] && &isNumChi($b[$i-1])
                    && ! &isNumChi($b[$i]));
                --$ja while ($ja > 0 && &isNumChi($a[$ja]));
                --$jb while ($jb > 0 && &isNumChi($b[$jb]));
                if ($ja != $jb) {
                    return ("X", $maina);
                }
                ++$ka while ($ka<=$#a && &isNumChi($a[$ka]));
                ++$kb while ($kb<=$#b && &isNumChi($b[$kb]));
                my $maina = join("", @a[0 .. $ja], "---", @a[$ka .. $#a]);
                my $mainb = join("", @b[0 .. $jb], "---", @b[$kb .. $#b]);

                ($main1, $main2) = ($maina, $mainb);
                ($main1, $main2) = ($main2, $main1) if (length($main1)
                    >length($main2));
                if (substr($main2,0,length($main1)) eq $main1
                    && substr($main2,length($main1)) =~ /^[a-zA-Z ]{0,2}$/) {
                    return ("O", $maina);
                }
                else {
                    return ("X", $maina);
                }
                return;
            }
            elsif ($a[$i] =~ /\d/ || $b[$i] =~ /\d/) {
                my $ja = $i;
                my $jb = $i;
                my $ka = $i;
                my $kb = $i;
                --$ja if (defined $a[$i-1] && $a[$i-1] =~ /\d/
                    && ! ($a[$i] =~ /\d/));
                --$jb if (defined $b[$i-1] && $b[$i-1] =~ /\d/
                    && ! ($b[$i] =~ /\d/));
                --$ja while ($ja > 0 && $a[$ja] =~ /\d/);
                --$jb while ($jb > 0 && $b[$jb] =~ /\d/);
                if ($ja != $jb) {
                    return ("X", $maina);
                }
                ++$ka while ($ka<=$#a && $a[$ka] =~ /\d/);
                ++$kb while ($kb<=$#b && $b[$kb] =~ /\d/);
                my $maina = join("", @a[0 .. $ja], "---", @a[$ka .. $#a]);
                my $mainb = join("", @b[0 .. $jb], "---", @b[$kb .. $#b]);

                ($main1, $main2) = ($maina, $mainb);
                ($main1, $main2) = ($main2, $main1) if (length($main1)
                    >length($main2));
                if (substr($main2,0,length($main1)) eq $main1
                    && substr($main2,length($main1)) =~ /^[a-zA-Z ]{0,2}$/) {
                    return ("O", $maina);
                }
                else {
                    return ("X", $maina);
                }
                return;
            }
            else {
                return ("X", $a);
            }
        }
    }
}

sub isNumChi ($) {
    my $c = shift;
    if ($c eq '零' || $c eq '一' || $c eq '二' || $c eq '三'
        || $c eq '四' || $c eq '五' || $c eq '六' || $c eq '七'
        || $c eq '八' || $c eq '九' || $c eq '十' || $c eq '百'
        || $c eq '千' || $c eq '幾' ) {
        return 1;
    }
    return 0;
}