Ext.define("BuddiLive.view.component.LazyComboMultiSelect", {
	"extend": "Ext.form.field.Tag",
	"alias": "widget.lazycombomultiselect",
	"requires": [
		
	],

	"listeners": {
		"beforedeselect": function(component, record, index){
			if (!component.initialConfig.allowBlank && component.getValue().length <= 1){
				return false;		//Prevent deselection of last item
			}
		},
		"select": function(component, records){
			if (component.sorted && Ext.isArray(records)){
				records.sort(function(a, b){
					return component.getStore().indexOf(a) - component.getStore().indexOf(b);
				});
				//Setting the same records in a different order doesn't actually change anything - you need to clear the store first to register a change.
				component.suspendEvents();
				component.setValue();
				component.setValue(records);
				component.resumeEvents();
			}
		}
	},

	"minChars": 1,
	"initComponent": function(){
		var combo = this;

		//We want the initial value to be an array...
		if (this.value != null && !Ext.isArray(this.value)){
			this.value = ("" + this.value).split(/,/g);
		}
		this.initialSetValue = (this.value != null);	//If there is a default value, set it once the store is loaded.
		
		var staticData = false;
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
			staticData = true;
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
			staticData = true;
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
				},
				"load": function(store, records, successful){
					if (successful){
						//Once the store is initially loaded, set the default value.
						if (combo.initialSetValue && combo.store){
							//debugger;
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
		
		if (staticData){
			//debugger;
		}
		
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
		
		if (staticData){
			//debugger;
			//var value = this.value;
			//this.setValue();
			//this.setValue(value);
		}
	},
	
	"setValue": function(value){
		if (value != null && !Ext.isArray(value)){
			value = ("" + value).split(/,/g);
		}
		if (this.getStore().isLoaded()){
			this.suspendEvent("beforedeselect");
			this.callParent();
			this.resumeEvent("beforedeselect");
			this.callParent(arguments);
		}
		else {
			this.initialSetValue = true;
			this.initialConfig.value = value;
			this.getStore().load();
		}
	}
});