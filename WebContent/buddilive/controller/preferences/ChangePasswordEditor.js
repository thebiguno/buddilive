Ext.define("BuddiLive.controller.preferences.ChangePasswordEditor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"changepasswordeditor button[itemId='ok']": {"click": this.ok},
			"changepasswordeditor button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"cancel": function(component){
		component.up("changepasswordeditor").close();
	},
	
	"ok": function(component){
		var window = component.up("changepasswordeditor");
		var panel = window.initialConfig.panel;

		var request = {"action": "update"};
		request.newPassword = window.down("passwordfield[itemId='newPassword']").getValue();
		request.currentPassword = window.down("textfield[itemId='currentPassword']").getValue();

		var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": window});
		mask.show();
		
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "data/changepassword",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				mask.hide();
				window.close();
				panel.reload();
			},
			"failure": function(response){
				mask.hide();
				BuddiLive.app.error(response);
			}
		});
	}
});