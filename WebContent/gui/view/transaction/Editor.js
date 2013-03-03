Ext.define('BuddiLive.view.transaction.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.transactioneditor",
	
	"requires": [
		"BuddiLive.view.transaction.split.Editor",
		"BuddiLive.store.transaction.DescriptionStore"
	],
	
	"initComponent": function(){
		this.layout = "vbox";
		this.items = [
			{
				"xtype": "panel",
				"width": "100%",
				"layout": "hbox",
				"border": false,
				"defaults": {
					"padding": "5 0 5 5"
				},
				"items": [
					{
						"xtype": "datefield",
						"itemId": "date",
						"flex": 1,
						"emptyText": "Date",
						"value": new Date()
					},
					{
						"xtype": "combobox",
						"itemId": "description",
						"flex": 2,
						"emptyText": "Description",
						"store": Ext.create("BuddiLive.store.transaction.DescriptionStore")
					},
					{
						"xtype": "textfield",
						"itemId": "number",
						"flex": 1,
						"emptyText": "Number",
						"padding": "5"
					}
				]
			},
			{"xtype": "spliteditor"}
		];
		
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "bottom",
			"items": [
				{
					"text": "Delete",
					"tooltip": "Delete Transaction",
					"icon": "img/minus-circle.png",
					"itemId": "deleteTransaction"
				},
				"->",
				{
					"text": "Clear",
					"tooltip": "Clear Transaction",
					"icon": "img/exclamation-circle.png",
					"itemId": "clearTransaction"
				},
				{
					"text": "Record",
					"tooltip": "Record Transaction",
					"icon": "img/tick-circle.png",
					"itemId": "recordTransaction"
				}
			]
		}];
		
		this.callParent(arguments);
	},
	
	"setTransaction": function(transaction){
		transaction = (transaction ? transaction : {});
		
		this.down("datefield[itemId='date']").setValue(Ext.Date.parse(transaction.date, "Y-m-d", true));
		this.down("combobox[itemId='description']").setValue(transaction.description);
		this.down("textfield[itemId='number']").setValue(transaction.number);
	
		//Remove all the split editors
		while (this.items.length > 1) this.remove(this.items.get(1));

		var splits = (transaction.splits ? transaction.splits : []);
		if (splits && splits.length > 0){
			//Add a new split editor for each split
			for (var i = 0; i < splits.length; i++){
				this.add({"xtype": "spliteditor", "value": splits[i]});
			}
		}
		else {
			//If the passed in splits are empty, add an empty editor
			this.add({"xtype": "spliteditor"});
		}
	}
});