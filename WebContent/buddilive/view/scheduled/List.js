Ext.define("BuddiLive.view.scheduled.List", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledlist",
	"requires": [
		"BuddiLive.store.scheduled.ListStore",
		"BuddiLive.view.scheduled.Editor"
	],
	
	"title": "${translation("SCHEDULED_TRANSACTIONS")?json_string}",
	"layout": "fit",
	"closable": true,
	"initComponent": function(){
		var d = this.initialConfig.data

		this.items = [
			{
				"xtype": "grid",
				"itemId": "scheduledTransactions",
				"store": Ext.create("BuddiLive.store.scheduled.ListStore"),
				"columns": [
					{
						"text": "${translation("SCHEDULED_TRANSACTION_NAME")?json_string}",
						"dataIndex": "name",
						"flex": 1
					},
					{
						"text": "${translation("SCHEDULED_TRANSACTION_REPEAT")?json_string}",
						"dataIndex": "repeat",
						"flex": 2,
						"renderer": function(value, metadata, record){
							var frequencyLookup = {
								"SCHEDULE_FREQUENCY_MONTHLY_BY_DATE": "${translation("SCHEDULE_FREQUENCY_MONTHLY_BY_DATE")?json_string}",
								"SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK": "${translation("SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK")?json_string}",
								"SCHEDULE_FREQUENCY_WEEKLY": "${translation("SCHEDULE_FREQUENCY_WEEKLY")?json_string}",
								"SCHEDULE_FREQUENCY_BIWEEKLY": "${translation("SCHEDULE_FREQUENCY_BIWEEKLY")?json_string}",
								"SCHEDULE_FREQUENCY_EVERY_DAY": "${translation("SCHEDULE_FREQUENCY_EVERY_DAY")?json_string}",
								"SCHEDULE_FREQUENCY_EVERY_X_DAYS": "${translation("SCHEDULE_FREQUENCY_EVERY_X_DAYS")?json_string}",
								"SCHEDULE_FREQUENCY_EVERY_WEEKDAY": "${translation("SCHEDULE_FREQUENCY_EVERY_WEEKDAY")?json_string}",
								"SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH": "${translation("SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH")?json_string}",
								"SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR": "${translation("SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR")?json_string}"
							};

							var result = frequencyLookup[value] || value;
							if (value == "SCHEDULE_FREQUENCY_MONTHLY_BY_DATE"){
								var monthlyByDateLookup = {
									1: "${translation("SCHEDULE_DATE_FIRST")?json_string}",
									2: "${translation("SCHEDULE_DATE_SECOND")?json_string}",
									3: "${translation("SCHEDULE_DATE_THIRD")?json_string}",
									4: "${translation("SCHEDULE_DATE_FOURTH")?json_string}",
									5: "${translation("SCHEDULE_DATE_FIFTH")?json_string}",
									6: "${translation("SCHEDULE_DATE_SIXTH")?json_string}",
									7: "${translation("SCHEDULE_DATE_SEVENTH")?json_string}",
									8: "${translation("SCHEDULE_DATE_EIGHTH")?json_string}",
									9: "${translation("SCHEDULE_DATE_NINETH")?json_string}",
									10: "${translation("SCHEDULE_DATE_TENTH")?json_string}",
									11: "${translation("SCHEDULE_DATE_ELEVENTH")?json_string}",
									12: "${translation("SCHEDULE_DATE_TWELFTH")?json_string}",
									13: "${translation("SCHEDULE_DATE_THIRTEENTH")?json_string}",
									14: "${translation("SCHEDULE_DATE_FOURTEENTH")?json_string}",
									15: "${translation("SCHEDULE_DATE_FIFTEENTH")?json_string}",
									16: "${translation("SCHEDULE_DATE_SIXTEENTH")?json_string}",
									17: "${translation("SCHEDULE_DATE_SEVENTEENTH")?json_string}",
									18: "${translation("SCHEDULE_DATE_EIGHTEENTH")?json_string}",
									19: "${translation("SCHEDULE_DATE_NINETEENTH")?json_string}",
									20: "${translation("SCHEDULE_DATE_TWENTIETH")?json_string}",
									21: "${translation("SCHEDULE_DATE_TWENTYFIRST")?json_string}",
									22: "${translation("SCHEDULE_DATE_TWENTYSECOND")?json_string}",
									23: "${translation("SCHEDULE_DATE_TWENTYTHIRD")?json_string}",
									24: "${translation("SCHEDULE_DATE_TWENTYFOURTH")?json_string}",
									25: "${translation("SCHEDULE_DATE_TWENTYFIFTH")?json_string}",
									26: "${translation("SCHEDULE_DATE_TWENTYSIXTH")?json_string}",
									27: "${translation("SCHEDULE_DATE_TWENTYSEVENTH")?json_string}",
									28: "${translation("SCHEDULE_DATE_TWENTYEIGHTH")?json_string}",
									29: "${translation("SCHEDULE_DATE_TWENTYNINETH")?json_string}",
									30: "${translation("SCHEDULE_DATE_THIRTIETH")?json_string}",
									31: "${translation("SCHEDULE_DATE_THIRTYFIRST")?json_string}",
									32: "${translation("SCHEDULE_DATE_LAST_DAY")?json_string}"
								};
								result += " " + monthlyByDateLookup[record.get("scheduleDay")];
							}
							else if (value == "SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK"){
								var monthlyByDayOfWeekLookup = {
									0: "${translation("SCHEDULE_DAY_FIRST_SUNDAY")?json_string}",
									1: "${translation("SCHEDULE_DAY_FIRST_MONDAY")?json_string}",
									2: "${translation("SCHEDULE_DAY_FIRST_TUESDAY")?json_string}",
									3: "${translation("SCHEDULE_DAY_FIRST_WEDNESDAY")?json_string}",
									4: "${translation("SCHEDULE_DAY_FIRST_THURSDAY")?json_string}",
									5: "${translation("SCHEDULE_DAY_FIRST_FRIDAY")?json_string}",
									6: "${translation("SCHEDULE_DAY_FIRST_SATURDAY")?json_string}"
								};
								result += " " + monthlyByDayOfWeekLookup[record.get("scheduleDay")];
							}
							else if (value == "SCHEDULE_FREQUENCY_WEEKLY"){
								var weekLookup = {
									0: "${translation("SCHEDULE_DAY_SUNDAY")?json_string}",
									1: "${translation("SCHEDULE_DAY_MONDAY")?json_string}",
									2: "${translation("SCHEDULE_DAY_TUESDAY")?json_string}",
									3: "${translation("SCHEDULE_DAY_WEDNESDAY")?json_string}",
									4: "${translation("SCHEDULE_DAY_THURSDAY")?json_string}",
									5: "${translation("SCHEDULE_DAY_FRIDAY")?json_string}",
									6: "${translation("SCHEDULE_DAY_SATURDAY")?json_string}"
								};
								result += " " + weekLookup[record.get("scheduleDay")];
							}
							else if (value == "SCHEDULE_FREQUENCY_BIWEEKLY"){
								var biWeeklyLookup = {
									0: "${translation("SCHEDULE_DAY_EVERY_OTHER_SUNDAY")?json_string}",
									1: "${translation("SCHEDULE_DAY_EVERY_OTHER_MONDAY")?json_string}",
									2: "${translation("SCHEDULE_DAY_EVERY_OTHER_TUESDAY")?json_string}",
									3: "${translation("SCHEDULE_DAY_EVERY_OTHER_WEDNESDAY")?json_string}",
									4: "${translation("SCHEDULE_DAY_EVERY_OTHER_THURSDAY")?json_string}",
									5: "${translation("SCHEDULE_DAY_EVERY_OTHER_FRIDAY")?json_string}",
									6: "${translation("SCHEDULE_DAY_EVERY_OTHER_SATURDAY")?json_string}"
								};
								result += " " + biWeeklyLookup[record.get("scheduleDay")];
							}
							else if (value == "SCHEDULE_FREQUENCY_EVERY_DAY"){
								//Nothing to do here - no configuration
							}
							else if (value == "SCHEDULE_FREQUENCY_EVERY_X_DAYS"){
								result = "${translation("REPEATING_EVERY_X_DAYS")?json_string} " + record.get("scheduleDay") + " ${translation("DAYS")?json_string}";
							}
							else if (value == "SCHEDULE_FREQUENCY_EVERY_WEEKDAY"){
								//Nothing to do here - no configuration
							}
							else if (value == "SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH"){
							
							}
							else if (value == "SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR"){
								var multipleMonthsEveryYearLookup = {
									1: "${translation("SCHEDULE_DATE_MONTHS_FIRST")?json_string}",
									2: "${translation("SCHEDULE_DATE_MONTHS_SECOND")?json_string}",
									3: "${translation("SCHEDULE_DATE_MONTHS_THIRD")?json_string}",
									4: "${translation("SCHEDULE_DATE_MONTHS_FOURTH")?json_string}",
									5: "${translation("SCHEDULE_DATE_MONTHS_FIFTH")?json_string}",
									6: "${translation("SCHEDULE_DATE_MONTHS_SIXTH")?json_string}",
									7: "${translation("SCHEDULE_DATE_MONTHS_SEVENTH")?json_string}",
									8: "${translation("SCHEDULE_DATE_MONTHS_EIGHTH")?json_string}",
									9: "${translation("SCHEDULE_DATE_MONTHS_NINETH")?json_string}",
									10: "${translation("SCHEDULE_DATE_MONTHS_TENTH")?json_string}",
									11: "${translation("SCHEDULE_DATE_MONTHS_ELEVENTH")?json_string}",
									12: "${translation("SCHEDULE_DATE_MONTHS_TWELFTH")?json_string}",
									13: "${translation("SCHEDULE_DATE_MONTHS_THIRTEENTH")?json_string}",
									14: "${translation("SCHEDULE_DATE_MONTHS_FOURTEENTH")?json_string}",
									15: "${translation("SCHEDULE_DATE_MONTHS_FIFTEENTH")?json_string}",
									16: "${translation("SCHEDULE_DATE_MONTHS_SIXTEENTH")?json_string}",
									17: "${translation("SCHEDULE_DATE_MONTHS_SEVENTEENTH")?json_string}",
									18: "${translation("SCHEDULE_DATE_MONTHS_EIGHTEENTH")?json_string}",
									19: "${translation("SCHEDULE_DATE_MONTHS_NINETEENTH")?json_string}",
									20: "${translation("SCHEDULE_DATE_MONTHS_TWENTIETH")?json_string}",
									21: "${translation("SCHEDULE_DATE_MONTHS_TWENTYFIRST")?json_string}",
									22: "${translation("SCHEDULE_DATE_MONTHS_TWENTYSECOND")?json_string}",
									23: "${translation("SCHEDULE_DATE_MONTHS_TWENTYTHIRD")?json_string}",
									24: "${translation("SCHEDULE_DATE_MONTHS_TWENTYFOURTH")?json_string}",
									25: "${translation("SCHEDULE_DATE_MONTHS_TWENTYFIFTH")?json_string}",
									26: "${translation("SCHEDULE_DATE_MONTHS_TWENTYSIXTH")?json_string}",
									27: "${translation("SCHEDULE_DATE_MONTHS_TWENTYSEVENTH")?json_string}",
									28: "${translation("SCHEDULE_DATE_MONTHS_TWENTYEIGHTH")?json_string}",
									29: "${translation("SCHEDULE_DATE_MONTHS_TWENTYNINETH")?json_string}",
									30: "${translation("SCHEDULE_DATE_MONTHS_THIRTIETH")?json_string}",
									31: "${translation("SCHEDULE_DATE_MONTHS_THIRTYFIRST")?json_string}",
									32: "${translation("SCHEDULE_DATE_MONTHS_LAST_DAY")?json_string}"
								};
								result += " " + multipleMonthsEveryYearLookup[record.get("scheduleDay")] + " (";
								var month = record.get("scheduleMonth");
								if (month & 1){
									result += "${translation("MONTH_JANUARY")?json_string}, ";
								}
								if (month & 2){
									result += "${translation("MONTH_FEBRUARY")?json_string}, ";
								}
								if (month & 4){
									result += "${translation("MONTH_MARCH")?json_string}, ";
								}
								if (month & 8){
									result += "${translation("MONTH_APRIL")?json_string}, ";
								}
								if (month & 16){
									result += "${translation("MONTH_MAY")?json_string}, ";
								}
								if (month & 32){
									result += "${translation("MONTH_JUNE")?json_string}, ";
								}
								if (month & 64){
									result += "${translation("MONTH_JULY")?json_string}, ";
								}
								if (month & 128){
									result += "${translation("MONTH_AUGUST")?json_string}, ";
								}
								if (month & 256){
									result += "${translation("MONTH_SEPTEMBER")?json_string}, ";
								}
								if (month & 512){
									result += "${translation("MONTH_OCTOBER")?json_string}, ";
								}
								if (month & 1024){
									result += "${translation("MONTH_NOVEMBER")?json_string}, ";
								}
								if (month & 2048){
									result += "${translation("MONTH_DECEMBER")?json_string}, ";
								}
								result = result.slice(0, result.length - 2);	//Remove the trailing comma and space
								result += ")";
							}

							return result;
						}
					},
					{
						"text": "${translation("SCHEDULED_TRANSACTION_LAST_TRIGGERED_DATE")?json_string}",
						"dataIndex": "lastCreatedDate",
						"width": 120
					},
					{
						"text": "${translation("SCHEDULED_TRANSACTION_END_DATE")?json_string}",
						"dataIndex": "end",
						"width": 120
					},
					{
						"text": "${translation("AMOUNT")?json_string}",
						"dataIndex": "splits",
						"width": 100,
						"renderer": function(value, metadata, record){
							var result = "";
							for (var i = 0; i < value.length; i++){
								if (i > 0){
									result += "<br/>";
								}
								result += value[i].amount;
							}
							return result;
						}
					},
					{
						"text": "${translation("SCHEDULED_TRANSACTION_MESSAGE")?json_string}",
						"dataIndex": "message",
						"flex": 2
					}
				]
			}
		];
		this.dockedItems = BuddiLive.app.viewport.getDockedItems("scheduled")
	
		this.callParent(arguments);
	}
});