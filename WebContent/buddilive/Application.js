Ext.Loader.setConfig({
	"enabled": true,
	"disableCaching": true,
	"paths": {
		"Login": "authentication"
	}
});

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
		"preferences.PreferencesEditor",
		"preferences.ChangePasswordEditor",
		"restore.Form",
		"transaction.List",
		"transaction.Editor",
		"transaction.split.Editor"
	],
	"launch": function() {
		// This is fired as soon as the page is ready
		var viewport = Ext.create("BuddiLive.view.Viewport");
		BuddiLive.app = this;
		BuddiLive.app.viewport = viewport;
		
		//Prevent backspace from going back in history
		Ext.EventManager.addListener(Ext.getBody(), 'keydown', function(e){
			if (e.getTarget().type != 'text' && e.getKey() == '8' ){
				e.preventDefault();
			}
		});
		
		Ext.util.TaskManager.start({
			"interval": 1000 * 60 * 60,  //1 hour
			"run": function(){
				var conn = Ext.create("Ext.data.Connection");
				conn.request({
					"url": "data/scheduledtransactions/execute",
					"method": "POST",
					"jsonData": Ext.Date.format(new Date(), "Y-m-d"),
					"success": function(response){
						var messages = Ext.decode(response.responseText, true);
						if (messages != null && messages.messages != null){
							if (messages.messages.length > 0){
								Ext.MessageBox.show({
									"title": "${translation("SCHEDULED_TRANSACTION_MESSAGES")?json_string}",
									"msg": messages.messages,
									"buttons": Ext.Msg.OK
								});
							}
							//If we are in here, it means that messages.messages was not null; therefore, there
							// was at least one transaction inserted.  Refresh the stores to ensure we are current.
							BuddiLive.app.controllers.get("transaction.Editor").getTransactionDescriptionComboboxStoreStore().load();
							BuddiLive.app.viewport.down("panel[itemId='myAccounts']").down("accounttree").getStore().reload();
							if (BuddiLive.app.viewport.down("transactionlist").getStore().getCount() > 0){
								//getCount() > 0 is not the same as the (non existent) isLoaded(), but it is close enough for our purposes.
								BuddiLive.app.viewport.down("transactionlist").reload();
							}
						}
					}
				});
			}
		});
	},
	"error": function(error){
		var message;
		var title;
		if (Ext.isString(error)){
			message = error;
			title = "Error";
		}
		else if (error.responseText != null){
			title = (error.statusText ? error.statusText : "${translation("ERROR")?json_string}");
			var json = Ext.decode(error.responseText, true);
			if (json == null){
				message = error.responseText;
			}
			else {
				message = json.msg;
			}
		}
		else {
			message = "${translation("ERROR_UNKNOWN")?json_string}";
			title = "${translation("ERROR")?json_string}";
		}
		
		Ext.MessageBox.show({
			"title": title,
			"msg": message,
			"buttons": Ext.Msg.OK
		});
	}
});

Ext.override(Ext.form.DateField, {
	"format": "${(user.extDateFormat!"Y-m-d")?json_string}"
});
