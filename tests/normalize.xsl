<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*" />
<xsl:output method="xml" indent="yes" />
 <xsl:template match="span[@class='logdate']">removed-logdate</xsl:template>
 <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
 	</xsl:template>
 	<xsl:template match="comment()">
    </xsl:template>
</xsl:stylesheet>