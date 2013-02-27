Ext.define('BuddiLive.view.Accounts', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.buddiaccounts",
	"requires": [
		"BuddiLive.store.Accounts"
	],
	
	"initComponent": function(){
		this.title = "My Accounts";
		this.layout = "fit";
		this.rootVisible = false;
		this.store = Ext.create("BuddiLive.store.Accounts");
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "name",
				"flex": 1,
				"xtype": "treecolumn"
			},
			{
				"text": "Balance",
				"dataIndex": "balance",
				"flex": 1
			}
		];
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "bottom",
			"items": [
				{
					"text": "Add Account",
					"icon": "img/table--plus.png",
					"itemId": "addAccount"
				},
				{
					"text": "Edit Transactions",
					"icon": "img/table--pencil.png",
					"itemId": "editTransactions",
					"disabled": true
				}
			]
		}];
	
		this.callParent(arguments);
	}
});