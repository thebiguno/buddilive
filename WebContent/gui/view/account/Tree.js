Ext.define('BuddiLive.view.account.Tree', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.accounttree",
	"requires": [
		"BuddiLive.store.account.TreeStore"
	],
	
	"initComponent": function(){
		this.layout = "fit";
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
					if (!record.raw.debit) metaData.style += " color:#D10000;";
					if (record.raw.deleted == true) metaData.style += " text-decoration: line-through;";
					return value;
				}
			},
			{
				"text": "Balance",
				"dataIndex": "balance",
				"flex": 1,
				"align": "right",
				"renderer": function(value, metaData, record){
					if (!record.raw.debit ^ record.raw.balance < 0) metaData.style = 'color:#D10000;' 
					return value;
				}
			}
		];
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "top",
			"items": [
				{
					"tooltip": "Add Account",
					"icon": "img/table--plus.png",
					"itemId": "addAccount"
				},
				{
					"tooltip": "Edit Account",
					"icon": "img/table--pencil.png",
					"itemId": "editAccount",
					"disabled": true
				},
				{
					"tooltip": "Delete Account",
					"icon": "img/table--pencil.png",
					"itemId": "deleteAccount",
					"disabled": true
				}
			]
		}];
	
		this.callParent(arguments);
	}
});