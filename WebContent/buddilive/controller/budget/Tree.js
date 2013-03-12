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
		var request = {"action": "update"};
		request.categoryId = data.record.raw.id;
		request.date = data.record.raw.currentDate;
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
				//TODO reload the entire tree to calculate differences?
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	},
	
	"selectionChange": function(selectionModel, selected){
		var panel = selectionModel.view.panel.up("budgetpanel");
		var selected = panel.getActiveTab().getSelectionModel().getSelection();
		var disabled = selected.length > 0;

		panel.down("button[itemId='editCategory']").setDisabled(!selected);
		panel.down("button[itemId='deleteCategory']").setDisabled(!selected);
		if (selected && selected[0].raw.deleted){
			panel.down("button[itemId='deleteCategory']").setTooltip("Undelete Budget Category");
		}
		else {
			panel.down("button[itemId='deleteCategory']").setTooltip("Delete Budget Category");
		}
	},
	
	"clickChangePeriod": function(component){
		var budgetTree = component.up("budgettree");
		var offset = (component.itemId == "previousPeriod" ? -1 : 1);
		budgetTree.getStore().load({"params": {"date": budgetTree.currentDate, "offset": offset}});
	}
});