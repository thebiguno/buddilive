Ext.override(Ext.form.NumberField, {
	"forcePrecision" : false,

	"valueToRaw": function(value) {
		var me = this, decimalSeparator = me.decimalSeparator;
		value = me.parseValue(value);
		value = me.fixPrecision(value);
		value = Ext.isNumber(value) ? value : parseFloat(String(value).replace(decimalSeparator, '.'));
		if (isNaN(value)){
			value = '';
		}
		else {
			value = me.forcePrecision ? value.toFixed(me.decimalPrecision) : parseFloat(value);
			value = String(value).replace(".", decimalSeparator);
		}
		return value;
	}
});

Ext.define('BuddiLive.view.transaction.split.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.spliteditor",
	
	"requires": [
		"BuddiLive.view.shared.LazyCombobox"
	],
	
	"initComponent": function(){
		var v = this.initialConfig.value ? this.initialConfig.value : {};
		this.layout = "hbox";
		this.border = false;
		this.width = "100%";
		this.defaults = {
			"padding": "0 0 5 5"
		};
		this.items = [
			{
				"xtype": "numberfield",
				"itemId": "amount",
				"flex": 1,
				"forcePrecision": true,
				"hideTrigger": true,
				"keyNavEnabled": false,
				"mouseWheelEnabled": false,
				"emptyText": "0.00 (Amount)",
				"value": v.amount / 100
			},
			{
				"xtype": "lazycombobox",
				"itemId": "from",
				"flex": 1,
				"emptyText": "From",
				"url": "gui/sources/from.json",
				"value": v.fromId
			},
			{"xtype": "panel", "html": "<img style='padding-top: 3px;' src='img/arrow.png'/>", "border": false, "width": 25, "height": 25},
			{
				"xtype": "lazycombobox",
				"itemId": "to",
				"flex": 1,
				"emptyText": "To",
				"url": "gui/sources/to.json",
				"value": v.toId
			},
			{
				"xtype": "textfield",
				"itemId": "memo",
				"flex": 2,
				"emptyText": "Memo",
				"padding": "0 5 0 5",
				"value": v.memo
			},
			{
				"xtype": "button",
				"icon": "img/plus-circle.png",
				"itemId": "addSplit",
				"tooltip": "Add split",
				"padding": "2 5 2 5",
				"margin": "0 5 0 0"
			},
			{
				"xtype": "button",
				"icon": "img/minus-circle.png",
				"itemId": "removeSplit",
				"tooltip": "Remove split",
				"padding": "2 5 2 5",
				"margin": "0 5 0 0",
				"hidden": true
			}
		];
		
		this.callParent(arguments);
	},
	
	"getSplit": function(){
		var s = {};
		s.amount = this.down("numberfield[itemId='amount']").getValue() * 100;
		s.fromSource = this.down("combo[itemId='from']").getValue();
		s.toSource = this.down("combo[itemId='to']").getValue();
		s.memo = this.down("textfield[itemId='memo']").getValue();
		return s;
	}
});