Ext.define('BuddiLive.view.budget.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.budgettree",
	"requires": [
		"BuddiLive.store.budget.TreeStore"
	],
	
	"initComponent": function(){
		var budgetTree = this;
		this.rootVisible = false;
		this.border = false;
		this.itemId = this.initialConfig.periodValue;
		this.store = Ext.create("BuddiLive.store.budget.TreeStore", {"periodType": this.initialConfig.periodValue});
		this.title = this.initialConfig.periodText;
		this.flex = 1;
		this.width = "100%";
		this.plugins = [
			Ext.create("Ext.grid.plugin.CellEditing", {
				"clicksToEdit": 1
			})
		];
		
		var numberCellRenderer = function(value, metaData, record){
			if (value == null || value.length == 0 || value == "0.00") return "-";
			else return value;
		};
		
		this.columns = [
			{
				"text": "Name",	//TODO i18n
				"dataIndex": "name",
				"flex": 2,
				"xtype": "treecolumn",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.nameStyle;
					return value;
				}
			},
			{
				"text": "Previous",	//TODO i18n
				"dataIndex": "previousAmount",
				"flex": 1,
				"align": "right",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.previousAmountStyle;
					return value;
				}
			},
			{
				"text": "Current",	//TODO i18n
				"dataIndex": "currentAmount",
				"flex": 1,
				"align": "right",
				"editor": {
					"xtype": "textfield",
					"emptyText": "0.00",
					
					"fieldStyle": "text-align: right;"
				},
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.currentAmountStyle;
					if (value == null || value == "" || value == "0.00") return "-"; 
					return value;
				}
			},
			{
				"text": "Actual Income / Expenses",	//TODO i18n
				"dataIndex": "actual",
				"flex": 1,
				"align": "right",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.actualStyle;
					return value;
				}
			},
			{
				"text": "Amount Remaining",	//TODO i18n
				"dataIndex": "difference",
				"flex": 1,
				"align": "right",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.differenceStyle;
					return value;
				}
			}
		];
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "top",
			"items": [
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
		}];

		this.callParent(arguments);
		
		//Load some of the contents of the loaded data packet into the GUI, and persist state for future requests
		this.getStore().addListener("load", function(store, records){
			budgetTree.down("textfield[itemId='currentPeriod']").setValue(store.proxy.reader.rawData.period);
			budgetTree.currentDate = store.proxy.reader.rawData.date;	//ISO Date string, will be used as current reference when passing nextPeriod / previousPeriod
		});
	}
});