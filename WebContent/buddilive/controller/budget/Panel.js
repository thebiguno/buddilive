Ext.define("BuddiLive.controller.budget.Panel", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"budgetpanel": {
				"afterrender": this.reload,
				"reload": this.reload
			}
		});
	},
	
	"reload": function(component){
		var budgetPanel = (component.xtype == 'budgetpanel' ? component : component.up("budgetpanel"));
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/categories/periods",
			"headers": {
				"Accept": "application/json"
			},
			"method": "GET",
			"success": function(response){
				var json = Ext.decode(response.responseText, true);
				Ext.suspendLayouts();
				budgetPanel.removeAll();
				if (json != null){
					for (var i = 0; i < json.data.length; i++){
						budgetPanel.add(
							{
								"xtype": "budgettree",
								"periodValue": json.data[i].value,
								"periodText": json.data[i].text
							}
						);
					}
				}
				budgetPanel.setActiveTab(budgetPanel.child("budgettree[itemId='MONTH']") || 1);
				Ext.resumeLayouts(true);
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	}
});