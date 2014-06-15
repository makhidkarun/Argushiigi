<%@ include file="/WEB-INF/prelude.jsp" %>
<fmt:bundle basename="org.charvolant.argushiigi.server.Messages">
<fmt:message var="title" key="jsp.index.title"/>
<html>
<head>
<%@ include file="/WEB-INF/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/header.jsp" %>
<div id="content">
<div id="about">
<fmt:message key="jsp.index.about"/>
</div>
<div id="worlds" class="resource-table">
<h2><fmt:message key="label.worlds"/></h2>
<table id="worlds-table"></table>
<script type="text/javascript">
$("#worlds-table").ready(function() {
	$("#worlds-table").dataTable( {
		bProcessing: true,
		sAjaxSource: "<c:url value="/data/query/type"><c:param name="type" value="http://data.travellerrpg.com/ontology/t5#World"/></c:url>",
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
			        return data;
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
<div id="characters" class="resource-table">
<h2><fmt:message key="label.characters"/></h2>
<table id="characters-table"></table>
<script type="text/javascript">
$("#characters-table").ready(function() {
	$("#characters-table").dataTable( {
		bProcessing: true,
		sAjaxSource: "<c:url value="/data/query/type"><c:param name="type" value="http://data.travellerrpg.com/ontology/rpg#Character"/></c:url>",
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
			        return data;
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