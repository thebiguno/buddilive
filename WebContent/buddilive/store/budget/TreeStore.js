Ext.define("BuddiLive.store.budget.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [
		"BuddiLive.model.budget.TreeModel"
	],
	"constructor": function(config){
		this.model = "BuddiLive.model.budget.TreeModel";
		this.proxy = {
			"type": "ajax",
			"url": "buddilive/categories.json?periodType=" + config.periodType,
			"reader": {
				"type": "json",
				"root": "children"
			}
		};
		this.autoload = true;
		this.callParent(config);
	},
});