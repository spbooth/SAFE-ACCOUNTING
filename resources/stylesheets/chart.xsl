<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:cha="http://safe.epcc.ed.ac.uk/chart"
	xmlns:plotter="xalan://uk.ac.ed.epcc.webacct.model.reports.ChartExtension"
	xmlns:period="xalan://uk.ac.ed.epcc.webacct.model.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.webacct.model.reports.FilterExtension"
	extension-element-prefixes="plotter">
	
	<!--  get the Extension object -->
	<xsl:param name="ChartExtension"/>
	<xsl:param name="PeriodExtension"/>
	<xsl:param name="FilterExtension"/>
	
	<!-- This supports xsl extensions use to generate the report -->
	<xsl:template match="cha:TimeChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="chart" select="plotter:makeTimeChart($ChartExtension,$period,.)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:copy-of select="plotter:addPlot($ChartExtension,$filter,$chart,.)"/>
	<xsl:for-each select="cha:AddChart">
		<xsl:copy-of select="plotter:addPlot($ChartExtension,filter:makeFilter($FilterExtension,$filter,fil:Filter),$chart,.)"/>
	</xsl:for-each>
	<xsl:choose>
	<xsl:when test="plotter:hasData($ChartExtension,$chart)">
	<xsl:copy-of select="plotter:addChart($ChartExtension,$chart,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:element name="NoData"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template match="cha:PieTimeChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="chart" select="plotter:makePieTimeChart($ChartExtension,$period,.)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:copy-of select="plotter:addPlot($ChartExtension,$filter,$chart,.)"/>
	<xsl:choose>
	<xsl:when test="plotter:hasData($ChartExtension,$chart)">
	<xsl:if test="not(@nographic)">
	<xsl:copy-of select="plotter:addChart($ChartExtension,$chart,$caption)"/>
	</xsl:if>
	<xsl:if test="@table">
	<xsl:copy-of select="plotter:addChartTable($ChartExtension,$chart,$caption)"/>
	</xsl:if>
	</xsl:when>
	<xsl:otherwise>
	<xsl:element name="NoData"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	
	
	<xsl:template match="cha:BarTimeChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="chart" select="plotter:makeBarTimeChart($ChartExtension,$period,.)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:copy-of select="plotter:addPlot($ChartExtension,$filter,$chart,.)"/>
	<xsl:choose>
	<xsl:when test="plotter:hasData($ChartExtension,$chart)">
	<xsl:if test="not(@nographic)">
	<xsl:copy-of select="plotter:addChart($ChartExtension,$chart,$caption)"/>
	</xsl:if>
	<xsl:if test="@table">
	<xsl:copy-of select="plotter:addChartTable($ChartExtension,$chart,$caption)"/>
	</xsl:if>
	</xsl:when>
	<xsl:otherwise>
	<xsl:element name="NoData"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>