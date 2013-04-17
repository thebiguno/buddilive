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
		this.disabled = true;
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
			"text": "${translation("DATE")?json_string}",
				"dataIndex": "date",
				"hideable": false,
				"sortable": false,
				"flex": 20
			},
			{
				"text": "${translation("DESCRIPTION")?json_string}",
				"dataIndex": "description",
				"hideable": false,
				"sortable": false,
				"flex": 30,
				"renderer": function(value, metadata, record){
					return "<b>" + value + "</b>";
				}
			},
			{
				"text": "${translation("AMOUNT_FROM")?json_string}",
				"hideable": false,
				"sortable": false,
				"flex": 15,
				"align": "right"
			},
			{
				"text": "${translation("AMOUNT_TO")?json_string}",
				"hideable": false,
				"sortable": false,
				"flex": 15,
				"align": "right"
			},
			{
				"text": "${translation("BALANCE")?json_string}",
				"hideable": false,
				"sortable": false,
				"flex": 20,
				"align": "right"
			}
		];
		
		this.dockedItems = [
			{
				"xtype": "toolbar",
				"dock": "bottom",
				"items": [
					"->",
					{
						"xtype": "textfield",
						"width": 200,
						"itemId": "search",
						"emptyText": "${translation("SEARCH")?json_string}"
					}
				]
			},
			{
				"xtype": "transactioneditor",
				"dock": "top"
			}
		];
		
		this.callParent(arguments);
		
		this.getStore().addListener("load", function(store, records){
			//We start the transaction list disabled, for now.  Unsure if this will stay.
			transactionList.enable();
		
			//var dateField = transactionList.down("datefield[itemId='date']");
			//dateField.focus(true);
		});
	}
});