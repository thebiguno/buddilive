Ext.define("BuddiLive.view.budget.ParentCombobox", {
	"extend": "Ext.form.field.ComboBox",
	"alias": "widget.parentcombobox",
	"requires": [
	],
	
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		this.displayField = "text";
		this.valueField = "value";
	
		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		this.editable = false;
		this.triggerAction = "all";
		//this.queryMode = "local";

		this.listConfig = {
			"itemTpl": "<div style='{style}'>{text}</div>"
		};
	
		this.store = {
			"autoLoad": true,
			"fields": ["value", "text", "income", "style"],
			"remoteFilter": false,
			"proxy": {
				"type": "ajax",
				"autoAbort": true, 
				"url": this.url,
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
		
		this.addListener("select", function(){
			//Don't let users select the separators.
			if (combo.getValue() == ""){
				combo.setValue();
			}
		});
	}
});