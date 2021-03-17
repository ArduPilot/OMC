#!/usr/bin/php
/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

<?php
use \DateTime;

define('SERVER_HOSTNAME','HOST');

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

 global $debug;
 $debug = FALSE;
 global $svnForce;
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
// 	    run ('rm '.$files); '//dont do this, it fill break stuff!
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
   $ini = parse_ini_file('OpenMissionControl/target/classes/application.properties');
   $dateTime=$ini['git.commit.time'];
   $dateTime = str_replace("@", "", $dateTime);
   $dateTime = str_replace("\.", "-", $dateTime);
   echo($dateTime."\n\n");
   return strtotime($dateTime);
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
		$typeVerbose = "OpenMissionControl" . $platform;
		break;
	default:
		echo 'ERROR!! Unrecognized BML-Typer "'.$type."\"\n";
		exit(-3);
		break;
	}
	echo 'https://HOST/index.php?serialNumber=' . $id . '&type=' . $type . '&file&time='.time()."\n\n";
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



function publish_fileLink($id,$type,$version,$humanReadableName,$bmlFile,$universalID,$platform,$extension) {
// 	$dirUniv = getPublishDir($universalID, $type);
	$dir = getPublishDir($id, $type);
	//$to = $dir.'/';
	global $release;
	if (file_exists($dir) && strlen($dir)>10) {
 	  #run("rm -rf $dir/*$extension"); //dont delete, to make sure multiple binaries could be produced
	}else{
	  mkdir($dir);
	}
// 	$xml = xmlToArray($bmlFile);


	echo "Web-Download Path:\n";
	switch ($type) {
	case 2:
		$typeVerbose = "OpenMissionControl" . $platform;
		break;
	default:
		echo 'ERROR!! Unrecognized BML-Typer "'.$type."\"\n";
		exit(-3);
		break;
	}
	echo 'https://HOST/index.php?serialNumber=' . $id . '&type=' . $type . '&file&time='.time()."\n\n";
	//ATTENTION!! MM: eg._ in filename has a special semantinc in the update process and will be parsed by the gui... so be careful with changing filenames!
	
	$from = '../' .getPublishSubDir($universalID,$type) . "/" . $typeVerbose.'_'.str_replace('_','-',str_replace(':','-',$universalID)) .'_'.$release.'.b'.$version.'.'.$extension;
	$to = $dir.'/' . $typeVerbose.'_'.str_replace('_','-',str_replace(':','-',$id)) .'_'.$release.'.b'.$version.'.'.$extension;

	//run("ln -Tfs $from $to");
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
	
	$javaVersion="jdk-10.0.1";
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

}


function build_cockpit($file) {
	global $debug;
 echo '----------- Building Android Cockpit file '.$file."... -----------\n";
 $xml = xmlToArray($file);
 if (count($xml)==0) return;
 $hardware_id = $xml['BML']['HARDWAREID'];
 $name = $xml['BML']['DISPLAYNAME'];
 $checkMac = $xml['BML']['CHECKMAC'];
 
 $xml['BML']['TYPE'] = 3;
 $rev = svn_rev('.');

 $settingsFile = './MAVinciAndroidPlaneInfo/res/values/mac.xml';
 $settings = '<?xml version="1.0" encoding="utf-8"?>'."\n";
 $settings .= '<resources>' . "\n";
 $settings .= '<string name="check_mac">'.$checkMac.'</string>' ."\n";
 $settings .= '<string name="mac">' . $hardware_id .'</string>' . "\n";
 $settings .= '<string name="default_device_name">'.$name.'</string>' . "\n";
 $settings .= '</resources>';
 write_file($settingsFile,$settings);

 run('cd ./MAVinciAndroidPlaneInfo && ant --noconfig release'. ($debug ? ' -DdebugOn=on':''));
 publish_file($hardware_id,$xml['BML']['TYPE'],'MAVinciAndroidPlaneInfo-release.apk','./MAVinciAndroidPlaneInfo/bin',$rev,'cockpit_'.$name,$file,null);
 $revert[] = './MAVinciAndroidPlaneInfo/res/values/svn_rev.xml';
 $revert[] = './MAVinciAndroidPlaneInfo/res/values/mac.xml';
 $revert[] = './MAVinciAndroidPlaneInfo/AndroidManifest.xml';
 $revert[] = './javaBuild/ant/svn_rev.jar';
 revert($revert);
}


