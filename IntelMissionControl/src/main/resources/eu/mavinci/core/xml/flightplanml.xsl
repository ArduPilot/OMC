<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
:begin
  JMP body
  <xsl:apply-templates />

 mov reentrypoint,<xsl:value-of select="/flightplan/head/landingpoint/@id" />
##########################################
# set flightphase to descending          #
##########################################
 cmp descendingmode,3
 jne descendx1
 mov waypointlat,landinglat
 mov waypointlon,landinglon
 mov navigationmode,0
 mov waypointreached,0
 mov circradius,0
# mov spaltitude,safealt

:desccirc1
 # fly back to landing point holding altitude
 cmp waypointreached,1
 je desccirc1end
 yield
 jmp desccirc1
:desccirc1end

 # now go down to landing altitude (usually 100m)
 mov assertaltitude,0
 mov navigationmode,1
 mov spaltitude,landingalt 
:desccirc
 yield
 mov waypointlat,landinglat
 mov waypointlon,landinglon
 jmp desccirc
:descendx1
 cmp descendingmode,4
 jne descend
 mov waypointlat,landinglat
 mov waypointlon,landinglon
 mov navigationmode,0
 mov waypointreached,0
 mov circradius,0
 mov assertaltitude,2
 mov spaltitude,circlandingexitalt
:descm4l
 cmp waypointreached,1
 je descm4lend
 yield
 jmp descm4l
:descm4lend
 # note: we switch to landing when reaching the point. The AP may do that earlier based on other criteria.
 mov flightphase,4
 jmp endoffp
:descend
 mov flightphase,3
:endoffp
</xsl:template>

<xsl:template match="head">
 INT tmpvar
 FLOAT yawdiff
  <xsl:apply-templates />
 JMP begin
</xsl:template>

<xsl:template match="body">
:body
  <xsl:apply-templates />

</xsl:template>

<xsl:template match="waypoint" name="waypoint">
 <xsl:param name="id" select="./@id" />
 <xsl:param name="lat" select="concat(./@lat,'d')" />
 <xsl:param name="lon" select="concat(./@lon,'d')" />
 <xsl:param name="alt" select="./@alt" />
 <xsl:param name="radius" select="./@radius" />
 <xsl:param name="assertaltitude" select="./@assertaltitude" />
 <xsl:param name="ignore" select="./@ignore" />
 <xsl:param name="speedmode" select="./@speedMode" />
 <xsl:param name="assertyaw" select="./@assertYaw" />
###############################
# set waypoint (blockierend)  #
###############################
 yield
<xsl:if test="$ignore='true'">
 jmp wpend<xsl:value-of select="$id" />
</xsl:if>
 mov navigationmode,0
 mov reentrypoint,<xsl:value-of select="$id" />
 mov waypointreached,0
 mov waypointlat,<xsl:value-of select="$lat" />
 mov waypointlon,<xsl:value-of select="$lon" />
 mov spaltitude,<xsl:value-of select="$alt" />
 mov airspeed_mode,0
<xsl:if test="$speedmode='slow'">
 mov airspeed_mode,1
</xsl:if>
<xsl:if test="$assertaltitude='false'">
 mov assertaltitude,0
</xsl:if>
<xsl:if test="$assertaltitude='true'">
 mov assertaltitude,1
</xsl:if>
<xsl:if test="$assertaltitude='linear'">
 mov assertaltitude,2
</xsl:if>
<xsl:if test="$radius=0">
:wploop<xsl:value-of select="$id" />
 # waypointreached wird durch die Navigation auf 1 gesetzt
 cmp waypointreached,1
 je wploopend<xsl:value-of select="$id" />
<xsl:variable name="currentid" select="$id"/>
<xsl:for-each select="ancestor::*">
 <xsl:if test="@time!=0">
 # is timer up?
 CMP timer,timer<xsl:value-of select="@id" />
 JL  wploopend<xsl:value-of select="$currentid" /> 
 </xsl:if>
