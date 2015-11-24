<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:filter="xalan://uk.ac.ed.epcc.webacct.model.reports.FilterExtension"
	extension-element-prefixes="filter">
	
	<!--  get the Extension object -->
	<xsl:param name="FilterExtension"/>
    <xsl:param name="PeriodExtension" />
	<!--  Delete the Filter This should match after the previous templates -->
	<xsl:template match="fil:Filter" ></xsl:template>

	
	
</xsl:stylesheet>    