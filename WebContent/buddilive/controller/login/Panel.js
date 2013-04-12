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
		
		var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": panel});
		mask.show();
		var conn = new Ext.data.Connection();
		conn.request({
			"url": ".",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				mask.hide();
				window.location.href += "";
			},
			"failure": function(response){
				mask.hide();
				if (response.status == 401){
					Ext.MessageBox.show({
						"title": "${translation("LOGIN_FAILED_TITLE")?json_string}",
						"msg": "${translation("LOGIN_FAILED_MESSAGE")?json_string}",
						"buttons": Ext.Msg.OK
					});
				}
				else {
					var json = Ext.decode(response.responseText);
					Ext.MessageBox.show({
						"title": json.title,
						"msg": json.msg,
						"buttons": Ext.Msg.OK
					});
				}
			}
		});
	}
});