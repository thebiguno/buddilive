Ext.define('BuddiLive.view.transaction.List', {
	"extend": "Ext.grid.Panel",
	"alias": "widget.transactionlist",
	"requires": [
		"BuddiLive.store.transaction.ListStore"
	],
	
	"initComponent": function(){
		var transactionList = this;
		this.layout = "fit";
		this.store = Ext.create("BuddiLive.store.transaction.ListStore");
		this.border = false;
		this.stateId = "transactionlist";
		this.stateful = true;
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
				"text": "From / To",	//TODO i18n
				"flex": 1,
				"renderer": function(value, metadata, record){
					return record.raw.from + " -> " + record.raw.to;
				}
			},
			{
				"text": "Debit",	//TODO i18n
				"dataIndex": "amount",
				"flex": 1,
				"renderer": function(value, metadata, record){
					return (record.raw.debit ? value : "");
				}
			},
			{
				"text": "Credit",	//TODO i18n
				"dataIndex": "amount",
				"flex": 1,
				"renderer": function(value, metadata, record){
					return (record.raw.debit ? "" : value);
				}
			},
			{
				"text": "Balance",	//TODO i18n
				"dataIndex": "balance",
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
		
		this.getStore().addListener("load", function(store){
			transactionList.getView().scrollBy(0, 10000000, false);
		});
	}
});