Ext.define("BuddiLive.controller.transaction.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"transactionlist button[itemId='recordTransaction']": {"click": this.recordTransaction}
		});
	},
	
	"recordTransaction": function(component){
		var editor = component.up("transactioneditor");
		
		var request = editor.getTransaction();
		if (request.date == null || request.description == null || request.splits.length == 0){
			return;
		}
		request.action = "insert";

		var conn = new Ext.data.Connection();
		conn.request({
			"url": "gui/transactions",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				editor.setTransaction();
				editor.up("transactionlist").getStore().reload();
				editor.up("panel[itemId='myAccounts']").down("accounttree").getStore().reload();
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	}
});