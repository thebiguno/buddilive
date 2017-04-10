Ext.define('BuddiLive.view.preferences.ChangePasswordEditor', {
	"extend": "Ext.window.Window",
	"alias": "widget.changepasswordeditor",
	"requires": [
		"Login.view.PasswordField"
	],
	
	"initComponent": function(){
		var d = this.initialConfig.data

		this.title = "${translation("CHANGE_PASSWORD")?json_string}";
		this.layout = "fit";
		this.modal = true;
		this.width = 500;
		this.items = [
			{
				"xtype": "form",
				"layout": "anchor",
				"bodyPadding": 5,
				"items": [
					{
						"xtype": "textfield",
						"itemId": "currentPassword",
						"inputType": "password",
						"fieldLabel": "${translation("CURRENT_PASSWORD")?json_string}"
					},
					{
						"xtype": "passwordfield",
						"itemId": "newPassword",
						"fieldLabel": "${translation("NEW_PASSWORD")?json_string}",
						"identifier": "anonymous"
					}
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("OK")?json_string}",
				"itemId": "ok"
			},
			{
				"text": "${translation("CANCEL")?json_string}",
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});