#!/usr/bin/env perl
use strict;
use warnings;
use Pithub::Repos::Downloads;

my $version = shift();

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
        name         => "tv_grab_nl_java-$version.zip",
        size         => ( stat("../tv_grab_nl_java-$version.zip") )[7],
        description  => "tv_grab_nl_java release $version" ,
        content_type => 'application/zip',
    },
);

if ( $result->success ) {
    my $upload = $download->upload(
        result => $result,
        file   => "../tv_grab_nl_java-$version.zip",
    );
    if ( $upload->is_success ) {
        printf "The file has been uploaded succesfully and is now available at: %s\n", $result->content->{html_url};
    }
}
