Ext.define("BuddiLive.controller.Reports", {
	"extend": "Ext.app.Controller",
	
	"requires": [
		"BuddiLive.view.report.picker.Interval",
		"BuddiLive.view.report.PieTotalsByCategory",
		"BuddiLive.view.report.IncomeAndExpensesByCategory",
		"BuddiLive.view.report.AverageIncomeAndExpensesByCategory",
		"BuddiLive.view.report.InflowAndOutflowByAccount",
		"BuddiLive.view.report.InflowAndOutflowByPayee",
		"BuddiLive.view.report.AccountBalancesOverTime",
		"BuddiLive.view.report.NetWorthOverTime"
	],

	"init": function() {
		this.control({
			"buddiviewport menuitem[itemId='showIncomeByCategoryPie']": {"click": this.showIncomeByCategoryPie},
			"buddiviewport menuitem[itemId='showExpensesByCategoryPie']": {"click": this.showExpensesByCategoryPie},
			"buddiviewport menuitem[itemId='showIncomeAndExpensesByCategoryTable']": {"click": this.showIncomeAndExpensesByCategoryTable},
			"buddiviewport menuitem[itemId='showAverageIncomeAndExpensesByCategoryTable']": {"click": this.showAverageIncomeAndExpensesByCategoryTable},
			"buddiviewport menuitem[itemId='showInflowAndOutflowByAccountTable']": {"click": this.showInflowAndOutflowByAccountTable},
			"buddiviewport menuitem[itemId='showInflowAndOutflowByPayeeTable']": {"click": this.showInflowAndOutflowByPayeeTable},
			"buddiviewport menuitem[itemId='showAccountBalancesOverTimeLine']": {"click": this.showAccountBalancesOverTimeLine},
			"buddiviewport menuitem[itemId='showNetWorthOverTimeLine']": {"click": this.showNetWorthOverTimeLine}
		});
	},
	
	"showIncomeByCategoryPie": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportpietotalsbycategory",
					"options": options,
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
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportpietotalsbycategory",
					"options": options,
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
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportincomeandexpensesbycategory",
					"options": options
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	},
	
	"showAverageIncomeAndExpensesByCategoryTable": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportaverageincomeandexpensesbycategory",
					"options": options
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	},
	
	"showInflowAndOutflowByAccountTable": function(component){
		<#if !((user.premium)!false)>
		Ext.MessageBox.show({
			"title": "${translation("PREMIUM_TITLE")?json_string}",
			"msg": "${translation("PREMIUM_MESSAGE")?json_string}",
			"buttons": Ext.Msg.OK
		});
		<#else>
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportinflowandoutflowbyaccount",
					"options": options
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
		</#if>
	},
	
	"showInflowAndOutflowByPayeeTable": function(component){
		<#if !((user.premium)!false)>
		Ext.MessageBox.show({
			"title": "${translation("PREMIUM_TITLE")?json_string}",
			"msg": "${translation("PREMIUM_MESSAGE")?json_string}",
			"buttons": Ext.Msg.OK
		});
		<#else>
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportinflowandoutflowbypayee",
					"options": options
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
		</#if>
	},
	
	"showAccountBalancesOverTimeLine": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportaccountbalancesovertime",
					"options": options
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	},
	
	"showNetWorthOverTimeLine": function(component){
		Ext.widget({
			"xtype": "reportpickerinterval",
			"callback": function(options){
				var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
				var report = Ext.widget({
					"xtype": "reportnetworthovertime",
					"options": options
				});
				tabPanel.add(report);
				tabPanel.setActiveTab(report);
			}
		}).show();
	}
});