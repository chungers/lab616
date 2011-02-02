#!/usr/bin/perl
# spm_jump2.pl
# 6.3. Data Recovery by Negative Acknowledgment

use strict;
use PGM::Test;

BEGIN { require "test.conf.pl"; }

$| = 1;

my $mon = PGM::Test->new(tag => 'mon', host => $config{mon}{host}, cmd => $config{mon}{cmd});
my $sim = PGM::Test->new(tag => 'sim', host => $config{sim}{host}, cmd => $config{sim}{cmd});
my $app = PGM::Test->new(tag => 'app', host => $config{app}{host}, cmd => $config{app}{cmd});

$mon->connect;
$sim->connect;
$app->connect;

sub close_ssh {
	$mon = $sim = $app = undef;
	print "finished.\n";
}

$SIG{'INT'} = sub { print "interrupt caught.\n"; close_ssh(); };

$mon->say ("filter $config{app}{ip}");
print "mon: ready.\n";

$app->say ("create ao");
$app->say ("bind ao");
$app->say ("connect ao");
$app->say ("listen ao");

$sim->say ("create fake ao");
$sim->say ("bind ao");
$sim->say ("connect ao");

print "sim: publish SPM txw_trail 90,001 txw_lead 90,000 at spm_sqn 3200.\n";
$sim->say ("net send spm ao 3200 90001 90000");

# no NAKs should be generated.
print "sim: waiting 2 seconds for erroneous NAKs ...\n";
$sim->die_on_nak({ 'timeout' => 2 });
print "sim: no NAKs received.\n";

print "sim: publish SPM txw_trail 90,001 txw_lead 90,001 at spm_sqn 3201.\n";
$sim->say ("net send spm ao 3201 90001 90001");

print "sim: waiting for valid NAK.\n";
$sim->wait_for_nak;
print "sim: NAK received.\n";

print "test completed successfully.\n";

$mon->disconnect (1);
$sim->disconnect;
$app->disconnect;
close_ssh;

# eof
