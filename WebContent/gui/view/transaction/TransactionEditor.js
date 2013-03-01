Ext.define('BuddiLive.view.transaction.TransactionEditor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.budditransactioneditor",
	
	"requires": [
		"BuddiLive.view.transaction.SplitEditor"
	],
	
	"initComponent": function(){
		this.layout = "hbox";
		this.store = Ext.create("BuddiLive.store.Transactions");
		this.defaults = {
			"padding": "5 0 5 5"
		};
		this.items = [
			{
				"xtype": "datefield",
				"itemId": "date",
				"width": 100,
				"value": new Date()
			},
			{
				"xtype": "combobox",
				"itemId": "description",
				"width": 200,
				"store": Ext.create("BuddiLive.store.TransactionDescriptions")
			},
			{
				"xtype": "textfield",
				"itemId": "number",
				"width": 100
			},
			{
				"xtype": "panel",
				"flex": 1,
				"layout": "vbox",
				"border": false,
				"items": [
					{"xtype": "buddispliteditor"}
				]
			}
		];
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "bottom",
			"items": [
				{
					"text": "Add Account",
					"icon": "img/table--plus.png",
					"itemId": "addAccount"
				},
				{
					"text": "Edit Account",
					"icon": "img/table--pencil.png",
					"itemId": "editAccount",
					"disabled": true
				},
				{
					"text": "Delete Account",
					"icon": "img/table--pencil.png",
					"itemId": "deleteAccount",
					"disabled": true
				}
			]
		}];
		
		this.callParent(arguments);
	}
});