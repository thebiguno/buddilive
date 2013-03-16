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
		this.viewConfig = {
			"stripeRows": true
		};
		
		this.plugins = [
			{
				"ptype": "bufferedrenderer"
			}
		];
		
		this.features = [
			{
				"ftype": "rowbody",
				"getAdditionalData": function(data, rowIndex, record, orig){
					var rowBody = "";
					var s = record.raw.splits;
					var headerCt = this.view.headerCt, colspan = headerCt.getColumnCount();
					for (var i = 0; i < s.length; i++){
						rowBody += "<div style='padding: 2px; height: 20px; width: 100%;'>"
								+ "<span style='display: inline-block; width: 23%;'></span>"
								+ "<span style='display: inline-block; width: 26%;'><i>" + s[i].from + " &rarr; " + s[i].to + "</i></span>" 
								+ "<span style='display: inline-block; text-align: right; width: 15%; " + s[i].amountStyle + "'>" + (s[i].amountInDebitColumn ? s[i].amount : "") + "</span>" 
								+ "<span style='display: inline-block; text-align: right; width: 15%; " + s[i].amountStyle + "'>" + (!s[i].amountInDebitColumn ? s[i].amount : "") + "</span>" 
								+ "<span style='display: inline-block; text-align: right; width: 20%; " + s[i].balanceStyle + "'>" + s[i].balance + "</span>"
								+ "</div>";
					}
					return {
						"rowBody": rowBody,
						"rowBodyCls": "",
						"rowBodyColspan": colspan
					};
				}
			},
			{
				"ftype": "rowwrap"	//Makes the extra rows look like normal rows
			}
		];
		
		this.columns = [
			{
			"text": "Date",	//TODO i18n
				"dataIndex": "date",
				"flex": 20
			},
			{
				"text": "Description",	//TODO i18n
				"dataIndex": "description",
				"flex": 30,
				"renderer": function(value, metadata, record){
					return "<b>" + value + "</b>";
				}
			},
			{
				"text": "Debit",	//TODO i18n
				"flex": 15,
				"align": "right"
			},
			{
				"text": "Credit",	//TODO i18n
				"flex": 15,
				"align": "right"
			},
			{
				"text": "Balance",	//TODO i18n
				"flex": 20,
				"align": "right"
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
		
		this.getStore().addListener("load", function(store, records){
			//TODO Clean this up a bit...
			//transactionList.getView().focusRow(records.length - 1);
			//transactionList.getSelectionModel().select(records[records.length - 1);
			//transactionList.getView().scrollBy(0, -10000000, false);
			//transactionList.getView().scrollBy(0, 10000000, false);
			
			//transactionList.down("datefield[itemId='date']").focus(true, 100);
		});
	}
});