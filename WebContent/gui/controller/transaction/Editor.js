Ext.define("BuddiLive.controller.transaction.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"transactionlist button[itemId='recordTransaction']": {"click": this.recordTransaction},
			"transactionlist button[itemId='clearTransaction']": {"click": this.clearTransaction},
			"transactionlist button[itemId='deleteTransaction']": {"click": this.deleteTransaction},
			"transactionlist field": {"keypress": this.validateFields},
			"transactionlist field": {"blur": this.validateFields}
		});
	},
	
	"validateFields": function(component){
		var editor = component.up("transactioneditor");
		var enabled = editor.validate();
		editor.down("button[itemId='recordTransaction']").setDisabled(!enabled);
	},
	
	"recordTransaction": function(component){
		var editor = component.up("transactioneditor");
		
		var request = editor.getTransaction();
		if (request.date == null || request.description == null || request.splits.length == 0){
			return;
		}
		request.action = "insert";
		
		//Disable the button before submitting to prevent double clicks
		editor.down("button[itemId='recordTransaction']").disable();

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
	},
	
	"clearTransaction": function(component){
		//TODO check if there is data here... if so, verify
		
		var editor = component.up("transactioneditor");
		editor.setTransaction();
	},
	
	"deleteTransaction": function(component){
		var editor = component.up("transactioneditor");
		
	}
});