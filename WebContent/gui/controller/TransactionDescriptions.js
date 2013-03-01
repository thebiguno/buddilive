Ext.define("BuddiLive.controller.TransactionDescriptions", {
	"extend": "Ext.app.Controller",
	"stores": ["TransactionDescriptions"],
	"models": ["TransactionDescriptions"],
	"views": [
		"Transactions"
	],
		
	"init": function() {
		this.control({
			"budditransactions": {
				//"celldblclick": this.editTransactions,
				//"selectionchange": this.selectionChange
			},
			"budditransactions button[itemId='add']": {
				"click": this.add
			}
		});
	},
	
	"add": function(component){
		component.up("budditransactions").getStore().load();
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