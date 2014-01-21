Ext.define("BuddiLive.view.login.Login", {
	"extend": 'Ext.form.Panel',
	"alias": "widget.login",
	"config": {
		"scrollable": "vertical",

		"items": [
			{
				"xtype": "fieldset",
				"title": "Buddi Live",
				"items": [
					{ "xtype": "textfield", "name": "identifier", "label": "Identifier" },
					{ "xtype": "passwordfield", "name": "secret", "label": "Password" },
					{ "xtype": "checkboxfield", "name": "remember", "label": "Remember Me" }
				]
			},
			{ "xtype": "label", "itemId": "message", "margin": 10, "html": "&nbsp;" },
			{
				"xtype": "button",
				"ui": "action",
				"text": "Authenticate",
				"itemId": "authenticate",
				"margin": "10"
			}
		]
	}
});
