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
		
		this.addListener("keyup", function(combo, e){
			if (e.getKey() == e.ESC){
				combo.setRawValue("");
				combo.getStore().clearFilter();
			}
			else if (e.getKey() == e.DOWN || e.getKey() == e.UP){
				return;
			}
			else {
				combo.getStore().clearFilter(true);	//Clear filter without updating UI
				combo.getStore().filter({"property": "value", "value": combo.getRawValue(), "anyMatch": true, "caseSensitive": false});
			}
		});
		this.addListener("blur", function(combo){
			combo.getStore().clearFilter();
		});
		this.addListener("focus", function(combo){
			combo.getStore().clearFilter();
		});
	}
});