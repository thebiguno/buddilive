Ext.define("BuddiLive.controller.account.Tree", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"accounttree": {"selectionchange": this.selectionChange}
		});
	},
	
	"selectionChange": function(selectionModel, selected){
		var panel = selectionModel.view.panel.up("buddiviewport");
		var selectedItem = selected[0].raw;
		var selectedType = selected.length > 0 ? selectedItem.nodeType : null;
		panel.down("button[itemId='editAccount']").setDisabled(selectedType != "account");
		panel.down("button[itemId='deleteAccount']").setDisabled(selectedType != "account");
		if (selectedType == "account" && selected[0].raw.deleted){
			panel.down("button[itemId='deleteAccount']").setText("Undelete Account");	//TODO i18n
		}
		else {
			panel.down("button[itemId='deleteAccount']").setText("Delete Account");	//TODO i18n
		}
		
		if (selectedType == "account"){
			var transactionList = panel.down("transactionlist");
			Ext.apply(transactionList.getStore().getProxy().extraParams, {
				"source": selectedItem.id
			}); 
			transactionList.getStore().load();
			transactionList.down("transactioneditor").setTransaction();
		}
	}
});