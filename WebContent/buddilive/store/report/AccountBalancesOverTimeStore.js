Ext.define("BuddiLive.store.report.AccountBalancesOverTimeStore", {
	"extend": "Ext.data.Store",
	"requires": [
	],
	"autoLoad": true,
	"constructor": function(config){
		this.fields = ["date", "a20", "a6"];
		this.proxy = {
			"type": "ajax",
			"url": "data/report/accountbalancesovertime.json?" + config.query,
			"reader": {
				"type": "json",
				"root": "data"
			}
		}
		
		this.callParent(arguments);
	}
});