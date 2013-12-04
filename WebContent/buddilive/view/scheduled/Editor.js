Ext.define('BuddiLive.view.scheduled.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.schedulededitor",
	"requires": [
		"BuddiLive.view.scheduled.panel.MonthlyByDate",
		"BuddiLive.view.scheduled.panel.MonthlyByDayOfWeek",
		"BuddiLive.view.scheduled.panel.Weekly",
		"BuddiLive.view.scheduled.panel.BiWeekly",
		"BuddiLive.view.scheduled.panel.EveryDay",
		"BuddiLive.view.scheduled.panel.EveryXDays",
		"BuddiLive.view.scheduled.panel.EveryWeekday",
		"BuddiLive.view.scheduled.panel.MultipleWeeksEveryMonth",
		"BuddiLive.view.scheduled.panel.MultipleMonthsEveryYear"
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected
		var editor = this;
		
		this.title = (s ? "${translation("EDIT_SCHEDULED_TRANSACTION")?json_string}" : "${translation("ADD_SCHEDULED_TRANSACTION")?json_string}");
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
						"disabled": s != null,
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
								{"text": "${translation("SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH")?json_string}", "value": "SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH"},
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
						"value": (s ? s.start : new Date),
						"disabled": s != null,
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_START_DATE")?json_string}",
						"allowBlank": false
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_END_DATE")?json_string}",
						"type": "datefield",
						"itemId": "endDate",
						"emptyText": "${translation("SCHEDULED_TRANSACTION_END_DATE_EMPTY_TEXT")?json_string}",
						"value": (s ? s.end : null),
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
							{"xtype": "scheduledpanelmonthlybydayofweek", "selected": s},
							{"xtype": "scheduledpanelweekly", "selected": s},
							{"xtype": "scheduledpanelbiweekly", "selected": s},
							{"xtype": "scheduledpaneleveryday", "selected": s},
							{"xtype": "scheduledpaneleveryxdays", "selected": s},
							{"xtype": "scheduledpaneleveryweekday", "selected": s},
							{"xtype": "scheduledpanelmultipleweekseverymonth", "selected": s},
							{"xtype": "scheduledpanelmultiplemonthseveryyear", "selected": s}
						],
						"listeners": {
							"afterrender": function(component){
								//Change the card layout to show the selected item if this is an editor
								if (s != null){
									component.getLayout().setActiveItem(s.repeat);
								}
							}
						}

					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_TRANSACTION")?json_string}",
						"type": "transactioneditor",
						"scheduledTransaction": true,
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_TRANSACTION")?json_string}",
						"transaction": s
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_SCHEDULED_TRANSACTION_MESSAGE")?json_string}",
						"type": "textarea",
						"itemId": "message",
						"value": (s ? s.message : null),
						"fieldLabel": "${translation("SCHEDULED_TRANSACTION_MESSAGE")?json_string}"
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