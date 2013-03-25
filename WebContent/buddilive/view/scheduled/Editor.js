Ext.define('BuddiLive.view.scheduled.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.scheduledtransactioneditor",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected
		var editor = this;
		
		this.title = (s ? "Edit Scheduled Transaction" : "Add Scheduled Transaction");	//TODO i18n
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
						"type": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_NAME")?json_string}",
						"allowBlank": false,
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_NAME")?json_string}",
						"listeners": {
							"afterrender": function(field) {
								field.focus(false, 100);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"type": "combobox",
						"itemId": "repeat",
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_REPEAT")?json_string}",
						"value": (s ? s.repeat : null),
						"url": "buddilive/scheduledtransactions/repeat.json",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_REPEAT")?json_string}",
						"listeners": {
							"change": function(){
								//TODO Change the visible fields below.
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"type": "datefield",
						"itemId": "startDate",
						"value": "",	//TODO Default to today
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_START_DATE")?json_string}",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_START_DATE")?json_string}",
						"allowBlank": false
					},
					{
						"xtype": "selfdocumentingfield",
						"type": "datefield",
						"itemId": "endDate",
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_END_DATE")?json_string}",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_END_DATE")?json_string}"
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
		]
	
		this.callParent(arguments);
	}
});