function build_map($file) {
	global $debug;
 echo '----------- Building Android Map file '.$file."... -----------\n";
 $xml = xmlToArray($file);
 if (count($xml)==0) return;
 $hardware_id = $xml['BML']['HARDWAREID'];
 $name = $xml['BML']['DISPLAYNAME']; 
 
 $xml['BML']['TYPE'] = 4;
 
 $rev = svn_rev('.');

 run('cd ./MAVinciAndroidMapViewer && ant --noconfig release'. ($debug ? ' -DdebugOn=on':''));
 publish_file($hardware_id,$xml['BML']['TYPE'],'MAVinciAndroidMapViewer-release.apk','./MAVinciAndroidMapViewer/bin',$rev,'map_'.$name,$file,null);
 $revert[] = './MAVinciAndroidMapViewer/res/values/svn_rev.xml';
 $revert[] = './MAVinciAndroidMapViewer/AndroidManifest.xml';
 $revert[] = './javaBuild/ant/svn_rev.jar';
 revert($revert);
}

function build_gui($file,$ressourcesDir) {
 $xml = xmlToArray($file);
 if (count($xml)==0) return;
 $platforms = explode(",",$xml['BML']['PLATFORM']);
 echo $xml['BML']['PLATFORM']."\n";
 
//  var_dump ($platforms);
 foreach ($platforms as $platform){
  build_gui_platform($file,$resourceDir,$platform);
 }
}

