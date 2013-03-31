Ext.define("BuddiLive.view.scheduled.panel.BiWeekly", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpanelbiweekly",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_BIWEEKLY";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_BIWEEKLY")?json_string}",
				"type": "combobox",
				"fieldLabel": "${translation("REPEATING_BIWEEKLY")?json_string}",
				"value": (s ? s.scheduleDay : 0),
				"displayField": "text",
				"valueField": "value",
				"allowBlank": false,
				"forceSelection": true,
				"store": new Ext.data.Store({
					"fields": ["text", "value"],
					"data": [
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_SUNDAY")?json_string}", "value": 0},
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_MONDAY")?json_string}", "value": 1},
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_TUESDAY")?json_string}", "value": 2},
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_WEDNESDAY")?json_string}", "value": 3},
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_THURSDAY")?json_string}", "value": 4},
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_FRIDAY")?json_string}", "value": 5},
						{"text": "${translation("SCHEDULE_DAY_EVERY_OTHER_SATURDAY")?json_string}", "value": 6}
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