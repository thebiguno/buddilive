Ext.define("BuddiLive.controller.preferences.Editor", {
	"extend": "Ext.app.Controller",

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

		var request = {"action": "update"};
		request.encrypt = window.down("checkbox[itemId='encrypt']").getValue();
		request.locale = window.down("combobox[itemId='locale']").getValue();
		request.dateFormat = window.down("combobox[itemId='dateFormat']").getValue();
		request.currencySymbol = window.down("combobox[itemId='currencySymbol']").getValue();
		request.currencySymbolAfterAmount = window.down("checkbox[itemId='currencySymbolAfterAmount']").getValue();
		request.showDeleted = window.down("checkbox[itemId='showDeleted']").getValue();
		request.showCleared = window.down("checkbox[itemId='showCleared']").getValue();
		request.showReconciled = window.down("checkbox[itemId='showReconciled']").getValue();

		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/userpreferences",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				window.close();
				panel.reload();
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	}
});