function build_gui_platform($file,$ressourcesDir,$platform) {
 global $debug;
 global $release;
 echo '----------- Building GUI file '.$file."...  for plattform $platform -----------\n";
 $xml = xmlToArray($file);
 if (count($xml)==0) return;

 $universalID = "MAVinciDesktop.$platform.debug".($debug?"On":"Off");
 
 $rev = git_rev();
 
 $platformOrg = $platform;
 
 //if (assertUniv($universalID)){
    
    if (strtolower($platform) == "deb64" || strtolower($platform) == "rpm64"){
      $platform = "Linux64";
    } else if (strtolower($platform) == "deb32" || strtolower($platform) == "rpm32"){
      $platform = "Linux32";
    }

    $dirBase = './OpenMissionControl/';
    $dir = $dirBase;

	run ('rm -rf ~/.m2/repository/com/airmap');//cleanup maven cache, important for context switches.. DONT DROP THE ENTIRE .m2 folder, otherwise proxy config will be gone
	run ('rm -rf ~/.m2/repository/gov');//cleanup maven cache, important for context switches.. DONT DROP THE ENTIRE .m2 folder, otherwise proxy config will be gone
    run ('"/home/compiler/maven/apache-maven-3.5.0/bin/mvn" -U clean install -Dmaven.test.skip=true');
    run ('"/home/compiler/maven/apache-maven-3.5.0/bin/mvn" -pl OpenMissionControl package dependency:copy-dependencies -Dmaven.test.skip=true');
	
	$rev = git_rev();

    run ('mkdir OpenMissionControl/setupFiles/ || true');
	run ('rm -rf OpenMissionControl/setupFiles/*');
   
	run ('mkdir OpenMissionControl/wrapper/ || true');
	run ('rm -rf OpenMissionControl/wrapper/*');
    run('cd OpenMissionControl/ant && pwd && ant --noconfig -f buildWinGeneral.xml'. ($debug ? ' -DdebugOn=on':'').(' -Dwin_version='.$release.'.'.$rev).(' -Dversion='.$release.'.b'.$rev));

    run('cd ' . $dir . 'ant && pwd && ant --noconfig -f build' . $platform . '.xml'. ($debug ? ' -DdebugOn=on':'').(' -Dwin_version='.$release.'.'.$rev).(' -Dversion='.$release.'.b'.$rev));

    $file_jar = 'OpenMissionControl.jar';
    

//     if ($jar == "true") {
//       $targetFile = $file_jar;
//     } else {
    //if (substr($platform,0,3) == 'Win') {
	//the ant skript allready made an installer...
	$targetFile = 'OpenMissionControl.exe';

	//$dir .= 'wrapper/';

    /*} else if (substr($platform,0,3) == 'Mac') {
      //the ant skript allready made an installer...
      $targetFile = 'MAVinciDesktop'.$platform.'*.dmg';
      $dir .= 'setupFiles/';
    } else { //make an RPM or debian file
      run('rm -f ./RPM/ressources-gui/opt/mavinci/lib/*.so');
      run('cp '.$dir.'lib/lib*.so ./RPM/ressources-gui/opt/mavinci/lib/');
      mycopy($dir.$file_jar,'./RPM/ressources-gui/opt/mavinci/bin/mavinci-desktop.jar');
      mycopy('./MAVinciDesktopBase/lib/jfreechart-1.0.13.jar','./RPM/ressources-gui/opt/mavinci/bin/jfreechart-1.0.13.jar');
      mycopy('./MAVinciDesktopBase/lib/jcommon-1.0.16.jar','./RPM/ressources-gui/opt/mavinci/bin/jcommon-1.0.16.jar');
      mycopy('./MAVinciDesktopBase/lib/jssc-2.8.0.jar','./RPM/ressources-gui/opt/mavinci/bin/jssc-2.8.0.jar');
      mycopy('./ftp4j/ftp4j-1.7.2.m1.jar','./RPM/ressources-gui/opt/mavinci/bin/ftp4j-1.7.2.m1.jar');
      
      mycopy('./MAVinciDesktopBase/lib/cacerts','./RPM/ressources-gui/opt/mavinci/jre/lib/security/cacerts');

      run('rsync -avr --exclude \'.svn\' ' . './pix4d/files/* ./RPM/ressources-gui/opt/mavinci/bin/pix4dUpload/');
      run('cp ' . './WorldWindJava/lib-external/gdal/data/* ./RPM/ressources-gui/opt/mavinci/bin/gdal-data/');
      clearRPMS();
      
	#openvpn key handling
	$vpnTargetDir = "./RPM/ressources-gui/etc/openvpn";
	
	run("rm -rf $vpnTargetDir");
	
      //running RPM build
      run('cd ./RPM/ && ./makerpm_gui.sh');
    
      $dir = './RPM/RPMS/x86_64/';
      $targetFile = 'mavinci*.rpm';

      if (strtolower(substr($platformOrg,0,3))=='deb'){
	run('cd '. $dir .'&& fakeroot alien -v '.$targetFile);
	$targetFile =  'mavinci*.deb';
      }
    }*/
//     }

    echo 'Target file '.$targetFile. '-----------------\n';
    $hardware_id = $xml['BML']['HARDWAREID'];
    publish_file($hardware_id,2,"OpenMissionControlSetup.exe","./OpenMissionControl/setupFiles/",$rev,null,$file,$platform);
    //$revert[] = './MAVinciDesktopBase/resource/eu/mavinci/strings/svnRev.properties';
    //$revert[] = './javaBuild/ant/svn_rev.jar';
    revert($revert);
 //}
  
  //if (substr($platformOrg,0,3) == 'Win') {
    $extension = "exe";
  //} else if (substr($platformOrg,0,3) == 'Mac') {
//    $extension = 'dmg';
//  } else if (strtolower(substr($platformOrg,0,3))=='deb'){
    //$extension = 'deb';
//  } else {
    //$extension = 'rpm';
  //}
 
 $hardware_id = $xml['BML']['HARDWAREID'];
 
 //generate symlinks in update system
 publish_fileLink($hardware_id,2,$rev,null,$file,$universalID,$platform,$extension);
 
}


?>
