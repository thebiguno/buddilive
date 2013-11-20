Ext.Loader.setConfig({"disableCaching": false});

Ext.define('BuddiLive.view.login.Login', {
	"extend": "Ext.tab.Panel",
	"alias": "widget.login",
	"requires": [
		"BuddiLive.view.login.ClickLabel",
		"BuddiLive.view.component.PasswordField",
		"BuddiLive.view.component.SelfDocumentingField",
		"BuddiLive.view.component.CurrenciesCombobox",
		"BuddiLive.view.component.LocalesCombobox"
	],

	"title": "Buddi Live Login",
	"renderTo": "form",
	"tabPosition": "bottom",
	"items": [
		{
			"xtype": "panel",
			"defaults": { "border": false, "margin": 10 },
			"title": "Log In",
			"layout": "card",
			"items": [
				{
					"xtype": "form",
					"itemId": "authenticate",
					"defaults": { "anchor": "100%", "allowBlank": false, "xtype": "textfield", "enableKeyEvents": true },
					"items": [
						{ "fieldLabel": "Email", "name": "identifier" },
						{ "fieldLabel": "Password", "inputType": "password", "name": "secret" },
						{ "xtype": "label", "itemId": "message", "text": "\xA0" }
					],
					"buttons": [
						{ "text": "Log In", "itemId": "authenticate" }
					]
				}
			]
		},
		{
			"xtype": "panel",
			"defaults": { "border": false, "margin": 10 },
			"title": "Create Account",
			"layout": "card",
			"items": [
				{
					"xtype": "form",
					"itemId": "enrole",
					"defaults": { "anchor": "100%", "allowBlank": false, "xtype": "textfield", "enableKeyEvents": true },
					"items": [
						{ "fieldLabel": "Email", "name": "identifier", "vtype": "email" },
						{
							"xtype": "selfdocumentingfield",
							"messageBody": "${translation("HELP_LOCALE")?json_string}",
							"type": "localescombobox",
							"name": "locale",
							"fieldLabel": "${translation("LOCALE")?json_string}",
							"value": "en_US"
						},
						{
							"xtype": "selfdocumentingfield",
							"messageBody": "${translation("HELP_CURRENCY")?json_string}",
							"type": "currenciescombobox",
							"name": "currency",
							"fieldLabel": "${translation("CURRENCY")?json_string}",
							"value": "USD"
						},
/*
						{
							"xtype": "selfdocumentingfield",
							"messageBody": "${translation("HELP_STORE_EMAIL")?json_string}",
							"type": "checkbox",
							"name": "email",
							"fieldLabel": " ",
							"labelSeparator": "",
							"boxLabel": "${translation("STORE_EMAIL")?json_string}"
						},
						{
							"xtype": "selfdocumentingfield",
							"messageBody": "${translation("HELP_ENCRYPT_DATA")?json_string}",
							"type": "checkbox",
							"name": "encrypt",
							"fieldLabel": " ",
							"labelSeparator": "",
							"boxLabel": "${translation("ENCRYPT_DATA")?json_string}"
						},
*/
						{
							"xtype": "selfdocumentingfield",
							"messageBody": "${translation("CREATE_USER_AGREEMENT_REQUIRED")?json_string}",
							"type": "checkbox",
							"boxLabel": "${translation("AGREE_TERMS_AND_CONDITIONS")?json_string}",
							"name": "agree",
							"fieldLabel": " ",
							"labelSeparator": "",
							"allowBlank": false
						},
						{ "xtype": "label", "itemId": "message", "text": "\xA0" }
						
					],
					"buttons": [
						{ "text": "I have a key", "itemId": "forward" },
						"->",
						{ "text": "Generate a key", "itemId": "enrole" }
					]
				},
				{
					"xtype": "form",
					"itemId": "activate",
					"defaults": { "anchor": "100%", "allowBlank": false, "xtype": "textfield", "enableKeyEvents": true },
					"items": [
						{ "fieldLabel": "Activation Key", "name": "identifier" },
						{ "fieldLabel": "New Password", "name": "secret", "xtype": "passwordfield" },
						{ "xtype": "label", "itemId": "message", "text": "\xA0" }
					],
					"buttons": [
						{ "text": "< Back", "itemId": "back" },
						{ "text": "Create Account", "itemId": "activate" }
					]
				}
			]
		},
		{
			"xtype": "panel",
			"defaults": { "border": false, "margin": 10 },
			"title": "Reset Password",
			"layout": "card",
			"items": [
				{
					"xtype": "form",
					"itemId": "enrole",
					"defaults": { "anchor": "100%", "allowBlank": false, "xtype": "textfield", "enableKeyEvents": true },
					"items": [
						{ 
							"xtype": "selfdocumentingfield",
							"messageBody": "Password reset is unavailable for encrypted accounts.",
							"type": "textfield", 
							"fieldLabel": "Email", 
							"name": "identifier" 
						},
						{ "xtype": "label", "itemId": "message", "text": "\xA0" }
					],
					"buttons": [
						{ "text": "I have a key", "itemId": "forward" },
						"->",
						{ "text": "Generate a key", "itemId": "reset" }
					]
				},
				{
					"xtype": "form",
					"itemId": "activate",
					"defaults": { "anchor": "100%", "allowBlank": false, "xtype": "textfield", "enableKeyEvents": true },
					"items": [
						{ "fieldLabel": "Activation Key", "name": "identifier" },
						{ "fieldLabel": "New Password", "name": "secret", "inputType": "password" },
						{ "fieldLabel": "Password", "name": "verify", "inputType": "password" },
						{ "xtype": "label", "itemId": "message", "text": "\xA0" }
					],
					"buttons": [
						{ "text": "< Back", "itemId": "back" },
						{ "text": "Activate", "itemId": "activate" }
					]
				}
			]
		}
	]
});