</xsl:for-each>

 yield
 jmp wploop<xsl:value-of select="$id" />
:wploopend<xsl:value-of select="$id" />
</xsl:if>
<xsl:if test="$assertaltitude='true'">
##########################################################################
# circle around the last waypoint if altitude is not yet reached.        #
##########################################################################
 yield
 mov waypointreached,0
 mov navigationmode,1
 mov circradius,0
:circ<xsl:value-of select="$id" />
<xsl:variable name="currentid1" select="$id"/>
 <xsl:for-each select="ancestor::*">
 <xsl:if test="@time!=0">
 # is timer up?
 CMP timer,timer<xsl:value-of select="@id" />
 JL  wpcircend<xsl:value-of select="$currentid1" /> 
 </xsl:if>
</xsl:for-each> 
 yield
# This gives us the possibility to cancel alt reach circling from AP by setting navigation mode back to 0
 cmp navigationmode,0
 je wpcircend<xsl:value-of select="$id" />
 mov tmpvar,<xsl:value-of select="$alt" />
 sub tmpvar,1000
 cmp altitude,tmpvar
 js circ<xsl:value-of select="$id" />
 add tmpvar,2000
 cmp altitude,tmpvar
 jl circ<xsl:value-of select="$id" />
:wpcircend<xsl:value-of select="$id" />
 mov navigationmode,0
</xsl:if>
<xsl:if test="$assertyaw  != ''">
#################################################################################
# circle around the last waypoint if assertYaw is set until we reach this yaw   #
#################################################################################
 yield
 mov waypointreached,0
 mov navigationmode,1
 mov circradius,0
:circay<xsl:value-of select="$id" />
<xsl:variable name="currentid1" select="$id"/>
 <xsl:for-each select="ancestor::*">
 <xsl:if test="@time!=0">
 # is timer up?
 CMP timer,timer<xsl:value-of select="@id" />
 JL  wpcircenday<xsl:value-of select="$currentid1" />
 </xsl:if>
</xsl:for-each>
 yield
# This gives us the possibility to cancel alt reach circling from AP by setting navigation mode back to 0
 cmp navigationmode,0
 je wpcircenday<xsl:value-of select="$id" />

 mov yawdiff,yaw
 sub yawdiff,<xsl:value-of select="$assertyaw" />
 cmp yawdiff,180.0
 jl fixyadiff1<xsl:value-of select="$id" />
 jmp yawdiffok1<xsl:value-of select="$id" />
:fixyadiff1<xsl:value-of select="$id" />
 sub yawdiff,360.0
:yawdiffok1<xsl:value-of select="$id" />
 cmp yawdiff,-180.0
 js fixyadiff2<xsl:value-of select="$id" />
 jmp yawdiffok2<xsl:value-of select="$id" />
:fixyadiff2<xsl:value-of select="$id" />
 add yawdiff,360.0
:yawdiffok2<xsl:value-of select="$id" />

 # compare if yawdiff is now between -5 and plus 5 degrees
 cmp yawdiff,-5.0
 js circay<xsl:value-of select="$id" />
 cmp yawdiff,5.0
 jl circay<xsl:value-of select="$id" />
:wpcircenday<xsl:value-of select="$id" />
 mov navigationmode,0
</xsl:if>
<xsl:if test="$radius!=0">
##########################################################################
# circle around the last waypoint one time if radius > 0                 #
##########################################################################
 yield
 mov waypointreached,0
 mov navigationmode,1
 mov circradius,<xsl:value-of select="$radius" />

:wpcircloop<xsl:value-of select="$id" />
 # waypointreached wird durch die Navigation auf 1 gesetzt
 cmp waypointreached,1
 je wpcircloopend<xsl:value-of select="$id" />
<xsl:variable name="currentid2" select="$id"/>
<xsl:for-each select="ancestor::*">
 <xsl:if test="@time!=0">
 # is timer up?
 CMP timer,timer<xsl:value-of select="@id" />
 JL  wpcircloopend<xsl:value-of select="$currentid2" />
 </xsl:if>
