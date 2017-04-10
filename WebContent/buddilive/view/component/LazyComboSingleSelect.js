Ext.define("BuddiLive.view.component.LazyComboSingleSelect", {
	"extend": "Ext.form.field.ComboBox",
	"alias": "widget.lazycombosingleselect",
	"requires": [
		
	],

	"onTriggerClick": function(){
		if (this.initialConfig.clearOnTrigger) this.setValue();
		this.callParent();
	},

	"minChars": 1,
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		//Default the display and value fields to "text" and "value".
		if (!this.displayField) this.displayField = "text";
		if (!this.valueField) this.valueField = "value";

		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		this.editable = this.searchable || this.typeAhead;
		
		if (this.initialConfig.storeData) {
			this.store = Ext.create("Ext.data.Store", {
				"data": {"data": this.initialConfig.storeData},
				"fields": [this.valueField, this.displayField],
				"proxy": {
					"type": "memory",
					"reader": {
						"type": "json",
						"rootProperty": "data"
					}
				}
			});
		}
		else if (this.initialConfig.getStoreData) {
			this.store = Ext.create("Ext.data.Store", {
				"data": {"data": this.initialConfig.getStoreData()},
				"fields": [this.valueField, this.displayField],
				"proxy": {
					"type": "memory",
					"reader": {
						"type": "json",
						"rootProperty": "data"
					}
				}
			});
		}
		
		this.store = this.store || {
			"autoLoad": (this.value != null),
			"fields": [this.valueField, this.displayField],
			"remoteFilter": false,	//This is not the same as a searchable combo box
			"proxy": {
				"type": "ajax",
				"timeout": Ext.Ajax.timeout,
				"url": this.url,
				"pageParam": undefined,
				"startParam": undefined,
				"limitParam": undefined,
				"reader": {
					"type": "json",
					"rootProperty": "data"
				}
			},
			"listeners": {
				"beforeload": function(store, operation){
					var proxy = store.getProxy()
					if (proxy && proxy.ebieLastRequest){
						proxy.ebieLastRequest.options.callback = null;
						Ext.Ajax.abort(proxy.ebieLastRequest);
					}

					var params = Ext.apply({}, operation.getParams());
					
					if (combo.initialSetValue && combo.initialConfig){
						if (combo.initialConfig.value != null) {
							params.filter = "";
						}
						else {
							combo.initialSetValue = false;
							return false;
						}
					}
					else if (!combo.initialSetValue){
						var value = (combo.getRawValue() ? combo.getRawValue() : "");
						params.filter="* co '" + value + "'";
					}
					else {
						params.filter = "";
					}
					delete params.query;	//No reason to send this...
					operation.setParams(params);
				},
				"load": function(store, records, successful){
					if (successful){
						//Once the store is initially loaded, set the default value.
						if (combo.initialSetValue && combo.store){
							combo.setValue(combo.initialConfig.value);
							if (combo.getValue() == null && combo.initialConfig.defaultValue){
								combo.setValue(combo.initialConfig.defaultValue);
							}
							combo.initialSetValue = false;
						}
					}
					else if (combo.initialConfig.errorHandler != null){
						combo.initialConfig.errorHandler();
					}
				}
			}
		};
		this.callParent(arguments);
		
		this.getStore().addListener("beforeload", function(){
			try {
				combo.mask("Loading...");
			}
			catch (err){}
		});
		this.getStore().addListener("load", function(){
			try {
				combo.unmask();
			}
			catch (err){}
		});
	},
	
	"setValue": function(value){
		if (this.getStore().isLoaded() || value == null){
			this.callParent(arguments);
		}
		else {
			this.initialSetValue = true;
			this.initialConfig.value = value;
			this.getStore().load();
		}
	}
});