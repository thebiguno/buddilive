Ext.define("BuddiLive.controller.login.Login", {
	"extend": "Ext.app.Controller",

	"stores": [
		"preferences.CurrenciesComboboxStore",
		"preferences.LocalesComboboxStore"
	],
	"onLaunch": function(){
		this.getPreferencesCurrenciesComboboxStoreStore().load();
		this.getPreferencesLocalesComboboxStoreStore().load();
	},
	
	"init": function() {
		this.control({
			"login button[itemId=back]": {
				"click": function(button) {
					button.up('form').up('panel').getLayout().prev();
				}
			},
			"login button[itemId=forward]": {
				"click": function(button) {
					button.up('form').up('panel').getLayout().next();
				}
			},
			"login button[itemId=authenticate]": { "click": this.authenticate },
			"login form[itemId=authenticate] textfield": { "keypress": this.authenticate },
			"login button[itemId=enrole]": { "click": this.enrole },
			"login form[itemId=enrole] textfield": { "keypress": this.enrole },
			"login button[itemId=reset]": { "click": this.reset },
			"login form[itemId=reset] textfield": { "keypress": this.enrole },
			"login button[itemId=activate]": { "click": this.activate },
			"login form[itemId=activate] textfield": { "keypress": this.activate }
		});
	},

	"authenticate": function(cmp, e) {
		if (e.getKey() == 0 || e.getKey() == e.ENTER) {
			var form = cmp.up('form').getForm();
			if (form.isValid() == false) return;
			form.submit({
				"url": "index",
				"params": { "action": "login" },
				"success": function() {
					window.location.reload();
				},
				"failure": function(form, action) {
					var key = action.result ? action.result.key : undefined;
					if (key) {
						var card = button.up('form').up('panel').getLayout().next();
						card.down('hiddenfield[name=identifier]').setValue(key);
					} else {
						animateMessage(cmp.up('form').down('label[itemId=message]'), "Invalid Credentials");
					}
				}
			});
		}
	},
	
	"enrole": function(cmp, e) {
		if (e.getKey() == 0 || e.getKey() == e.ENTER) {
			var form = cmp.up('form').getForm();
			if (form.isValid() == false) return;
			form.submit({
				"url": "index",
				"params": { "action": "enrole" },
				"success": function() {
					cmp.up('form').up('panel').getLayout().next();
				},
				"failure": function(form, action) {
					var response = Ext.decode(action.response.responseText, true);
					var message = response && response.msg ? response.msg : "An error has occurred";
					animateMessage(cmp.up('form').down('label[itemId=message]'), message);
				}
			});
		}
	},
	
	"reset": function(cmp, e) {
		if (e.getKey() == 0 || e.getKey() == e.ENTER) {
			var form = cmp.up('form').getForm();
			if (form.isValid() == false) return;
			form.submit({
				"url": "index",
				"params": { "action": "reset" },
				"success": function() {
					cmp.up('form').up('panel').getLayout().next();
				},
				"failure": function(form, action) {
					animateMessage(cmp.up('form').down('label[itemId=message]'), "An error has occurred");
				}
			});
		}
	},
	
	"activate": function(cmp, e) {
		if (e.getKey() == 0 || e.getKey() == e.ENTER) {
			var form = cmp.up('form').getForm();
			if (form.isValid() == false) return;
			form.submit({
				"url": "index",
				"params": { "action": "activate" },
				"success": function() {
					window.location.reload();
				},
				"failure": function(form, action) {
					animateMessage(cmp.up('form').down('label[itemId=message]'), "An error has occurred");
				}
			});
		}
	}
});
