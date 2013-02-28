Ext.define("BuddiLive.controller.dialog.Account", {
	"extend": "Ext.app.Controller",
	"views": [
		"dialog.Account"
	],
		
	"init": function() {
		this.control({
			"buddiaccount component": {"blur": this.updateButtons},
			"buddiaccount button[itemId='ok']": {"click": this.ok},
			"buddiaccount button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"cancel": function(component){
		component.up("buddiaccount").close();
	},
	
	"ok": function(component){
		var window = component.up("buddiaccount");
		var grid = window.initialConfig.grid;
		var selected = window.initialConfig.selected;

		var request = {};
		request.action = (selected ? "update" : "insert");
		if (selected) request.id = selected.id;
		request.name = window.down("textfield[itemId='name']").getValue();
		request.accountType = window.down("textfield[itemId='accountType']").getValue();
		request.type = window.down("combobox[itemId='type']").getValue();
		request.startBalance = window.down("numberfield[itemId='startBalance']").getValue();

		var conn = new Ext.data.Connection();
		conn.request({
			"url": "gui/accounts",
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
		var window = component.up("buddiaccount");
		var ok = window.down("button[itemId='ok']");
		var name = window.down("textfield[itemId='name']");
		var accountType = window.down("textfield[itemId='accountType']");
		var type = window.down("combobox[itemId='type']");
		
		ok.setDisabled(name.getValue().length == 0 || accountType.getValue().length == 0 || type.getValue().length == 0);
	}
});