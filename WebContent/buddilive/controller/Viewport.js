Ext.define("BuddiLive.controller.Viewport", {
	"extend": "Ext.app.Controller",
	"stores": [
		"transaction.split.FromComboboxStore",
		"transaction.split.ToComboboxStore"
	],

	"init": function() {
		this.control({
			"buddiviewport button[itemId='addAccount']": {"click": this.addAccount},
			"buddiviewport button[itemId='editAccount']": {"click": this.editAccount},
			"buddiviewport button[itemId='deleteAccount']": {"click": this.deleteAccount},
			"buddiviewport button[itemId='addCategory']": {"click": this.addCategory},
			"buddiviewport button[itemId='editCategory']": {"click": this.editCategory},
			"buddiviewport button[itemId='deleteCategory']": {"click": this.deleteCategory},
			"buddiviewport button[itemId='addScheduled']": {"click": this.addScheduled},
			"buddiviewport button[itemId='editScheduled']": {"click": this.editScheduled},
			"buddiviewport button[itemId='deleteScheduled']": {"click": this.deleteScheduled},
			"buddiviewport menuitem[itemId='showScheduled']": {"click": this.showScheduled},
			"buddiviewport menuitem[itemId='changePassword']": {"click": this.changePassword},
			"buddiviewport menuitem[itemId='showPreferences']": {"click": this.showPreferences},
			"buddiviewport menuitem[itemId='backup']": {"click": this.backup},
			"buddiviewport menuitem[itemId='restore']": {"click": this.restore},
			"buddiviewport menuitem[itemId='deleteUser']": {"click": this.deleteUser},
			"buddiviewport menuitem[itemId='gettingStarted']": {"click": this.gettingStarted},
			"buddiviewport button[itemId='logout']": {"click": function(component){
				Ext.Ajax.request({
					"url": "index",
					"method": "DELETE",
					"success": function() {
						window.location.reload();
					}
				});
			}}
		});
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
		var me = this;
		var viewport = component.up("buddiviewport");
		var grid = viewport.down("accounttree");
		var selected = grid.getSelectionModel().getSelection()[0].raw;
		
		if (selected == null) return;
		
		if (selected.deleted){
			var request = {"action": "undelete", "id": selected.id};
			var conn = new Ext.data.Connection();
			var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": viewport});
			mask.show();
			conn.request({
				"url": "data/accounts",
				"headers": {
					"Accept": "application/json"
				},
				"method": "POST",
				"jsonData": request,
				"success": function(response){
					mask.hide();
					window.close();
					grid.getStore().reload();
					me.getTransactionSplitFromComboboxStoreStore().load();
					me.getTransactionSplitToComboboxStoreStore().load();
				},
				"failure": function(response){
					mask.hide();
					BuddiLive.app.error(response);
				}
			});
		}
		else {
			Ext.MessageBox.show({
				"title": "${translation("DELETE_ACCOUNT")?json_string}",
				"msg": "${translation("CONFIRM_DELETE_ACCOUNT")?json_string}",
				"buttons": Ext.MessageBox.YESNO,
				"fn": function(buttonId){
					if (buttonId != "yes") return;
					
					var request = {"action": "delete", "id": selected.id};
					var conn = new Ext.data.Connection();
					var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": viewport});
					mask.show();
					conn.request({
						"url": "data/accounts",
						"headers": {
							"Accept": "application/json"
						},
						"method": "POST",
						"jsonData": request,
						"success": function(response){
							mask.hide();
							window.close();
							grid.getStore().reload();
							me.getTransactionSplitFromComboboxStoreStore().load();
							me.getTransactionSplitToComboboxStoreStore().load();
						},
						"failure": function(response){
							mask.hide();
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
		var selected = panel.getActiveTab().getSelectionModel().getSelection()[0].raw;
		Ext.widget("budgeteditor", {
			"panel": panel,
			"selected": selected
		}).show();
	},
	"deleteCategory": function(component){
		var me = this;
		var viewport = component.up("buddiviewport");
		var panel = viewport.down("budgetpanel");
		var budgetTrees = Ext.ComponentQuery.query("budgettree", panel);
		
		var selected = panel.getActiveTab().getSelectionModel().getSelection()[0].raw;

		if (selected == null) return;
		
		if (selected.deleted){
			var request = {"action": "undelete", "id": selected.id};
			var conn = new Ext.data.Connection();
			var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": viewport});
			mask.show();
			conn.request({
				"url": "data/categories",
				"headers": {
					"Accept": "application/json"
				},
				"method": "POST",
				"jsonData": request,
				"success": function(response){
					mask.hide();
					window.close();
					panel.fireEvent("reload", panel);
					me.getTransactionSplitFromComboboxStoreStore().load();
					me.getTransactionSplitToComboboxStoreStore().load();
				},
				"failure": function(response){
					mask.hide();
					BuddiLive.app.error(response);
				}
			});
		}
		else {
			Ext.MessageBox.show({
				"title": "${translation("DELETE_CATEGORY")?json_string}",
				"msg": "${translation("CONFIRM_DELETE_CATEGORY")?json_string}",
				"buttons": Ext.MessageBox.YESNO,
				"fn": function(buttonId){
					if (buttonId != "yes") return;
					
					var request = {"action": "delete", "id": selected.id};
					var conn = new Ext.data.Connection();
					var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": viewport});
					mask.show();
					conn.request({
						"url": "data/categories",
						"headers": {
							"Accept": "application/json"
						},
						"method": "POST",
						"jsonData": request,
						"success": function(response){
							mask.hide();
							window.close();
							panel.fireEvent("reload", panel);
							me.getTransactionSplitFromComboboxStoreStore().load();
							me.getTransactionSplitToComboboxStoreStore().load();
						},
						"failure": function(response){
							mask.hide();
							BuddiLive.app.error(response);
						}
					});
				}
			});
		}
	},
	
	"addScheduled": function(component){
		var panel = component.up("buddiviewport").down("scheduledlist").down("grid");
		Ext.widget("schedulededitor", {
			"panel": panel
		}).show();
	},
	"editScheduled": function(component){
		var panel = component.up("buddiviewport").down("scheduledlist").down("grid");
		var selected = panel.getSelectionModel().getSelection()[0].raw;
		Ext.widget("schedulededitor", {
			"panel": panel,
			"selected": selected
		}).show();
	},
	"deleteScheduled": function(component){
		var viewport = component.up("buddiviewport");
		var panel = viewport.down("scheduledlist").down("grid");
		
		var selected = panel.getSelectionModel().getSelection()[0].raw;

		if (selected == null) return;
		
		Ext.MessageBox.show({
			"title": "${translation("DELETE_SCHEDULED")?json_string}",
			"msg": "${translation("CONFIRM_DELETE_SCHEDULED")?json_string}",
			"buttons": Ext.MessageBox.YESNO,
			"fn": function(buttonId){
				if (buttonId != "yes") return;
				
				var request = {"action": "delete", "id": selected.id};
				var conn = new Ext.data.Connection();
				var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": viewport});
				mask.show();
				conn.request({
					"url": "data/scheduledtransactions",
					"headers": {
						"Accept": "application/json"
					},
					"method": "POST",
					"jsonData": request,
					"success": function(response){
						mask.hide();
						window.close();
						panel.getStore().reload();
					},
					"failure": function(response){
						mask.hide();
						BuddiLive.app.error(response);
					}
				});
			}
		});
	},
	
	"changePassword": function(component){
		var panel = component.up("buddiviewport");
		var conn = new Ext.data.Connection();
		Ext.widget("changepasswordeditor", {
			"panel": panel
		}).show();
	},
	
	"showPreferences": function(component){
		var panel = component.up("buddiviewport");
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "data/userpreferences",
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
	
	"showScheduled": function(component){
		var tabPanel = component.up("tabpanel[itemId='budditabpanel']");
		if (tabPanel.down("scheduledlist") == null){
			tabPanel.add({"xtype": "scheduledlist"});
		}
		tabPanel.setActiveTab(tabPanel.down("scheduledlist"));
	},
	
	"gettingStarted": function(){
		Ext.MessageBox.show({
			"title": "${translation("HELP_GETTING_STARTED_TITLE")?json_string}",
			"msg": "${translation("HELP_GETTING_STARTED")?json_string}",
			"buttons": Ext.Msg.OK
		});
	},
	
	"backup": function(component){
		window.open("data/backup.json");
	},

	"restore": function(component){
		Ext.widget("restoreform").show();
	},
	
	"deleteUser": function(component){
		var viewport = component.up("buddiviewport");
		Ext.MessageBox.show({
			"title": "${translation("DELETE_USER")?json_string}",
			"msg": "${translation("CONFIRM_DELETE_USER")?json_string}",
			"buttons": Ext.MessageBox.YESNO,
			"fn": function(buttonId){
				if (buttonId != "yes") return;
				
				Ext.MessageBox.show({
					"title": "${translation("DELETE_USER")?json_string}",
					"msg": "${translation("CONFIRM_DELETE_USER2")?json_string}",
					"buttons": Ext.MessageBox.YESNO,
					"fn": function(buttonId){
						if (buttonId != "yes") return;
						
						var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": viewport});
						mask.show();
						new Ext.data.Connection().request({
							"url": "data/userpreferences",
							"jsonData": {"action": "delete"},
							"headers": {"Accept": "application/json"},
							"method": "POST",
							"timeout": 10 * 60 * 1000,	//Set a high timeout value to give time for deleting everything
							"success": function(response){
								mask.hide();
								location.reload();
							},
							"failure": function(response){
								mask.hide();
								BuddiLive.app.error(response);
							}
						});
					}
				});
			}
		});
	}
});