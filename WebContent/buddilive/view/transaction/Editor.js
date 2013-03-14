Ext.define('BuddiLive.view.transaction.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.transactioneditor",
	
	"requires": [
		"BuddiLive.view.transaction.DescriptionCombobox",
		"BuddiLive.view.transaction.split.Editor"
	],
	
	"initComponent": function(){
		this.layout = "vbox";
		this.border = false;
		this.items = [
			{"xtype": "spliteditor"}
		];
		
		this.dockedItems = [
			{
				"xtype": "panel",
				"width": "100%",
				"dock": "top",
				"layout": "hbox",
				"border": false,
				"defaults": {
					"padding": "5 0 5 5"
				},
				"items": [
					{
						"xtype": "hidden",
						"itemId": "id",
						"hidden": true
					},
					{
						"xtype": "datefield",
						"itemId": "date",
						"flex": 1,
						"enableKeyEvents": true,
						"emptyText": "Date"	//TODO i18n
					},
					{
						"xtype": "descriptioncombobox",
						"itemId": "description",
						"flex": 2,
						"emptyText": "Description",	//TODO i18n
						"enableKeyEvents": true
					},
					{
						"xtype": "textfield",
						"itemId": "number",
						"flex": 1,
						"emptyText": "Number",	//TODO i18n
						"padding": "5"
					}
				]
			},
			{
				"xtype": "toolbar",
				"dock": "bottom",
				"items": [
					{
						"text": "Delete",	//TODO i18n
						"icon": "img/minus-circle.png",
						"itemId": "deleteTransaction",
						"disabled": true
					},
					"->",
					{
						"text": "Clear",	//TODO i18n
						"icon": "img/exclamation-circle.png",
						"itemId": "clearTransaction"
					},
					{
						"text": "Record",	//TODO i18n
						"icon": "img/tick-circle.png",
						"itemId": "recordTransaction",
						"disabled": true
					}
				]
			}
		];
		
		this.callParent(arguments);
		
		this.setTransaction();
	},
	
	"getTransaction": function(transaction){
		var t = {};
		t.id = this.down("hidden[itemId='id']").getValue();
		t.date = Ext.Date.format(this.down("datefield[itemId='date']").getValue(), "Y-m-d");
		t.description = this.down("combobox[itemId='description']").getValue();
		t.number = this.down("textfield[itemId='number']").getValue();
		t.splits = [];
		
		for (var i = 0;  i < this.items.length; i++){
			t.splits.push(this.items.get(i).getSplit());
		}
		return t;
	},
	
	"setTransaction": function(transaction){
		Ext.suspendLayouts();
		transaction = (transaction ? transaction : {});
		
		this.down("hidden[itemId='id']").setValue(transaction.id);
		this.down("datefield[itemId='date']").setValue(Ext.Date.parse(transaction.date, "Y-m-d", true));
		this.down("combobox[itemId='description']").setValue(transaction.description);
		this.down("textfield[itemId='number']").setValue(transaction.number);
	
		//Remove all the split editors
		while (this.items.length > 0) this.remove(this.items.get(0));

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
		
		this.down("textfield[itemId='number']").focus();
		this.down("datefield[itemId='date']").focus(true, 100);
		Ext.resumeLayouts(true);
	},
	
	"validate": function(){
		if (!Ext.isDate(this.down("datefield[itemId='date']").getValue())) return false;
		var description = this.down("combobox[itemId='description']").getValue();
		if (description == null || description.length == 0) return false;
		if (this.items.length <= 1) return false;
		
		for (var i = 1;  i < this.items.length; i++){
			var split = this.items.get(i).getSplit();
			if (split.amount == 0) return false;
			if (!split.fromId || !split.toId) return false;
		}
		return true;
	}
});