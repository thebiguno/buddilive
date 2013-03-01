Ext.define('BuddiLive.view.Transactions', {
	"extend": "Ext.grid.Panel",
	"alias": "widget.budditransactions",
	
	"initComponent": function(){
		this.layout = "fit";
		this.store = Ext.create("BuddiLive.store.Transactions");
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "date",
				"flex": 1
			}
		];
		this.dockedItems = [
			{
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
			},
			{
				"xtype": "budditransactioneditor",
				"dock": "bottom"
			}
		];
		
		this.callParent(arguments);
	}
});