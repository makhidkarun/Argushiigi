<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ag="http://data.travellerrpg.com/ontology/argushiigi#">
  <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>
  <xsl:param name="label.copyright"/>
  <xsl:param name="label.copyright.detail"/>
  <xsl:param name="label.home"/>
  <xsl:param name="label.home.detail"/>
  <xsl:param name="label.html"/>
  <xsl:param name="label.html.detail"/>
  <xsl:param name="label.references"/>
  <xsl:param name="label.references.detail"/>
  <xsl:param name="label.rdf"/>
  <xsl:param name="label.rdf.detail"/>
  <xsl:param name="label.ttl"/>
  <xsl:param name="label.ttl.detail"/>
  <xsl:param name="label.xml"/>
  <xsl:param name="label.xml.detail"/>
  <xsl:param name="link.references"/>
  <xsl:variable name="base" select="/@ag:base"/>
  
  <xsl:template match="/ag:resource">
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
    <head>
      <title><xsl:value-of select="@ag:name"/></title>
      <link rel="shortcut icon" href="/argushiigi/favicon.ico"/>
      <link href="http://fonts.googleapis.com/css?family=Marcellus|Open+Sans:400,400italic,600,600italic,700,700italic&amp;subset=latin,latin-ext" rel="Stylesheet" type="text/css"/>
      <link rel="Stylesheet" type="text/css"><xsl:attribute name="href">/argushiigi/css/argushiigi.css</xsl:attribute></link>
      <link rel="stylesheet" type="text/css" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.min.css"/>
      <script type="text/javascript" src="http://code.jquery.com/jquery-2.0.3.js"><xsl:text> </xsl:text></script>
      <script type="text/javascript" src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"><xsl:text> </xsl:text></script>
      <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css"/>
      <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"><xsl:text> </xsl:text></script>    
    </head>
    <body>
    <div id="header">
    <div id="left-header"><a title="{$label.home.detail}" href="/argushiigi/index.jsp"><span><xsl:value-of select="$label.home"/></span></a></div>
    <div id="centre-header">
    <div id="title"><xsl:value-of select="@ag:name"/></div>
    </div>
    <div id="right-header">
    <xsl:call-template name="format-link"><xsl:with-param name="href" select="@ag:href"/><xsl:with-param name="format">rdf</xsl:with-param><xsl:with-param name="title" select="$label.rdf.detail"/><xsl:with-param name="label" select="$label.rdf"/></xsl:call-template>
    <xsl:call-template name="format-link"><xsl:with-param name="href" select="@ag:href"/><xsl:with-param name="format">ttl</xsl:with-param><xsl:with-param name="title" select="$label.ttl.detail"/><xsl:with-param name="label" select="$label.ttl"/></xsl:call-template>
    <xsl:call-template name="format-link"><xsl:with-param name="href" select="@ag:href"/><xsl:with-param name="format">xml</xsl:with-param><xsl:with-param name="title" select="$label.xml.detail"/><xsl:with-param name="label" select="$label.xml"/></xsl:call-template>
    <span id="references-button" class="format-link"><xsl:attribute name="title"><xsl:value-of select="$label.references.detail"/></xsl:attribute><xsl:value-of select="$label.references"/></span>
    </div>
    </div>
    <div id="references" style="display: none;" class="resource-table"><xsl:attribute name="title"><xsl:value-of select="$label.references"/></xsl:attribute>
    <table id="references-table"><xsl:text> </xsl:text></table>
    </div>
    <script type="text/javascript">    
    function showReferences() {
      $("#references-table").dataTable( {
          bDestroy: true,
          bProcessing: true,
          sAjaxSource: "<xsl:value-of select="$link.references"/>",
          aoColumns: [
         {
           sTitle: "Name",
           sClass: "resource-name",
           mData: "name",
           mRender: function ( data, type, full ) {
                <xsl:text disable-output-escaping="yes"><![CDATA[return "<a href=\""+full.href+"\">" + data + "</a>";]]></xsl:text>
           }
         },
         {
           sTitle: "Class",
           sClass: "resource-class",
           mData: "cls",
           mRender: function ( data, type, full ) {
                return data;
           }
         },
         {
           sTitle: "URI",
           sClass: "resource-uri",
           mData: "uri",
           mRender: function ( data, type, full ) {
                <xsl:text disable-output-escaping="yes"><![CDATA[return "<a href=\""+full.href+"\">" + data + "</a&>";]]></xsl:text>
           }
         }
      ]
     });
     $("#references").dialog({
      modal: true,
      height: "auto",
      buttons: {
        Close: function() {
          $(this).dialog("close");
        }
      }
    });
  }
  
  $("#references-button").ready(function() {
    $("#references-button").click(showReferences)
  });
  </script>
    
    <div id="content">
    <xsl:apply-templates select="ag:category"/>
    </div>
    <div id="footer">
    <div id="left-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    <div id="centre-footer">
    <div id="copyright"><a title="{$label.copyright.detail}" href="/argushiigi/copyright.jsp"><xsl:value-of select="$label.copyright"/></a></div>
    </div>
    <div id="right-footer"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>
    </div>    
    </body>
    </html>
  </xsl:template>
  
  <xsl:template match="ag:category">
    <div class="category"><xsl:attribute name="id"><xsl:value-of select="@ag:id"/></xsl:attribute>
     <div class="label"><xsl:value-of select="@ag:name"/>
    </div>
    <xsl:apply-templates select="ag:category"/>
    <xsl:if test="ag:property">
    <table>
    <xsl:apply-templates select="ag:property"/>
    </table>
    </xsl:if>
    </div>
  </xsl:template>
   
  <xsl:template match="ag:property">
    <tr><xsl:if test="@ag:direct='false'"><xsl:attribute name="class">template</xsl:attribute></xsl:if>
    <td class="key"><xsl:apply-templates select="ag:key"/></td>
    <td class="value"><xsl:apply-templates select="ag:value"/></td>
    </tr>
  </xsl:template>
  
   <!-- default rule: copy any node beneath <ag:value> -->
  <xsl:template match="ag:key//*">
    <xsl:copy>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

  <!-- override rule: <link> nodes get special treatment -->
  <xsl:template match="ag:key//ag:link">
    <a><xsl:attribute name="href"><xsl:value-of select="@ag:href"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="@ag:uri"/></xsl:attribute><xsl:apply-templates /></a>
  </xsl:template>
  
   <!-- default rule: copy any node beneath <ag:value> -->
  <xsl:template match="ag:value//*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

  <!-- override rule: <link> nodes get special treatment -->
  <xsl:template match="ag:value//ag:link">
    <a><xsl:if test="@ag:external"><xsl:attribute name="class">external</xsl:attribute></xsl:if><xsl:attribute name="href"><xsl:value-of select="@ag:href"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="@ag:uri"/></xsl:attribute><xsl:apply-templates /></a>
  </xsl:template>

  <!-- default rule: ignore any unspecific text node -->
  <xsl:template match="text()" />

  <!-- override rule: copy any text node beneath description -->
  <xsl:template match="ag:key//text()">
    <xsl:copy-of select="." />
  </xsl:template>

  <!-- override rule: copy any text node beneath description -->
  <xsl:template match="ag:value//text()">
    <xsl:copy-of select="." />
  </xsl:template>
  
      
  <xsl:template name="format-link">
    <xsl:param name="href"/>
    <xsl:param name="format"/>
    <xsl:param name="title"/>
    <xsl:param name="label"/>
    <a class="format-link">
    <xsl:attribute name="href"><xsl:call-template name="add-parameter"><xsl:with-param name="href" select="$href"/><xsl:with-param name="param">format=<xsl:value-of select="$format"/></xsl:with-param></xsl:call-template></xsl:attribute>
    <xsl:attribute name="title" select="$title"/>
    <xsl:value-of select="$label"/>
    </a>
  </xsl:template>
  
  <xsl:template name="add-parameter">
    <xsl:param name="href"/>
    <xsl:param name="param"/>
    <xsl:value-of select="$href"/><xsl:choose><xsl:when test="contains($href,'?')">&amp;</xsl:when><xsl:otherwise>?</xsl:otherwise></xsl:choose><xsl:value-of select="$param"/>
  </xsl:template>
  
</xsl:stylesheet>