Ext.define("BuddiLive.controller.budget.Tree", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"budgettree": {
				"edit": this.editCell,
				"selectionchange": this.selectionChange
			},
			"budgettree button[itemId='copyFromPreviousPeriod']": { "click": this.clickCopyFromPreviousPeriod },
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
			"url": "data/categories/entries",
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
			viewport.down("button[itemId='deleteCategory']").setText("${translation("UNDELETE_BUDGET_CATEGORY")?json_string}");
		}
		else {
			viewport.down("button[itemId='deleteCategory']").setText("${translation("DELETE_BUDGET_CATEGORY")?json_string}");
		}
	},

	"clickCopyFromPreviousPeriod": function(component){
		var budgetTree = component.up("budgettree");
		var request = {"action": "copyFromPrevious"};
		request.type = budgetTree.periodValue;
		request.date = budgetTree.currentDate;
		var conn = new Ext.data.Connection();
		var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": budgetTree});
		mask.show();
		conn.request({
			"url": "data/categories",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				mask.hide();
				budgetTree.getStore().reload();
			},
			"failure": function(response){
				mask.hide();
				BuddiLive.app.error(response);
			}
		});
	},
		
	"clickChangePeriod": function(component){
		var budgetTree = component.up("budgettree");
		var offset = (component.itemId == "previousPeriod" ? -1 : 1);
		budgetTree.getStore().load({"params": {"date": budgetTree.currentDate, "offset": offset}});
	}
});