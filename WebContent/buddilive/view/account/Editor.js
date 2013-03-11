Ext.define('BuddiLive.view.account.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.accounteditor",
	"requires": [
		
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected

		this.title = (s ? "Edit Account" : "Add Account");	//TODO i18n
		this.layout = "fit";
		this.modal = true;
		this.width = 400;
		this.items = [
			{
				"xtype": "form",
				"layout": "form",
				"bodyPadding": 5,
				"items": [
					{
						"xtype": "hidden",
						"itemId": "id",
						"value": (s ? s.id : null)
					},
					{
						"xtype": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_NAME")?json_string}",
						"allowBlank": false,
						"emptyText": "TD Savings, Visa Gold, Cash in Wallet, etc"	//TODO i18n
					},
					{
						"xtype": "textfield",
						"itemId": "accountType",
						"value": (s ? s.accountType : null),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_TYPE")?json_string}",
						"allowBlank": false,
						"emptyText": "Credit Card, Savings, etc"	//TODO i18n
					},
					{
						"xtype": "combobox",
						"itemId": "type",
						"value": (s ? s.type : "D"),
						"fieldLabel": "Type",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "Debit", "value": "D"},	//TODO i18n
								{"text": "Credit", "value": "C"}	//TODO i18n
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "numberfield",
						"itemId": "startBalance",
						"value": (s ? s.startBalance : null),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_STARTING_BALANCE")?json_string}",
						"hideTrigger": true,
						"keyNavEnabled": false,
						"mouseWheelEnabled": false,
						"emptyText": "0.00"
					}
				
				]
			}
		];
		this.buttons = [
			{
				"text": "OK",	//TODO i18n
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "Cancel",	//TODO i18n
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});