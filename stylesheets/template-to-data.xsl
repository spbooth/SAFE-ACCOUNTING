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
<xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
 </xsl:template>
 
	<xsl:template match="rep:Report">
	<xsl:element name="DataReport">
	<xsl:apply-templates />
	</xsl:element>		
	</xsl:template>

<xsl:template match="rep:Section">
<xsl:element name="DataSection">
<xsl:attribute name="name"><xsl:copy-of select="rep:Title/text()"></xsl:copy-of></xsl:attribute>
<xsl:apply-templates/>
</xsl:element>
</xsl:template>

<xsl:template match="rep:SubSection">
<xsl:attribute name="name"><xsl:copy-of select="rep:Title/text()"></xsl:copy-of></xsl:attribute>
<xsl:element name="DataSubSection">
<xsl:apply-templates/>
</xsl:element>
</xsl:template>

	<!-- Convert title -->	
<xsl:template match="rep:Report/rep:Title|rep:Report/Title">
</xsl:template>

<!-- Convert headings -->	
<xsl:template match="rep:Heading">
</xsl:template>

<!-- Convert paragraphs -->
<xsl:template match="rep:Text|P|p|Para|Text">
</xsl:template>

<!-- Convert sections  -->	
<xsl:template match="rep:Section/rep:Title|rep:Section/Title">
</xsl:template>
	
<!-- Convert sub-sections -->
<xsl:template match="rep:SubSection/rep:Title|rep:SubSection/Title">
</xsl:template>


</xsl:stylesheet>