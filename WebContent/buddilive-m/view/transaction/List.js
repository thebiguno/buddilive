Ext.define("BuddiLive.view.transaction.List", {
	"extend": "Ext.dataview.List",
	"alias": "widget.transactionlist",
	"requires": [
		"BuddiLive.store.transaction.List"
	],
	
	"config": {
		"fullscreen": true,
		"store": Ext.create("BuddiLive.store.transaction.List"),
		"itemTpl": "<div>{description}</div>"
	}
});
