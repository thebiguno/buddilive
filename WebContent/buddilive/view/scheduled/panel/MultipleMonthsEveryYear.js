Ext.define("BuddiLive.view.scheduled.panel.MultipleMonthsEveryYear", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpanelmultiplemonthseveryyear",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_MULTIPLE_MONTHS_EVERY_YEAR")?json_string}",
				"type": "combobox",
				"fieldLabel": "${translation("REPEATING_MONTHLY")?json_string}",
				"value": (s ? s.scheduleDay : 1),
				"displayField": "text",
				"valueField": "value",
				"allowBlank": false,
				"forceSelection": true,
				"store": new Ext.data.Store({
					"fields": ["text", "value"],
					"data": [
						{"text": "${translation("SCHEDULE_DATE_MONTHS_FIRST")?json_string}", "value": 1},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_SECOND")?json_string}", "value": 2},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_THIRD")?json_string}", "value": 3},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_FOURTH")?json_string}", "value": 4},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_FIFTH")?json_string}", "value": 5},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_SIXTH")?json_string}", "value": 6},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_SEVENTH")?json_string}", "value": 7},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_EIGHTH")?json_string}", "value": 8},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_NINETH")?json_string}", "value": 9},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TENTH")?json_string}", "value": 10},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_ELEVENTH")?json_string}", "value": 11},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWELFTH")?json_string}", "value": 12},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_THIRTEENTH")?json_string}", "value": 13},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_FOURTEENTH")?json_string}", "value": 14},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_FIFTEENTH")?json_string}", "value": 15},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_SIXTEENTH")?json_string}", "value": 16},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_SEVENTEENTH")?json_string}", "value": 17},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_EIGHTEENTH")?json_string}", "value": 18},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_NINETEENTH")?json_string}", "value": 19},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTIETH")?json_string}", "value": 20},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYFIRST")?json_string}", "value": 21},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYSECOND")?json_string}", "value": 22},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYTHIRD")?json_string}", "value": 23},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYFOURTH")?json_string}", "value": 24},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYFIFTH")?json_string}", "value": 25},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYSIXTH")?json_string}", "value": 26},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYSEVENTH")?json_string}", "value": 27},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYEIGHTH")?json_string}", "value": 28},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_TWENTYNINETH")?json_string}", "value": 29},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_THIRTIETH")?json_string}", "value": 30},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_THIRTYFIRST")?json_string}", "value": 31},
						{"text": "${translation("SCHEDULE_DATE_MONTHS_LAST_DAY")?json_string}", "value": 32}
					]
				}),
				"queryMode": "local",
				"valueField": "value"
			},
			{
				"xtype": "fieldcontainer",
				"fieldLabel": " ",
				"labelSeparator": "",
				"layout": "hbox",
				"border": false,
				"items": [
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_JANUARY")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 1 : false),
						"itemId": "MONTH_JANUARY"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_FEBRUARY")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 2 : false),
						"itemId": "MONTH_FEBRUARY"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_MARCH")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 4 : false),
						"itemId": "MONTH_MARCH"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_APRIL")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 8 : false),
						"itemId": "MONTH_APRIL"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_MAY")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 16 : false),
						"itemId": "MONTH_MAY"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_JUNE")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 32 : false),
						"itemId": "MONTH_JUNE"
					}
				]
			},
			{
				"xtype": "fieldcontainer",
				"fieldLabel": " ",
				"labelSeparator": "",
				"layout": "hbox",
				"border": false,
				"items": [
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_JULY")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 64 : false),
						"itemId": "MONTH_JULY"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_AUGUST")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 128 : false),
						"itemId": "MONTH_AUGUST"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_SEPTEMBER")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 256 : false),
						"itemId": "MONTH_SEPTEMBER"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_OCTOBER")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 512 : false),
						"itemId": "MONTH_OCTOBER"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_NOVEMBER")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 1024 : false),
						"itemId": "MONTH_NOVEMBER"
					},
					{
						"xtype": "checkbox",
						"boxLabel": "${translation("MONTH_DECEMBER")?json_string}",
						"margin": "0 10 0 0",
						"flex": 1,
						"checked": (s ? s.scheduleMonth & 2048 : false),
						"itemId": "MONTH_DECEMBER"
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
		return 0;
	},
	"getScheduleMonth": function(){
		var result = 0;
		if (this.down("checkbox[itemId='MONTH_JANUARY']").getValue()) result += 1;
		if (this.down("checkbox[itemId='MONTH_FEBRUARY']").getValue()) result += 2;
		if (this.down("checkbox[itemId='MONTH_MARCH']").getValue()) result += 4;
		if (this.down("checkbox[itemId='MONTH_APRIL']").getValue()) result += 8;
		if (this.down("checkbox[itemId='MONTH_MAY']").getValue()) result += 16;
		if (this.down("checkbox[itemId='MONTH_JUNE']").getValue()) result += 32;
		if (this.down("checkbox[itemId='MONTH_JULY']").getValue()) result += 64;
		if (this.down("checkbox[itemId='MONTH_AUGUST']").getValue()) result += 128;
		if (this.down("checkbox[itemId='MONTH_SEPTEMBER']").getValue()) result += 256;
		if (this.down("checkbox[itemId='MONTH_OCTOBER']").getValue()) result += 512;
		if (this.down("checkbox[itemId='MONTH_NOVEMBER']").getValue()) result += 1024;
		if (this.down("checkbox[itemId='MONTH_DECEMBER']").getValue()) result += 2048;
		return result;
	}
});