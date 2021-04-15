<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:par="http://safe.epcc.ed.ac.uk/parameter"
	xmlns:acc="http://safe.epcc.ed.ac.uk/restrict"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:param="xalan://uk.ac.ed.epcc.safe.accounting.reports.ParameterExtension"
	extension-element-prefixes="param">
<!-- This style-sheet is used for an initial pass that expands the form parameters 
 -->
	<!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />
	<xsl:import href="restrict.xsl" />
	<!-- register external parameters with param statements -->
	<xsl:param name="ParameterExtension"/>
	
	<!-- remove all par:param -->
  	<xsl:template match="par:Parameter">		
		<xsl:copy-of select="param:parameter($ParameterExtension,self::*)" />
	</xsl:template>
	<xsl:template match="par:FormatParameter">		
		<xsl:copy-of select="param:formatParameter($ParameterExtension,@name,child::*)" />
	</xsl:template>
	<xsl:template match="par:Repeat">		
		<xsl:apply-templates select="param:repeat($ParameterExtension,self::*)" />
	</xsl:template>
	<xsl:template match="par:For">		
		<xsl:apply-templates select="param:For($ParameterExtension,self::*)" />
	</xsl:template>
	<xsl:template match="par:Distinct">	
	<xsl:variable name="filter" select="ancestor::*/fil:Filter"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>	
		<xsl:apply-templates select="param:Distinct($ParameterExtension,$PeriodNode,$filter,self::*)" />
	</xsl:template>
	<!--  remove the parameter defs etc-->
    <xsl:template match="par:ParameterDef"/>
    <xsl:template match="par:Stage"/>
    <xsl:template match="par:EagerStage"/>
    
    <xsl:template match="par:IfSet">
    <xsl:choose>
	<xsl:when test="param:isSet($ParameterExtension,@name)">
	<xsl:apply-templates select="par:Content" mode="use" />
	</xsl:when>
	<xsl:otherwise>
	<xsl:apply-templates select="par:Fallback" mode="use"/>
	</xsl:otherwise>
	</xsl:choose>
    </xsl:template>
    
    <xsl:template match="par:IfNotSet">
    <xsl:choose>
	<xsl:when test="not(param:isSet($ParameterExtension,@name))">
	<xsl:apply-templates select="par:Content" mode="use" />
	</xsl:when>
	<xsl:otherwise>
	<xsl:apply-templates select="par:Fallback" mode="use"/>
	</xsl:otherwise>
	</xsl:choose>
    </xsl:template>
    <xsl:template match="par:Content|par:Fallback"/>
    <xsl:template match="par:Content|par:Fallback" mode="use">
    <xsl:apply-templates/>
    </xsl:template>
    
    <!--  remove parameter text -->
    <xsl:template match="par:Text"/>
    
    <!--  Remove PageTitle element -->
    <xsl:template match="par:PageTitle" />   
    
    <xsl:template match="par:Optional">
    <xsl:variable name="text" select="param:value($ParameterExtension,.)" />
    <xsl:apply-templates select="par:Option[par:Target/text()=$text]/par:Content" mode="use"/>
    </xsl:template>
</xsl:stylesheet>