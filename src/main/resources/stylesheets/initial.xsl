<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:acc="http://safe.epcc.ed.ac.uk/restrict"
	xmlns:par="http://safe.epcc.ed.ac.uk/parameter">
<!-- This style-sheet is used before the parameter form is built. 
 -->
 
	<!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />
	<xsl:import href="restrict.xsl" />
<!-- Do includes in the initial transform -->
	<xsl:template match="par:Include">
	<xsl:copy-of select="document(@file)"></xsl:copy-of>
	</xsl:template>
	
</xsl:stylesheet>