Ext.define("BuddiLive.view.scheduled.panel.MultipleWeeksEveryMonth", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpanelmultipleweekseverymonth",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_MULTIPLE_WEEKS_EVERY_MONTH")?json_string}",
				"type": "combobox",
				"fieldLabel": "${translation("REPEATING_WEEKLY")?json_string}",
				"value": (s ? s.scheduleDay : 0),
				"displayField": "text",
				"valueField": "value",
				"allowBlank": false,
				"forceSelection": true,
				"store": new Ext.data.Store({
					"fields": ["text", "value"],
					"data": [
						{"text": "${translation("SCHEDULE_DAY_SUNDAY")?json_string}", "value": 0},
						{"text": "${translation("SCHEDULE_DAY_MONDAY")?json_string}", "value": 1},
						{"text": "${translation("SCHEDULE_DAY_TUESDAY")?json_string}", "value": 2},
						{"text": "${translation("SCHEDULE_DAY_WEDNESDAY")?json_string}", "value": 3},
						{"text": "${translation("SCHEDULE_DAY_THURSDAY")?json_string}", "value": 4},
						{"text": "${translation("SCHEDULE_DAY_FRIDAY")?json_string}", "value": 5},
						{"text": "${translation("SCHEDULE_DAY_SATURDAY")?json_string}", "value": 6}
					]
				}),
				"queryMode": "local",
				"valueField": "value"
			},
			{
				"xtype": "fieldcontainer",
				"fieldLabel": "${translation("REPEATING_ON_WEEKS")?json_string}",
				"layout": "hbox",
				"border": false,
				"items": [
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("SCHEDULE_WEEK_FIRST")?json_string}",
						"margin": "0 10 0 0",
						"checked": (s ? s.scheduleWeek & 1 : false),
						"itemId": "SCHEDULE_WEEK_FIRST"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("SCHEDULE_WEEK_SECOND")?json_string}",
						"margin": "0 10 0 0",
						"checked": (s ? s.scheduleWeek & 2 : false),
						"itemId": "SCHEDULE_WEEK_SECOND"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("SCHEDULE_WEEK_THIRD")?json_string}",
						"margin": "0 10 0 0",
						"checked": (s ? s.scheduleWeek & 4 : false),
						"itemId": "SCHEDULE_WEEK_THIRD"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("SCHEDULE_WEEK_FOURTH")?json_string}",
						"margin": "0 25 0 0",
						"checked": (s ? s.scheduleWeek & 8 : false),
						"itemId": "SCHEDULE_WEEK_FOURTH"
					},
					{
						"xtype": "displayfield",
						"value": "${translation("OF_THE_MONTH")?json_string}"
					}

				]
			}
		];
	
		this.callParent(arguments);
	},
	
	"getScheduleDay": function(){
		return this.down("combobox").getValue();
	},
	"getScheduleWeek": function(){
		var result = 0;
		if (this.down("checkbox[itemId='SCHEDULE_WEEK_FIRST']").getValue()) result += 1;
		if (this.down("checkbox[itemId='SCHEDULE_WEEK_SECOND']").getValue()) result += 2;
		if (this.down("checkbox[itemId='SCHEDULE_WEEK_THIRD']").getValue()) result += 4;
		if (this.down("checkbox[itemId='SCHEDULE_WEEK_FOURTH']").getValue()) result += 8;
		return result;
	},
	"getScheduleMonth": function(){
		return 0;
	}
});