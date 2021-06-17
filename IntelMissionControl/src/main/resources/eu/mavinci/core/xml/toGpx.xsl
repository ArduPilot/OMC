<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0"
 encoding="utf-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
<gpx xmlns="http://www.topografix.com/GPX/1/1">
  <rte>
    <xsl:apply-templates />
  </rte>
</gpx>
</xsl:template>

<xsl:template match="head">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="body">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="waypoint">
  <rtept lat="{./@lat}" lon="{./@lon}">
    <ele><xsl:choose>
    <xsl:when test="/flightplan/head/startAlt/@geoidOffset"><xsl:value-of select="((./@alt div 100.)+/flightplan/head/startAlt/@alt)-/flightplan/head/startAlt/@geoidOffset"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="./@alt div 100"/></xsl:otherwise>
  </xsl:choose></ele>
    <magvar>NaN</magvar>
    <name>MAV_<xsl:value-of select="./@id" /></name>
  </rtept>
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
