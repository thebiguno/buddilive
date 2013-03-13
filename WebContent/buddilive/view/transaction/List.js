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
		this.columns = [
			{
				"flex": 1,
				"renderer": function(value, metadata, record){
					return "<table><tr><td>" + record.raw.date + "</td><td>" + record.raw.description + "</td><td>" + record.raw.number + "</td></tr>"
					+ "<tr><td>" + record.raw.from + " -> " + record.raw.to + "</td><td>" + record.raw.amount + "</td><td>" + record.raw.balance + "</td></tr></table>";
				}
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