Ext.define("BuddiLive.controller.preferences.Editor", {
	"extend": "Ext.app.Controller",
	"stores": [
		"preferences.CurrenciesComboboxStore",
		"preferences.LocalesComboboxStore",
		"preferences.TimezonesComboboxStore"
	],
	"onLaunch": function(){
		var currenciesComboboxStore = this.getPreferencesCurrenciesComboboxStoreStore();
		currenciesComboboxStore.load();
		var localesComboboxStore = this.getPreferencesLocalesComboboxStoreStore();
		localesComboboxStore.load();
		var timezonesComboboxStore = this.getPreferencesTimezonesComboboxStoreStore();
		timezonesComboboxStore.load();
	},

	"init": function() {
		this.control({
			"preferenceseditor button[itemId='ok']": {"click": this.ok},
			"preferenceseditor button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"cancel": function(component){
		component.up("preferenceseditor").close();
	},
	
	"ok": function(component){
		var window = component.up("preferenceseditor");
		var panel = window.initialConfig.panel;
		var originalData = window.initialConfig.data;

		if (window.down("checkbox[itemId='encrypt']").getValue() != originalData.encrypt && window.down("textfield[itemId='password']").getValue().length == 0){
			Ext.MessageBox.show({
				"title": "${translation("INVALID")?json_string}",
				"msg": "${translation("ENTER_PASSWORD_TO_CHANGE_ENCRYPTION")?json_string}",
				"buttons": Ext.MessageBox.OK
			});
			return;
		}

		var request = {"action": "update"};
		request.encrypt = window.down("checkbox[itemId='encrypt']").getValue();
		request.encryptPassword = window.down("textfield[itemId='password']").getValue();
		request.locale = window.down("combobox[itemId='locale']").getValue();
		request.timezone = window.down("combobox[itemId='timezone']").getValue();
		request.currency = window.down("combobox[itemId='currency']").getValue();
		var dateFormat = window.down("combobox[itemId='dateFormat']").getValue();
		request.dateFormat = dateFormat ? dateFormat : "";
		request.showDeleted = window.down("checkbox[itemId='showDeleted']").getValue();

		var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": window});
		mask.show();
		
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/userpreferences",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				mask.hide();
				window.close();
				panel.reload();
			},
			"failure": function(response){
				mask.hide();
				BuddiLive.app.error(response);
			}
		});
	}
});