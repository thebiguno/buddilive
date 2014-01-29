Ext.define("BuddiLive.store.report.NetWorthOverTimeStore", {
	"extend": "Ext.data.Store",
	"requires": [
	],
	"autoLoad": true,
	"constructor": function(config){
		this.fields = ["date", "netWorth"];
		this.proxy = {
			"type": "ajax",
			"url": "data/report/balancesovertime.json?netWorthOnly=true&" + config.query,
			"reader": {
				"type": "json",
				"root": "data"
			}
		}
		
		this.callParent(arguments);
	}
});