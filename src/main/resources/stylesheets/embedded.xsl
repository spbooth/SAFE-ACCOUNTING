<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:emb="http://safe.epcc.ed.ac.uk/embedded"
	xmlns:embedded="xalan://uk.ac.ed.epcc.safe.accounting.reports.EmbeddedExtension"
>

<!-- Add Embedded fragments to the EmbeddedExtension.

 -->
<xsl:import href="identity.xsl" />

<xsl:param name="EmbeddedExtension" />
	<xsl:template match="emb:Define">
	    <xsl:variable name="content">
	    <xsl:apply-templates/>
	    </xsl:variable>
		<xsl:value-of select="embedded:addFragment($EmbeddedExtension,@name,$content)"></xsl:value-of>
	</xsl:template>
</xsl:stylesheet>