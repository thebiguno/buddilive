Ext.Loader.setConfig({"enabled": true, "disableCaching": true});

Ext.state.Manager.setProvider(Ext.supports.LocalStorage ? new Ext.state.LocalStorageProvider() : new Ext.state.CookieProvider());

Ext.application({
	"name": "BuddiLive",
	"appFolder": "buddilive",
	
	"requires": [
		"BuddiLive.view.Viewport"
	],
	
	"controllers": [
		"Reports",
		"Viewport",
		"account.Tree",
		"account.Editor",
		"budget.Editor",
		"budget.Panel",
		"budget.Tree",
		"scheduled.Editor",
		"scheduled.List",
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

Ext.override(Ext.form.DateField, {
	"format": "${extDateFormat?json_string}"
});
