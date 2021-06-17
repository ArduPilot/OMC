<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0"
 encoding="utf-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
<kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2" xmlns:kml="http://www.opengis.net/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom">
  <Document>
    <name><xsl:value-of select="/flightplan/head/name/@name" /></name>
    <xsl:apply-templates />
  </Document>
</kml>
</xsl:template>

<xsl:template match="head">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="body">
 <Placemark>
  <name>Flightplan</name>
  <LineString>
                        <extrude>1</extrude>
                        <tessellate>1</tessellate>
                        <altitudeMode><xsl:choose>
    <xsl:when test="/flightplan/head/startAlt/@geoidOffset">absolute</xsl:when>
    <xsl:otherwise>relativeToGround</xsl:otherwise>
  </xsl:choose></altitudeMode>

     <coordinates>
       <xsl:apply-templates match="waypoint"/>
     </coordinates>
  </LineString>
 </Placemark>
</xsl:template>

<xsl:template match="waypoint" name="waypoint">
 <xsl:param name="id" select="./@id" />
 <xsl:param name="lat" select="./@lat" />
 <xsl:param name="lon" select="./@lon" />
 <xsl:param name="alt" select="./@alt" />
 <xsl:param name="radius" select="./@radius" />
 <xsl:param name="assertaltitude" select="./@assertaltitude" />
 <xsl:value-of select="$lon"/>,<xsl:value-of select="$lat"/>,<xsl:choose>
    <xsl:when test="/flightplan/head/startAlt/@geoidOffset"><xsl:value-of select="(($alt div 100.)+/flightplan/head/startAlt/@alt)-/flightplan/head/startAlt/@geoidOffset"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="$alt div 100"/></xsl:otherwise>
  </xsl:choose> <xsl:text> </xsl:text>

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
