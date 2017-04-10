Ext.define("BuddiLive.view.component.LazyCombo", {
	"extend": "Ext.form.FieldContainer",
	"alias": "widget.lazycombo",
	"requires": [
		"BuddiLive.view.component.LazyComboSingleSelect",
		"BuddiLive.view.component.LazyComboMultiSelect"
	],
	
	"layout": "card",

	"listeners": {
		"afterrender": function(component){
			Ext.defer(function(){
				if (component.initialConfig.multiSelect){
					component.setMultiSelect(component.initialConfig.multiSelect);
				}
			}, 100);
		}
	},

	"minChars": 1,
	"initComponent": function(){
		var lazycombo = this;
		
		//We use a custom function rather than separating arguments with || because false or 0 may be a valid argument for many of the below.
		var notNull = function(){
			for(var i = 0; i < arguments.length; i++){
				if (arguments[i] != null) return arguments[i];
			}
			return undefined;
		};
		
		var singleCombo = {
			"xtype": "lazycombosingleselect",
			"allowBlank": notNull(this.initialConfig.allowBlank, this.allowBlank, true),
			"clearOnTrigger": notNull(this.initialConfig.clearOnTrigger, this.clearOnTrigger, true),
			"displayField": notNull(this.initialConfig.displayField, this.displayField, "text"),
			"editable": notNull(this.initialConfig.searchable, this.searchable, this.initialConfig.typeAhead, this.typeAhead),
			"emptyCls": notNull(this.initialConfig.emptyCls, this.emptyCls),
			"emptyText": notNull(this.initialConfig.emptyText, this.emptyText),
			"forceSelection": notNull(this.initialConfig.forceSelection, this.forceSelection),
			"listeners": notNull(this.initialConfig.listeners, this.listeners),
			"margin": 0,
			"padding": 0,
			"queryMode": notNull(this.initialConfig.queryMode, this.queryMode),
			"searchable": notNull(this.initialConfig.searchable, this.searchable),
			"selectOnFocus": false,
			"store": notNull(this.initialConfig.store, this.store),
			"storeData": notNull(this.initialConfig.storeData, this.storeData),
			"typeAhead": notNull(this.initialConfig.typeAhead, this.typeAhead),
			"url": notNull(this.initialConfig.url, this.url),
			"value": notNull(this.initialConfig.value, this.value),
			"valueField": notNull(this.initialConfig.valueField, this.valueField, "value"),
			"width": "100%"
		};
		var multiCombo = {
			"xtype": "lazycombomultiselect",
			"allowBlank": notNull(this.initialConfig.allowBlank, this.allowBlank, true),
			"autoSelect": false,
			"displayField": notNull(this.initialConfig.displayField, this.displayField, "text"),
			"editable": notNull(this.initialConfig.typeAhead, this.typeAhead),
			"emptyCls": notNull(this.initialConfig.emptyCls, this.emptyCls),
			"emptyText": notNull(this.initialConfig.emptyText, this.emptyText),
			"forceSelection": notNull(this.initialConfig.forceSelection, this.forceSelection),
			"forceSelection": notNull(this.initialConfig.forceSelection, this.forceSelection),
			"grow": notNull(this.initialConfig.grow, this.grow, false),
			"listeners": notNull(this.initialConfig.listeners, this.listeners),
			"margin": 0,
			"padding": 0,
			"queryMode": "local",
			"searchable": notNull(this.initialConfig.searchable, this.searchable),
			"selectOnFocus": false,
			"sorted": notNull(this.initialConfig.sorted, this.sorted),
			"store": notNull(this.initialConfig.store, this.store),
			"storeData": notNull(this.initialConfig.storeData, this.storeData),
			"typeAhead": notNull(this.initialConfig.typeAhead, this.typeAhead),
			"url": notNull(this.initialConfig.url, this.url),
			"value": notNull(this.initialConfig.value, this.value),
			"valueField": notNull(this.initialConfig.valueField, this.valueField, "value"),
			"width": "100%"
		};
		
		this.items = [
			singleCombo,
			multiCombo
		];
		this.callParent(arguments);
	},
	
	"getValue": function(){
		return this.getLayout().getActiveItem().getValue();
	},
	
	"setValue": function(value){
		this.getLayout().getActiveItem().setValue(value);
	},
	
	"getRawValue": function(){
		return this.getLayout().getActiveItem().getRawValue();
	},
	
	"setRawValue": function(value){
		this.getLayout().getActiveItem().setRawValue(value);
	},
	
	"setMultiSelect": function(multiSelect){
		if (!this.items || this.items.items.length != 2){
			debugger;
			var me = this;
			Ext.defer(function(){
				//console.log("Delaying...");
				me.setMultiSelect(multiSelect);
			}, 100);
			return;
		}
		
		if (multiSelect){
			this.getLayout().setActiveItem(this.down("lazycombomultiselect"));
		}
		else {
			this.getLayout().setActiveItem(this.down("lazycombosingleselect"));
		}
	},
	
	"getStore": function(){
		return this.getLayout().getActiveItem().getStore();
	},
	
	"validate": function(){
		return this.getLayout().getActiveItem().validate();
	}
});