Ext.define("BuddiLive.controller.Viewport", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"tabpanel[itemId='budditabpanel']": {"tabchange": this.tabChange},
			"buddiviewport button[itemId='addAccount']": {"click": this.addAccount},
			"buddiviewport button[itemId='editAccount']": {"click": this.editAccount},
			"buddiviewport button[itemId='deleteAccount']": {"click": this.deleteAccount},
			"buddiviewport button[itemId='addCategory']": {"click": this.addCategory},
			"buddiviewport button[itemId='editCategory']": {"click": this.editCategory},
			"buddiviewport button[itemId='deleteCategory']": {"click": this.deleteCategory},
			"buddiviewport button[itemId='preferences']": {"click": this.editPreferences},
			"buddiviewport button[itemId='logout']": {"click": this.logout}
		});
	},
	
	"tabChange": function(tabPanel, newCard, oldCard){
		var accountActive = newCard.itemId == "myAccounts";
		var budgetActive = newCard.itemId == "myBudget";
		
		tabPanel.down("button[itemId='addAccount']").setVisible(accountActive);
		tabPanel.down("button[itemId='editAccount']").setVisible(accountActive);
		tabPanel.down("button[itemId='deleteAccount']").setVisible(accountActive);
		tabPanel.down("button[itemId='addCategory']").setVisible(budgetActive);
		tabPanel.down("button[itemId='editCategory']").setVisible(budgetActive);
		tabPanel.down("button[itemId='deleteCategory']").setVisible(budgetActive);
	},
	
	"addAccount": function(component){
		var grid = component.up("buddiviewport").down("accounttree");
		Ext.widget("accounteditor", {
			"grid": grid
		}).show();
	},
	"editAccount": function(component){
		var grid = component.up("buddiviewport").down("accounttree");
		var selected = grid.getSelectionModel().getSelection()[0].raw;
		Ext.widget("accounteditor", {
			"grid": grid,
			"selected": selected
		}).show();
	},
	"deleteAccount": function(component){
		var grid = component.up("buddiviewport").down("accounttree");
		var selected = grid.getSelectionModel().getSelection()[0].raw;
		
		if (selected == null) return;
		
		if (selected.deleted){
			var request = {"action": "undelete", "id": selected.id};
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
				}
			});
		}
	},
	
	"addCategory": function(component){
		var panel = component.up("buddiviewport").down("budgetpanel");
		Ext.widget("budgeteditor", {
			"panel": panel
		}).show();
	},
	"editCategory": function(component){
		var panel = component.up("buddiviewport").down("budgetpanel");
		var budgetTrees = Ext.ComponentQuery.query("budgettree", panel);
		for (var i = 0; i < budgetTrees.length; i++){
			if (!budgetTrees[i].collapsed){
				var selected = budgetTrees[i].getSelectionModel().getSelection()[0].raw;
				Ext.widget("budgeteditor", {
					"panel": panel,
					"selected": selected
				}).show();
			}
		}
	},
	"deleteCategory": function(component){
		var panel = component.up("buddiviewport").down("budgetpanel");
		var budgetTrees = Ext.ComponentQuery.query("budgettree", panel);
		var selected = null;
		for (var i = 0; i < budgetTrees.length; i++){
			if (!budgetTrees[i].collapsed) selected = budgetTrees[i].getSelectionModel().getSelection()[0].raw;
		}
		
		if (selected == null) return;
		
		var request = {"action": selected.deleted ? "undelete" : "delete", "id": selected.id};
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/categories",
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
	},
	
	"editPreferences": function(component){
		var panel = component.up("buddiviewport");
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/userpreferences",
			"headers": {
				"Accept": "application/json"
			},
			"method": "GET",
			"success": function(response){
				var data = Ext.decode(response.responseText);
				Ext.widget("preferenceseditor", {
					"panel": panel,
					"data": data
				}).show();
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	},
	
	"logout": function(component){
		window.location.search += '&logout';
	}
});