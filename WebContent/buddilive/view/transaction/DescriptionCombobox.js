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
			//Don't let users select the separators.
			if (combo.getValue() == ""){
				combo.setValue();
			}
			
			if (record.length > 0){
				combo.up("transactioneditor").setTransaction(record[0].raw.transaction, true);
			}
		});
	}
});