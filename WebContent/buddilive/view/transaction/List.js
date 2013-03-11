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
				"text": "Date",	//TODO i18n
				"dataIndex": "date",
				"flex": 1
			},
			{
				"text": "Description",	//TODO i18n
				"dataIndex": "description",
				"flex": 1
			},
			{
				"text": "Number",	//TODO i18n
				"dataIndex": "number",
				"flex": 1
			},
			{
				"text": "Amount",	//TODO i18n
				"dataIndex": "amount",
				"flex": 1
			},
			{
				"text": "From",	//TODO i18n
				"dataIndex": "from",
				"flex": 1
			},
			{
				"text": "To",	//TODO i18n
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
						"emptyText": "Search"	//TODO i18n
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