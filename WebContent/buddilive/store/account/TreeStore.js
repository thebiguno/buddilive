Ext.define("BuddiLive.store.account.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [],
	"fields": [
		"id", 
		"name", 
		"type", 
		"accountType",
		"startBalance",
		"deleted", 
		"balance", 
		"nodeType"
	],
	"proxy": {
		"type": "ajax",
		"url": "data/accounts.json",
		"reader": {
			"type": "json",
			"rootProperty": "children"
		}
	}
});