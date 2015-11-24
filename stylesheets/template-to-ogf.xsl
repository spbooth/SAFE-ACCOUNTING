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
	xmlns:urwg="http://www.gridforum.org/2003/ur-wg" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
	exclude-result-prefixes="rep acc par per fil tab cha ato fmt">
	<xsl:output method="xml" />
	<!-- Import the identity transformation. -->
	<xsl:include href="identity.xsl" />

	<!-- Remove the newlines for the stripped out ParamererDef etc. -->
	<xsl:strip-space elements="urwg:JobUsageRecord"/>
	
<xsl:template match="rep:Report">
<xsl:element name="UsageRecords" namespace="http://www.gridforum.org/2003/ur-wg">
<xsl:apply-templates />
</xsl:element>
</xsl:template>

</xsl:stylesheet>