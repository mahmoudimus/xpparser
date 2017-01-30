<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:output method="text"/>

  <xsl:param name="fragments"
             select="('xpath-1.0-core.rnc', 'xpath-1.0-downward.rnc', 'xpath-1.0-forward.rnc', 'xpath-1.0-vertical.rnc', 'xpath-1.0-data.rnc', 'xpath-1.0-eval.rnc', 'xpath-1.0.rnc', 'xpath-modal.rnc', 'xpath-hybrid.rnc')"/>

  <xsl:template match="/">
    <xsl:variable name="root" select="/"/>
    <!-- fragments -->
    <xsl:result-document method="text" href="fragments.csv">
      <xsl:text>name,color,entries
</xsl:text>
      <xsl:for-each select="$fragments">
        <xsl:call-template name="fragment">
          <xsl:with-param name="f" select="string(current())"/>
          <xsl:with-param name="root" select="$root"/>
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
          <xsl:with-param name="source" select="string(current())"/>
          <xsl:with-param name="root" select="$root"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
    </xsl:result-document>
  </xsl:template>

  <!-- one line of the matrix -->
  <xsl:template name="line">
    <xsl:param name="source"/>
    <xsl:param name="root"/>
    <xsl:text>[</xsl:text>
    <xsl:for-each select="$fragments">
      <xsl:call-template name="entry">
        <xsl:with-param name="source" select="$source"/>
        <xsl:with-param name="target" select="string(current())"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
    <xsl:choose>
      <xsl:when test="$source != $fragments[last()]">
        <xsl:text>,</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- one entry of the matrix -->
  <xsl:template name="entry">
    <xsl:param name="source"/>
    <xsl:param name="target"/>
    <xsl:param name="root"/>
    <xsl:value-of select="count($root//xpath[validation[@schema=$source and @valid='yes'] and validation[@schema=$target and @valid='no']])"/>    
    <xsl:choose>
      <xsl:when test="$target != $fragments[last()]">
        <xsl:text>,</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- one fragment -->
  <xsl:template name="fragment">
    <xsl:param name="f"/>
    <xsl:param name="root"/>
    <xsl:variable name="name" select="substring-before(substring-after($f,'xpath-'),'.rnc')"/>
    <xsl:choose>
      <xsl:when test="matches($name,'1.0-.*')">
        <xsl:value-of select="substring-after($name,'1.0-')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="matches($f,'.*-core.rnc')">
        <xsl:text>,#ccebc5,</xsl:text>
      </xsl:when>
      <xsl:when test="matches($f,'.*-(downward|forward|vertical).rnc')">
        <xsl:text>,#b3cde3,</xsl:text>
      </xsl:when>
      <xsl:when test="matches($f,'.*-(data|eval|leashed).rnc')">
        <xsl:text>,#fbb4ae,</xsl:text>
      </xsl:when>
      <xsl:when test="matches($f,'.*-[123].0.rnc')">
        <xsl:text>,#decbe4,</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>,#fed9a6,</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="count($root//xpath[validation[@schema=$f and @valid='yes']])"/>
  </xsl:template>
</xsl:stylesheet>
                