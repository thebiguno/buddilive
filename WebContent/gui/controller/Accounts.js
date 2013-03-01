Ext.define("BuddiLive.controller.Accounts", {
	"extend": "Ext.app.Controller",
	"stores": ["Accounts"],
	"models": ["Accounts"],
	"views": [
		"Accounts",
		"Transactions",
		"dialog.Account"
	],
		
	"init": function() {
		this.control({
			"buddiaccounts": {
				"selectionchange": this.selectionChange
			},
			"buddiaccounts button[itemId='addAccount']": {"click": this.addAccount},
			"buddiaccounts button[itemId='editAccount']": {"click": this.editAccount},
			"buddiaccounts button[itemId='deleteAccount']": {"click": this.deleteAccount}
		});
	},
	
	"addAccount": function(component){
		var grid = component.up("buddiaccounts");
		Ext.widget("buddiaccount", {
			"grid": grid
		}).show();
	},
	"editAccount": function(component){
		var grid = component.up("buddiaccounts");
		var selected = grid.getSelectionModel().getSelection()[0].raw;
		Ext.widget("buddiaccount", {
			"grid": grid,
			"selected": selected
		}).show();
	},
	"deleteAccount": function(component){
		var grid = component.up("buddiaccounts");
		var selected = grid.getSelectionModel().getSelection()[0].raw;
		
		if (selected == null) return;
		
		if (selected.deleted){
			var request = {"action": "undelete", "id": selected.id};
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
		}
		else {
			Ext.MessageBox.show({
				"title": "Delete Account",
				"msg": "Are you sure you want to delete this account?",
				"buttons": Ext.MessageBox.YESNO,
				"fn": function(buttonId){
					var request = {"action": "delete", "id": selected.id};
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
				}
			});
		}
	},
	
	"selectionChange": function(selectionModel, selected){
		var panel = selectionModel.view.panel;
		var selectedType = selected.length > 0 ? selected[0].raw.nodeType : null;
		panel.down("button[itemId='editTransactions']").setDisabled(selectedType != "account");
		panel.down("button[itemId='editAccount']").setDisabled(selectedType != "account");
		panel.down("button[itemId='deleteAccount']").setDisabled(selectedType != "account");
		if (selectedType == "account" && selected[0].raw.deleted){
			panel.down("button[itemId='deleteAccount']").setText("Undelete Account");
		}
		else {
			panel.down("button[itemId='deleteAccount']").setText("Delete Account");
		}
	}
});