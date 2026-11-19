#!/usr/bin/perl -w
use strict;
require 5.004;
use FileHandle;
use DirHandle;
use CGI qw(:html -no_debug);

require "./Size.pm";
import Image::Size;

my $CONVERTPRG = "c:\\util\\ImageMagick-4.0.2\\convert";
my $DESCFNAME = "desc.txt";

my @desctable = ();

sub tothumb {
    my $fname = shift;
    $fname =~ s/.(gif|jpg)$//;
    "thumb-$fname.jpg";
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

# Each file name looks like this prefix99.jpg .
# This function splits each file name into its prefix and file number 
# components. Then it sorts the file names by the order given in the
# second (ref to array) argument. If two file names have the same prefix,
# the file numbers are compared. File names whose prefixes aren't in the
# order array are given an order of 9999 and will end up at the end of
# the sorted list of file names. In that case, the prefixes themselves
# will be compared instead of the order numbers.
# The function returns a list of the sorted file names.
sub sortfiles {
    my $files = shift;
    my $order = shift;
    local $_;

    map { $_->{filename} } 
	sort {
	    $a->{ordernum} <=> $b->{ordernum} or
		$a->{fileprefix} cmp $b->{fileprefix} or
		$a->{filenum} <=> $b->{filenum};
	} 
	map {
	    my ($fileprefix, $filenum) = /^([a-zA-Z]+)([0-9]*)/;
	    $fileprefix = lc $fileprefix;
	    defined $filenum or $filenum = 0;
	    
	    my $ordernum = 9999;
	    for my $orderindx (0..$#$order) {
		if ($fileprefix eq $order->[$orderindx]) {
		    $ordernum = $orderindx;
		    last;
		}
	    }

	    { 
		filename => $_, 
		ordernum => $ordernum,
		fileprefix => $fileprefix,
		filenum => $filenum
	    };
	} @$files;

} # sortfiles

sub process_dir {
    my $dir = shift;
    my $title = shift;
    my $order = shift;

    my $dh = new DirHandle $dir;
    my @files = grep { not /^thumb-/ and /\.(gif|jpg)$/i and $_ = lc $_ } $dh->read;

    # sort files using the given order.
    @files = sortfiles \@files, $order;
#     print join(' ', @files), "\n";
#     return;

    my $fh = new FileHandle "$dir/index.html", 'w';
    defined $fh or die "can't open $dir/index.html for writing: $!\n";

    my $style = <<END;
img { margin: 10px; border: 1px solid #00f; }
END

    print $fh start_html(-title => $title,
	-head => [
	    meta({-http_equiv=>"Content-Type", -content=>"text/html; charset=utf-8"}),
	    meta({-http_equiv=>"Content-Language", -content=>"en-us"}),
	    meta({-http_equiv=>"imagetoolbar", -content=>"false"}),
	    Link({-rel=>"Start", -href=>"../index.html"})
	],
	-meta=>{
	    "MSSmartTagsPreventParsing"=>"true",
	    "robots"=>"all",
	    "author"=>"Po Shan Cheah",
	    "Copyright"=>"Copyright (c) 1999-2004 Po Shan Cheah"
	},
	-author => 'morton@mortonfox.com', 
	-dtd => ["-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"], 
	-style=>{'src'=>"../../common.css", 'code'=>$style}
# 	-bgcolor => '#FFFFFF', 
# 	-text => '#000000', 
# 	-vlink => '#551A8B', 
# 	-link => '#0000EE', 
# 	-alink => '#FF0000'
	);

    print $fh h1({-align => 'center'}, $title), "\n";

    for my $file (@files) {
	my $fname = $file;
	my $thumbname = tothumb $file;

	my ($width, $height) = makethumb "$dir/$fname", "$dir/$thumbname";

	print $fh 
	    a({ -href => $fname, -class => "imglink" }, 
		img({ -src => $thumbname, 
		    -width => $width, 
		    -height => $height, 
		    -alt => $file})), "\n";
    }

    print $fh hr;

    print $fh div({-id=>"footleft"},
		  a({-href=>"../index.html"}, "Back"));

    print $fh address(p('Po Shan Cheah ', br,
	a({-href => 'mailto:morton@mortonfox.com'}, 'morton@mortonfox.com'),
	br,
	'ICQ: ',
	a({-href => 'http://wwp.mirabilis.com/2896743'}, '2896743')));

    print $fh end_html;
    
    $fh->close;
}

sub readdesc {
    local $_;
    
    my $descfh = new FileHandle $DESCFNAME, "r";
    defined $descfh or die "Can't open $DESCFNAME: $!\n";

    while (<$descfh>) {
	chomp;
	next unless /^\*/;
	my ($tag, $args) = split ' ', $_, 2;

	if ($tag eq "*dir") {
	    my ($dirname) = split ' ', $args, 2;
	    push @desctable, {
		dirname => $dirname,
		title => '',
		order => []
	    };
	}
	elsif ($tag eq "*title") {
	    $desctable[$#desctable]->{title} = $args;
	}
	elsif ($tag eq "*order") {
	    push @{$desctable[$#desctable]->{order}}, split(' ', $args);
	}
    }

#     for (@desctable) {
# 	print "$_->{dirname}\n";
# 	print "$_->{title}\n";
# 	print join(' ', @{$_->{order}}), "\n";
#     }

    close $descfh;

} # readdesc

readdesc;
for (@desctable) {
    process_dir $_->{dirname}, $_->{title}, $_->{order};
}
__END__
