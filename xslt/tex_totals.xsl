<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">

  <xsl:output method="text"/>

  <xsl:param name="xml_fragments"
    select="../relaxng/none.xml" />

  <xsl:param name="fragments"
             select="document($xml_fragments)//schema"/>

  <xsl:template match="/">
    <xsl:variable name="benchmarks" select="/"/>

    <xsl:text>\begin{tabular}{|r|</xsl:text>
    <xsl:for-each select="//benchmark">
      <xsl:text>l</xsl:text>
    </xsl:for-each>
    <xsl:text>|l|}&#xa;\toprule&#xa;</xsl:text>

    <!-- line with benchmark names -->
    <xsl:for-each select="//benchmark">
      <xsl:text> &amp; </xsl:text>
      <xsl:value-of select="current()/@name" />
    </xsl:for-each>
    <xsl:text> &amp; \\ &#xa;\midrule&#xa;</xsl:text>

    <!-- line for the size of benchmarks -->
    <xsl:text>size</xsl:text>
    <xsl:for-each select="//benchmark">
      <xsl:text> &amp; </xsl:text>
      <xsl:value-of select="count(document(current()/@href)//xpath)" />
    </xsl:for-each>
    <xsl:text> &amp; total \\ &#xa;\midrule&#xa;</xsl:text>

    <!-- for each benchmark, a line of results -->
    <xsl:for-each select="$fragments">
      <xsl:call-template name="line">
        <xsl:with-param name="fragment" select="current()"/>
        <xsl:with-param name="benchmarks" select="$benchmarks"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:text>\bottomrule&#xa;\end{tabular}&#xa;</xsl:text>

  </xsl:template>

  <!-- one line of the matrix -->
  <xsl:template name="line">
    <xsl:param name="fragment"/>
    <xsl:param name="benchmarks"/>
    <xsl:value-of select="$fragment/@name" />
    <xsl:text> &amp; </xsl:text>
    <xsl:for-each select="$benchmarks//benchmark">
      <xsl:call-template name="entry">
        <xsl:with-param name="fragment" select="$fragment"/>
        <xsl:with-param name="benchmark" select="current()"/>
      </xsl:call-template>
    </xsl:for-each>
    <!-- total number of entries in the fragment -->
    <xsl:value-of select="count(document($benchmarks//benchmark/@href)//xpath[schemas/validation[@schema=$fragment/@file and @valid='yes']])" />
    <xsl:text> \\</xsl:text>
  </xsl:template>

  <!-- one entry of the matrix -->
  <xsl:template name="entry">
    <xsl:param name="fragment"/>
    <xsl:param name="benchmark"/>
    <xsl:value-of select="count(document($benchmark/@href)//xpath[schemas/validation[@schema=$fragment/@file and @valid='yes']])"/> 
    <xsl:text> &amp; </xsl:text>
  </xsl:template>

</xsl:stylesheet>
