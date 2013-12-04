Ext.define('BuddiLive.view.account.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.accounteditor",
	"requires": [
		"BuddiLive.view.component.CurrencyField"
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected

		this.title = (s ? "${translation("EDIT_ACCOUNT")?json_string}" : "${translation("ADD_ACCOUNT")?json_string}");
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
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_ACCOUNT_EDITOR_NAME")?json_string}",
						"type": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_NAME")?json_string}",
						"allowBlank": false,
						"enableKeyEvents": true,
						"emptyText": "${translation("ACCOUNT_EDITOR_NAME_EXAMPLES")?json_string}",
						"listeners": {
							"afterrender": function(field) {
								field.focus(false, 500);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_ACCOUNT_EDITOR_ACCOUNT_TYPE")?json_string}",
						"type": "textfield",
						"itemId": "accountType",
						"value": (s ? s.accountType : null),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_ACCOUNT_TYPE")?json_string}",
						"allowBlank": false,
						"enableKeyEvents": true,
						"emptyText": "${translation("ACCOUNT_EDITOR_ACCOUNT_TYPE_EXAMPLES")?json_string}"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_ACCOUNT_EDITOR_TYPE")?json_string}",
						"type": "combobox",
						"itemId": "type",
						"value": (s ? s.type : "D"),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_TYPE")?json_string}",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("DEBIT")?json_string}", "value": "D"},
								{"text": "${translation("CREDIT")?json_string}", "value": "C"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_ACCOUNT_EDITOR_STARTING_BALANCE")?json_string}",
						"type": "currencyfield",
						"itemId": "startBalance",
						"value": (s ? s.startBalance : null),
						"fieldLabel": "${translation("ACCOUNT_EDITOR_STARTING_BALANCE")?json_string}"
					}
				
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("OK")?json_string}",
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "${translation("CANCEL")?json_string}",
				"itemId": "cancel"
			}
		];
	
		this.callParent(arguments);
	}
});