<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/flightplan">
<xsl:apply-templates select="body"/>
<xsl:apply-templates select="head"/>
</xsl:template>

<xsl:template match="head">
 <xsl:apply-templates />
</xsl:template>

<xsl:template match="body">
 <xsl:text>#"lat(deg)";"lon(deg)";"altOverStart(m)";altOverWgs84(m);altOverEGM(m);"id";"body";"camRoll(deg)";"camPitch(deg)";"camYaw(deg)"
</xsl:text>
 <xsl:apply-templates />
</xsl:template>

<xsl:template match="waypoint">
 <xsl:value-of select="./@lat" />;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="(./@alt div 100.)+/flightplan/head/startAlt/@alt"/>";<xsl:value-of select="((./@alt div 100.)+/flightplan/head/startAlt/@alt)-/flightplan/head/startAlt/@geoidOffset"/>";<xsl:value-of select="./@id" />;"<xsl:value-of select="./@body" />";<xsl:value-of select="@camRoll" />;<xsl:value-of select="@camPitch" />;<xsl:value-of select="@camYaw" /><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="landingpoint">
 <xsl:value-of select="./@lat" />;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="(./@alt div 100.)+/flightplan/head/startAlt/@alt"/>";<xsl:value-of select="((./@alt div 100.)+/flightplan/head/startAlt/@alt)-/flightplan/head/startAlt/@geoidOffset"/>";<xsl:value-of select="./@id" />;"Landingpoint"<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="dump"></xsl:template>

</xsl:stylesheet>
