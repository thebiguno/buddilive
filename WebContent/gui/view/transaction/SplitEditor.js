Ext.define('BuddiLive.view.transaction.SplitEditor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.buddispliteditor",
	
	"requires": [
		"BuddiLive.store.SourcesFrom",
		"BuddiLive.store.SourcesTo"
	],
	
	"initComponent": function(){
		this.layout = "hbox";
		this.store = Ext.create("BuddiLive.store.Transactions");
		this.border = false;
		this.defaults = {
			"padding": "0 5 0 0"
		};
		this.items = [
			{
				"xtype": "numberfield",
				"itemId": "amount",
				"width": 100,
				"hideTrigger": true,
				"keyNavEnabled": false,
				"mouseWheelEnabled": false,
				"value": 0
			},
			{
				"xtype": "combobox",
				"itemId": "from",
				"width": 150,
				"store": Ext.create("BuddiLive.store.SourcesFrom")
			},
			{"xtype": "panel", "html": "<img src='img/folder-open-table.png'/>", "border": false, "padding": "3 5 0 0"},
			{
				"xtype": "combobox",
				"itemId": "to",
				"width": 150,
				"store": Ext.create("BuddiLive.store.SourcesTo")
			},
			{
				"xtype": "textfield",
				"itemId": "memo",
				"flex": 1
			}
		];
		
		this.callParent(arguments);
	}
});