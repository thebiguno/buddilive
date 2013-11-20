Ext.define("BuddiLive.store.report.PieTotalsByCategoryStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.report.PieModel"
	],
	"model": "BuddiLive.model.report.PieModel",
	"autoLoad": true,
	"constructor": function(config){
		this.proxy = {
			"type": "ajax",
			"url": "data/report/pietotalsbycategory.json?type=" + config.type + "&" + config.query,
			"reader": {
				"type": "json",
				"root": "data"
			}
		}
		
		this.callParent(arguments);
	}
});