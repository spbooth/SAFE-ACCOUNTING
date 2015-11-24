<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
	version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"	
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:acc="http://safe.epcc.ed.ac.uk/restrict"
	xmlns:par="http://safe.epcc.ed.ac.uk/parameter"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:tab="http://safe.epcc.ed.ac.uk/table"
	xmlns:cha="http://safe.epcc.ed.ac.uk/chart"
	xmlns:ato="http://safe.epcc.ed.ac.uk/atom"
	xmlns:fmt="http://safe.epcc.ed.ac.uk/format"
	exclude-result-prefixes="acc par per fil tab cha ato fmt">

	<!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />
	
	<xsl:include href="filter.xsl" />
	<xsl:include href="period.xsl" />
	<xsl:include href="tables.xsl" />
	<xsl:include href="chart.xsl" />
	<xsl:include href="format.xsl" />
	
	<!-- Remove the access control elements. These have to be removed here
	rather than in restrict.xsl because restrict.xml is used in the initial parse
	before the elements are read. -->
    <xsl:template match="acc:RequireRole" />
	<xsl:template match="acc:SuficientRole" /> 
	

    <!--  remove comments -->
    <xsl:template match="rep:Comment" />
</xsl:stylesheet>    