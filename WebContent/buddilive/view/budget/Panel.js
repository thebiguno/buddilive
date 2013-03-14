Ext.define('BuddiLive.view.budget.Panel', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.budgetpanel",
	
	"requires": [
		"BuddiLive.view.budget.Editor"
	],
	
	"initComponent": function(){
		this.layout = "accordion";
		this.stateId = "budgetpanel";
		this.stateful = true;
		this.defaults = {
			"stateEvents": ["collapse","expand"],
			"stateful": true,
			"getState": function() {
				return { "collapsed": this.collapsed };
			}
		};
		this.items = [
			{
				"xtype": "budgettree",
				"periodText": "${translation("BUDGET_CATEGORY_TYPE_WEEK")}",
				"periodValue": "WEEK",
				"stateId": "budgettreeweek"
			},
			{
				"xtype": "budgettree",
				"periodText": "${translation("BUDGET_CATEGORY_TYPE_SEMI_MONTH")}",
				"periodValue": "SEMI_MONTH",
				"stateId": "budgettreesemimonth"
			},
			{
				"xtype": "budgettree",
				"periodText": "${translation("BUDGET_CATEGORY_TYPE_MONTH")}",
				"periodValue": "MONTH",
				"stateId": "budgettreemonth"
			},
			{
				"xtype": "budgettree",
				"periodText": "${translation("BUDGET_CATEGORY_TYPE_QUARTER")}",
				"periodValue": "QUARTER",
				"stateId": "budgettreequarter"
			},
			{
				"xtype": "budgettree",
				"periodText": "${translation("BUDGET_CATEGORY_TYPE_SEMI_YEAR")}",
				"periodValue": "SEMI_YEAR",
				"stateId": "budgettreesemiyear"
			},
			{
				"xtype": "budgettree",
				"periodText": "${translation("BUDGET_CATEGORY_TYPE_YEAR")}",
				"periodValue": "YEAR",
				"stateId": "budgettreeyear"
			}
		];
		this.callParent(arguments);
	},
	
	"reload": function(){
		//Reload all children stores
		for (var i = 0; i < this.items.length; i++){
			this.items.getAt(i).getStore().reload();
		}
		
		//Reset selection by firing a selection change on any of the budget trees
		this.items.getAt(0).fireEvent("selectionchange", this.items.getAt(0).getSelectionModel(), null);
	}
});