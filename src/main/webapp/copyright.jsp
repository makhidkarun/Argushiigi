<%@ include file="/WEB-INF/prelude.jsp" %>
<fmt:bundle basename="org.charvolant.argushiigi.server.Messages">
<fmt:message var="title" key="jsp.copyright.title"/>
<html>
<head>
<%@ include file="/WEB-INF/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/header.jsp" %>
<div id="content">
<div id="about">
<fmt:message key="jsp.copyright.about"/>
</div>
<div id="settings" class="resource-table">
<h2><fmt:message key="label.copyrightRightsLicenses"/></h2>
<table id="settings-table"></table>
<script type="text/javascript">
$("#settings-table").ready(function() {
	$("#settings-table").dataTable( {
		bProcessing: true,
		sAjaxSource: "<c:url value="/data/query/type"><c:param name="type" value="http://purl.org/dc/terms/RightsStatement"/></c:url>",
		aoColumns: [
		   {
			   sTitle: "<fmt:message key="label.name"/>",
			   sClass: "resource-name",
			   mData: "name",
			   mRender: function ( data, type, full ) {
			        return "<a href=\""+full.href+"\">" + data + "</a>";
			   }
		   },
		   {
			   sTitle: "<fmt:message key="label.class"/>",
			   sClass: "resource-class",
			   mData: "cls",
			   mRender: function ( data, type, full ) {
			        return "<a href=\""+full.clsHref+"\">" + data + "</a>";
			   }
		   },
		   {
			   sTitle: "<fmt:message key="label.uri"/>",
			   sClass: "resource-uri",
			   mData: "uri",
			   mRender: function ( data, type, full ) {
			        return "<a href=\""+full.href+"\">" + data + "</a>";
			   }
		   }
		]
	});
});
</script>
</div>
<br/>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
</body>
</html>
</fmt:bundle>