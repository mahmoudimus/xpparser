<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  >

  <!--
	   This XSLT stylesheet is meant to strip XML benchmarks
       from all information that is not necessary for the visualization
       of results in results.js/html.
	   Currently, the AST is actually stripped first using a Perl hack,
       because xsltproc does not support huge XML files.
    -->

  <xsl:output method="xml"/>

  <xsl:template match="/">
    <benchmark>
      <xsl:for-each select="//xpath">
        <xpath>

		  <xsl:attribute name="filename">
			<xsl:value-of select="@filename" />
		  </xsl:attribute>

		  <xsl:attribute name="line">
			<xsl:value-of select="@line" />
		  </xsl:attribute>

		  <xsl:attribute name="column">
			<xsl:value-of select="@column" />
		  </xsl:attribute>

          <xsl:for-each select="query">
            <query>
              <xsl:copy-of select="node()"/>
            </query>
          </xsl:for-each>
          <xsl:for-each select="validation">
            <validation>
              <xsl:attribute name="schema">
                <xsl:value-of select="@schema" />
              </xsl:attribute>
              <xsl:attribute name="valid">
                <xsl:value-of select="@valid" />
              </xsl:attribute>
			  <!-- <xsl:copy-of select="node()"/> -->
            </validation>
          </xsl:for-each>

        </xpath>
      </xsl:for-each>
    </benchmark>
  </xsl:template>

</xsl:stylesheet>
