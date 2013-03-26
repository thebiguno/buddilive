Ext.define('BuddiLive.view.scheduled.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.schedulededitor",
	"requires": [
		"BuddiLive.view.scheduled.panel.MonthlyByDate",
		"BuddiLive.view.scheduled.panel.MonthlyByDayOfWeek"
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected
		var editor = this;
		
		this.title = (s ? "Edit Scheduled Transaction" : "Add Scheduled Transaction");	//TODO i18n
		this.layout = "fit";
		this.modal = true;
		this.width = 750;
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
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_NAME")?json_string}",
						"type": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_NAME")?json_string}",
						"allowBlank": false,
						"listeners": {
							"afterrender": function(field) {
								field.focus(false, 500);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_REPEAT")?json_string}",
						"type": "combobox",
						"itemId": "repeat",
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_REPEAT")?json_string}",
						"value": (s ? s.repeat : "SCHEDULE_FREQUENCY_MONTHLY_BY_DATE"),
						"displayField": "text",
						"valueField": "value",
						"allowBlank": false,
						"editable": false,
						"forceSelection": true,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("SCHEDULE_FREQUENCY_MONTHLY_BY_DATE")?json_string}", "value": "SCHEDULE_FREQUENCY_MONTHLY_BY_DATE"},
								{"text": "${translation("SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK")?json_string}", "value": "SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK"},
								{"text": "${translation("SCHEDULE_FREQUENCY_WEEKLY")?json_string}", "value": "SCHEDULE_FREQUENCY_WEEKLY"},
								{"text": "${translation("SCHEDULE_FREQUENCY_BIWEEKLY")?json_string}", "value": "SCHEDULE_FREQUENCY_BIWEEKLY"},
								{"text": "${translation("SCHEDULE_FREQUENCY_EVERY_DAY")?json_string}", "value": "SCHEDULE_FREQUENCY_EVERY_DAY"},
								{"text": "${translation("SCHEDULE_FREQUENCY_EVERY_X_DAYS")?json_string}", "value": "SCHEDULE_FREQUENCY_EVERY_X_DAYS"},
								{"text": "${translation("SCHEDULE_FREQUENCY_EVERY_WEEKDAY")?json_string}", "value": "SCHEDULE_FREQUENCY_EVERY_WEEKDAY"},
								{"text": "${translation("SCHEDULE_FREQUENCY_EVERY_WEEKDAY")?json_string}", "value": "SCHEDULE_FREQUENCY_EVERY_WEEKDAY"},
								{"text": "${translation("SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR")?json_string}", "value": "SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR"}
							]
						}),
						"queryMode": "local",
						"valueField": "value",
						"listeners": {
							"change": function(component){
								//Change the card layout to show the new item
								component.up("form").down("panel[itemId='cardLayoutPanel']").getLayout().setActiveItem(component.getValue());
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_START_DATE")?json_string}",
						"type": "datefield",
						"itemId": "startDate",
						"value": "",	//TODO Default to today
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_START_DATE")?json_string}",
						"allowBlank": false
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_END_DATE")?json_string}",
						"type": "datefield",
						"itemId": "endDate",
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_END_DATE")?json_string}"
					},
					{
						"xtype": "panel",
						"itemId": "cardLayoutPanel",
						"layout": "card",
						"border": false,
						"padding": 0,
						"items": [
							{"xtype": "scheduledpanelmonthlybydate", "selected": s},
							{"xtype": "scheduledpanelmonthlybydayofweek", "selected": s}
						]
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_TRANSACTION")?json_string}",
						"type": "transactioneditor",
						"scheduledTransaction": true,
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_TRANSACTION")?json_string}"
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