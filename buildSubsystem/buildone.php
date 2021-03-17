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


//build in copy seems not to overwrite files
function mycopy($from, $to){
    run('cp "' .$from. '" "' . $to.'"');
}


 global $myOS;
 global $myArch;
 $myArch = run("uname -m");
 echo "this architecture is: $myArch\n";
 require_once("xml.php");
 require_once("crypto_helper.php");

 global $debug;
 $debug = FALSE;
 global $svnForce;
 $svnForce = FALSE;
 
 global $licenceOnly;
 $svnForce = FALSE;

 if (!file_exists('~/.rpmmacros')=="") {echo "no rpmmacros not found - this will cause a rm -rf /* if you run the SPEC files.";exit(1);}
 echo "MAVinci build script for ONE target\n";
 echo "\n";
 echo "usage:\n";
 echo "first arg: destination folder for binaries\n";
 echo "second arg: bml file to build\n";
 echo "third arg: resources dir\nFollowed by a subset of options:\n";
 echo "option -d compile debugging targets (not for customers!)\n";
 echo "option -m build even if svn is not in sync or modified!\n";
 echo "option -n updatig every used breanch to the newest revision prior building and ATTENTION: and reverting local changes (if running on server)!!\n";
 echo "option -r REVISION updatig every used breanch to the requested revision prior building and ATTENTION: and reverting local changes (if running on server)!!\n";
 echo "option -lo licence build only without GUI installers\n";
 
 build();

function clearRPMS(){
	echo "Clear RPMS directory\n";
	run('rm -rf ./RPM/RPMS/*');
}

function clearTGZS(){
	echo "Clear TGZS directory\n";
	run('rm -rf ./TGZ/TGZS/*');
}


function revert($files){
	if (is_array($files) ){
	    foreach ($files as $file) {
	      revert($file);
	    }
	} else {
// 	    run ('rm '.$files); '//dont do this, it will break stuff!
	    run('git checkout -- '.$files);
	}
}


	//$thisFile = dirname(getcwd().'/'.$argv[0]);

/**
* returns true, if the lock is created the first time, otherwise false
*/
function assertUniv($name){
 $lock = 'buildSubsystem/asserts/'.$name.'.lock';
 if (file_exists($lock)) return FALSE;
//  run('pwd');
 echo "\ncreating Assert Lockfile: $lock\n\n";
 touch($lock);
 return TRUE;
}


function svn_rev($path) {
	$subject = shell_exec('svnversion -c -n "'.$path.'"');
// 	echo "org:".$subject;
	$pos = strpos($subject, ":");
// 	echo "pos:".$pos."\n";
	if ($pos !== FALSE) $subject=substr($subject,$pos+1);
// 	echo "after: split ".$subject."\n";
	if (!is_numeric($subject)) $subject=substr($subject,0,-1);
// 	echo "final:".$subject."\n";
	return $subject;
}

function git_rev() {
   $ini = parse_ini_file('IntelMissionControl/target/classes/application.properties');
   $dateTime=$ini['git.commit.time'];
   $dateTime = str_replace("@", "", $dateTime);
   $dateTime = str_replace("\.", "-", $dateTime);
   $date = new DateTime($dateTime);
   return $date->format('YmdHi');
   //return strtotime($dateTime);
}

function write_file($path,$content) {
	//echo 'publish version info: '.$id.'->'.$version;
 	echo "write to file :" . $path . ": " . $content."\n";
	$hnd = fopen($path,'w');
	if ($hnd===FALSE) die(123);
	fwrite($hnd,$content);
	fclose($hnd);
}

function getPublishDir($id,$type){
	global $binariesDir;
	return $binariesDir.'/'.getPublishSubDir($id,$type);
}

function getPublishSubDir($id,$type){
	return str_replace(':','_',$id).'__'.$type;
}

function publish_version($id,$type,$version) {
	//echo 'publish version info: '.$id.'->'.$version;
	$to = getPublishDir($id, $type).'/version';
	write_file($to,$version);
	run("cp version.txt ".getPublishDir($id,$type));
}

function publish_bml($id,$type,$bmlfile) {
	mycopy($bmlfile,getPublishDir($id, $type).'/bml');
}

function GetFileExtension($Filepath)
{
    $PathInf = pathinfo($Filepath);
    return $PathInf['extension'];
}

