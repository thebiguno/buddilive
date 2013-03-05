Ext.define('BuddiLive.view.transaction.List', {
	"extend": "Ext.grid.Panel",
	"alias": "widget.transactionlist",
	"requires": [
		"BuddiLive.store.transaction.ListStore"
	],
	
	"initComponent": function(){
		this.layout = "fit";
		this.store = Ext.create("BuddiLive.store.transaction.ListStore");
		this.border = false;
		this.columns = [
			{
				"text": "Date",
				"dataIndex": "date",
				"flex": 1
			},
			{
				"text": "Description",
				"dataIndex": "description",
				"flex": 1
			},
			{
				"text": "Number",
				"dataIndex": "number",
				"flex": 1
			},
			{
				"text": "Amount",
				"dataIndex": "amount",
				"flex": 1
			},
			{
				"text": "From",
				"dataIndex": "from",
				"flex": 1
			},
			{
				"text": "To",
				"dataIndex": "to",
				"flex": 1
			}
		];
		this.dockedItems = [
			{
				"xtype": "toolbar",
				"dock": "top",
				"items": [
					"->",
					{
						"xtype": "textfield",
						"emptyText": "Search"
					},
					{
						"xtype": "button",
						"icon": "img/magnifier--arrow.png"
					}
				]
			},
			{
				"xtype": "transactioneditor",
				"dock": "bottom"
			}
		];
		
		this.callParent(arguments);
	}
});