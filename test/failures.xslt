<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:output method="xml" indent="yes" />

  <xsl:param name="results"
             select="document('results.xml')/benchmark"/>

  <xsl:template match="/">
    <tests>
      <xsl:for-each select="//test">
        <test>

		<xsl:for-each select="query">
		  <query>
			<xsl:copy-of select="node()"/>
		  </query>
		</xsl:for-each>

		<xsl:variable name="query" select="query/text()" />

		<xsl:for-each select="validation">

		  <xsl:variable name="schema" select="@schema" />
		  <xsl:variable name="expected" select="@valid" />

		  <xsl:if test="not($results/xpath[query/text()=$query]/validation[@schema=$schema])">
			<failure text="test result not found">
			  <xsl:attribute name="schema">
				<xsl:value-of select="@schema" />
			  </xsl:attribute>
			</failure>
		  </xsl:if>

		  <xsl:if test="$results/xpath[query/text()=$query]/validation[@schema=$schema][@valid!=$expected]">
			<failure text="incorrect validation result">
			  <xsl:attribute name="schema">
				<xsl:value-of select="@schema" />
			  </xsl:attribute>
			  <xsl:attribute name="expected">
				<xsl:value-of select="@valid" />
			  </xsl:attribute>
			</failure>
		  </xsl:if>
		</xsl:for-each>

	  </test>
	  </xsl:for-each>
	</tests>
  </xsl:template>

</xsl:stylesheet>
