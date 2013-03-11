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
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "top",
			"items": [
				{
					"tooltip": "${translation("NEW_ACCOUNT")?json_string}",
					"icon": "img/table--plus.png",
					"itemId": "addAccount"
				},
				{
					"tooltip": "${translation("MODIFY_ACCOUNT")?json_string}",
					"icon": "img/table--pencil.png",
					"itemId": "editAccount",
					"disabled": true
				},
				{
					"tooltip": "${translation("DELETE_ACCOUNT")?json_string}",
					"icon": "img/table--minus.png",
					"itemId": "deleteAccount",
					"disabled": true
				}
			]
		}];
	
		this.callParent(arguments);
	}
});