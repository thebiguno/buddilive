Ext.define('BuddiLive.view.transaction.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.transactioneditor",
	
	"requires": [
		"BuddiLive.view.transaction.DescriptionCombobox",
		"BuddiLive.view.transaction.split.Editor"
	],
	
	"layout": "vbox",
	"border": false,
	"initComponent": function(){
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
					"padding": (this.initialConfig.scheduledTransaction ? "0 0 5 0" : "5 0 5 5")
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
						"hidden": this.initialConfig.scheduledTransaction == true,
						"flex": 1,
						"enableKeyEvents": true,
						"emptyText": "${translation("DATE")?json_string}"
					},
					{
						"xtype": "descriptioncombobox",
						"itemId": "description",
						"flex": 2,
						"emptyText": "${translation("DESCRIPTION")?json_string}",
						"enableKeyEvents": true
					},
					{
						"xtype": "textfield",
						"itemId": "number",
						"flex": 1,
						"emptyText": "${translation("NUMBER")?json_string}",
						"padding": (this.initialConfig.scheduledTransaction ? "1 0 5 5" : "5 5 5 5")
					}
				]
			},
			{
				"xtype": "toolbar",
				"hidden": this.initialConfig.scheduledTransaction == true,
				"dock": "bottom",
				"items": [
					{
						"text": "${translation("DELETE_TRANSACTION")?json_string}",
						"tooltip": "${translation("HELP_DELETE_TRANSACTION")?json_string}",
						"icon": "img/minus-circle.png",
						"itemId": "deleteTransaction",
						"disabled": true
					},
					"->",
					{
						"text": "${translation("CLEAR_TRANSACTION")?json_string}",
						"tooltip": "${translation("HELP_CLEAR_TRANSACTION")?json_string}",
						"icon": "img/exclamation-circle.png",
						"itemId": "clearTransaction"
					},
					{
						"text": "${translation("RECORD_UPDATE_TRANSACTION")?json_string}",
						"tooltip": "${translation("HELP_RECORD_UPDATE_TRANSACTION")?json_string}",
						"icon": "img/tick-circle.png",
						"itemId": "recordTransaction",
						"disabled": true
					}
				]
			}
		];
		
		this.callParent(arguments);
		
		this.setTransaction(this.initialConfig.transaction);
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

	//transaction is the transaction to set -- leave as null to blank out the existing transaction
	//loadFromDescription is true if we are loading this transaction from the saved transactions via description pulldown.  When true, we do not update 
	// any transaction-specific details, only the splits info.
	//preserveDate is true if we want to keep the date.  This is used when we have just recorded a transaction, and want to keep it for the next one.
	"setTransaction": function(transaction, loadFromDescription, preserveDate){
		Ext.suspendLayouts();
		
		//Save this so that we can check in the controller what has changed.
		this.lastTransaction = transaction;
		
		transaction = (transaction ? transaction : {});
		
		if (!loadFromDescription) {
			if (!preserveDate){
				if (transaction && transaction.dateIso) this.down("datefield[itemId='date']").setValue(Ext.Date.parse(transaction.dateIso, "Y-m-d", true));
				else this.down("datefield[itemId='date']").setValue();
			}
			this.down("hidden[itemId='id']").setValue(transaction.id);
			if (transaction.description) this.down("combobox[itemId='description']").setValue(transaction.description);
			else this.down("combobox[itemId='description']").clearValue();
			this.down("textfield[itemId='number']").setValue(transaction.number);
		}
	
		//Remove all the split editors
		while (this.items.length > 0) this.remove(this.items.get(0));

		var splits = (transaction.splits ? transaction.splits : []);
		if (splits && splits.length > 0){
			//Add a new split editor for each split
			for (var i = 0; i < splits.length; i++){
				var split = Ext.apply(splits[i]);
				if (loadFromDescription && !this.initialConfig.scheduledTransaction){
					//If this is being set from a description selection, we need to ensure that
					// a) one of the sources is set to the selected source
					// b) if not a), then the source we change to selected source should be an account, not a budget category.
					if (this.source != split.fromId && this.source != split.toId){
						if (split.fromType == "E" || split.fromType == "I"){
							split.toId = this.source;
						}
						else if (split.toType == "E" || split.toType == "E"){
							split.fromId = this.source;
						}
						else if (split.toType == "C"){
							split.fromId = this.source;
						}
						else {
							split.toId = this.source;
						}
					}
				}
				this.add({"xtype": "spliteditor", "source": this.source, "value": split, "scheduledTransaction": this.initialConfig.scheduledTransaction});
			}
		}
		else {
			//If the passed in splits are empty, add an empty editor
			this.add({"xtype": "spliteditor", "source": this.source, "scheduledTransaction": this.initialConfig.scheduledTransaction});
		}
		
		Ext.resumeLayouts(true);
		
		this.fireEvent("change", this);
	},
	
	"setSource": function(source){
		this.source = source;
	},
	
	"validate": function(){
		if (!this.initialConfig.scheduledTransaction && !Ext.isDate(this.down("datefield[itemId='date']").getValue())) return false;
		var description = this.down("combobox[itemId='description']").getValue();
		if (description == null || description.length == 0) return false;
		if (this.items.length == 0) return false;
		
		for (var i = 0;  i < this.items.length; i++){
			var split = this.items.get(i).getSplit();
			if (split.amount == 0) return false;
			if (!split.fromId || !split.toId) return false;
		}
		return true;
	}
});