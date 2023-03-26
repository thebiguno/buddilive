Ext.define('BuddiLive.view.account.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.accounttree",
	"requires": [
		"BuddiLive.store.account.TreeStore",
		"BuddiLive.view.account.Editor"
	],
	
	"rootVisible": false,
	"border": false,
	"viewConfig": {
		"stripeRows": true
	},
	
	"columns": [
		{
			"text": "Name",
			"dataIndex": "name",
			"flex": 3,
			"sortable": false,
			"xtype": "treecolumn",
			"renderer": function(value, metaData, record){
				metaData.style = record.data.style;
				return value;
			}
		},
		{
			"text": "Balance",
			"dataIndex": "balance",
			"flex": 1,
			"sortable": false,
			"hideable": false,
			"align": "right",
			"renderer": function(value, metaData, record){
				metaData.style = record.data.balanceStyle 
				return value;
			}
		}
	],
	
	"initComponent": function(){
		this.store = Ext.create("BuddiLive.store.account.TreeStore");
	
		this.callParent(arguments);
	}
});