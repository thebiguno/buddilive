Ext.define("BuddiLive.controller.budget.Tree", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"budgettree": {
				"edit": this.editCell,
				"selectionchange": this.selectionChange
			},
			"budgettree button[itemId='previousPeriod']": { "click": this.clickChangePeriod },
			"budgettree button[itemId='nextPeriod']": { "click": this.clickChangePeriod }
		});
	},
	
	"editCell": function(editor, data){
		//If nothing has changed, no point in reloading
		if (data.originalValue == data.value) return;
		
		var budgetTree = editor.cmp;
		var request = {"action": "update"};
		request.categoryId = data.record.raw.id;
		request.date = data.record.raw.dateIso;
		request.amount = data.value;
	
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "buddilive/categories/entries",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				budgetTree.getStore().reload();
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	},
	
	"selectionChange": function(selectionModel, selected){
		var enabled = selected && selected.length > 0;

		var viewport = selectionModel.view.panel.up("buddiviewport");
		viewport.down("button[itemId='editCategory']").setDisabled(!enabled);
		viewport.down("button[itemId='deleteCategory']").setDisabled(!enabled);
		if (selected && selected.length > 0 && selected[0].raw.deleted){
			viewport.down("button[itemId='deleteCategory']").setText("Undelete Budget Category");	//TODO i18n
		}
		else {
			viewport.down("button[itemId='deleteCategory']").setText("Delete Budget Category");	//TODO i18n
		}
	},
	
	"clickChangePeriod": function(component){
		var budgetTree = component.up("budgettree");
		var offset = (component.itemId == "previousPeriod" ? -1 : 1);
		budgetTree.getStore().load({"params": {"date": budgetTree.currentDate, "offset": offset}});
	}
});