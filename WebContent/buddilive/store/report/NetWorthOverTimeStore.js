Ext.define("BuddiLive.store.report.NetWorthOverTimeStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.report.NetWorthOverTimeModel"
	],
	"model": "BuddiLive.model.report.NetWorthOverTimeModel",
	"autoLoad": true,
	"constructor": function(config){
		this.proxy = {
			"type": "ajax",
			"url": "data/report/networthovertime.json?" + config.query,
			"reader": {
				"type": "json",
				"root": "data"
			}
		}
		
		this.callParent(arguments);
	}
});