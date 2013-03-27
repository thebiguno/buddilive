Ext.define("BuddiLive.controller.scheduled.List", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"scheduledlist grid": { "selectionchange": this.selectionChange }
		});
	},

	"selectionChange": function(selectionModel, selected){
		var enabled = selected && selected.length > 0;

		var viewport = selectionModel.view.panel.up("buddiviewport");
		viewport.down("button[itemId='editScheduled']").setDisabled(!enabled);
		viewport.down("button[itemId='deleteScheduled']").setDisabled(!enabled);
	}
});