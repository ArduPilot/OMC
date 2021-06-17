<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:java="http://xml.apache.org/xslt/java" 
exclude-result-prefixes="java">  
<xsl:output method="xml" version="1.0"
 encoding="utf-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
<flight-plan xmlns="http://www8.garmin.com/xmlschemas/FlightPlan/v1">
  <created><xsl:value-of  select="java:format(java:java.text.SimpleDateFormat.new('yyyy-MM-dd HH:mm:ss'), java:java.util.Date.new())"/></created>
    <xsl:apply-templates />
</flight-plan>
</xsl:template>

<xsl:template match="head">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="body">
  
        <waypoint-table>
            <xsl:apply-templates select="waypoint"/>
        </waypoint-table>
        <route>
        <route-name>FLIGHT_PLAN</route-name>
		<flight-plan-index>1</flight-plan-index>
            <xsl:apply-templates select="waypoint" mode="ro"/>
        </route>
  
</xsl:template>


<xsl:template match="waypoint">
  <waypoint> 
    <identifier>MAV<xsl:value-of select="./@id" /></identifier>
    <type>USER WAYPOINT</type>
    <country-code>ED</country-code>
    <lat><xsl:value-of select="./@lat" /></lat>
    <lon><xsl:value-of select="./@lon" /></lon>
    <comment />
  </waypoint>
</xsl:template>

<xsl:template match="waypoint" mode="ro">
    <route-point>
      <waypoint-identifier>MAV<xsl:value-of select="./@id" /></waypoint-identifier>
      <waypoint-type>USER WAYPOINT</waypoint-type>
      <waypoint-country-code>ED</waypoint-country-code>
    </route-point>
</xsl:template>

<xsl:template match="landingpoint">
</xsl:template>


<xsl:template match="startingprocedure">
</xsl:template>


<xsl:template match="loop">
 <xsl:apply-templates />
</xsl:template>

<xsl:template match="photo" name="photo">
</xsl:template>

<xsl:template match="photosettings">
</xsl:template>


<xsl:template match="dump">
</xsl:template>

</xsl:stylesheet>
