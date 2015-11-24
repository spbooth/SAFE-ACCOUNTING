<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:tab="http://safe.epcc.ed.ac.uk/table"
	xmlns:sched="http://safe.epcc.ed.ac.uk/schedule"
	xmlns:table="xalan://uk.ac.ed.epcc.webacct.model.reports.TableExtension"
	xmlns:period="xalan://uk.ac.ed.epcc.webacct.model.reports.PeriodExtension"
	xmlns:filter="xalan://uk.ac.ed.epcc.webacct.model.reports.FilterExtension"
	xmlns:schedule="xalan://uk.ac.ed.epcc.planning.model.balance.ScheduleExtension"
	>
	<xsl:import href="identity.xsl" />
	
	<!-- register external parameters with param statements -->
	<xsl:param name="TableExtension" />
	<xsl:param name="PeriodExtension" />
	<xsl:param name="FilterExtension" />
	<xsl:param name="ScheduleExtension" />
	<xsl:param name="AuthenticatedUser" />
	
	<xsl:template match="sched:Schedule">
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
	  <xsl:variable name="scheduleTable" select="schedule:makeScheduleTable($ScheduleExtension,$AuthenticatedUser,$period)" />
	 	<xsl:copy-of select="table:postProcess($TableExtension,$scheduleTable,.)" />
	</xsl:template>
	
<xsl:template match="sched:Notes">
		<xsl:variable name="period" select="period:makePeriod($PeriodExtension,(ancestor::*/per:Period|per:Period)[last()])" />
	  <xsl:variable name="notesTable" select="schedule:makeNotesTable($ScheduleExtension,$AuthenticatedUser,$period)" />
	 	<xsl:copy-of select="table:postProcess($TableExtension,$notesTable,.)" />
	</xsl:template>
</xsl:stylesheet>   