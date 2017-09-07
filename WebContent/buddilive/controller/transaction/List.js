Ext.define("BuddiLive.controller.transaction.List", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"transactionlist": { "selectionchange": this.selectionChange },
			"transactionlist textfield[itemId='search']": {
				"blue": this.search,
				"specialkey": this.search
			},
			"accounttree button[itemId='add']": { "click": this.add }
		});
	},
	
	"add": function(component){
		component.up("transactionlist").reload()
	},
	
	"editTransactions": function(component){
		var tabs = component.up("budditabpanel");
		tabs.add({
			"xtype": "transactionlist"
		}).show();
	},
	
	"selectionChange": function(selectionModel, selected){
		var panel = selectionModel.view.panel;
		if (selected.length > 0){
			var transaction = selected[0].data;
			panel.down("transactioneditor").setTransaction(transaction);
			panel.down("button[itemId='deleteTransaction']").enable();
		}
		else {
			panel.down("button[itemId='deleteTransaction']").disable();
		}
	},
	
	"search": function(component, e) {
		if (e.getKey == null || e.getKey() == e.ENTER){
			var transactionList = component.up("transactionlist");
			var searchText = transactionList.down("textfield[itemId='search']");
			Ext.apply(transactionList.getStore().getProxy().extraParams, {
				"search": searchText.getValue()
			}); 
			transactionList.reload();
		}
	}
});