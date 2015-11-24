<xsl:stylesheet 
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	

<xsl:template match="document">
<xsl:apply-templates/>
</xsl:template>


<xsl:template match="test1">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="test2">
<xsl:apply-templates/>
</xsl:template>

<!--<xsl:template match="name1"/>-->
<!--<xsl:template match="name2"/>-->

<!--  A non query template that needs to be run as well -->
<xsl:template match="block">
Start Block
<xsl:apply-templates/>
End Block
</xsl:template>

<xsl:template match="bracket">[<xsl:apply-templates/>]</xsl:template>

<xsl:template match="probe" >
<xsl:variable name="test1" select="ancestor-or-self::*/name1/text()"/>
<xsl:variable name="test2" select="ancestor-or-self::*/name2/text()"/>
Test 1 <xsl:copy-of select="$test1"/>
Test 2 <xsl:copy-of select="$test2"/>
</xsl:template>

  <!--  Remove macro definition nodes -->
    <xsl:template match="MacroDefinition"/>
    
    <!--  Expand definition use -->
    <xsl:template match="UseMacro">
    <xsl:variable name="MacroName" select="@name"/>
    <xsl:apply-templates select="ancestor::*/MacroDefinition[@name=$MacroName]/*"/>
    </xsl:template>

</xsl:stylesheet>