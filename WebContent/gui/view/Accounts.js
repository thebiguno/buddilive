Ext.define('BuddiLive.view.Accounts', {
	"extend": "Ext.tree.Panel",
	"alias": "widget.buddiaccounts",
	"requires": [
		"BuddiLive.store.Accounts"
	],
	
	"initComponent": function(){
		this.layout = "fit";
		this.rootVisible = false;
		this.store = Ext.create("BuddiLive.store.Accounts");
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "name",
				"width": 300,
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
				"width": 100,
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
					"text": "Add Account",
					"icon": "img/table--plus.png",
					"itemId": "addAccount"
				},
				{
					"text": "Edit Account",
					"icon": "img/table--pencil.png",
					"itemId": "editAccount",
					"disabled": true
				},
				{
					"text": "Delete Account",
					"icon": "img/table--pencil.png",
					"itemId": "deleteAccount",
					"disabled": true
				}
			]
		}];
	
		this.callParent(arguments);
	}
});