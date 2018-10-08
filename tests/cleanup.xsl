<?xml version="1.0" encoding="UTF-8"?>
<!-- A generic stylesheet to normalise SAFE dump data to be independent of when tests were run

 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*" />
<xsl:output method="xml" indent="yes" />
    <xsl:template match="PasswordChangeRequest/Check">
    <xsl:element name="Check">FakeCheck</xsl:element>
    </xsl:template>
    <xsl:template match="PasswordChangeRequest/Tag">
    <xsl:element name="Tag">FakeTag</xsl:element>
    </xsl:template>
     <xsl:template match="EmailChangeRequest/Tag">
    <xsl:element name="Tag">1-xxxxxxxxxxxxxxxx</xsl:element>
    </xsl:template>
    <xsl:template match="Inserted">
	<xsl:element name="Inserted">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="person/Updated">
	<xsl:element name="Updated">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="person/SignupDate">
	<xsl:element name="SignupDate">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="person/Salt">
	<xsl:element name="Salt">FakeSalt</xsl:element>
	</xsl:template>
	<xsl:template match="person/Password">
	<xsl:element name="Password">FakePassword</xsl:element>
	</xsl:template>
	<xsl:template match="Person/Updated">
	<xsl:element name="Updated">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="SignupDate">
	<xsl:element name="SignupDate">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="Person/Salt">
	<xsl:element name="Salt">FakeSalt</xsl:element>
	</xsl:template>
	<xsl:template match="Person/Password">
	<xsl:element name="Password">FakePassword</xsl:element>
	</xsl:template>
	<xsl:template match="CreationDate">
	<xsl:element name="CreationDate">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="LastUpdated">
	<xsl:element name="LastUpdated">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="StartTime">
	<xsl:element name="StartTime">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="StartDate">
	<xsl:element name="StartDate">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="Date">
	<xsl:element name="Date">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="LastContact">
	<xsl:element name="LastContact">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="ModifyDate">
	<xsl:element name="ModifyDate">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="EndTime">
	<xsl:element name="EndTime">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="Modified">
	<xsl:element name="Modified">1234567890</xsl:element>
	</xsl:template>
	<xsl:template match="Message">
	<xsl:element name="Message">removed-message</xsl:element>
	</xsl:template>
	 <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="node()">
      <xsl:sort select="name()" />
      </xsl:apply-templates>
    </xsl:copy>
 	</xsl:template>
 	<xsl:template match="comment()">
    </xsl:template>
</xsl:stylesheet>