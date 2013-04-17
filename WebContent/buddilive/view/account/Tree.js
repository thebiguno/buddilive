Ext.define('BuddiLive.view.account.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.accounttree",
	"requires": [
		"BuddiLive.store.account.TreeStore",
		"BuddiLive.view.account.Editor"
	],
	
	"initComponent": function(){
		this.rootVisible = false;
		this.border = false;
		this.store = Ext.create("BuddiLive.store.account.TreeStore");
		this.viewConfig = {
			"stripeRows": true
		};
		
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "name",
				"flex": 3,
				"xtype": "treecolumn",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.style;
					return value;
				}
			},
			{
				"text": "Balance",
				"dataIndex": "balance",
				"flex": 1,
				"align": "right",
				"renderer": function(value, metaData, record){
					metaData.style = record.raw.balanceStyle 
					return value;
				}
			}
		];
	
		this.callParent(arguments);
	}
});