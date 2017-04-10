Ext.define("BuddiLive.store.report.AccountBalancesOverTimeStore", {
	"extend": "Ext.data.Store",
	"requires": [
	],
	"autoLoad": true,
	"constructor": function(config){
		this.proxy = {
			"type": "ajax",
			"url": "data/report/balancesovertime.json?" + config.query,
			"reader": {
				"type": "json",
				"rootProperty": "data"
			}
		}
		
		this.callParent(arguments);
	}
});