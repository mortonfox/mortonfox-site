#!/usr/bin/perl -w
use strict;
require 5.004;
use FileHandle;
use CGI qw(:html -no_debug);

require "./Size.pm";
import Image::Size;

my $CONVERTPRG = "c:\\util\\ImageMagick-4.0.2\\convert";

my $DESCFILE = "plushdesc.txt";

my @plushdb = ();

my $plushtotal = 0;
my $uniquetotal = 0;

sub readdesc {
    my $preamble = 1;
    my $fh;
    my $fname;
    my $title;
    my $dbentry = undef;
    local $_;
    
    $fh = new FileHandle $DESCFILE, 'r' or die "Can't open $DESCFILE: $!\n";

    while (<$fh>) {
	chomp;
	
	if ($preamble) {
	    if (/^\*/) {
		$preamble = 0;
	    }
	    else {
		next;
	    }
	}

	if (/^\*/) {
	    if (defined $dbentry) {
		push @plushdb, $dbentry;
	    }
	    $dbentry = {};
	    ( undef, $fname, $title ) = split(' ', $_, 3);
	    $dbentry->{fname} = $fname;
	    $dbentry->{title} = $title;
	    $dbentry->{files} = [];
	}
	else {
	    ++$uniquetotal unless s/^=//;
	    ++$plushtotal;
	    ( $fname, $title ) = split(' ', $_, 2);
	    next unless defined $title;
	    my $fileentry = {};
	    $fileentry->{fname} = lc $fname;
	    $fileentry->{title} = $title;
	    push @{$dbentry->{files}}, $fileentry;
	}
    }

    if (defined $dbentry) {
	push @plushdb, $dbentry;
    }

    $fh->close;

    print "$plushtotal total pictures.\n$uniquetotal unique plushies.\n";
}

sub toimage {
    my $fname = shift;
    "images/$fname";
}

sub tothumb {
    my $fname = shift;
    $fname =~ /^(.+)\.(gif|jpg)$/;
    "images/thumb-$1.jpg";
}

sub makethumb {
    my $fname = shift;
    my $thumbname = shift;

    unless (-e $thumbname) {
	print "Making thumbnail: $fname -> $thumbname\n";

# Make a thumbnail image. The thumbnail is a maximum of 75x75 pixels
# and it is not interlaced. Also, the thumbnail will always be a JPEG file
# no matter what the format of the original image file.

	system "$CONVERTPRG -geometry 75x75 -interlace NONE " . 
	    "$fname $thumbname";
    }
    my ( $thumbw, $thumbh ) = imgsize($thumbname);
    return ( $thumbw, $thumbh );
} #makethumb

sub genpage {
    my $dbentry = shift;
    my $fh;
    local $_;

    $fh = new FileHandle $dbentry->{fname}, 'w';

    print $fh start_html(-title => $dbentry->{title}, 
	-head => [
	    meta({-http_equiv=>"Content-Type", -content=>"text/html; charset=utf-8"}),
	    meta({-http_equiv=>"Content-Language", -content=>"en-us"}),
	    meta({-http_equiv=>"imagetoolbar", -content=>"false"}),
	    Link({-rel=>"Start", -href=>"index.html"})
	],
	-meta=>{
	    "MSSmartTagsPreventParsing"=>"true",
	    "robots"=>"all",
	    "author"=>"Po Shan Cheah",
	    "Copyright"=>"Copyright (c) 1999-2004 Po Shan Cheah"
	},
	-author => 'morton@mortonfox.com', 
	-dtd => ["-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"], 
	-style=>{'src'=>"../common.css"}
# 	-bgcolor => '#FFFFFF', 
# 	-text => '#000000', 
# 	-vlink => '#551A8B', 
# 	-link => '#0000EE', 
# 	-alink => '#FF0000'
	);

    print $fh h1({-align => 'center'}, $dbentry->{title});

    my $rows = '';
    my @files = sort { $a->{fname} cmp $b->{fname} } @{$dbentry->{files}};

    my $PICSPERROW = 3;
    my $i;

    for ($i = 0; $i < @files; $i += $PICSPERROW) {
	my $row = '';
	my $j;
	for ($j = $i; $j < $i + $PICSPERROW and $j < @files; ++$j) {
	    my $fname = toimage $files[$j]->{fname};
	    my $thumbname = tothumb $files[$j]->{fname};
	    my ( $width, $height ) = makethumb $fname, $thumbname;
	    $row .= td({ -width => 150 }, 
	    	p,
		a({ -href => $fname, -class=>"imglink" }, 
		    img({ -src => $thumbname, 
		    	-width => $width, 
			-height => $height, 
			-alt => $fname})
		), br,
		$files[$j]->{title}) . "\n";
	}
	$rows .= Tr({ -align => 'center', -valign => 'top'}, "\n$row") . "\n";
    }

    print $fh "\n";

    print $fh table({-border => 0, -cellpadding => 5, -cellspacing => 2, 
	-align => 'center'}, "\n$rows");

    print $fh hr;

    print $fh div({-id=>"footleft"},
		  a({-href=>"index.html"}, "Back"));

    print $fh address(p('Po Shan Cheah ', br,
	a({-href => 'mailto:morton@mortonfox.com'}, 'morton@mortonfox.com'),
	br,
	'ICQ: ',
	a({-href => 'http://wwp.mirabilis.com/2896743'}, '2896743')));


    print $fh end_html;
    
    $fh->close;
}

readdesc;
for (@plushdb) {
    genpage $_;
}

# for my $x (@plushdb) {
#     print "== $x->{fname} $x->{title}\n";
#     for my $y (@{$x->{files}}) {
# 	print "$y->{fname} $y->{title}\n";
#     }
# }
