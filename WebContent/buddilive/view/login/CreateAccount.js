Ext.define('BuddiLive.view.login.CreateAccount', {
	"extend": "Ext.window.Window",
	"alias": "widget.createaccount",
	"requires": [
		"BuddiLive.view.login.ClickLabel",
		"BuddiLive.view.login.PasswordField"
	],
		
	"initComponent": function(){
		this.title = "Buddi Live Create Account";	//TODO i18n
		this.renderTo = "form";
		this.layout = "fit";
		this.modal = true;
		this.width = 500;
		this.items = [
			{
				"items": [
					{
						"xtype": "form",
						"layout": "form",
						"bodyPadding": 5,
						"items": [
							{
								"xtype": "hidden",
								"itemId": "id",
								"hidden": true
							},
							{
								"xtype": "textfield",
								"itemId": "identifier",
								"fieldLabel": "${translation("CREATE_USER_USERNAME")?json_string}",
								"allowBlank": false,
								"emptyText": "user@example.com",
								"vtype": "email"
							},
							{
								"xtype": "passwordfield",
								"itemId": "password",
								"fieldLabel": "${translation("CREATE_USER_PASSWORD")?json_string}",
								"allowBlank": false
							},
							{
								"xtype": "combobox",
								"itemId": "locale",
								"fieldLabel": "${translation("CREATE_USER_LOCALE")?json_string}",
								"editable": false,
								"value": "${locale?json_string}",
								"forceSelection": true,
								"store": new Ext.data.Store({
									"fields": ["text", "value"],
									"data": [
										{"text": "Deutsch", "value": "de"},
										{"text": "English", "value": "en"},
										{"text": "English (US)", "value": "en_US"},
										{"text": "Español", "value": "es"},
										{"text": "Español (Mexico)", "value": "es_MX"},
										{"text": "Francais", "value": "fr"},
										{"text": "Italiano", "value": "it"},
										{"text": "Nederlands", "value": "nl"},	//Dutch
										{"text": "Norsk", "value": "no"},	//Norwegian
										{"text": "Portugues", "value": "pt"},
										{"text": "Portugues (Brasil)", "value": "pt-BR"},
										{"text": "Svenska", "value": "sv"},	//Swedish
										{"text": "ελληνικά", "value": "el"},	//Greek
										{"text": "עִבְרִית", "value": "he"},	//Hebrew
										{"text": "ру́сский", "value": "ru"},	//Russian
										{"text": "српски", "value": "sr"}	//Serbian
									]
								}),
								"queryMode": "local",
								"valueField": "value"
							},
							{
								"xtype": "panel",
								"border": false,
								"layout": "hbox",
								"items": [
									{
										"xtype": "checkbox",
										"itemId": "email",
										"fieldLabel": " ",
										"labelSeparator": "",
										"boxLabel": "${translation("CREATE_USER_REMEMBER_EMAIL")?json_string}"
									},
									{
										"xtype": "clicklabel",
										"flex": 1,
										"text": "${translation("CREATE_USER_WHAT_IS_THIS")?json_string}",
										"listeners": {
											"click": function(){
												Ext.MessageBox.show({
													"title": "${translation("CREATE_USER_REMEMBER_EMAIL")?json_string}",
													"msg": "${translation("CREATE_USER_WHAT_IS_REMEMBER_EMAIL")?json_string}",
													"buttons": Ext.MessageBox.OK
												});
											}
										}
									}
								]
							},
							{
								"xtype": "panel",
								"border": false,
								"layout": "hbox",
								"items": [
									{
										"xtype": "checkbox",
										"itemId": "encrypt",
										"fieldLabel": " ",
										"labelSeparator": "",
										"boxLabel": "${translation("CREATE_USER_ENCRYPT_DATA")?json_string}"
									},
									{
										"xtype": "clicklabel",
										"flex": 1,
										"text": "${translation("CREATE_USER_WHAT_IS_THIS")?json_string}",
										"listeners": {
											"click": function(){
												Ext.MessageBox.show({
													"title": "${translation("CREATE_USER_ENCRYPT_DATA")?json_string}",
													"msg": "${translation("CREATE_USER_WHAT_IS_ENCRYPT_DATA")?json_string}",
													"buttons": Ext.MessageBox.OK
												});
											}
										}
									}
								]
							},
							{
								"xtype": "panel",
								"border": false,
								"layout": "hbox",
								"items": [
									{
										"xtype": "checkbox",
										"itemId": "agree",
										"fieldLabel": " ",
										"labelSeparator": "",
										//"boxLabel": "${translation("CREATE_USER_AGREE")?json_string}",
										"allowBlank": false
									},
									{
										"xtype": "clicklabel",
										"flex": 1,
										"style": "padding-left: 4px; padding-top: 3px;",
										"html": "${translation("CREATE_USER_AGREE")?json_string}",
										"listeners": {
											"click": function(){
												Ext.MessageBox.show({
													"title": "${translation("CREATE_USER_TERMS_AND_CONDITIONS_TITLE")?json_string}",
													"msg": "${translation("CREATE_USER_TERMS_AND_CONDITIONS")?json_string}",
													"buttons": Ext.MessageBox.OK
												});
											}
										}
									}
								]
							},
						
						]
					}
				],
				"buttons": [
					{
						"text": "OK",	//TODO i18n
						"itemId": "ok"
					},
					{
						"text": "Cancel",	//TODO i18n
						"itemId": "cancel"
					}
				]
			}
		]

		this.callParent(arguments);
	}
});