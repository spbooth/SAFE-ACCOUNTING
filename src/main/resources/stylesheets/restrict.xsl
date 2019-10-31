<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:restrict="xalan://uk.ac.ed.epcc.safe.accounting.reports.RestrictExtension"
	xmlns:acc="http://safe.epcc.ed.ac.uk/restrict"
	extension-element-prefixes="restrict">
	
	<xsl:param name="RestrictExtension"/>
		
	<xsl:template match="acc:RestrictedSection">
	<xsl:choose>
	<xsl:when test="restrict:checkAccess($RestrictExtension,acc:Roles)">
	<xsl:apply-templates select="acc:Content" mode="use" />
	</xsl:when>
	<xsl:otherwise>
	<xsl:apply-templates select="acc:Fallback" mode="use"/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:template>

   
    <xsl:template match="acc:Roles|acc:Content|acc:Fallback"/>
    <xsl:template match="acc:Roles|acc:Content|acc:Fallback" mode="use">
    <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="acc:RequireRole"></xsl:template>
    <xsl:template match="acc:SufficientRole"></xsl:template>
    <xsl:template match="acc:RequireRelationship"></xsl:template>
    <xsl:template match="acc:SufficientRelationship"></xsl:template>
</xsl:stylesheet>