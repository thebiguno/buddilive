Ext.define("BuddiLive.controller.account.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"accounteditor component": {
				"blur": this.updateButtons,
				"keypress": this.updateButtons,
				"afterrender": this.updateButtons,
				"specialkey": this.specialKey
			},
			"accounteditor button[itemId='ok']": {"click": this.ok},
			"accounteditor button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"specialKey": function(component, e){
		if (e.getKey() == e.ENTER){
			this.ok(component);
		}
	},
	
	"cancel": function(component){
		component.up("accounteditor").close();
	},
	
	"ok": function(component){
		var window = component.up("accounteditor");
		var grid = window.initialConfig.grid;
		var selected = window.initialConfig.selected;

		var request = {};
		request.action = (selected ? "update" : "insert");
		if (selected) request.id = selected.id;
		request.name = window.down("textfield[itemId='name']").getValue();
		request.accountType = window.down("textfield[itemId='accountType']").getValue();
		request.type = window.down("combobox[itemId='type']").getValue();
		var startBalance = window.down("numberfield[itemId='startBalance']").getValue();
		if (startBalance) request.startBalance = startBalance;

		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/accounts",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				window.close();
				grid.getStore().reload();
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	},
	
	"updateButtons": function(component){
		var window = component.up("accounteditor");
		var ok = window.down("button[itemId='ok']");
		var name = window.down("textfield[itemId='name']");
		var accountType = window.down("textfield[itemId='accountType']");
		var type = window.down("combobox[itemId='type']");
		
		ok.setDisabled(name.getValue().length == 0 || accountType.getValue().length == 0 || type.getValue().length == 0);
	}
});