function publish_file($id,$type,$filename,$from_path,$version,$humanReadableName,$bmlFile,$platform) {
	$dir = getPublishDir($id, $type);
	//$to = $dir.'/';
	global $release;
	$extension = GetFileExtension($filename);
	
	if (file_exists($dir) && strlen($dir)>10) {
 	  run("rm -rf $dir/*$extension"); //dont delete, to make sure multiple binaries could be produced
	}else{
	  mkdir($dir);
	}
	
// 	$xml = xmlToArray($bmlFile);

	echo "Web-Download Path:\n";
	switch ($type) {
	case 2:
		$typeVerbose = "IntelMissionControl" . $platform;
		break;
	case 5:
		$typeVerbose = "IMClicence";
		echo 'https://u.mavinci.de/index.php?serialNumber=' . $id . '&type=' . $type . "&qr\n";
		break;
	default:
		echo 'ERROR!! Unrecognized BML-Typer "'.$type."\"\n";
		exit(-3);
		break;
	}
	echo 'https://u.mavinci.de/index.php?serialNumber=' . $id . '&type=' . $type . '&file&time='.time()."\n\n";
	//ATTENTION!! MM: eg._ in filename has a special semantinc in the update process and will be parsed by the gui... so be careful with changing filenames!
	
	$to = $dir.'/' . $typeVerbose.'_'.str_replace('_','-',str_replace(':','-',$id)) .'_'.$release.'.b'.$version.'.'.$extension;
	

	run("mv ".$from_path.'/'.$filename." ". $to);
	global $binariesDir;
	if ($humanReadableName != null && $humanReadableName != '') run('ln -Tfs "'.getPublishSubDir($id,$type).'" "' . $binariesDir.'/'.$humanReadableName.'"');
	//if ($bmlFile != '' && $type != 2) publish_bml($id,$type,$bmlFile); //published bmls where never used, and are a break of the encryption, so dont publish them anymore!!
	if ($bmlFile != '') publish_bml($id,$type,$bmlFile); //published bmls where never used, and are a break of the encryption, so dont publish them anymore!!
	if ($version != '') publish_version($id,$type,$version);
	return $to;
}




function get_filenames($dir) {
	echo "dir listing" .$dir. "\n";
	$ret = array();
	if (is_dir($dir)) {
	    if ($dh = opendir($dir)) {
	        while (($file = readdir($dh)) !== false) {
	            if ($file{0}!='.' && substr($file,-4)=='.bml') {
	              $ret[] = $dir.'/'.$file;
	            } else if (is_dir($dir.'/'.$file) && $file != ".." && $file!="."){
		      $tmp =  get_filenames($dir.'/'.$file);
		      foreach($tmp as $t) $ret[] = $t;
		    }
	        }
	        closedir($dh);
	    }
	 } else { 
	 	echo "Cannot find ".$dir." directory.\n";
		exit(-6);
	 }
	 return $ret;
}

function build() {// loop over all BML files, ore use first given argument as bml to build
	//echo "Calling this without an commandline arguments uses every BML file in the subfolder bml/ of the PWD, or you can specify one as first argument\n";
	
	$javaVersion="jdk-11.0.2";
	putenv("JAVA_BINDIR=/usr/java/$javaVersion/bin");
	putenv("JAVA_HOME=/usr/java/$javaVersion");
	putenv("JAVA_ROOT=/usr/java/$javaVersion");
	putenv("JDK_HOME=/usr/java/$javaVersion");
	putenv("JRE_HOME=/usr/java/$javaVersion/jre");
	if (strrpos(getenv("PATH"), getenv("JAVA_BINDIR")) !== 0){
	  print("patch PATH");
	  putenv("PATH=".getenv("JAVA_BINDIR").":".getenv("PATH"));
	}
// 	run("export");
	print("new PATH= " . getenv("PATH"));
	//exit(-1);
	
	global $argv;
	global $argc;
	global $binariesDir;
	global $release;
	global $debug;
	global $svnForce;
	global $revisionTarget;
	global $licenceOnly;
	$svnUpdate = FALSE;
	$revisionTarget = -1;

// 	var_dump($argv);
	if ($argc < 3) exit(4);

	$binariesDir= $argv[1];
	$file = $argv[2];
	$i = 3;
	//$ressourceDir="/home/compiler/drohne-src/buildSystem/bmls/guis.d/internal/Git_master.bml";
	if ($argc > 3 && $argv[3]{0}!='-') {
	  $ressourcesDir = $argv[3];
	  echo "resource dir found\n";
	  $i = 4;
	} else {
	  $ressourcesDir =FALSE;
	}
        
	for (; $i < count($argv); $i++){
	  if ($argv[$i] == '-d'){
	    $debug = TRUE;
	  } else if ($argv[$i] == '-m'){
	    $svnForce = TRUE;
	  } else if ($argv[$i] == '-n'){
	    $svnUpdate = TRUE;
	  } else if ($argv[$i] == '-r'){
	    $svnUpdate = TRUE;
	    $i++;
	    $revisionTarget = intval($argv[$i]);
	  } else if ($argv[$i] == '-lo'){
	    $licenceOnly=TRUE;
	  } 
	  
	}
	if (!is_file($file)) exit(1);
	$xml = xmlToArray($file);
	if (count($xml)==0) exit(2);
	if (count($xml['BML'])==0) exit(3);

	
	if ($debug) echo "DebugMode is:ON\n\n";
	if ($svnForce) echo "SVN Force Mode is:ON\n\n";
	if ($svnUpdate) echo "SVN Update Mode is:ON\n\n";
	
	//exit(4);
	//if ($svnUpdate && assertUniv('svnUpdate')){
	  //run('./gitUpdate.php');
	//}

	
	
	if (!$svnForce && assertUniv('svnCheck')){
	    $ret = run('svnversion');
	    if (strstr($ret,":") !== FALSE) {
	    echo "\n\nthe current branch has not a unique SVN revision. blease correct this!! Version is:$ret\n\n";
	    debug_print_backtrace();
	    die(-668);
	    }
	    if (strstr(strtoupper($ret),"M") !== FALSE) {
	    echo "\n\nthe current branch is not comleatly commited. blease correct this!! Version is:$ret\n\n";
	    debug_print_backtrace();
	    die(-667);
	    }
	}

	
	echo "\n\n\nbuilding in this dir: " . getcwd()."\n";
	$release = trim(implode('', file('version.txt')));
	echo "building Release Version:$release";
	build_gui_licence($file, $ressourcesDir);
	

}


