Ext.define('BuddiLive.view.budget.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.budgettree",
	"requires": [
		"BuddiLive.store.budget.TreeStore"
	],
	
	"initComponent": function(){
		this.rootVisible = false;
		this.border = false;
		this.store = Ext.create("BuddiLive.store.budget.TreeStore");
		this.title = this.initialConfig.period;
		this.flex = 1;
		this.width = "100%";
		
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "name",
				"flex": 2,
				"xtype": "treecolumn",
				"renderer": function(value, metaData, record){
					if (!record.raw.debit) metaData.style += " color: #D10000;";
					if (record.raw.deleted == true) metaData.style += " text-decoration: line-through;";
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
				"flex": 1
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