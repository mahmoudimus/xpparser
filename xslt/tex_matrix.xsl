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
    <xsl:variable name="root"
                  select="document(//benchmark[@type='xquery' or @type='xslt']/@href)"/>

    <xsl:text>\begin{tabular}{l</xsl:text>
    <xsl:for-each select="$fragments">
      <xsl:text>r</xsl:text>
    </xsl:for-each>
    <xsl:text>}&#xa;\toprule&#xa;</xsl:text>

    <xsl:text></xsl:text>
    <xsl:for-each select="$fragments">
      <xsl:text> &amp; </xsl:text>
      \textsf{<xsl:value-of select="current()/@nametex" />}
    </xsl:for-each>
    <xsl:text> \\ &#xa;\midrule&#xa;</xsl:text>

    <xsl:for-each select="$fragments">
      <xsl:call-template name="line">
        <xsl:with-param name="source" select="current()"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:text>\bottomrule&#xa;\end{tabular}&#xa;</xsl:text>

  </xsl:template>

  <!-- one line of the matrix -->
  <xsl:template name="line">
    <xsl:param name="source"/>
    <xsl:param name="root"/>
    \textsf{<xsl:value-of select="$source/@nametex" />}
    <xsl:text> &amp; </xsl:text>
    <xsl:for-each select="$fragments">
      <xsl:call-template name="entry">
        <xsl:with-param name="source" select="$source"/>
        <xsl:with-param name="target" select="current()"/>
        <xsl:with-param name="root" select="$root"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:text> \\&#xa;</xsl:text>
  </xsl:template>

  <!-- one entry of the matrix -->
  <xsl:template name="entry">
    <xsl:param name="source"/>
    <xsl:param name="target"/>
    <xsl:param name="root"/>
    <xsl:value-of select="format-number(count($root//xpath[schemas/validation[@schema=$source/@file and @valid='yes'] and schemas/validation[@schema=$target/@file and @valid='no']]),'#,##0')"/>
    <xsl:if test="not($target is $fragments[last()])">
      <xsl:text> &amp; </xsl:text>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
