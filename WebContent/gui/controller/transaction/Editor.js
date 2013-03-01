Ext.define("BuddiLive.controller.transaction.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"transactionlist": {
				//"celldblclick": this.editTransactions,
				//"selectionchange": this.selectionChange
			},
			"transactionlist button[itemId='add']": {
				"click": this.add
			}
		});
	},
	
	"add": function(component){
		component.up("transactionlist").getStore().load();
	},
	
	"editTransactions": function(component){
		var tabs = component.up("budditabpanel");
		tabs.add({
			"xtype": "transactionlist"
		}).show();
	},
	
	"selectionChange": function(selectionModel, selected){
		var panel = selectionModel.view.panel;
		var selectedType = selected.length > 0 ? selected[0].raw.type : null;
		panel.down("button[itemId='editTransactions']").setDisabled(selectedType != "account");
	}
});