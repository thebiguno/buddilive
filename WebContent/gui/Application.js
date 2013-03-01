Ext.Loader.setConfig({"enabled": true});
//Ext.Loader.loadScript({"url": "parameters.js"});

Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());

Ext.application({
	"name": "BuddiLive",
	"appFolder": "gui",
	
	"requires": [
		"BuddiLive.view.Viewport"
	],
	
	"controllers": [
		"account.Tree",
		"account.Editor",
		"transaction.List",
		"transaction.Editor"
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
			title = (error.statusText ? error.statusText : "Error");
			var json = Ext.decode(error.responseText, false);
			if (json == null){
				message = error.responseText;
			}
			else {
				message = json.msg;
			}
		}
		else {
			message = "Unknown error";
			title = "Error";
		}
		
		Ext.MessageBox.show({
			"title": title,
			"msg": message,
			"buttons": Ext.Msg.OK
		});
	}
});