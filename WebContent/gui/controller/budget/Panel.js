Ext.define("BuddiLive.controller.budget.Panel", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"budgetpanel": {"afterrender": this.load}
		});
	},
	
	"load": function(component){
		var budgetPanel = (component.xtype == 'budgetpanel' ? component : component.up("budgetpanel"));
		budgetPanel.removeAll();
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "gui/categories/periods",
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
							"period": json.data[i]
						});
					}
				}
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	}
});