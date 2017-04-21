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
	xmlns:ato="http://safe.epcc.ed.ac.uk/atom">
	
	<!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />
	
	<!-- Sets output to text without indentation-->
	<xsl:output method="text" indent="no" />
	
	<!-- Remove the newlines for the stripped out ParamererDef etc. -->
	<xsl:strip-space elements="rep:Report rep:Title rep:Heading rep:table rep:tr rep:Text text p para Title title heading Heading"/>
	
	<!-- Makes sure the elements which can remain do perserve their spaces -->
	<xsl:preserve-space elements="rep:td rep:th th td"/>
	
	<!-- generate csv skeleton on root element using the identity transformation -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- Convert  adding newline after each one-->	
	<xsl:template match="rep:Section|rep:SubSection|Section|Subsection">
		<xsl:apply-templates />	
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>
	<!--  remove commas from text -->
<xsl:template match="rep:Title|rep:Heading|rep:Text|P|p|Para|Text|Title|Heading">
        <xsl:text></xsl:text>
		<xsl:value-of select="translate(normalize-space(text()),',','')"/>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>
	<!-- tables map onto csv tables as one might expect -->
	<xsl:template match="rep:table|Table|table">
		<xsl:apply-templates />
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>

	<xsl:template match="rep:tr|Tr|tr">
		<xsl:apply-templates/>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>
	
	<xsl:template match="rep:th|rep:td|Th|Td|th|td">
		<xsl:value-of select="translate(normalize-space(text()),',','')"/>
		<xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
	</xsl:template>
	

	
</xsl:stylesheet>     

