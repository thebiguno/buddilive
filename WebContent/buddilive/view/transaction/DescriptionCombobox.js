Ext.define("BuddiLive.view.transaction.DescriptionCombobox", {
	"extend": "Ext.form.field.ComboBox",
	"alias": "widget.descriptioncombobox",
	"requires": [
	],
	
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		this.displayField = "description";
		this.valueField = "description";
	
		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		this.editable = true;
		this.triggerAction = "all";
		this.queryMode = "local";

		//this.listConfig = {
		//	"itemTpl": "<div style='{style}'>{description}</div>"
		//};
	
		this.store = {
			"autoLoad": true,
			"fields": ["description"],
			"remoteFilter": false,
			"proxy": {
				"type": "ajax",
				"autoAbort": true, 
				"url": "gui/transactions/descriptions.json",
				"reader": {
					"type": "json",
					"root": "data"
				}
			},
			"listeners": {
				"load": function(){
					//Once the store is initially loaded, set the default value.
					if (combo.initialSetValue){
						combo.setValue(combo.getValue());
						combo.initialSetValue = false;
					}
				}
			}
		};
		
		this.callParent(arguments);
		
		this.addListener("select", function(combo, record){
			//Don't let users select the separators.
			if (combo.getValue() == ""){
				combo.setValue();
			}
			
			if (record.length > 0){
				combo.up("transactioneditor").setTransaction(record[0].raw.transaction);
			}
			
			//TODO Don't let them select two categories
			//TODO Ensure one of the items is the currently selected source
		});
	}
});