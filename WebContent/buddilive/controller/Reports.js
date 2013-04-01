Ext.define("BuddiLive.controller.Reports", {
	"extend": "Ext.app.Controller",
	
	"requires": [
		"BuddiLive.view.report.IncomeAndExpensesByCategory",
	],

	"init": function() {
		this.control({
			"buddiviewport menuitem[itemId='showIncomeAndExpensesByCategory']": {"click": this.showIncomeAndExpensesByCategory}
		});
	},
	
	"showIncomeAndExpensesByCategory": function(component){
		var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
		var report = Ext.widget({
			"xtype": "reportincomeandexpensesbycategory"
		});

		tabPanel.add(report);
		tabPanel.setActiveTab(report);
	}
});