Ext.define("BuddiLive.controller.Reports", {
	"extend": "Ext.app.Controller",
	
	"requires": [
		"BuddiLive.view.report.picker.Interval",
		"BuddiLive.view.report.PieTotalsByCategory"
	],

	"init": function() {
		this.control({
			"buddiviewport menuitem[itemId='showIncomeByCategoryPie']": {"click": this.showIncomeByCategoryPie},
			"buddiviewport menuitem[itemId='showExpensesByCategoryPie']": {"click": this.showExpensesByCategoryPie},
			"buddiviewport menuitem[itemId='showIncomeAndExpensesByCategoryTable']": {"click": this.showIncomeAndExpensesByCategoryTable}
		});
	},
	
	"showIncomeByCategoryPie": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(interval){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportpietotalsbycategory",
					"interval": interval,
					"type": "I"
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	},
	"showExpensesByCategoryPie": function(component){
		var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
		var report = Ext.widget({
			"xtype": "reportpietotalsbycategory"
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