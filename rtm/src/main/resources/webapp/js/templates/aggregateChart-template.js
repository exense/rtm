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
  <a id="dLabel" class="btn btn-default metricChooser" role="button" data-toggle="dropdown" data-target="#" href="">
    metric <span class="caret"></span>
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
<% var pLegendHeight = (20 * (Math.abs(nbSeries / factor))) + 20 %>

</div> <!-- /dropdown-menu -->
<div id="svgContainer" style="height: 0; display: inline-block; position: relative; width: 100%; height: 100%; padding-bottom: 5%">
  <!-- //<svg id="chartSVG" width="<%=pWidth%>" height="<%=pChartHeight%>"></svg> -->
	<svg id="chartSVG" viewBox="0 0 <%=pWidth%> <%=pChartHeight%>" style="display: inline-block; position: relative; top: 10px; left: 0;"></svg>
</div> <!-- /svgContainer -->
<div id="legendSVG" width="<%=pWidth%>" height="<%=pLegendHeight%>"></div>
<div class="divToolTip" style="display: inline-block;" />
</div> <!-- /panel-body -->
</div> <!-- /panel-collapse -->
</div> <!-- /panel -->

<div>&nbsp</div>
