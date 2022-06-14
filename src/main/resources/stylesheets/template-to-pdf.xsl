<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
	version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:xlink="http://www.w3.org/1999/xlink"	
	xmlns:rep="http://safe.epcc.ed.ac.uk/report"
	xmlns:acc="http://safe.epcc.ed.ac.uk/restrict"
	xmlns:par="http://safe.epcc.ed.ac.uk/parameter"
	xmlns:per="http://safe.epcc.ed.ac.uk/period"
	xmlns:fil="http://safe.epcc.ed.ac.uk/filter"
	xmlns:tab="http://safe.epcc.ed.ac.uk/table"
	xmlns:cha="http://safe.epcc.ed.ac.uk/chart"
	xmlns:ato="http://safe.epcc.ed.ac.uk/atom"
	xmlns:svg="http://www.w3.org/2000/svg">

    <!-- Generate a FOP docment to be converted to PDF.
    *  Note that style information needs to be implemented directly here
    *
    *
    *
     -->

   	<!--  Let stylesheet set the line breaks -->
	<xsl:strip-space elements="rep:table rep:tr Table table Tr tr"/>
	<!-- generate PDF skeleton on root element -->
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="page"
					page-height="29.7cm" page-width="21cm" margin-top="1cm"
					margin-bottom="2cm" margin-left="1.5cm" margin-right="1.5cm">
					<fo:region-body margin-top="3cm" />
					<fo:region-before extent="3cm" />

					<fo:region-after extent="1.5cm" />
				</fo:simple-page-master>

				<fo:page-sequence-master master-name="all">
					<fo:repeatable-page-master-alternatives>

						<fo:conditional-page-master-reference
							master-reference="page" page-position="first" />
						<fo:conditional-page-master-reference
							master-reference="page" page-position="rest" />
					</fo:repeatable-page-master-alternatives>
				</fo:page-sequence-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="all">
				<fo:flow flow-name="xsl-region-body">
					<fo:block>
						<xsl:apply-templates />
					</fo:block>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<!-- Convert title to a XSL-FO heading -->
	<xsl:template match="rep:Report/rep:Title|rep:Report/Title">
		<fo:block font-size="20pt" text-align="center" color="black" keep-with-next.within-page="always">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>

	<!-- Convert headings to XSL-FO headings -->
	<xsl:template match="rep:Report/rep:Heading">
		<fo:block font-size="18pt" color="black" font-weight="bold"
		keep-with-next.within-page="always"
			space-before.optimum="20pt">
			<xsl:apply-templates select="text()" />
		</fo:block>
	</xsl:template>

	<!-- Convert paragraphs -->
	<xsl:template match="rep:Text|P|p|Para|Text">
		<fo:block space-before.optimum="10pt" font-size="12pt">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>
	
		<!-- Convert paragraphs -->
	<xsl:template match="rep:PreFormatted|PreFormatted">
		<fo:block space-before.optimum="10pt" font-size="12pt" wrap-option="wrap" white-space="pre" linefeed-treatment="preserve">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>
	
	<xsl:template match="rep:Nodata|NoData">
	   <fo:block space-before.optimum="10pt" font-size="12pt" font-weight="bold" text-align="center">
	    This plot contained no data
	    </fo:block>
	</xsl:template>
	<!-- Convert sections to XSL-FO headings -->
	
