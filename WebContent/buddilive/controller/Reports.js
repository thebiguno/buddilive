Ext.define("BuddiLive.controller.Reports", {
	"extend": "Ext.app.Controller",
	
	"requires": [
		"BuddiLive.view.report.PieIncomeByCategory",
		"BuddiLive.view.report.PieExpensesByCategory"
	],

	"init": function() {
		this.control({
			"buddiviewport menuitem[itemId='showIncomeByCategoryPie']": {"click": this.showIncomeByCategoryPie},
			"buddiviewport menuitem[itemId='showExpensesByCategoryPie']": {"click": this.showExpensesByCategoryPie},
			"buddiviewport menuitem[itemId='showIncomeAndExpensesByCategoryTable']": {"click": this.showIncomeAndExpensesByCategoryTable}
		});
	},
	
	"showIncomeByCategoryPie": function(component){
		var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
		var report = Ext.widget({
			"xtype": "reportpieincomebycategory"
		});

		tabPanel.add(report);
		tabPanel.setActiveTab(report);
	},
	"showExpensesByCategoryPie": function(component){
		var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
		var report = Ext.widget({
			"xtype": "reportpieexpensesbycategory"
		});

		tabPanel.add(report);
		tabPanel.setActiveTab(report);
	},
	
	"showIncomeAndExpensesByCategoryTable": function(component){
		var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
		var report = Ext.widget({
			"xtype": "reportincomeandexpensesbycategory"
		});

		tabPanel.add(report);
		tabPanel.setActiveTab(report);
	}
});