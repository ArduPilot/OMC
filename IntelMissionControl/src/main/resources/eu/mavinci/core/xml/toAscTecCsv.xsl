<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:variable name="changeHeightF" select="0."/>
<xsl:variable name="changeHeightP" select="0."/>
<xsl:variable name="changeHeightParent" select="0."/>
<xsl:param name="pitchOffset" select="-90"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/flightplan">Type;Longitude;Latitude;Height;Heading;Camera pitch;Velocity;Comment
<xsl:apply-templates select="body"/>
<xsl:apply-templates select="head"/>
</xsl:template>

<xsl:template match="head">
 <xsl:apply-templates />
</xsl:template>

<xsl:template match="wbody">
</xsl:template>

 
<xsl:template match="body">
 <xsl:apply-templates />
</xsl:template>

<xsl:template match="waypoint">
 
 <xsl:variable name="changeHeightF" select="(following-sibling::waypoint[1]/@alt div 100.)"/>
 <xsl:variable name="changeHeightP" select="(preceding-sibling::waypoint[1]/@alt div 100.)"/>
 <xsl:variable name="changeHeightParent" select="(../preceding-sibling::waypoint[1]/@alt div 100.)"/>
   
<xsl:choose>

<xsl:when test="$changeHeightP!=(./@alt div 100.) and string(number($changeHeightP)) != 'NaN' ">
<xsl:text>S</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;<xsl:value-of select="@camPitch +$pitchOffset" />;<xsl:value-of select="(/flightplan/head/photosettings/@maxGroundSpeedKMH div 3.6)"/>;<xsl:value-of select="./@id"/>, following height: <xsl:value-of select="$changeHeightF"/>, previous height: <xsl:value-of select="$changeHeightP"/>, parent height: <xsl:value-of select="$changeHeightParent"/><xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="string(number($changeHeightP)) = 'NaN' and string(number($changeHeightParent)) = 'NaN'  ">
<xsl:text>M</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;<xsl:value-of select="@camPitch +$pitchOffset" />;<xsl:value-of select="(/flightplan/head/photosettings/@maxGroundSpeedKMH div 3.6)"/>;<xsl:value-of select="./@id"/>, following height: <xsl:value-of select="$changeHeightF"/>, previous height: <xsl:value-of select="$changeHeightP"/>, parent height: <xsl:value-of select="$changeHeightParent"/><xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="string(number($changeHeightP)) = 'NaN' and $changeHeightParent != (./@alt div 100.) ">
<xsl:text>S</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;<xsl:value-of select="@camPitch +$pitchOffset" />;<xsl:value-of select="(/flightplan/head/photosettings/@maxGroundSpeedKMH div 3.6)"/>;<xsl:value-of select="./@id"/>, following height: <xsl:value-of select="$changeHeightF"/>, previous height: <xsl:value-of select="$changeHeightP"/>, parent height: <xsl:value-of select="$changeHeightParent"/><xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="string(number($changeHeightP)) = 'NaN' ">
<xsl:text>M</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;<xsl:value-of select="@camPitch +$pitchOffset" />;<xsl:value-of select="(/flightplan/head/photosettings/@maxGroundSpeedKMH div 3.6)"/>;<xsl:value-of select="./@id"/>, following height: <xsl:value-of select="$changeHeightF"/>, previous height: <xsl:value-of select="$changeHeightP"/>, parent height: <xsl:value-of select="$changeHeightParent"/><xsl:text>
</xsl:text>
</xsl:when>


<xsl:when test="string(number($changeHeightF)) != 'NaN' ">
<xsl:text>M</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;<xsl:value-of select="@camPitch +$pitchOffset" />;<xsl:value-of select="(/flightplan/head/photosettings/@maxGroundSpeedKMH div 3.6)"/>;<xsl:value-of select="./@id"/>, following height: <xsl:value-of select="$changeHeightF"/>, previous height: <xsl:value-of select="$changeHeightP"/>, parent height: <xsl:value-of select="$changeHeightParent"/><xsl:text>
</xsl:text>
</xsl:when>

<xsl:otherwise>
<xsl:text>M</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;<xsl:value-of select="@camPitch +$pitchOffset" />;<xsl:value-of select="(/flightplan/head/photosettings/@maxGroundSpeedKMH div 3.6)"/>;<xsl:value-of select="./@id" />, following height: <xsl:value-of select="$changeHeightF"/>, previous height: <xsl:value-of select="$changeHeightP"/>, parent height: <xsl:value-of select="$changeHeightParent"/><xsl:text>
</xsl:text>
</xsl:otherwise>
</xsl:choose>


</xsl:template>

<!--
 <xsl:template match="landingpoint">
  <xsl:variable name="changeHeightP" select="(../../descendant::waypoint[last()]/@alt div 100.)"/>

 <xsl:choose>

 <xsl:when test="$changeHeightP=(./@alt div 100.)">
 <xsl:text>M</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;;;<xsl:value-of select="./@id" />, Landingpoint, previous height: <xsl:value-of select="$changeHeightP"/><xsl:text>
 </xsl:text>
 </xsl:when>

 <xsl:otherwise>
 <xsl:text>S</xsl:text>;<xsl:value-of select="./@lon" />;<xsl:value-of select="./@lat" />;<xsl:value-of select="./@alt div 100." />;<xsl:value-of select="@camYaw" />;;;<xsl:value-of select="./@id" />, Landingpoint, previous height: <xsl:value-of select="$changeHeightP"/><xsl:text>
 </xsl:text>
 </xsl:otherwise>

 </xsl:choose>

  </xsl:template>
-->

 <xsl:template match="dump"></xsl:template>

 </xsl:stylesheet>
