Ext.Loader.setConfig({"enabled": true});
//Ext.Loader.loadScript({"url": "parameters.js"});

Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());

Ext.application({
	"name": "BuddiLive",
	"appFolder": "buddilive",
	
	"requires": [
		"BuddiLive.view.Viewport"
	],
	
	"controllers": [
		"Viewport",
		"account.Tree",
		"account.Editor",
		"budget.Editor",
		"budget.Panel",
		"budget.Tree",
		"scheduled.Editor",
		"preferences.Editor",
		"transaction.List",
		"transaction.Editor",
		"transaction.split.Editor"
	],
	"launch": function() {
		// This is fired as soon as the page is ready
		var viewport = Ext.create("BuddiLive.view.Viewport");
		BuddiLive.app = this;
		BuddiLive.app.viewport = viewport;
	},
	"error": function(error){
		var message;
		var title;
		if (Ext.isString(error)){
			message = error;
			title = "Error";
		}
		else if (error.responseText != null){
			title = (error.statusText ? error.statusText : "Error");	//TODO i18n
			var json = Ext.decode(error.responseText, true);
			if (json == null){
				message = error.responseText;
			}
			else {
				message = json.msg;
			}
		}
		else {
			message = "Unknown error";	//TODO i18n
			title = "Error";	//TODO i18n
		}
		
		Ext.MessageBox.show({
			"title": title,
			"msg": message,
			"buttons": Ext.Msg.OK
		});
	}
});

//Forces all number fields with the "forcePrecision" field set to true to show exactly two decimal points at all times.
Ext.override(Ext.form.NumberField, {
	"forcePrecision" : false,

	"valueToRaw": function(value) {
		var me = this, decimalSeparator = me.decimalSeparator;
		value = me.parseValue(value);
		value = me.fixPrecision(value);
		value = Ext.isNumber(value) ? value : parseFloat(String(value).replace(decimalSeparator, '.'));
		if (isNaN(value)){
			value = '';
		}
		else {
			value = me.forcePrecision ? value.toFixed(me.decimalPrecision) : parseFloat(value);
			value = String(value).replace(".", decimalSeparator);
		}
		return value;
	}
});