Ext.define("BuddiLive.controller.preferences.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"preferenceseditor button[itemId='done']": {"click": this.done},
			"preferenceseditor button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"done": function(component){
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
		request.dateFormat = window.down("combobox[itemId='dateFormat']").getValue();
		request.currencySymbol = window.down("combobox[itemId='currencySymbol']").getValue();
		request.currencySymbolAfterAmount = window.down("checkbox[itemId='currencySymbolAfterAmount']").getValue();
		request.showDeleted = window.down("checkbox[itemId='showDeleted']").getValue();
		request.showCleared = window.down("checkbox[itemId='showCleared']").getValue();
		request.showReconciled = window.down("checkbox[itemId='showReconciled']").getValue();

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