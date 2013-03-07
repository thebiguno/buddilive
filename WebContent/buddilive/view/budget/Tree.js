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
		this.store = Ext.create("BuddiLive.store.budget.TreeStore", {"periodType": this.initialConfig.period});
		this.title = this.initialConfig.period;
		this.flex = 1;
		this.width = "100%";
		this.plugins = [
			Ext.create("Ext.grid.plugin.CellEditing", {
				"clicksToEdit": 1
			})
		];
		
		var numberCellRenderer = function(value, metaData, record){
			if (value == null || value == 0 || isNaN(value)){
				return "-";
			}
			return value.toFixed(2);
		};
		
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "name",
				"flex": 2,
				"xtype": "treecolumn",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.style;
					return value;
				}
			},
			{
				"text": "Previous",
				"dataIndex": "previousAmount",
				"flex": 1,
				"align": "right",
				"renderer": numberCellRenderer
			},
			{
				"text": "Current",
				"dataIndex": "currentAmount",
				"flex": 1,
				"align": "right",
				"editor": {
					"xtype": "numberfield",
					"hideTrigger": true,
					"keyNavEnabled": false,
					"mouseWheelEnabled": false,
					"emptyText": "0.00",
					"fieldStyle": "text-align: right;"
				},
				"renderer": numberCellRenderer
			},
			{
				"text": "Actual",
				"dataIndex": "actual",
				"flex": 1,
				"align": "right",
				"renderer": numberCellRenderer
			},
			{
				"text": "Difference",
				"dataIndex": "difference",
				"flex": 1,
				"align": "right",
				"renderer": numberCellRenderer
			}
		];
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "top",
			"items": [
				"->",
				{
					"xtype": "button",
					"tooltip": "Previous Period",
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
					"tooltip": "Next Period",
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