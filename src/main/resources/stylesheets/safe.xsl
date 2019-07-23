<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:cha="http://safe.epcc.ed.ac.uk/chart"
	xmlns:saf="http://safe.epcc.ed.ac.uk/safe"
	xmlns:plotter="xalan://uk.ac.ed.epcc.webacct.model.reports.ChartExtension"
	xmlns:period="xalan://uk.ac.ed.epcc.webacct.model.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.webacct.model.reports.FilterExtension"
	xmlns:table="xalan://uk.ac.ed.epcc.webacct.model.reports.TableExtension"
	xmlns:safe="xalan://uk.ac.hpcx.report.SafeExtension"
	extension-element-prefixes="plotter period filter safe" >
	<!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />
	
	<!--  get the Extension object -->
	<xsl:param name="ChartExtension"/>
	<xsl:param name="PeriodExtension"/>
	<xsl:param name="FilterExtension"/>
	<xsl:param name="SafeExtension"/>
	<xsl:param name="TableExtension"/>
	
	
	<!-- This supports xsl extensions use to generate the report -->
	<xsl:template match="saf:UserChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:variable name="chart" select="safe:addUserData($SafeExtension,plotter:makeTimeChart($ChartExtension,$period,.))"/>
	<xsl:choose>
	<xsl:when test="plotter:hasData($ChartExtension,$chart)">
	<xsl:copy-of select="plotter:addChart($ChartExtension,$chart,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:element name="NoData"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<!-- This supports xsl extensions use to generate the report -->
	<xsl:template match="saf:QualityTokenChart">
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="chart" select="safe:addQualityTokenData($SafeExtension,plotter:makeTimeChart($ChartExtension,$period,.))"/>
	<xsl:choose>
	<xsl:when test="plotter:hasData($ChartExtension,$chart)">
	<xsl:copy-of select="plotter:addChart($ChartExtension,$chart,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:element name="NoData"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template match="saf:MaxRate">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="pool" select="text()"/>
	<xsl:value-of select="safe:maxRate($SafeExtension,$pool,$period)"></xsl:value-of>
	</xsl:template>
	
	<xsl:template match="saf:ProjectDiskTable">
	<xsl:variable name="filter" select="filter:makeObjectSet($FilterExtension,fil:ObjectSet)"/>
	<xsl:variable name="fs" select="saf:FileSystem/text()"/>
	<xsl:variable name="table" select="safe:getProjectQuotaTable($SafeExtension,$fs,$filter)"/>
	<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	
	<xsl:template match="saf:DefaultUnitChargedProp">
	<xsl:value-of select="safe:getChargedProp($SafeExtension)"/>
	</xsl:template>
	<xsl:template match="saf:DefaultUnitRawProp">
	<xsl:value-of select="safe:getRawProp($SafeExtension)"/>
	</xsl:template>
	<xsl:template match="saf:DefaultUnit">
	<xsl:value-of select="safe:getUnit($SafeExtension)"/>
	</xsl:template>
	<xsl:template match="saf:DefaultUnits">
	<xsl:value-of select="safe:getUnits($SafeExtension)"/>
	</xsl:template>
	<xsl:template match="saf:DefaultUnitCharged">
	<xsl:value-of select="safe:getChargedName($SafeExtension)"/>
	</xsl:template>
	<xsl:template match="saf:DefaultUnitRaw">
	<xsl:value-of select="safe:getRawName($SafeExtension)"/>
	</xsl:template>
</xsl:stylesheet>