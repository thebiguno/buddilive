Ext.define("BuddiLive.controller.Reports", {
	"extend": "Ext.app.Controller",
	
	"requires": [
		"BuddiLive.view.report.picker.Interval",
		"BuddiLive.view.report.PieTotalsByCategory",
		"BuddiLive.view.report.IncomeAndExpensesByCategory"
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
			"callback": function(query){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportpietotalsbycategory",
					"query": query,
					"type": "I"
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	},
	"showExpensesByCategoryPie": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(query){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportpietotalsbycategory",
					"query": query,
					"type": "E"
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	},
	
	"showIncomeAndExpensesByCategoryTable": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(query){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportincomeandexpensesbycategory",
					"query": query,
					"type": "E"
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	}
});