<div class="panel panel-primary">
<div class="panel-heading">
<h4 class="panel-title">
<a data-toggle="collapse" data-parent="#accordion" href="#collapseASP">Service Params</a>
</h4>
</div>

<div id="collapseASP" class="panel-collapse collapse in">
<div class="panel-body">
<form class="form-inline">
<div class="form-group">
<div class="form-group row">

<div class="col-md-2">
<label class="control-label">bucket size (ms)</label> &nbsp
<input type="text" class="form-control granularity aggserviceparams" id="granularity" value="<%= content.granularity %>">
</div>

<div class="col-md-2">
<label class="control-label">groupby</label> &nbsp
<input type="text" class="form-control groupby aggserviceparams" id="groupby" value="<%= content.groupby %>">
</div>

<div class="col-md-2">
<label class="control-label">cpu #</label> &nbsp
<input type="text" class="form-control cpu aggserviceparams" id="cpu" value="<%= content.cpu %>">
</div>

<div class="col-md-2">
<label class="control-label">partition #</label> &nbsp
<input type="text" class="form-control partition aggserviceparams" id="partition" value="<%= content.partition %>">
</div>


</div>

</div>

</form>
</div> <!-- /panel-body -->
</div> <!-- /panel-collapse -->
</div> <!-- /panel -->

<div>&nbsp</div>