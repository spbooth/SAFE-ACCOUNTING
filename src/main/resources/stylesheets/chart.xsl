<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:cha="http://safe.epcc.ed.ac.uk/chart"
	xmlns:par="http://safe.epcc.ed.ac.uk/parameter"
	xmlns:plotter="xalan://uk.ac.ed.epcc.safe.accounting.reports.ChartExtension"
	xmlns:period="xalan://uk.ac.ed.epcc.safe.accounting.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.safe.accounting.reports.FilterExtension"
	xmlns:param="xalan://uk.ac.ed.epcc.safe.accounting.reports.ParameterExtension"
	extension-element-prefixes="plotter filter param">
	
	<!--  get the Extension object -->
	<xsl:param name="ChartExtension"/>
	<xsl:param name="PeriodExtension"/>
	<xsl:param name="FilterExtension"/>
	<xsl:param name="ParameterExtension"/>
	
	<!--  inline templates for deferred charts -->
	<xsl:template mode="inline" match="@*|node()">
	<xsl:param name="filcontext"/>
    <xsl:copy>
      <xsl:apply-templates mode="inline" select="@*|node()">
        <xsl:with-param name="filcontext" select="$filcontext"/>
      </xsl:apply-templates>
    </xsl:copy>
 	</xsl:template>
 	<xsl:template mode="inline" match="par:ParameterRef">
 	<xsl:copy-of select="param:inlineParameterRef($ParameterExtension,self::*)" />
 	</xsl:template>
	<xsl:template mode="inline" match="fil:Filter">
	<xsl:param name="filcontext"/>
	<xsl:variable name="filter" select="filter:makeFilterFragment($FilterExtension,$filcontext,.)"/>
	<xsl:copy-of select="plotter:formatRecordSet($ChartExtension,$filter)" />
	</xsl:template>
	
	
	<!-- This supports xsl extensions use to generate the report -->
	<xsl:template match="cha:TimeChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	
	<xsl:choose>
	<xsl:when test="plotter:deferrCharts($ChartExtension) and not(@table)">
	<!-- deferred chart -->
	<xsl:variable name="spec">
	<xsl:copy>
	    <xsl:apply-templates mode="inline" select="@*"/>
		<xsl:copy-of select="$PeriodNode"/>
		<xsl:copy-of select="plotter:formatRecordSet($ChartExtension,$filter)" />
		<xsl:apply-templates mode="inline" select="cha:*">
		<xsl:with-param name="filcontext" select="$filter"/>
		</xsl:apply-templates>
	</xsl:copy>
	</xsl:variable>
	<xsl:copy-of select="plotter:emitDeferredChart($ChartExtension,$spec,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<!-- inline chart -->
	<xsl:variable name="plotentry" select="plotter:getPlotEntry($ChartExtension,$filter,.)"/>
	<xsl:variable name="mapperentry" select="plotter:getMapperEntry($ChartExtension,$filter,.)"/>
	<xsl:variable name="chart" select="plotter:makeTimeChart($ChartExtension,$period,.)"/>
	<xsl:variable name="ds" select="plotter:makeDataSet($ChartExtension,$filter,$plotentry,$mapperentry,$chart,.)"/>
	<xsl:for-each select="cha:AddData">
		<xsl:variable name="fil2" select="filter:makeFilter($FilterExtension,$filter,fil:Filter)"/>
	    <xsl:variable name="plotentry2" select="plotter:getPlotEntry($ChartExtension,$fil2,.)"/>
		<xsl:variable name="ds2" select="plotter:makeDataSet($ChartExtension,$ds,$fil2,$plotentry2,$mapperentry,$chart,.)"/>
	</xsl:for-each>
	<xsl:copy-of select="plotter:addPlot($ChartExtension,$ds,$filter,$plotentry,$mapperentry,$chart,.)"/>
	<xsl:for-each select="cha:AddChart">
	    <xsl:variable name="fil2" select="filter:makeFilter($FilterExtension,$filter,fil:Filter)"/>
	    <xsl:variable name="plotentry2" select="plotter:getPlotEntry($ChartExtension,$fil2,.)"/>
		<xsl:variable name="mapperentry2" select="plotter:getMapperEntry($ChartExtension,$fil2,.)"/>
		<xsl:variable name="ds2" select="plotter:makeDataSet($ChartExtension,$fil2,$plotentry2,$mapperentry2,$chart,.)"/>
		<xsl:for-each select="cha:AddData">
		  <xsl:variable name="fil3" select="filter:makeFilter($FilterExtension,$fil2,fil:Filter)"/>
	      <xsl:variable name="plotentry3" select="plotter:getPlotEntry($ChartExtension,$fil3,.)"/>
		  <xsl:variable name="ds3" select="plotter:makeDataSet($ChartExtension,$ds2,$fil3,$plotentry3,$mapperentry2,$chart,.)"/>
	    </xsl:for-each>
		<xsl:copy-of select="plotter:addPlot($ChartExtension,$ds2,$fil2,$plotentry2,$mapperentry2,$chart,.)"/>
	</xsl:for-each>
	<xsl:choose>
	<xsl:when test="plotter:hasData($ChartExtension,$chart)">
	<xsl:copy-of select="plotter:addChart($ChartExtension,$chart,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:if test="not(@quiet)">
	<xsl:copy-of select="plotter:addNoData($ChartExtension,$chart)"/>
	</xsl:if>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template match="cha:PieTimeChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:choose>
	<xsl:when test="plotter:deferrCharts($ChartExtension) and not(@table)">
	<!-- deferred chart -->
	<xsl:variable name="spec">
	<xsl:copy>
	    <xsl:apply-templates mode="inline" select="@*"/>
		<xsl:copy-of select="$PeriodNode"/>
		<xsl:copy-of select="plotter:formatRecordSet($ChartExtension,$filter)" />
		<xsl:apply-templates mode="inline" select="cha:*">
		<xsl:with-param name="filcontext" select="$filter"/>
		</xsl:apply-templates>
	</xsl:copy>
	</xsl:variable>
	<xsl:copy-of select="plotter:emitDeferredChart($ChartExtension,$spec,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<!-- inline chart -->
	<xsl:variable name="plotentry" select="plotter:getPlotEntry($ChartExtension,$filter,.)"/>
	<xsl:variable name="mapperentry" select="plotter:getMapperEntry($ChartExtension,$filter,.)"/>
	<xsl:variable name="chart" select="plotter:makePieTimeChart($ChartExtension,$period,.)"/>
	<xsl:variable name="ds" select="plotter:makeDataSet($ChartExtension,$filter,$plotentry,$mapperentry,$chart,.)"/>
	<xsl:for-each select="cha:AddData">
		<xsl:variable name="fil2" select="filter:makeFilter($FilterExtension,$filter,fil:Filter)"/>
	    <xsl:variable name="plotentry2" select="plotter:getPlotEntry($ChartExtension,$fil2,.)"/>
		<xsl:variable name="ds2" select="plotter:makeDataSet($ChartExtension,$ds,$fil2,$plotentry2,$mapperentry,$chart,.)"/>
	</xsl:for-each>
	<xsl:copy-of select="plotter:addPlot($ChartExtension,$ds,$filter,$plotentry,$mapperentry,$chart,.)"/>
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
	<xsl:if test="not(@quiet)">
	<xsl:copy-of select="plotter:addNoData($ChartExtension,$chart)"/>
	</xsl:if>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	
	
	<xsl:template match="cha:BarTimeChart">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:variable name="caption" select="cha:Caption/text()"/>
	<xsl:choose>
	<xsl:when test="plotter:deferrCharts($ChartExtension) and not(@table)">
	<!-- deferred chart -->
	<xsl:variable name="spec">
	<xsl:copy>
	    <xsl:apply-templates mode="inline" select="@*"/>
		<xsl:copy-of select="$PeriodNode"/>
		<xsl:copy-of select="plotter:formatRecordSet($ChartExtension,$filter)" />
		<xsl:apply-templates mode="inline" select="cha:*">
		<xsl:with-param name="filcontext" select="$filter"/>
		</xsl:apply-templates>
	</xsl:copy>
    </xsl:variable>
	<xsl:copy-of select="plotter:emitDeferredChart($ChartExtension,$spec,$caption)"/>
	</xsl:when>
	<xsl:otherwise>
	<!-- inline chart -->
	<xsl:variable name="plotentry" select="plotter:getPlotEntry($ChartExtension,$filter,.)"/>
	<xsl:variable name="mapperentry" select="plotter:getMapperEntry($ChartExtension,$filter,.)"/>
	<xsl:variable name="chart" select="plotter:makeBarTimeChart($ChartExtension,$period,.)"/>
	<xsl:variable name="ds" select="plotter:makeDataSet($ChartExtension,$filter,$plotentry,$mapperentry,$chart,.)"/>
		<xsl:for-each select="cha:AddData">
		<xsl:variable name="fil2" select="filter:makeFilter($FilterExtension,$filter,fil:Filter)"/>
	    <xsl:variable name="plotentry2" select="plotter:getPlotEntry($ChartExtension,$fil2,.)"/>
		<xsl:variable name="ds2" select="plotter:makeDataSet($ChartExtension,$ds,$fil2,$plotentry2,$mapperentry,$chart,.)"/>
	</xsl:for-each>
	<xsl:copy-of select="plotter:addPlot($ChartExtension,$ds,$filter,$plotentry,$mapperentry,$chart,.)"/>
	<xsl:for-each select="cha:AddSeries">
	    <xsl:variable name="fil2" select="filter:makeFilter($FilterExtension,$filter,fil:Filter)"/>
	    <xsl:variable name="plotentry2" select="plotter:getPlotEntry($ChartExtension,$fil2,.)"/>
		<xsl:variable name="ds2" select="plotter:makeDataSet($ChartExtension,$fil2,$plotentry2,$mapperentry,$chart,.)"/>
		<xsl:for-each select="cha:AddData">
		  <xsl:variable name="fil3" select="filter:makeFilter($FilterExtension,$fil2,fil:Filter)"/>
	      <xsl:variable name="plotentry3" select="plotter:getPlotEntry($ChartExtension,$fil3,.)"/>
		  <xsl:variable name="ds3" select="plotter:makeDataSet($ChartExtension,$ds2,$fil3,$plotentry3,$mapperentry,$chart,.)"/>
	    </xsl:for-each>
		<xsl:copy-of select="plotter:addPlot($ChartExtension,$ds2,$fil2,$plotentry2,$mapperentry,$chart,.)"/>
	</xsl:for-each>
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
	<xsl:if test="not(@quiet)">
	<xsl:copy-of select="plotter:addNoData($ChartExtension,$chart)"/>
	</xsl:if>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>