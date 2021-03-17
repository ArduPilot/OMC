#!/usr/bin/php
/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

<?php
use \DateTime;

define('SERVER_HOSTNAME','intern.mavinci.de');

function run($cmd){
	echo $cmd."\n";
	$out = system ($cmd, $ret);
	if ($ret != 0){
	    echo "\n\nBUILD-ALL-FAILED with error code $ret\nBacktrace:\n";
	    debug_print_backtrace();
	    die(-1);
	}
	return $out;
}

run('git fetch https://ekorotkova:KOR-2017@nearshore.altran.pt/git/root/INMAV/');
run('git pull https://ekorotkova:KOR-2017@nearshore.altran.pt/git/root/INMAV/');

?>
