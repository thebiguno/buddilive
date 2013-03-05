Ext.define('BuddiLive.view.budget.Panel', {
	"extend": "Ext.tab.Panel",
	"alias": "widget.budgetpanel",
	
	"requires": [
		"BuddiLive.view.budget.Editor"
	],
	
	"initComponent": function(){
		this.layout = "vbox";
		this.dockedItems = [{
			"xtype": "toolbar",
			"dock": "top",
			"items": [
				{
					"tooltip": "Add Budget Category",
					"icon": "img/table--plus.png",
					"itemId": "addCategory"
				},
				{
					"tooltip": "Edit Budget Category",
					"icon": "img/table--pencil.png",
					"itemId": "editCategory",
					"disabled": true
				},
				{
					"tooltip": "Delete Budget Category",
					"icon": "img/table--minus.png",
					"itemId": "deleteCategory",
					"disabled": true
				}
			]
		}];
		
		this.callParent(arguments);
	},
	
	"reload": function(){
		//Reload all children stores
		for (var i = 0; i < this.items.length; i++){
			this.items.getAt(i).getStore().reload();
		}
	}
});