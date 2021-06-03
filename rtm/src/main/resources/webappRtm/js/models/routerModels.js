var Router = Backbone.Router
	.extend({
		routes : {
			"" : "Measurement",
			"Measurement" : "Measurement",
			"Measurement/select/:guiState" : "Measurement/selected",
			"Measurement/select/:guiState/:id" : "Measurement/selected",

			"Aggregate" : "Aggregate",
			"Aggregate/select/:guiState" : "Aggregate/selected",
			"Aggregate/select/:guiState/:id" : "Aggregate/selected",

			"Compare" : "Compare",
			"Compare/select/:guiState" : "Compare/selected",
			"Compare/select/:guiState/:id" : "Compare/selected"
		}
	});
