Ext.define('BuddiLive.view.budget.Panel', {
	"extend": "Ext.tab.Panel",
	"alias": "widget.budgetpanel",
	
	"requires": [
		"BuddiLive.view.budget.Editor"
	],
	
	"initComponent": function(){
		this.layout = "vbox";
		this.callParent(arguments);
	},
	
	"reload": function(){
		//Reload all children stores
		for (var i = 0; i < this.items.length; i++){
			this.items.getAt(i).getStore().reload();
		}
	}
});