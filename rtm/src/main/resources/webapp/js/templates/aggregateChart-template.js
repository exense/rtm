<div class="panel panel-primary" id="chartPanel">
<div class="panel-heading">
	      <h4 class="panel-title">
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseAChart">
          Chart
        </a>
      </h4>
</div>
<div id="collapseAChart" class="panel-collapse collapse in">
<div class="panel-body">
<div class="dropdown">
<label> Metric : </label>
  <a id="dLabel" class="btn btn-default metricChooser" role="button" data-toggle="dropdown" data-target="#" href="">
    <%=currentChartMetricChoice%> <span class="caret"></span>
  </a>
  <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
  <% var length = metricsList.length %>
  <% for (i = 0; i < length; i++) { %>
	  <li role="presentation"><a role="menuitem" tabindex="-1" href="#" class="<%=metricsList[i]%> metricChoice" id="<%=metricsList[i]%>"><%=metricsList[i]%></a></li>
	<% } %>
  </ul>

<% var pWidth = $(".panel").width() -20 %>
<% var pChartHeight = pWidth * 0.5 %>
<!-- //TODO:  compute dynamically based on the number of series -->
<% var pLegendHeight = 50 %>

</div> <!-- /dropdown-menu -->
  <svg id="chartSVG" width="<%=pWidth%>" height="<%=pChartHeight%>"></svg>
	<svg id="legendSVG" width="<%=pWidth%>" height="<%=pLegendHeight%>"></svg>
</div> <!-- /panel-body -->
</div> <!-- /panel-collapse -->
</div> <!-- /panel -->

<div>&nbsp</div>
