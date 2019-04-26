<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xsd="http://www.w3.org/2001/XMLSchema"
>
    <!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />

	<xsl:template match="import">
	<xsl:value-of select="document(@schemaLocation)"></xsl:value-of>
	</xsl:template>
	
	
	
	
	
</xsl:stylesheet>