Ext.define("BuddiLive.controller.budget.Panel", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			
			"budgetpanel": {"afterrender": this.load},
			"budgetpanel button[itemId='addCategory']": {"click": this.addCategory},
			"budgetpanel button[itemId='editCategory']": {"click": this.editCategory},
			"budgetpanel button[itemId='deleteCategory']": {"click": this.deleteCategory}
		});
	},
	
	"load": function(component){
		var budgetPanel = (component.xtype == 'budgetpanel' ? component : component.up("budgetpanel"));
		budgetPanel.removeAll();
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/categories/periods",
			"headers": {
				"Accept": "application/json"
			},
			"method": "GET",
			"success": function(response){
				var json = Ext.decode(response.responseText, true);
				if (json != null){
					for (var i = 0; i < json.data.length; i++){
						budgetPanel.add({
							"xtype": "budgettree",
							"periodText": json.data[i].text,
							"periodValue": json.data[i].value
						});
					}
					budgetPanel.setActiveTab(0);
				}
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	},
	
	"addCategory": function(component){
		var panel = component.up("budgetpanel");
		Ext.widget("budgeteditor", {
			"panel": panel
		}).show();
	},
	"editCategory": function(component){
		var panel = component.up("budgetpanel");
		var selected = panel.getActiveTab().getSelectionModel().getSelection()[0].raw;
		Ext.widget("budgeteditor", {
			"panel": panel,
			"selected": selected
		}).show();
	},
	"deleteCategory": function(component){
		var panel = component.up("budgetpanel");
		var selected = panel.getActiveTab().getSelectionModel().getSelection()[0].raw;
		
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
	}
});