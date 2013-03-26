Ext.define("BuddiLive.view.scheduled.List", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledlist",
	"requires": [
		"BuddiLive.store.scheduled.ListStore",
		"BuddiLive.view.scheduled.Editor"
	],
	
	"initComponent": function(){
		var d = this.initialConfig.data

		this.title = "${translation("SCHEDULED_TRANSACTIONS")?json_string}";
		this.layout = "fit";
		this.store = Ext.create("BuddiLive.store.scheduled.ListStore");
		this.closable = true;
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
		this.dockedItems = BuddiLive.app.viewport.getDockedItems("scheduled")
	
		this.callParent(arguments);
	}
});