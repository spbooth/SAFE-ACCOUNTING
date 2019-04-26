<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:period="xalan://uk.ac.ed.epcc.webacct.model.reports.PeriodExtension"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	extension-element-prefixes="period">

<xsl:param name="PeriodExtension"/>


<!-- Remove Period element from output, 
This should have lower priority than previous template
 -->
<xsl:template match="per:Period"/>

<xsl:template match="per:StartDate">
<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
<xsl:variable name="format" select="@format"/>
<xsl:copy-of select="period:getStart($PeriodExtension,$period,$format)" />
</xsl:template>
<xsl:template match="per:EndDate">
<xsl:variable name="PeriodNode" select="(ancestor::*/per:Period|per:Period)[last()]"/>
<xsl:variable name="period" select="period:makePeriod($PeriodExtension,$PeriodNode)"/>
<xsl:variable name="format" select="@format"/>
<xsl:copy-of select="period:getEnd($PeriodExtension,$period,$format)" />
</xsl:template>

</xsl:stylesheet>