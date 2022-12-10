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
	exclude-result-prefixes="rep acc par per fil tab cha ato"
	>
	
	

	<!--  Let stylesheet set the line breaks -->
	<xsl:strip-space elements="rep:table rep:tr Table table Tr tr"/>
	
	<!-- Inline fragment so just apply templates -->	
	<xsl:template match="/">
		<div class="report">	
				<xsl:apply-templates />
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

<!--  
<xsl:template match="rep:Section">
<div class="block">
<xsl:apply-templates/>
</div>
</xsl:template>
-->
	
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
<xsl:element name="figure">
<img src="{@src}" alt="{@alt}"/>
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

<!--  Copy processing instruction as the result includes these directly -->
<xsl:template match="processing-instruction('external-content')">
<xsl:copy/>
</xsl:template>

</xsl:stylesheet>