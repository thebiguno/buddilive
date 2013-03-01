Ext.define('BuddiLive.view.transaction.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.transactioneditor",
	
	"requires": [
		"BuddiLive.view.transaction.split.Editor",
		"BuddiLive.store.transaction.DescriptionStore"
	],
	
	"initComponent": function(){
		this.layout = "vbox";
		this.items = [
			{
				"xtype": "panel",
				"width": "100%",
				"layout": "hbox",
				"border": false,
				"defaults": {
					"padding": "5 0 5 5"
				},
				"items": [
					{
						"xtype": "datefield",
						"itemId": "date",
						"flex": 1,
						"emptyText": "Date",
						"value": new Date()
					},
					{
						"xtype": "combobox",
						"itemId": "description",
						"flex": 2,
						"emptyText": "Description",
						"store": Ext.create("BuddiLive.store.transaction.DescriptionStore")
					},
					{
						"xtype": "textfield",
						"itemId": "number",
						"flex": 1,
						"emptyText": "Number",
						"padding": "5"
					}
				]
			},
			{"xtype": "spliteditor"}
		];
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "bottom",
			"items": [
				{
					"tooltip": "Delete Transaction",
					"icon": "img/table-delete-row.png",
					"itemId": "deleteTransaction"
				},
				"->",
				{
					"tooltip": "Clear Transaction",
					"icon": "img/table-join-row.png",
					"itemId": "clearTransaction"
				},
				{
					"text": "Record Transaction",
					"icon": "img/table-insert-row.png",
					"itemId": "recordTransaction"
				}
			]
		}];
		
		this.callParent(arguments);
	}
});