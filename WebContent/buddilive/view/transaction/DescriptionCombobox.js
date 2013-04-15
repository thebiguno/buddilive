Ext.define("BuddiLive.view.transaction.DescriptionCombobox", {
	"extend": "Ext.form.field.ComboBox",
	"alias": "widget.descriptioncombobox",
	"requires": [
	],
	
	"store": "transaction.DescriptionComboboxStore",
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		this.displayField = "value";
		this.valueField = "value";
	
		this.editable = true;
		this.triggerAction = "all";
		this.queryMode = "local";

		this.callParent(arguments);
		
		this.addListener("select", function(combo, record){
			if (record.length > 0){
				combo.up("transactioneditor").setTransaction(record[0].raw.transaction, true);
			}
		});
		
		this.addListener("keyup", function(){
			this.getStore().clearFilter(true);	//Clear filter without updating UI
			this.getStore().filter({"property": "value", "value": this.getRawValue(), "anyMatch": true, "caseSensitive": false});
		});
	}
});