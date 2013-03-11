Ext.define("BuddiLive.controller.login.Panel", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"loginpanel button[itemId='createAccount']": {"click": this.createAccount},
			"loginpanel button[itemId='login']": {"click": this.login}
		});
	},
	
	"createAccount": function(component){
		Ext.widget("createaccount").show();
	},
	
	"login": function(button){
		var panel = button.up("loginpanel");
		var request = {"action": "login"};
		request.identifier = panel.down("textfield[itemId='identifier']").getValue();
		request.credentials = panel.down("textfield[itemId='credentials']").getValue();

		var conn = new Ext.data.Connection();
		conn.request({
			"url": ".",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				window.location.href += "";
			},
			"failure": function(response){
				alert("failure");
			}
		});
	}
});