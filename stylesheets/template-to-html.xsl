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
	<!--  Let style-sheet set the line breaks -->
	<xsl:strip-space elements="Table table Tr tr rep:table"/>
	<xsl:output method="html" />
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
<xsl:template match="rep:Section/rep:Heading">
<h3><xsl:apply-templates /></h3>
</xsl:template>
<xsl:template match="rep:SubSection/rep:Heading">
<h4><xsl:apply-templates /></h4>
</xsl:template>	
<!-- Convert paragraphs -->
<xsl:template match="rep:Text|P|p|Para|Text">
<p><xsl:apply-templates /></p>
</xsl:template>

<!-- Convert sections  -->	
<xsl:template match="rep:Section/rep:Title|rep:Section/Title">
<h3><xsl:apply-templates/></h3>
</xsl:template>
	
<!-- Convert sub-sections -->
<xsl:template match="rep:SubSection/rep:Title|rep:SubSection/Title">
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

<!-- images mapped onto pngs -->
<xsl:template match="rep:Figure|Figure">
<xsl:element name="div">
<xsl:attribute name="class">graph</xsl:attribute>
<img src="{@src}" alt="{@alt}"/>
<xsl:apply-templates select="rep:Caption|Caption|caption" />
</xsl:element>
</xsl:template>

<xsl:template match="rep:Caption|Caption|caption" >
<p><xsl:apply-templates/></p>
</xsl:template>

<xsl:template match="rep:NoData|NoData">
<div class='nodata'>This plot contained no data</div>
</xsl:template>
</xsl:stylesheet>