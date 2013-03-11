Ext.define("BuddiLive.controller.account.Tree", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"accounttree": {"selectionchange": this.selectionChange},
			"accounttree button[itemId='addAccount']": {"click": this.addAccount},
			"accounttree button[itemId='editAccount']": {"click": this.editAccount},
			"accounttree button[itemId='deleteAccount']": {"click": this.deleteAccount}
		});
	},
	
	"addAccount": function(component){
		var grid = component.up("accounttree");
		Ext.widget("accounteditor", {
			"grid": grid
		}).show();
	},
	"editAccount": function(component){
		var grid = component.up("accounttree");
		var selected = grid.getSelectionModel().getSelection()[0].raw;
		Ext.widget("accounteditor", {
			"grid": grid,
			"selected": selected
		}).show();
	},
	"deleteAccount": function(component){
		var grid = component.up("accounttree");
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
				"msg": "Are you sure you want to delete this account?",	//TODO i18n
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
		var selectedItem = selected[0].raw;
		var selectedType = selected.length > 0 ? selectedItem.nodeType : null;
		panel.down("button[itemId='editAccount']").setDisabled(selectedType != "account");
		panel.down("button[itemId='deleteAccount']").setDisabled(selectedType != "account");
		if (selectedType == "account" && selected[0].raw.deleted){
			panel.down("button[itemId='deleteAccount']").setTooltip("Undelete Account");	//TODO i18n
		}
		else {
			panel.down("button[itemId='deleteAccount']").setTooltip("Delete Account");	//TODO i18n
		}
		
		if (selectedType == "account"){
			var transactionList = panel.up("panel[itemId='myAccounts']").down("transactionlist");
			transactionList.getStore().load({"params": {"source": selectedItem.id}});
			transactionList.down("transactioneditor").setTransaction();
		}
	}
});