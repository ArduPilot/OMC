<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:strip-space elements="*"/>
<xsl:template match="/flightplan">B UTF-8
G WGS 84
U 1
R 0,<xsl:value-of select="/flightplan/head/name/@name" />,5,-1
<xsl:apply-templates select="body"/>
<xsl:apply-templates select="head"/>
</xsl:template>

<xsl:template match="head">
 <xsl:apply-templates />
</xsl:template>

<xsl:template match="body">
 <xsl:apply-templates />
</xsl:template>
 
<xsl:template match="waypoint">W Rwp<xsl:number level="any" count="waypoint"/> A <xsl:choose><xsl:when test="./@lon >=0"><xsl:value-of select="./@lon" />°N</xsl:when><xsl:otherwise><xsl:value-of select="-./@lon" />°S</xsl:otherwise></xsl:choose> <xsl:choose><xsl:when test="./@lat >=0"><xsl:value-of select="./@lat" />°E</xsl:when><xsl:otherwise><xsl:value-of select="-./@lat" />°W</xsl:otherwise></xsl:choose> 1-JAN-16 00:00:00 <xsl:choose>
    <xsl:when test="/flightplan/head/startAlt/@geoidOffset"><xsl:value-of select="((./@alt div 100.)+/flightplan/head/startAlt/@alt)-/flightplan/head/startAlt/@geoidOffset"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="./@alt div 100"/></xsl:otherwise>
  </xsl:choose><xsl:text>
</xsl:text>w Point,0,-1.0,-1,-65536,1,5,,0.0,,-1,0
</xsl:template>

<xsl:template match="dump"></xsl:template>

</xsl:stylesheet>
