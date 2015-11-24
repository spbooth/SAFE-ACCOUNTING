<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:tab="http://safe.epcc.ed.ac.uk/table"
	xmlns:table="xalan://uk.ac.ed.epcc.webacct.model.reports.TableExtension"
	xmlns:period="xalan://uk.ac.ed.epcc.webacct.model.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.webacct.model.reports.FilterExtension"
	extension-element-prefixes="table">

	<!-- register external parameters with param statements -->
	<xsl:param name="TableExtension" />
	<xsl:param name="PeriodExtension" />
	<xsl:param name="FilterExtension" />
	
	<xsl:template match="tab:CompoundTable">
		<xsl:variable name="compoundtable" select="table:newCompoundTable($TableExtension)"/>
	   	<xsl:apply-templates mode="CompoundTable">
			<xsl:with-param name="compoundtable" select="$compoundtable" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$compoundtable,.)" />
	</xsl:template>
	
	<xsl:template match="tab:CompoundTable" mode="CompoundTable">
		<xsl:param name="compoundtable"/>
		<xsl:variable name="innertable" select="table:newCompoundTable($TableExtension,$compoundtable)"/>
	   	<xsl:apply-templates mode="CompoundTable">
			<xsl:with-param name="compoundtable" select="$innertable" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$innertable,.)" />
	</xsl:template>
	<xsl:template match="tab:SummaryTable" mode="CompoundTable">
		<xsl:param name="compoundtable"/>
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
	   	<xsl:variable name="table" select="table:newSummaryTable($TableExtension,$compoundtable,$period,$filter,.)" />
		<xsl:apply-templates mode="SummaryTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:SummaryObjectTable" mode="CompoundTable">
		<xsl:param name="compoundtable"/>
		<xsl:variable name="filter" select="filter:makeObjectSet($FilterExtension,fil:ObjectSet)" />
	   	<xsl:variable name="table" select="table:newSummaryObjectTable($TableExtension,$compoundtable,$filter,.)" />
		<xsl:apply-templates mode="SummaryTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:Table" mode="CompoundTable">
		<xsl:param name="compoundtable"/>
	    <xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
		<xsl:variable name="table" select="table:newSimpleTable($TableExtension,$compoundtable,$period,$filter,.)"/>
		<xsl:apply-templates mode="SimpleTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:ObjectTable" mode="CompoundTable">
		<xsl:param name="compoundtable"/>
		<xsl:variable name="filter" select="filter:makeObjectSet($FilterExtension,fil:ObjectSet)" />
		<xsl:variable name="table" select="table:newObjectTable($TableExtension,$compoundtable,$filter,.)"/>
		<xsl:apply-templates mode="SimpleTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:SummaryTable">
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
	   	<xsl:variable name="table" select="table:newSummaryTable($TableExtension,$period,$filter,.)" />
		<xsl:apply-templates mode="SummaryTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:SummaryObjectTable">
		<xsl:variable name="filter" select="filter:makeObjectSet($FilterExtension,fil:ObjectSet)" />
	   	<xsl:variable name="table" select="table:newSummaryObjectTable($TableExtension,$filter,.)" />
		<xsl:apply-templates mode="SummaryTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:Table">
	    <xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
		<xsl:variable name="filter" select="filter:makeFilter($FilterExtension,ancestor::*/fil:Filter|fil:Filter)" />
		<xsl:variable name="table" select="table:newSimpleTable($TableExtension,$period,$filter,.)"/>
		<xsl:apply-templates mode="SimpleTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:ObjectTable">
		<xsl:variable name="filter" select="filter:makeObjectSet($FilterExtension,fil:ObjectSet)" />
		<xsl:variable name="table" select="table:newObjectTable($TableExtension,$filter,.)"/>
		<xsl:apply-templates mode="SimpleTable">
			<xsl:with-param name="table" select="$table" />
		</xsl:apply-templates>
		<xsl:copy-of select="table:postProcess($TableExtension,$table,.)" />
	</xsl:template>
	<xsl:template match="tab:Index|tab:Column|tab:SumColumn|tab:AverageColumn|tab:MinColumn|tab:MaxColumn" mode="SummaryTable">
		<xsl:param name="table" />
		<xsl:value-of select="table:addColumn($TableExtension,$table,.)" />
	</xsl:template>
	
	<xsl:template match="tab:Column" mode="SimpleTable">
		<xsl:param name="table" />
		<xsl:value-of select="table:addColumn($TableExtension,$table,.)" />
	</xsl:template>
	
	<xsl:template match="@*|node()" mode="CompoundTable"></xsl:template>
	<xsl:template match="@*|node()" mode="SummaryTable"></xsl:template>
	<xsl:template match="@*|node()" mode="SimpleTable"></xsl:template>

</xsl:stylesheet>   