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
	<!-- This is intended for html embedded in 
	* an email. Email clients tend to work best with
	* inline styling rather than external style-sheets
	* or even style tags so this stylesheet has to 
	* introduce important styling directly into the html.
	* We can't expect particularly fine control so keep this to the important bits.
	* 
	 -->
	
	
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
			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
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
<h2 style="text-align: center"><xsl:apply-templates /></h2>
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
<xsl:attribute name="style">width:100%;border:solid black</xsl:attribute>
<xsl:apply-templates mode="copy" select="@*|node()"/>
</xsl:element>
</xsl:template>
    
<xsl:template match="rep:tr|Tr|tr" mode="copy">
<xsl:element name="tr">
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>

<xsl:template match="rep:tr[@class='highlight']|Tr[@class='highlight']|tr[@class='highlight']" mode="copy">
<xsl:element name="tr">
<xsl:attribute name="style">background-color: #ffffcc</xsl:attribute>
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>

<xsl:template match="rep:tr[@class='notice']|Tr[@class='notice']|tr[@class='notice']" mode="copy">
<xsl:element name="tr">
<xsl:attribute name="style">background-color: #ffcccc</xsl:attribute>
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>
    
<xsl:template match="rep:th|Th|th" mode="copy">
<xsl:element name="th">
<xsl:attribute name="style">background-color: #e7e7e7</xsl:attribute>
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>
    
<xsl:template match="rep:td|Td|td" mode="copy">
<xsl:element name="td">
<xsl:attribute name="style">background-color: #eeeeee</xsl:attribute>
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>

<xsl:template match="rep:td[@class='notice']|Td[@class='notice']|td[@class='notice']" mode="copy">
<xsl:element name="td">
<xsl:attribute name="style">background-color:#ffcccc</xsl:attribute>
<xsl:apply-templates select="@*|node()" mode="copy"/>
</xsl:element>
</xsl:template>

<xsl:template match="rep:td[@class='highlight']|Td[@class='highlight']|td[@class='highlight']" mode="copy">
<xsl:element name="td">
<xsl:attribute name="style">background-color:#ffffcc</xsl:attribute>
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
<xsl:attribute name="style">text-align: center</xsl:attribute>
<img src="{@src}" alt="{@alt}"/>
<xsl:apply-templates select="rep:Caption|Caption|caption" />
</xsl:element>
</xsl:template>

<xsl:template match="rep:Caption|Caption|caption" >
<p style='text-align: center'><xsl:apply-templates/></p>
</xsl:template>

<xsl:template match="rep:NoData|NoData">
<div class='nodata' style='text-align: center'>This plot contained no data</div>
</xsl:template>
</xsl:stylesheet>