<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:ato="http://safe.epcc.ed.ac.uk/atom"
	xmlns:period="xalan://uk.ac.ed.epcc.safe.accounting.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.safe.accounting.reports.FilterExtension"
	xmlns:atom="xalan://uk.ac.ed.epcc.safe.accounting.reports.AtomExtension"
	extension-element-prefixes="atom period filter">
    
    <xsl:import href="identity.xsl" />

	<!-- register external parameters with param statements -->
	<xsl:param name="AtomExtension" />
	<xsl:param name="PeriodExtension" />
	<xsl:param name="FilterExtension" />
	
    <xsl:template match="ato:Define">
    <!--  Note we have to include child elements in filter and period as query element is a child-->
    <xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period|*/per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter|*/fil:Filter)" />
		<xsl:value-of select="atom:define($AtomExtension,@name,$period,$filter,.)" /> 
    </xsl:template>
    
    <xsl:template match="ato:Percentage">
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
		<xsl:value-of select="atom:percent($AtomExtension,$period,$filter,.)" />
	</xsl:template>
    
	<xsl:template match="ato:Sum|ato:Distinct|ato:Average|ato:Median|ato:Minimum|ato:Maximum|ato:Count|ato:Number|ato:Value|ato:Add|ato:Sub|ato:Mul|ato:Div|ato:Atom">
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
		<xsl:value-of select="atom:formatAtom($AtomExtension,$period,$filter,.)" />
	</xsl:template>
	<xsl:template match="ato:AtomValue">
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
		<xsl:value-of select="atom:rawAtom($AtomExtension,$period,$filter,.)" />
	</xsl:template>
    <xsl:template match="ato:Property">
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
		<xsl:value-of select="atom:formatPropertyList($AtomExtension,$period,$filter,.)" />
	</xsl:template>
	

	
	<xsl:template match="ato:IfRecords">
	<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
	<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />  
	<xsl:if test="filter:hasRecords($FilterExtension,$period,$filter)">
	  <xsl:apply-templates  />
	</xsl:if>
	</xsl:template>
</xsl:stylesheet>   