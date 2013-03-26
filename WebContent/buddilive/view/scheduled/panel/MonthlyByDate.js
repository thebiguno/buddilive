Ext.define("BuddiLive.view.scheduled.panel.MonthlyByDate", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpanelmonthlybydate",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_MONTHLY_BY_DATE";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_MONTHLY")?json_string}",
				"type": "combobox",
				"fieldLabel": "${translation("REPEATING_MONTHLY")?json_string}",
				"value": (s ? s.scheduleDay : "1"),
				"displayField": "text",
				"valueField": "value",
				"allowBlank": false,
				"forceSelection": true,
				"store": new Ext.data.Store({
					"fields": ["text", "value"],
					"data": [
						{"text": "${translation("SCHEDULE_DATE_FIRST")?json_string}", "value": "1"},
						{"text": "${translation("SCHEDULE_DATE_SECOND")?json_string}", "value": "2"},
						{"text": "${translation("SCHEDULE_DATE_THIRD")?json_string}", "value": "3"},
						{"text": "${translation("SCHEDULE_DATE_FOURTH")?json_string}", "value": "4"},
						{"text": "${translation("SCHEDULE_DATE_FIFTH")?json_string}", "value": "5"},
						{"text": "${translation("SCHEDULE_DATE_SIXTH")?json_string}", "value": "6"},
						{"text": "${translation("SCHEDULE_DATE_SEVENTH")?json_string}", "value": "7"},
						{"text": "${translation("SCHEDULE_DATE_EIGHTH")?json_string}", "value": "8"},
						{"text": "${translation("SCHEDULE_DATE_NINETH")?json_string}", "value": "9"},
						{"text": "${translation("SCHEDULE_DATE_TENTH")?json_string}", "value": "10"},
						{"text": "${translation("SCHEDULE_DATE_ELEVENTH")?json_string}", "value": "11"},
						{"text": "${translation("SCHEDULE_DATE_TWELFTH")?json_string}", "value": "12"},
						{"text": "${translation("SCHEDULE_DATE_THIRTEENTH")?json_string}", "value": "13"},
						{"text": "${translation("SCHEDULE_DATE_FOURTEENTH")?json_string}", "value": "14"},
						{"text": "${translation("SCHEDULE_DATE_FIFTEENTH")?json_string}", "value": "15"},
						{"text": "${translation("SCHEDULE_DATE_SIXTEENTH")?json_string}", "value": "16"},
						{"text": "${translation("SCHEDULE_DATE_SEVENTEENTH")?json_string}", "value": "17"},
						{"text": "${translation("SCHEDULE_DATE_EIGHTEENTH")?json_string}", "value": "18"},
						{"text": "${translation("SCHEDULE_DATE_NINETEENTH")?json_string}", "value": "19"},
						{"text": "${translation("SCHEDULE_DATE_TWENTIETH")?json_string}", "value": "20"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYFIRST")?json_string}", "value": "21"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYSECOND")?json_string}", "value": "22"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYTHIRD")?json_string}", "value": "23"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYFOURTH")?json_string}", "value": "24"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYFIFTH")?json_string}", "value": "25"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYSIXTH")?json_string}", "value": "26"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYSEVENTH")?json_string}", "value": "27"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYEIGHTH")?json_string}", "value": "28"},
						{"text": "${translation("SCHEDULE_DATE_TWENTYNINETH")?json_string}", "value": "29"},
						{"text": "${translation("SCHEDULE_DATE_THIRTIETH")?json_string}", "value": "30"},
						{"text": "${translation("SCHEDULE_DATE_THIRTYFIRST")?json_string}", "value": "31"},
						{"text": "${translation("SCHEDULE_DATE_LAST_DAY")?json_string}", "value": "32"}
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