Ext.define('BuddiLive.view.budget.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.budgettree",
	"requires": [
		"BuddiLive.store.budget.TreeStore"
	],
	
	"initComponent": function(){
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
				"dataIndex": "previous",
				"flex": 1
			},
			{
				"text": "Current",
				"dataIndex": "current",
				"flex": 1,
				"editor": {
					"xtype": "numberfield",
					"hideTrigger": true,
					"keyNavEnabled": false,
					"mouseWheelEnabled": false,
					"emptyText": "0.00"
				}
			},
			{
				"text": "Actual",
				"dataIndex": "actual",
				"flex": 1
			},
			{
				"text": "Difference",
				"dataIndex": "difference",
				"flex": 1
			}
		];
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "top",
			"items": [
				"->",
				{
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
					"style": "color: black",
					"value": "TODO 2012-01-01 to 2012-02-01"
				},
				{
					"tooltip": "Next Period",
					"icon": "img/calendar-next.png",
					"itemId": "nextPeriod"
				}
			]
		}];

		this.callParent(arguments);
	}
});