</xsl:for-each>
 yield
 jmp wpcircloop<xsl:value-of select="$id" />
:wpcircloopend<xsl:value-of select="$id" />
 mov navigationmode,0
</xsl:if>
<xsl:if test="$ignore='true'">
:wpend<xsl:value-of select="$id" />
</xsl:if>
</xsl:template>

<xsl:template match="landingpoint">
##########################################
# set landing point (at the beginning!)  # 
##########################################
 mov landinglat,<xsl:value-of select="./@lat" />d
 mov landinglon,<xsl:value-of select="./@lon" />d
 mov landingalt,<xsl:value-of select="./@alt" />
<xsl:if test="./@idLandingBegin">
 mov landingreentry,<xsl:value-of select="./@idLandingBegin" />
</xsl:if>

<xsl:if test="./@altBreakout">
 mov circlandingexitalt,<xsl:value-of select="./@altBreakout" />
</xsl:if>
<xsl:if test="not(./@mode)">
 mov descendingmode,3
</xsl:if>
<xsl:if test="./@mode">
 mov descendingmode,<xsl:value-of select="./@mode" />
</xsl:if>
<xsl:if test="./@yaw">
 mov landingyaw,<xsl:value-of select="./@yaw" />
</xsl:if>
</xsl:template>

<xsl:template match="eventActions">
##########################################
# set safetyAltitude (at the beginning!)  # 
##########################################
 mov safeAlt,<xsl:value-of select="./@safetyAlt" />
 
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="event">
##########################################
# set event actions <xsl:value-of select="./@name" /> (at the beginning!)  # 
##########################################
 mov event_<xsl:value-of select="./@name" />_delay,<xsl:value-of select="./@delay" />
 mov event_<xsl:value-of select="./@name" />_action,<xsl:value-of select="./@action" />
 mov event_<xsl:value-of select="./@name" />_recover,<xsl:value-of select="./@recover" />
 <xsl:if test="@level">
 mov event_<xsl:value-of select="./@name" />_level,<xsl:value-of select="./@level" />
 </xsl:if>
</xsl:template>


<xsl:template match="startprocedure">
##########################################
# Starting procedure                     #
##########################################
 <xsl:call-template name="photo">
  <xsl:with-param name="id" select="./@id" />
  <xsl:with-param name="distance" select="'-1.0'" />
  <xsl:with-param name="distanceMax" select="'-1.0'" />
  <xsl:with-param name="power" select="'on'"/>
 </xsl:call-template>

 <xsl:choose>
    <xsl:when test="./@alt">
        <xsl:call-template name="waypoint">
            <xsl:with-param name="id" select="./@id" />
            <xsl:with-param name="lat" select="'landinglat'" />
            <xsl:with-param name="lon" select="'landinglon'" />
            <xsl:with-param name="alt" select="./@alt" />
            <xsl:with-param name="radius" select="0" />
            <xsl:with-param name="assertaltitude" select="'true'" />
        </xsl:call-template>
        <xsl:call-template name="waypoint">
            <xsl:with-param name="id" select="./@id" />
            <xsl:with-param name="lat" select="'landinglat'" />
            <xsl:with-param name="lon" select="'landinglon'" />
            <xsl:with-param name="alt" select="./@alt" />
            <xsl:with-param name="radius" select="-1" />
            <xsl:with-param name="assertaltitude" select="'false'" />
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
        <xsl:call-template name="waypoint">
            <xsl:with-param name="id" select="./@id" />
            <xsl:with-param name="lat" select="'landinglat'" />
            <xsl:with-param name="lon" select="'landinglon'" />
            <xsl:with-param name="alt" select="'landingalt'" />
            <xsl:with-param name="radius" select="0" />
            <xsl:with-param name="assertaltitude" select="'true'" />
        </xsl:call-template>
        <xsl:call-template name="waypoint">
            <xsl:with-param name="id" select="./@id" />
            <xsl:with-param name="lat" select="'landinglat'" />
            <xsl:with-param name="lon" select="'landinglon'" />
            <xsl:with-param name="alt" select="'landingalt'" />
            <xsl:with-param name="radius" select="-1" />
            <xsl:with-param name="assertaltitude" select="'false'" />
        </xsl:call-template>
    </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="preApproach">
