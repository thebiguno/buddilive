Ext.define('BuddiLive.view.budget.Panel', {
	"extend": "Ext.tab.Panel",
	"alias": "widget.budgetpanel",
	
	"requires": [
		"BuddiLive.view.budget.Editor"
	],
	
	"initComponent": function(){
		this.layout = "fill";
		this.stateId = "budgetpanel";
		this.stateful = true;
		this.callParent(arguments);
	}
});