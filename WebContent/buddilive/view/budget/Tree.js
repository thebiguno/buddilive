Ext.define('BuddiLive.view.budget.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.budgettree",
	"requires": [
		"BuddiLive.store.budget.TreeStore"
	],
	
	"rootVisible": false,
	"border": false,
	"stateful": true,
	"flex": 1,
	"width": "100%",
	"viewConfig": {
		"stripeRows": true
	},
	"plugins": [{
		"ptype": "cellediting",
		"clicksToEdit": 1
	}],
	
	"dockedItems": [{
		"xtype": "toolbar",
		"dock": "top",
		"items": [
			{
				"xtype": "button",
				"tooltip": "${translation("COPY_FROM_PREVIOUS_BUDGET_PERIOD_TOOLTIP")?json_string}",
				"text": "${translation("COPY_FROM_PREVIOUS_BUDGET_PERIOD")?json_string}",
				"icon": "img/calendar-import.png",
				"itemId": "copyFromPreviousPeriod"
			},
			"->",
			{
				"xtype": "label",
				"text": "${translation("CURRENT_BUDGET_PERIOD")?json_string}"
			},
			" ",
			{
				"xtype": "button",
				"tooltip": "${translation("PREVIOUS_BUDGET_PERIOD")?json_string}",
				"icon": "img/calendar-previous.png",
				"itemId": "previousPeriod"
			},
			{
				"xtype": "textfield",
				"width": 200,
				"itemId": "currentPeriod",
				"disabled": true,
				"disabledCls": "",
				"style": "color: black"
			},
			{
				"xtype": "button",
				"tooltip": "${translation("NEXT_BUDGET_PERIOD")?json_string}",
				"icon": "img/calendar-next.png",
				"itemId": "nextPeriod"
			}
		]
	}],
	
	"initComponent": function(){
		var budgetTree = this;
		this.itemId = this.initialConfig.periodValue;
		this.stateId = "budgettree" + this.initialConfig.periodValue;
		this.store = Ext.create("BuddiLive.store.budget.TreeStore", {"periodType": this.initialConfig.periodValue});
		this.title = this.initialConfig.periodText;
		
		var styledRenderer = function(value, metaData, record){
			metaData.style = record.data[metaData.column.dataIndex + "Style"];
			return value;
		};
		
		this.columns = [
			{
				"text": "${translation("NAME")?json_string}",
				"dataIndex": "name",
				"flex": 2,
				"xtype": "treecolumn",
				"renderer": function(value, metaData, record){
					metaData.style = record.data.nameStyle;
					return value;
				}
			},
			{
				"text": "${translation("PREVIOUS")?json_string}",
				"dataIndex": "previous",
				"flex": 1,
				"align": "right",
				"renderer": styledRenderer
			},
			{
				"text": "${translation("CURRENT")?json_string}",
				"dataIndex": "current",
				"flex": 1,
				"align": "right",
				"editor": {
					"xtype": "currencyfield",
					"fieldStyle": "text-align: right;",
					"listeners": {
						"focus": function(component){
							component.selectText();
						}
					}
				},
				"renderer": function(value, metaData, record){
					metaData.style = record.data.currentStyle;
					if (record.data.currentAmount == 0) {
						return "${translation("CLICK_TO_ENTER_BUDGETED_AMOUNT")}";
					}
					return value;
				}
			},
			{
				"text": "${translation("ACTUAL_INCOME_EXPENSES")?json_string}",
				"dataIndex": "actual",
				"flex": 1,
				"align": "right",
				"renderer": styledRenderer
			},
			{
				"text": "${translation("AMOUNT_REMAINING")?json_string}",
				"dataIndex": "difference",
				"flex": 1,
				"align": "right",
				"renderer": styledRenderer
			}
		];

		this.callParent(arguments);
	}
});