<xsl:template match="rep:Section/rep:Title|rep:Section/rep:Heading|rep:Section/Title">
		<fo:block font-size="16pt" color="black" font-weight="bold"
		keep-with-next.within-page="always"
		break-before="page"
			space-before.optimum="20pt">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>
	<!--  No page break before first section -->
	<xsl:template match="rep:Section[1]/rep:Title|rep:Section[1]/rep:Heading|rep:Section[1]/Title">
		<fo:block font-size="16pt" color="black" font-weight="bold"
		keep-with-next.within-page="always"
			space-before.optimum="20pt">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>
	<!-- Convert sub-sections to XSL-FO headings -->
	<xsl:template match="rep:SubSection/rep:Title|rep:SubSection/rep:Heading|rep:SubSection/Title">
		<fo:block font-size="14pt" color="black" font-weight="bold"
		keep-with-next.within-page="always"
			space-before.optimum="15pt">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	

	<!-- Convert tables to fo:tables -->
	<xsl:template match="rep:table|Table|table">
		<fo:table table-layout="fixed" width="100%">
			<xsl:apply-templates select="rep:tr[position()=1]/*|Tr[position()=1]/*|tr[position()=1]/*" mode="columns" />
			<xsl:if test="rep:tr[rep:th][not(child::rep:td)]|Tr[Th][not(child::Td)]|tr[th][not(child::td)]">
			<fo:table-header font-size="10pt" font-family="sans-serif" background-color="#e7e7e7">
			<!-- Assume all th cells come from header rows -->
			<xsl:apply-templates select="rep:tr[rep:th][not(child::rep:td)]|Tr[Th][not(child::Td)]|tr[th][not(child::td)]" mode="header"/>
			</fo:table-header>
			</xsl:if>
			<fo:table-body font-size="8pt" font-family="sans-serif"
				hyphenate="true">
				<!-- Assume all td cells come from body -->
				<xsl:apply-templates select="rep:tr[rep:td]|Tr[Td]|tr[td]" />
			</fo:table-body>
		</fo:table>
	</xsl:template>
 
	<xsl:template match="rep:th|rep:td|Th|Td|th|td" mode="columns">
		<fo:table-column column-width="proportional-column-width(1)"/>
	</xsl:template>
	<xsl:template match="rep:th[@colspan]|rep:td[@colspan]|Th[@colspan]|Td[@colspan]|th[@colspan]|td[@colspan]" mode="columns">
		<fo:table-column column-width="proportional-column-width(1)" number-columns-repeated="{@colspan}"/>
	</xsl:template>
	<xsl:template match="rep:tr|Tr|tr" mode="header">
			<fo:table-row>
				<xsl:apply-templates select="rep:th|Th|th" />
			</fo:table-row>
	</xsl:template>
	
	<xsl:template match="rep:tr|Tr|tr">
		<fo:table-row>
			<xsl:apply-templates select="rep:th|rep:td|Th|Td|th|td" />
		</fo:table-row>
	</xsl:template>
	
	<xsl:template match="rep:tr[@class='highlight']|Tr[@class='highlight']|tr[@class='highlight']">
		<fo:table-row background-color="#ffffcc">
			<xsl:apply-templates select="rep:th|rep:td|Th|Td|th|td" />
		</fo:table-row>
	</xsl:template>

	<xsl:template match="rep:tr[@class='notice']|Tr[@class='notice']|tr[@class='notice']">
		<fo:table-row background-color="#ffcccc">
			<xsl:apply-templates select="rep:th|rep:td|Th|Td|th|td" />
		</fo:table-row>
	</xsl:template>
    <xsl:template mode="span" match="@*"></xsl:template>
	<xsl:template mode="span" match="@rowspan">
	<xsl:attribute name="number-rows-spanned"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template mode="span" match="@colspan">
	<xsl:attribute name="number-columns-spanned"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="rep:th|Th|th">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm">
			<xsl:apply-templates mode="span" select="@*"/>
			<fo:block font-weight="bold" text-align="center" background-color="inherit">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>

	
	<xsl:template match="rep:td[@class='highlight']|Td[@class='highlight']|td[@class='highlight']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#ffffcc">
				<xsl:apply-templates mode="span" select="@*"/>
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	
	<xsl:template match="rep:td[@class='frac_0']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#ed1818">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_1']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#ed4218">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_2']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#ed6d18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_3']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#ed9818">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_4']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#edc218">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_5']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#eded18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_6']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#c2ed18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_7']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#98ed18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_8']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#6ded18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_9']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#42ed18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='frac_10']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#18ed18">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td[@class='notice']|Td[@class='notice']|td[@class='notice']">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="#ffcccc">
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="rep:td|Td|td">
		<fo:table-cell border-color="black" border-style="solid"
			border-width="0.2mm" background-color="inherit">
				<xsl:apply-templates mode="span" select="@*"/>
			<fo:block text-align="right">
				<xsl:apply-templates />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	
	<!-- images mapped onto external graphics -->
	<xsl:template match="rep:Figure|Figure|figure">
		<fo:block space-before.optimum="10pt" text-align="center">
			<fo:table table-layout="fixed" width="100%">
				<fo:table-column column-width="proportional-column-width(1)"/>
				<fo:table-column column-width="12cm"/>
				<fo:table-column column-width="proportional-column-width(1)"/>
				<fo:table-body>
					<fo:table-row>
						<fo:table-cell column-number="2">
							<fo:block text-align="center">
								<fo:instream-foreign-object
									content-width="18cm" scaling="scale-to-fit">
									<xsl:copy-of select="svg:*" />
								</fo:instream-foreign-object>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell column-number="2">
							<fo:block font-size="10pt" space-before.optimum="0pt"
								keep-with-previous.within-page="always"
								text-align="left">
								<xsl:apply-templates select="rep:Caption|Caption|caption"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-body>
			</fo:table>
		</fo:block>
	</xsl:template>
</xsl:stylesheet>

       
