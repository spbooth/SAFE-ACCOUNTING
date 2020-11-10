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
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:ato="http://safe.epcc.ed.ac.uk/atom"
	xmlns="http://www.w3.org/1999/xhtml"
	>
	<xsl:output method="xhtml" version="1.0" 
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
<!--  Let stylesheet set the line breaks -->
	<xsl:strip-space elements="rep:table rep:tr Table table Tr tr"/>
	
	<!-- generate HTML skeleton on root element -->	
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:apply-templates select="rep:Report/rep:Title/text()|rep:Report/Title/text()" />
				</title>
<xsl:call-template name="css"/>
			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>

<xsl:param name="WebRoot"/>
<xsl:param name="CssPath"/>
<xsl:template name="css">
<xsl:element name="link">
<xsl:attribute name="href"><xsl:value-of select="$CssPath"/></xsl:attribute>
<xsl:attribute name="rel">stylesheet</xsl:attribute>
<xsl:attribute name="type">text/css</xsl:attribute>
</xsl:element>
</xsl:template>

<xsl:template match="rep:Report">
<div class="report">
<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="rep:Section">
<div class="section">
<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template match="rep:SubSection">
<div class="subsection">
<xsl:apply-templates/>
</div>
</xsl:template>

	<!-- Convert title -->	
<xsl:template match="rep:Report/rep:Title|rep:Report/Title">
<h1><xsl:apply-templates /></h1>
</xsl:template>

<!-- Convert headings -->	
<xsl:template match="rep:Report/rep:Heading">
<h2><xsl:apply-templates /></h2>
</xsl:template>
	
<!-- Convert paragraphs -->
<xsl:template match="rep:Text|P|p|Para|Text">
<p><xsl:apply-templates /></p>
</xsl:template>

<!-- Convert sections  -->	
<xsl:template match="rep:Section/rep:Title|rep:Section/rep:Heading|rep:Section/Title">
<h3><xsl:apply-templates/></h3>
</xsl:template>
	
<!-- Convert sub-sections -->
<xsl:template match="rep:SubSection/rep:Title|rep:SubSection/rep:Heading|rep:SubSection/Title">
<h4><xsl:apply-templates/></h4>
</xsl:template>



<xsl:template match="rep:table|Table|table">
<xsl:element name="table">
<xsl:apply-templates mode="copy" select="@*|node()"/>
</xsl:element>
</xsl:template>
    
<xsl:template match="rep:tr|Tr|tr" mode="copy">
<xsl:element name="tr">
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>
    
<xsl:template match="rep:th|Th|th" mode="copy">
<xsl:element name="th">
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>
    
<xsl:template match="rep:td|Td|td" mode="copy">
<xsl:element name="td">
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>

<xsl:template match="@*|node()" mode="copy">
<xsl:copy>
<xsl:apply-templates mode="copy" select="@*|node()"/>
</xsl:copy>
</xsl:template>

<!-- images using embedded svg -->
<xsl:template match="rep:Figure|Figure|figure">
<xsl:element name="div">
<xsl:attribute name="class">graph</xsl:attribute>
<xsl:element name="figure">
<xsl:copy-of select="svg:*" />
<xsl:apply-templates select="rep:Caption|Caption|caption" />
</xsl:element>
</xsl:element>
</xsl:template>

<xsl:template match="rep:Caption|Caption|caption" >
<figcaption><xsl:apply-templates/></figcaption>
</xsl:template>

<xsl:template match="rep:NoData|NoData">
<div class='nodata'>This plot contained no data</div>
</xsl:template>
</xsl:stylesheet>