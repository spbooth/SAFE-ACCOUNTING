<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:cha="http://safe.epcc.ed.ac.uk/chart"
	xmlns:fmt="http://safe.epcc.ed.ac.uk/format"
	xmlns:format="xalan://uk.ac.ed.epcc.webacct.model.reports.FormatExtension"
	xmlns:period="xalan://uk.ac.ed.epcc.webacct.model.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.webacct.model.reports.FilterExtension"
	extension-element-prefixes="format">
	
	<!--  get the Extension object -->
	<xsl:param name="FormatExtension"/>
	<xsl:param name="PeriodExtension"/>
	<xsl:param name="FilterExtension"/>
	
	<!-- This supports xsl extensions use to generate the report -->
	<xsl:template match="fmt:Format">
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)"/>
	<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
	<xsl:copy-of select="fmt:format($FormatExtension,$filter,$period,child::node())" />
	</xsl:template>
	
	
	
	
</xsl:stylesheet>