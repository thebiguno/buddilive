Ext.define("BuddiLive.view.scheduled.panel.MonthlyByDayOfWeek", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpanelmonthlybydayofweek",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_MONTHLY_BY_DAY_OF_WEEK")?json_string}",
				"type": "combobox",
				"fieldLabel": "${translation("REPEATING_MONTHLY")?json_string}",
				"value": (s ? s.scheduleDay : "0"),
				"displayField": "text",
				"valueField": "value",
				"allowBlank": false,
				"forceSelection": true,
				"store": new Ext.data.Store({
					"fields": ["text", "value"],
					"data": [
						{"text": "${translation("SCHEDULE_DAY_FIRST_SUNDAY")?json_string}", "value": "0"},
						{"text": "${translation("SCHEDULE_DAY_FIRST_MONDAY")?json_string}", "value": "1"},
						{"text": "${translation("SCHEDULE_DAY_FIRST_TUESDAY")?json_string}", "value": "2"},
						{"text": "${translation("SCHEDULE_DAY_FIRST_WEDNESDAY")?json_string}", "value": "3"},
						{"text": "${translation("SCHEDULE_DAY_FIRST_THURSDAY")?json_string}", "value": "4"},
						{"text": "${translation("SCHEDULE_DAY_FIRST_FRIDAY")?json_string}", "value": "5"},
						{"text": "${translation("SCHEDULE_DAY_FIRST_SATURDAY")?json_string}", "value": "6"}
					]
				}),
				"queryMode": "local",
				"valueField": "value"
			}
		];
	
		this.callParent(arguments);
	},
	
	"getScheduleDay": function(){
		return this.down("combobox").getValue();
	},
	"getScheduleWeek": function(){
		return 0;
	},
	"getScheduleMonth": function(){
		return 0;
	}
});