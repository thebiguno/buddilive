Ext.define("BuddiLive.view.transaction.split.SourceCombobox", {
	"extend": "Ext.form.field.ComboBox",
	
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		this.displayField = "text";
		this.valueField = "value";
		this.enableKeyEvents = true;
	
		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		this.editable = true;
		this.triggerAction = "all";
		this.forceSelection = true;
		this.matchFieldWidth = false;

		this.listConfig = {
			"itemTpl": "<div style='{style}'>{text}</div>",
			"width": 300
		};
	
		this.callParent(arguments);

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
				combo.getStore().filter([
					{"property": "text", "value": Ext.String.trim(this.getRawValue()), "anyMatch": true, "caseSensitive": false},
					{
						"filterFn": function(item){
							return combo.getRawValue().length == 0 || item.data.type != null;
						}
					}
				]);
			}
		});
		
		this.addListener("blur", function(combo){
			combo.getStore().clearFilter();
		});
		this.addListener("focus", function(combo){
			combo.getStore().clearFilter();
		});

/*
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
*/
		
		//Ensure that from / to comboboxes are valid in context with each other
		this.addListener("select", function(thisCombo){
			//Don't let users select the separators.
			if (thisCombo.getValue() == ""){
				thisCombo.setValue();
			}

			var otherCombo = thisCombo.up("spliteditor").down(thisCombo.xtype == "fromcombobox" ? "tocombobox" : "fromcombobox");
			var source = thisCombo.initialConfig.source;
			if (source != null){
				//If we have a source, it will be an account; thus we guarantee that there will be at most one category.
				if (thisCombo.getValue() != source){
					otherCombo.setValue(source);
				}
				
				//If both from / to sources are identical, blank the other one
				if (thisCombo.getValue() == otherCombo.getValue()){
					otherCombo.setValue();
				}
			}
			else {
				//If we don't have a source, we are probably in a scheduled transaction editor.  This makes things a bit harder; we need 
				// to look up the type from the source ID, and switch from that.
				var thisType = thisCombo.getStore().findRecord("value", thisCombo.getValue());
				var otherType = otherCombo.getStore().findRecord("value", otherCombo.getValue());
				if ((thisType == "I" || thisType == "E") && (otherType == "I" || otherType == "E")){
					otherCombo.setValue();
				}
			}
		});
	}
});