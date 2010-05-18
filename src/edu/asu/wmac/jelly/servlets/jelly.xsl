<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="requestURL"/>

<xsl:template match="projects">
<head><title>JELLY</title></head>
Please select a project:
<p/>
<ul>
 <xsl:for-each select="project">
  <li><a href="Jelly?action=selectproject&amp;project={@name}"><xsl:value-of select="@name"/></a></li>
 </xsl:for-each>
</ul>
</xsl:template>

<xsl:template match="optionsPage">
<form action="{$requestURL}" method="post">
<input type="hidden" name="action" value="go"/>
<input type="hidden" name="project" value="{project/@name}"/>
Project: <xsl:value-of select="project/@name"/>
<p/>
Source:
<xsl:value-of select="source/@class"/>
<p/>
<xsl:apply-templates select="source/options"/>
<p/>

Builder:
<xsl:value-of select="builder/@class"/>
<p/>
<xsl:apply-templates select="builder/options"/>
<p/>

Deployer:
<xsl:value-of select="deployer/@class"/>
<p/>
<xsl:apply-templates select="deployer/options"/>
<p/>
<input type="submit" value="Go"/>
</form>
</xsl:template>

<xsl:template match="options">
 <xsl:for-each select="option">
  <b><xsl:value-of select="@label"/>:</b>
  <xsl:choose>
   <xsl:when test="@type = 'boolean'">
    <input type="checkbox" name="{name(../..)}-{@name}"/>
   </xsl:when>
   <xsl:when test="@type = 'list'">
    <select name="{name(../..)}-{@name}">
     <xsl:for-each select="choice">
      <option><xsl:value-of select="@value"/></option>
     </xsl:for-each>
    </select>
   </xsl:when>
   <xsl:otherwise>
    <input name="{name(../..)}-{@name}"/>
   </xsl:otherwise>
  </xsl:choose>
  <br/>
 </xsl:for-each>
</xsl:template>

<xsl:template match="go">
Done.
<p/>
Build output:
<pre>
<xsl:value-of select="build"/>
</pre>
</xsl:template>

<xsl:template match="error">
 <font color="red">Error</font>
 <xsl:value-of select="@message"/>
</xsl:template>

</xsl:stylesheet>