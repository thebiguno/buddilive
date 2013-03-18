Ext.define("BuddiLive.view.transaction.split.SourceCombobox", {
	"extend": "Ext.form.field.ComboBox",
	
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		this.displayField = "text";
		this.valueField = "value";
		this.enableKeyEvents = true;
	
		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		this.editable = false;
		this.triggerAction = "all";
		this.searchText = "";	//Used to jump to records

		this.listConfig = {
			"itemTpl": "<div style='{style}'>{text}</div>"
		};
	
		this.callParent(arguments);
		
		this.addListener("select", function(){
			//Don't let users select the separators.
			if (combo.getValue() == ""){
				combo.setValue();
			}
			
			//TODO Don't let them select two categories
			//TODO Ensure one of the items is the currently selected source
		});

		this.addListener("keyup", function(combo, e){
			if (e.getKey() == e.DELETE || e.getKey() == e.ESC || e.getKey() == e.BACKSPACE){
				combo.searchText = "";
				combo.setValue(null);
			}
			else {
				var c = String.fromCharCode(e.charCode || e.keyCode);
				if (c.match(/[a-zA-Z]+/)) combo.searchText += c;
				else combo.searchText = "";
			}

			if (combo.searchText.length > 0){
				combo.expand();
				var record = combo.getStore().findRecord("search", combo.searchText);	//We prefer to find text matching at the beginning
				if (record == null) record = combo.getStore().findRecord("text", combo.searchText, 0, true, false, false);	//If that doesn't work, try anywhere.
				combo.select(record);
				combo.setValue(record);
			}
			
		});
	}
});