function build_gui($file,$ressourcesDir, $licenceFile) {
 $xml = xmlToArray($file);
 if (count($xml)==0) return;
 $platforms = explode(",",$xml['BML']['PLATFORM']);
 echo $xml['BML']['PLATFORM']."\n";
 
//  var_dump ($platforms);
 foreach ($platforms as $platform){
  build_gui_platform($file,$ressourcesDir,$platform, $licenceFile);
 }
}

function build_gui_platform($file,$ressourcesDir,$platform, $licenceFile) {
	global $debug;
	global $release;
	echo '----------- Building GUI file '.$file."...  for Windows -----------\n";
	$xml = xmlToArray($file);
	if (count($xml)==0) return;

	$licenceTarget = 'IntelMissionControl/src/main/resources/eu/mavinci/default.mlf';
	$revert[] = $licenceTarget;
	run('cp '.$licenceFile.' '.$licenceTarget);

	run('rm -rf ~/.m2/repository/com/airmap');//cleanup maven cache, important for context switches.. DONT DROP THE ENTIRE .m2 folder, otherwise proxy config will be gone
	run('rm -rf ~/.m2/repository/gov');//cleanup maven cache, important for context switches.. DONT DROP THE ENTIRE .m2 folder, otherwise proxy config will be gone
	
	run('cd IntelMissionControl/ant && pwd && ant --noconfig -f obfuscatePy.xml'. ($debug ? ' -DdebugOn=on':'').(' -Dwin_version='.$release.'.'.$rev).(' -Dversion='.$release.'.b'.$rev));
	
	run('"/home/compiler/maven/apache-maven-3.5.0/bin/mvn" -U clean install -Dmaven.test.skip=true -Dlint4gj.skip=true -DversionExt='.$release.($debug ? ' -DdebugOn=on':''));
	run('"/home/compiler/maven/apache-maven-3.5.0/bin/mvn" -pl IntelMissionControl package dependency:copy-dependencies -Dmaven.test.skip=true -Dlint4gj.skip=true -DversionExt='.$release.($debug ? ' -DdebugOn=on':''));

	$rev = git_rev();

	run('mkdir IntelMissionControl/setupFiles/ || true');
	run('rm -rf IntelMissionControl/setupFiles/*');

	run('mkdir IntelMissionControl/wrapper/ || true');
	run('rm -rf IntelMissionControl/wrapper/*');
	run('cd IntelMissionControl/ant && pwd && ant --noconfig -f installer.xml'. ($debug ? ' -DdebugOn=on':'').(' -Dwin_version='.$release.'.'.$rev).(' -Dversion='.$release.'.b'.$rev));
	
	echo 'Target file '.$targetFile. '-----------------\n';
	$hardware_id = $xml['BML']['HARDWAREID'];
	publish_file($hardware_id,2,"IntelMissionControlSetup.exe","./IntelMissionControl/setupFiles/",$rev,null,$file,$platform);
	revert($revert);
	
}


?>
