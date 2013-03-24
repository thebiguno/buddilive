Ext.define('BuddiLive.view.transaction.scheduled.List', {
	"extend": "Ext.window.Window",
	"alias": "widget.scheduledtransactionlist",
	"requires": [
		"BuddiLive.store.transaction.scheduled.ListStore"
	],
	
	"initComponent": function(){
		var d = this.initialConfig.data

		this.title = "${translation("SCHEDULED_TRANSACTIONS")?json_string}";
		this.layout = "fit";
		this.store = Ext.create("BuddiLive.store.transaction.scheduled.ListStore");
		this.modal = true;
		this.width = 400;
		this.height = 300;
		this.items = [
			{
				"xtype": "grid",
				"itemId": "scheduledTransactions",
				"columns": [
					{
					"text": "Name",	//TODO i18n
						"dataIndex": "name",
						"flex": 1
					}
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("NEW")?json_string}",
				"itemId": "new"
			},
			{
				"text": "${translation("EDIT")?json_string}",
				"itemId": "edit"
			},
			{
				"text": "${translation("DELETE")?json_string}",
				"itemId": "delete"
			},
			"->",
			{
				"text": "${translation("DONE")?json_string}",
				"itemId": "done"
			}
		]
	
		this.callParent(arguments);
	}
});