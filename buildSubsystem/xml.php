<?php

/**
* 13.11.2012 Extension by Marco to make it automatically detect multiple nodes with same name, and somehow deal with it!
*/

function xmlToArray($file) {
 $xml_parser = xml_parser_create();
 
 if (!($fp = fopen($file, "r"))) {
     die("could not open XML input");
 }
 
 $data = fread($fp, filesize($file));
 fclose($fp);
 xml_parse_into_struct($xml_parser, $data, $vals, $index);
 xml_parser_free($xml_parser);
 
 $params = array();
 $level = array();
 foreach ($vals as $xml_elem) {
   if ($xml_elem['type'] == 'open') {
     if (array_key_exists('attributes',$xml_elem)) {
       list($level[$xml_elem['level']],$extra) = array_values($xml_elem['attributes']);
     } else {
       $level[$xml_elem['level']] = $xml_elem['tag'];
     }
   }
   if ($xml_elem['type'] == 'complete') {
     $start_level = 1;
     $php_stmt = '$params';
     while($start_level < $xml_elem['level']) {
       $php_stmt .= '[$level['.$start_level.']]';
       $start_level++;
     }
     $php_stmt .= '[$xml_elem[\'tag\']]';
     $exist = false;
     eval('$exist = isset('.$php_stmt.');');
     if ($exist){
      $isarr = false;
      eval('$isarr = is_array('.$php_stmt.');');
      if (!$isarr) eval($php_stmt .' = array('.$php_stmt.');');
      $php_stmt .= '[]';
     }

     //set value
     eval($php_stmt. ' = isset($xml_elem[\'value\']) ? $xml_elem[\'value\'] : NULL;');
   }
 }
/* 
 echo "<pre>";
 print_r ($params);
 echo "</pre>";*/
 return $params;
}
?>