##########################################
# preApproach for full 3d landing        #
##########################################
 <xsl:call-template name="waypoint">
  <xsl:with-param name="id" select="./@id" />
  <xsl:with-param name="lat" select="concat(./@lat,'d')" />
  <xsl:with-param name="lon" select="concat(./@lon,'d')" />
  <xsl:with-param name="alt" select="./@alt - 300" />
  <xsl:with-param name="radius" select="0" />
  <xsl:with-param name="assertaltitude" select="'false'" />
  <xsl:with-param name="speedmode" select="'slow'" />
 </xsl:call-template>
 mov flightphase,3
</xsl:template>

<xsl:template match="wbody">
</xsl:template>

<xsl:template match="loop">
##########################################
# Loop n times                           #
##########################################
 <xsl:if test="./@ignore='true'">
  jmp loopend<xsl:value-of select="./@id" />
 </xsl:if>
 int loopcount<xsl:value-of select="./@id" />
 mov reentrypoint,<xsl:value-of select="./@id" />
 mov loopcount<xsl:value-of select="./@id" />,0
 <xsl:if test="@time!=0">
 int timer<xsl:value-of select="./@id" />
 mov timer<xsl:value-of select="./@id" />,timer
 add timer<xsl:value-of select="./@id" />,<xsl:value-of select="./@time" />
</xsl:if>
:loop<xsl:value-of select="./@id" />
 <xsl:apply-templates />
 <xsl:if test="@time!=0">
 # timer up? 
 CMP timer,timer<xsl:value-of select="@id" />
 JL  loopend<xsl:value-of select="@id" /> 
 </xsl:if>
 <xsl:if test="@counter!=0">
 add loopcount<xsl:value-of select="./@id" />,1
 cmp loopcount<xsl:value-of select="./@id" />,<xsl:value-of select="./@counter" />
 js   loop<xsl:value-of select="./@id" />
 </xsl:if>
 <xsl:if test="@counter=0">
 jmp   loop<xsl:value-of select="./@id" />
 </xsl:if>
:loopend<xsl:value-of select="./@id" />
</xsl:template>

<xsl:template match="photo" name="photo">
##########################################
# Activate/Deactivate photos             #
##########################################
 <xsl:param name="id" select="./@id" />
 <xsl:param name="distance" select="./@distance" />
 <xsl:param name="distanceMax" select="./@distanceMax" />
 <xsl:param name="power" select="./@power" />
 mov reentrypoint,<xsl:value-of select="$id" />
 mov photo_delay_distance,<xsl:value-of select="$distance" />
 mov photo_delay_distancemax,<xsl:value-of select="$distanceMax" />
 <xsl:if test="$power='on'">
 cmd 1
 </xsl:if>
 <xsl:if test="$power='off'">
 cmd 2
 </xsl:if>
</xsl:template>

<xsl:template match="photosettings">
##########################################
# Set photo parameters                   #
##########################################
 mov photo_max_roll,<xsl:value-of select="./@maxroll" />
 mov photo_max_pitch,<xsl:value-of select="./@maxnick" />
 mov photo_delay_float,<xsl:value-of select="./@mintimeinterval" />
</xsl:template>


<xsl:template match="picarea">
##########################################
# set picarea reentrypoint               # 
##########################################
 <xsl:param name="id" select="./@id" />
 mov reentrypoint,<xsl:value-of select="$id" />
 <xsl:apply-templates />
</xsl:template>


<xsl:template match="dump">
##########################################
# Set photo parameters                   #
##########################################
 dump "<xsl:value-of select="." />"
</xsl:template>

<xsl:template match="learningmode">
 mov learningmode,1
</xsl:template>

</xsl:stylesheet>
