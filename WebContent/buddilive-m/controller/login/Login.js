Ext.define("BuddiLive.controller.login.Login", {
	"extend": "Ext.app.Controller",
	"config": {
		"control": {
			"login button[itemId=authenticate]": {
				"tap": function(button) {
					var form = button.up('formpanel');
					form.submit({
						"url": "index",
						"method": "POST",
						"params": { "action": "login" },
						"success": function() {
							window.location.reload();
						},
						"failure": function(form, result) {
							form.down('label[itemId=message]').setHtml("Invalid Credentials");
						}
					});
				}
			}
		}
	}
});
