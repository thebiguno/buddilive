Ext.define('BuddiLive.view.component.CurrencyField', {
	"extend": "Ext.form.NumberField",
	"alias": "widget.currencyfield",

	"decimalSeparator": "${user.decimalSeparator!?json_string}",
	"thousandSeparator": "${user.thousandSeparator!?json_string}",
	"forcePrecision" : false,
	"hideTrigger": true,
	"keyNavEnabled": false,
	"mouseWheelEnabled": false,
	"emptyText": "0${user.decimalSeparator!?json_string}00",
	
	"initComponent": function(){
		Ext.util.Format.decimalSeparator = "${user.decimalSeparator!?json_string}";
		Ext.util.Format.thousandSeparator = "${user.thousandSeparator!?json_string}";

		this.callParent(arguments);
	},

	//Convert a string to a number
	"parseValue": function(value){
		var me = this;
		if (!isNaN(value)) return value;	//If this is already a number, then just return it.
		var parsedValue = parseFloat(String(value).split("${user.currencySymbol!?json_string}").join("").split("${user.thousandSeparator!?json_string}").join("").split("${user.decimalSeparator!?json_string}").join("."));
		return isNaN(parsedValue) ? null : parsedValue;
	},
	
	//Convert a number or number-ish string to a formatted string for display
	"valueToRaw": function(value) {
		var me = this;
		value = me.parseValue(value);
		if (isNaN(value)){
			return "";
		}
		else {
			return Ext.util.Format.number(value, "0,000.00");
		}
	},
	
	"rawToValue": function(raw) {
		var me = this;
		return me.parseValue(raw);
	},
	
	"validate": function(){
		return !isNaN(this.rawToValue(this.getRawValue()));	
	}
});