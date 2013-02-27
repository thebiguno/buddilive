Ext.define('BuddiLive.view.TabPanel', {
	"extend": "Ext.tab.Panel",
	"alias": "widget.budditabpanel",
	
	"requires": [
		"BuddiLive.view.Accounts",
		"BuddiLive.view.Budget",
		"BuddiLive.view.Reports"
	],
	
	"initComponent": function(){
		this.layout = "fit";
		this.items = [
			{"xtype": "buddiaccounts"},
			{"xtype": "buddibudget"},
			{"xtype": "buddireports"}
		];
	
		this.callParent(arguments);
	}
});