Ext.define("BuddiLive.view.shared.LazyCombobox", {
	"extend": "Ext.form.field.ComboBox",
	"alias": "widget.lazycombobox",
	"requires": [
	],
	
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		//Default the display and value fields to "text" and "value".
		if (!this.displayField) this.displayField = "text";
		if (!this.valueField) this.valueField = "value";
	
		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		this.editable = this.searchable;
		this.triggerAction = (this.seachable ? null : "all");
		this.pageSize = (this.searchable ? 25 : null);
		this.minChars = 1,
	
		this.store = {
			"autoLoad": (this.value != null),
			"fields": [this.valueField, this.displayField],
			"remoteFilter": (this.searchable ? true : false),
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
		
		if (this.sorted && this.multiSelect){
			this.addListener("select", function(combo, records){
				combo.setValue(records.sort(function(a, b){
					return a.index - b.index;
				}));
			});
		}
	}
});