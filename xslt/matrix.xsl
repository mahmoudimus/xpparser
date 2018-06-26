<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:output method="text"/>

  <xsl:param name="fragments"
             select="document('../relaxng/fragments-full.xml')//schema[@name
                     != 'Downward' and @name != 'Forward']"/>

  <xsl:param name="nexamples" select="15"/>

  <xsl:template match="/">
    <xsl:variable name="benchmarks" select="document(//benchmark/@href)"/>
    <!-- fragments -->
    <xsl:result-document method="text" href="fragments.csv">
      <xsl:text>name,color,entries
</xsl:text>
      <xsl:for-each select="$fragments">
        <xsl:call-template name="fragment">
          <xsl:with-param name="f" select="current()"/>
          <xsl:with-param name="benchmarks" select="$benchmarks"/>
        </xsl:call-template>
        <xsl:text>
</xsl:text>
      </xsl:for-each>      
    </xsl:result-document>
    <!-- matrix -->
    <xsl:result-document method="text" href="matrix.json">
      <xsl:text>[</xsl:text>
      <xsl:for-each select="$fragments">
        <xsl:call-template name="line">
          <xsl:with-param name="source" select="current()"/>
          <xsl:with-param name="benchmarks" select="$benchmarks"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
    </xsl:result-document>
  </xsl:template>

  <!-- one line of the matrix -->
  <xsl:template name="line">
    <xsl:param name="source"/>
    <xsl:param name="benchmarks"/>
    <xsl:text>[</xsl:text>
    <xsl:for-each select="$fragments">
      <xsl:call-template name="entry">
        <xsl:with-param name="source" select="$source"/>
        <xsl:with-param name="target" select="current()"/>
        <xsl:with-param name="benchmarks" select="$benchmarks"/>
        <xsl:with-param name="fragments" select="$fragments"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
    <xsl:if test="$source ne $fragments[last()]">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- one entry of the matrix -->
  <xsl:template name="entry">
    <xsl:param name="source"/>
    <xsl:param name="target"/>
    <xsl:param name="benchmarks"/>
    <xsl:param name="fragments"/>
    <xsl:variable name="examples" select="$benchmarks//xpath[schemas/validation[@schema=$source/@file and @valid='yes'] and schemas/validation[@schema=$target/@file and @valid='no']]"/> 
    <xsl:text>{ "z": </xsl:text><xsl:value-of select="count($examples)"/>
    <xsl:if test="$examples">
      <xsl:text>, "examples": [</xsl:text>
      <xsl:for-each select="$examples[position() &lt;= $nexamples]">
        <xsl:text>"</xsl:text><xsl:value-of 
        select="replace(replace(normalize-space(current()/query),'&quot;',&quot;'&quot;),'\\','\\\\')"/>
        <xsl:text>"</xsl:text>
        <xsl:if test="position() != last() and position() &lt; $nexamples">
          <xsl:text>,</xsl:text>
        </xsl:if>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
    </xsl:if>
    <xsl:text> }</xsl:text>
    <xsl:if test="$target != $fragments[last()]">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- one fragment -->
  <xsl:template name="fragment">
    <xsl:param name="f"/>
    <xsl:param name="benchmarks"/>
    <xsl:value-of select="$f/@name"/>
    <xsl:text>,</xsl:text>
    <xsl:value-of select="$f/@color"/>
    <xsl:text>,</xsl:text>
    <xsl:value-of select="count($benchmarks//xpath[schemas/validation[@schema=$f/@file and @valid='yes']])"/>
  </xsl:template>
</xsl:stylesheet>
