#!/usr/bin/env perl
use strict;
use warnings;
use Pithub::Repos::Downloads;
use Getopt::Long;
use File::Basename;

# Perl script to upload file to the github download
# section. Called by upload.sh
 
sub usage
{
  print "Unknown option: @_\n" if ( @_ );
  print "usage: program --location PATH [--filename FILENAME] [--description DESC] [--content-type] [--help|-?]\n";
  print "       location:    file to upload, e.g.  \"../tv_grab_nl_java-0.9.1.zip\"\n";
  print "       filename:    remote filename, defaults to basename of location\n";
  print "       description: defaults to \"tv_grab_nl_java release <version>\"\n";
  print "       content-type: defaults to \"application/zip\"\n";
  exit;
}

my ($version, $filename, $location, $description, $content_type, $help);
  
#-- prints usage if no command line parameters are passed or there is an unknown
#   parameter or help option is passed
usage($ARGV) if ( @ARGV < 1 or
            ! GetOptions('help|?' => \$help, 
            'filename=s' => \$filename, 
            'location=s' => \$location, 
            'description:s' => \$description, 
            'content-type:s' => \$content_type, 
            ) or defined $help );
   
die("--location is obligatory") if !defined($location);

if (!defined($description)) {
  $description = "tv_grab_nl_java release $version";
}
if (!defined($filename)) {
  $filename = basename($location);
}
if (!defined($content_type)) {
  $content_type = "application/zip";
}

open(TOKENFILE,'github-token');
my $token=<TOKENFILE>;
chomp($token);

my $download = Pithub::Repos::Downloads->new(
    user  => 'janpascal',
    repo  => 'tv_grab_nl_java',
    token => $token 
);

my $result = $download->create(
    data => {
        name         => $filename,
        size         => ( stat($location) )[7],
        description  => $description,
        content_type => $content_type
    },
);

if ( $result->success ) {
    my $upload = $download->upload(
        result => $result,
        file   => $location,
    );
    if ( $upload->is_success ) {
        printf "The file has been uploaded succesfully and is now available at: %s\n", $result->content->{html_url};
    } else {
      printf "Error uploading file\n";
    }

} else {
  printf "Error uploading file (2)\n";
}
