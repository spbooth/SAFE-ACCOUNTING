<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:proj="http://schemas.microsoft.com/project">
 <xsl:output method="xml" indent="yes"/>
 <xsl:template match="text()|@*"/>
 <xsl:template match="proj:Project">
 <xsl:element name="Planning">
 <xsl:apply-templates/>
 </xsl:element>
 </xsl:template>
 <xsl:template name="TaskTemplate">
 	<xsl:element name="UID"><xsl:value-of select="proj:UID"/></xsl:element>
 	<xsl:element name="Position"><xsl:value-of select="proj:OutlineNumber"/></xsl:element>
	<xsl:element name="Name"><xsl:value-of select="proj:Name"/></xsl:element>
	<xsl:element name="Start"><xsl:value-of select="proj:Start"/></xsl:element>
	<xsl:element name="Finish"><xsl:value-of select="proj:Finish"/></xsl:element>
	<xsl:element name="Work"><xsl:value-of select="proj:Work"/></xsl:element>
 </xsl:template>
	<xsl:template match="proj:Task[proj:OutlineLevel=2]">
	<xsl:element name="Project">
    <xsl:call-template name="TaskTemplate"/>
	</xsl:element>
	</xsl:template>
	<xsl:template match="proj:Task[proj:OutlineLevel=3]">
	<xsl:element name="Task">
    <xsl:call-template name="TaskTemplate"/>
	</xsl:element>
	</xsl:template>
	<xsl:template match="proj:Task[proj:OutlineLevel=4]">
	<xsl:element name="SubTask">
    <xsl:call-template name="TaskTemplate"/>
	</xsl:element>
	</xsl:template>
	<xsl:template match="proj:Task[proj:OutlineLevel=5]">
	<xsl:element name="SubTask">
    <xsl:call-template name="TaskTemplate"/>
	</xsl:element>
	</xsl:template>
	<xsl:template match="proj:Task[proj:OutlineLevel=6]">
	<xsl:element name="SubTask">
    <xsl:call-template name="TaskTemplate"/>
	</xsl:element>
	</xsl:template>
	<xsl:template match="proj:Resource">
	<xsl:element name="Person">
	<xsl:element name="UID"><xsl:value-of select="proj:UID"/></xsl:element>
	<xsl:element name="Name"><xsl:value-of select="proj:Name"/></xsl:element>
	</xsl:element>
	</xsl:template>
	<xsl:template match="proj:Assignment">
	<xsl:element name="Assignment">
	<xsl:element name="TaskID">
	<xsl:value-of select="proj:TaskUID"/>
	</xsl:element>
	<xsl:element name="PersonID">
	<xsl:value-of select="proj:ResourceUID"/>
	</xsl:element>
	<xsl:element name="Start">
	<xsl:value-of select="proj:Start"/>
	</xsl:element>
	<xsl:element name="Finish">
	<xsl:value-of select="proj:Finish"/>
	</xsl:element>
	<xsl:element name="Work">
	<xsl:value-of select="proj:Work"/>
	</xsl:element>
	<xsl:apply-templates select="proj:TimephasedData"/>
	</xsl:element>
	</xsl:template>
	
	<xsl:template match="proj:TimephasedData">
	<xsl:element name="Data">
	<xsl:element name="Start"><xsl:value-of select="proj:Start"/></xsl:element>
	<xsl:element name="Finish"><xsl:value-of select="proj:Finish"/></xsl:element>
	<xsl:element name="Value"><xsl:value-of select="proj:Value"/></xsl:element>
	</xsl:element>
	</xsl:template>
</xsl:stylesheet>