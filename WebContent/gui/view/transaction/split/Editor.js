Ext.define('BuddiLive.view.transaction.split.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.spliteditor",
	
	"requires": [
		"BuddiLive.store.transaction.split.FromStore",
		"BuddiLive.store.transaction.split.ToStore"
	],
	
	"initComponent": function(){
		this.layout = "hbox";
		this.border = false;
		this.width = "100%";
		this.defaults = {
			"padding": "0 0 5 5"
		};
		this.items = [
			{
				"xtype": "numberfield",
				"itemId": "amount",
				"flex": 1,
				"hideTrigger": true,
				"keyNavEnabled": false,
				"mouseWheelEnabled": false,
				"emptyText": "0.00 Amount"
			},
			{
				"xtype": "combobox",
				"itemId": "from",
				"flex": 1,
				"emptyText": "From",
				"store": Ext.create("BuddiLive.store.transaction.split.FromStore")
			},
			{"xtype": "panel", "html": "<img src='img/folder-open-table.png'/>", "border": false, "padding": "3 5 0 0"},
			{
				"xtype": "combobox",
				"itemId": "to",
				"flex": 1,
				"emptyText": "To",
				"store": Ext.create("BuddiLive.store.transaction.split.ToStore")
			},
			{
				"xtype": "textfield",
				"itemId": "memo",
				"flex": 2,
				"emptyText": "Memo",
				"padding": "0 5 5 5"
			}
		];
		
		this.callParent(arguments);
	}
});