Ext.define("BuddiLive.controller.Transactions", {
	"extend": "Ext.app.Controller",
	"stores": ["Transactions"],
	"models": ["Transactions"],
	"views": [
		"Transactions"
	],
		
	"init": function() {
		this.control({
			"buddiaccounts": {
				//"celldblclick": this.editTransactions,
				//"selectionchange": this.selectionChange
			},
			"buddiaccounts button[itemId='add']": {
				//"click": this.add
			}
		});
	},
	
	"add": function(component){
	},
	
	"editTransactions": function(component){
		var tabs = component.up("budditabpanel");
		tabs.add({
			"xtype": "budditransactions"
		}).show();
	},
	
	"selectionChange": function(selectionModel, selected){
		var panel = selectionModel.view.panel;
		var selectedType = selected.length > 0 ? selected[0].raw.type : null;
		panel.down("button[itemId='editTransactions']").setDisabled(selectedType != "account");
	}
});