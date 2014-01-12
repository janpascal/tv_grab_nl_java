<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:fn="http://www.w3.org/2005/xpath-functions" 
version="2.0" >
<xsl:output method="text" omit-xml-declaration="yes" /> 

<xsl:variable name="sources" as="element()*">
    <Item>tvgids.nl</Item>
    <Item>rtl.nl</Item>
    <Item>horizon.tv</Item>
    <Item>ziggogids.nl</Item>
</xsl:variable>

<xsl:variable name="today" select="current-date()" />
<xsl:variable name="day" select="xs:duration('P1D')" />

<xsl:variable name="dates" as="element()*">
    <xsl:for-each select="0 to 21">
        <date><xsl:value-of select="$today + .*xs:dayTimeDuration('P1D')"/></date>
    </xsl:for-each>
</xsl:variable>

<xsl:template match="/"> 
    <xsl:text>BEGIN TRANSACTION;&#10;</xsl:text> 
    <xsl:text>CREATE TABLE channel (id varchar(30), source varchar(20), name varchar(255));&#10;</xsl:text> 
    <xsl:for-each select="tv/channel"> 
        <xsl:text>INSERT INTO channel (id,source,name) VALUES (&quot;</xsl:text> 
        <xsl:value-of select="@id"/> 
        <xsl:text>&quot;,&quot;</xsl:text> 
        <xsl:value-of select="fn:substring-after(@id,'.')"/> 
        <xsl:text>&quot;,&quot;</xsl:text> 
        <xsl:value-of select="display-name"/> 
        <xsl:text>&quot;);&#10;</xsl:text> 
    </xsl:for-each> 
<!--
    <xsl:for-each select="tv"> 
    <xsl:text>Total channels: </xsl:text>
        <xsl:value-of select="count(channel)"/> 
        <xsl:text>&#10;</xsl:text>

        <xsl:for-each select="$sources"> 
           <xsl:variable name="source" select="."/>
           <xsl:text>Number of </xsl:text>
           <xsl:value-of select="."/>
           <xsl:text> channels: </xsl:text>
           <xsl:value-of select="count(//channel[fn:ends-with(@id, $source)])"/> 
           <xsl:text>&#10;</xsl:text>
        </xsl:for-each>

        <xsl:for-each select="$sources"> 
            <xsl:variable name="source" select="."/>
            <xsl:text>Source: </xsl:text>
            <xsl:value-of select="$source"/>
            <xsl:text>&#10;</xsl:text>

            <xsl:for-each select="$dates"> 
               <xsl:variable name="date" select="format-date(., '[Y0001][M01][D01]')"/>
               <xsl:text>Date: </xsl:text>
               <xsl:value-of select="$date"/>
               <xsl:text>&#10;</xsl:text>

               <xsl:text>Count: </xsl:text>
               <xsl:value-of select="count(//programme[fn:starts-with(@start, $date) and fn:ends-with(@channel,$source)])"/>
               <xsl:text>&#10;</xsl:text>
            </xsl:for-each>
        </xsl:for-each>

    </xsl:for-each> 
-->
<!--    <xsl:text>Programmes:&#10;</xsl:text> -->
    <xsl:text>CREATE TABLE programme(datum date, tijd time, channel varchar(30), source varchar(20), name varchar(255));&#10;</xsl:text> 
    <xsl:for-each select="tv/programme"> 
        <xsl:text>INSERT INTO programme(datum,tijd,channel,source,name) VALUES (&quot;</xsl:text> 
        <xsl:value-of select="fn:concat(fn:substring(@start,1,4),'-',fn:substring(@start,5,2),'-',fn:substring(@start,7,2))"/> 
        <xsl:text>&quot;, &quot;</xsl:text> 
        <xsl:value-of select="fn:concat(fn:substring(@start,9,2),':',fn:substring(@start,11,2))"/> 
        <xsl:text>&quot;, &quot;</xsl:text> 
        <xsl:value-of select="@channel"/> 
        <xsl:text>&quot;,&quot;</xsl:text> 
        <xsl:value-of select="fn:substring-after(@channel,'.')"/> 
        <xsl:text>&quot;,&quot;</xsl:text> 
        <xsl:value-of select="fn:translate(title,'&quot;','')" /> 
        <xsl:text>&quot;);&#10;</xsl:text> 
    </xsl:for-each> 
    <xsl:text>COMMIT TRANSACTION;&#10;</xsl:text> 
</xsl:template> 
</xsl:stylesheet>
