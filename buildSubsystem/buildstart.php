#!/usr/bin/php
/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

<?php
$file = dirname(getcwd().'/'.$argv[0]);

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

 echo "\n\n=======================================\nresetting all asserts in this branch: " . dirname($file) . "\n";
 run ('rm -rf '.$file.'/asserts/*.lock');
 echo "\n\n\n";
?>
