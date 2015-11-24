<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:mac="http://safe.epcc.ed.ac.uk/macro"
	xmlns:xalan="http://xml.apache.org/xalan" >
 
	<!-- Import the identity transformation. -->
	<xsl:import href="identity.xsl" />
    
    <!-- We also perform Macro expansion in this pass so that the expanded macro will be
         visible in the input Node tree of the processing style-sheet. -->
    <!--  Remove macro definition nodes -->
    <xsl:template match="mac:MacroDef"/>
    
    <!--  Expand definition use -->
    <xsl:template match="mac:Macro">
    	<xsl:variable name="macro_name" select="@name"/>
    	<xsl:apply-templates select="preceding::mac:MacroDef[@name=$macro_name]" mode="expand"/>
    </xsl:template>
    
    <xsl:template match="mac:MacroDef" mode="expand">
    <xsl:apply-templates/>
    </xsl:template>
    
    
    <xsl:template match="mac:Switch">
    <xsl:for-each select="mac:Case">
    <xsl:variable name="key" select="@Key"/>
    <xsl:variable name="value">
    <xsl:apply-templates select="mac:Value"/>
    </xsl:variable>
    <xsl:if test="$value=$key">
    <xsl:apply-templates select="mac:Body" mode="expand"/>
    </xsl:if>
    </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="mac:Body" mode="expand"><xsl:apply-templates/></xsl:template>
</xsl:stylesheet>