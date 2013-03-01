Ext.define('BuddiLive.view.TabPanel', {
	"extend": "Ext.tab.Panel",
	"alias": "widget.budditabpanel",
	
	"requires": [
		"BuddiLive.view.Accounts",
		"BuddiLive.view.Budget",
		"BuddiLive.view.Reports",
		"BuddiLive.view.Transactions",
		"BuddiLive.view.transaction.TransactionEditor"
	],
	
	"initComponent": function(){
		this.layout = "fit";
		this.items = [
			{
				"xtype": "panel",
				"title": "My Accounts",
				"layout": "border",
				"items": [
					{
						"xtype": "buddiaccounts",
						"region": "west"
					},
					{
						"xtype": "budditransactions",
						"region": "center"
					}
				]
			},
			{"xtype": "buddibudget"},
			{"xtype": "buddireports"}
		];
	
		this.callParent(arguments);
	}
});