Ext.define("BuddiLive.view.scheduled.List", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledlist",
	"requires": [
		"BuddiLive.store.scheduled.ListStore",
		"BuddiLive.view.scheduled.Editor"
	],
	
	"title": "${translation("SCHEDULED_TRANSACTIONS")?json_string}",
	"layout": "fit",
	"closable": true,
	"initComponent": function(){
		var d = this.initialConfig.data

		this.items = [
			{
				"xtype": "grid",
				"itemId": "scheduledTransactions",
				"store": Ext.create("BuddiLive.store.scheduled.ListStore"),
				"columns": [
					{
						"text": "${translation("SCHEDULED_TRANSACTION_NAME")?json_string}",
						"dataIndex": "name",
						"flex": 1
					}
				]
			}
		];
		this.dockedItems = BuddiLive.app.viewport.getDockedItems("scheduled")
	
		this.callParent(arguments);
	}
});