Ext.define('BuddiLive.view.transaction.split.Editor', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.spliteditor",
	
	"requires": [
		"BuddiLive.view.transaction.split.FromCombobox",
		"BuddiLive.view.transaction.split.ToCombobox"
	],
	
	"initComponent": function(){
		var v = this.initialConfig.value ? this.initialConfig.value : {};
		var splitEditor = this;
		this.layout = "hbox";
		this.border = false;
		this.width = "100%";
		this.height = 28;
		this.defaults = {
			"padding": "0 0 5 5"
		};
		
		this.listeners = {
			"boxready": function(){
				//Needed when there are multiple splits loaded from setTransaction()
				splitEditor.fireEvent("updateButtons", splitEditor.up("transactioneditor"));
			}
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
				"emptyText": "0.00 (Amount)",	//TODO i18n
				"padding": this.initialConfig.scheduledTransaction ? "0 0 5 0" : "0 0 5 5",
				"value": v.amount
			},
			{
				"xtype": "fromcombobox",
				"itemId": "from",
				"flex": 1,
				"emptyText": "From",	//TODO i18n
				"value": v.fromId
			},
			{"xtype": "panel", "html": "<img style='padding-top: 3px;' src='img/arrow.png'/>", "border": false, "width": 25, "height": 25},
			{
				"xtype": "tocombobox",
				"itemId": "to",
				"flex": 1,
				"emptyText": "To",	//TODO i18n
				"value": v.toId
			},
			{
				"xtype": "textfield",
				"itemId": "memo",
				"flex": 2,
				"emptyText": "Memo",	//TODO i18n
				"value": v.memo
			},
			{
				"xtype": "button",
				"icon": "img/minus-circle.png",
				"itemId": "removeSplit",
				"tooltip": "Remove split",	//TODO i18n
				"padding": "2 5 2 5",
				"margin": "0 0 0 5",
				"hidden": true
			},
			{
				"xtype": "tbspacer",
				"itemId": "addSpacer",
				"width": 28,
				"padding": "2 5 2 5",
				"margin": this.initialConfig.scheduledTransaction ? "0 0 0 5" : "0 5 0 5"
			},
			{
				"xtype": "button",
				"icon": "img/plus-circle.png",
				"itemId": "addSplit",
				"tooltip": "Add split",	//TODO i18n
				"padding": "2 5 2 5",
				"margin": this.initialConfig.scheduledTransaction ? "0 0 0 5" : "0 5 0 5"
			}
		];
		
		this.callParent(arguments);
	},
	
	"getSplit": function(){
		var s = {};
		s.amount = this.down("numberfield[itemId='amount']").getValue();
		s.fromId = this.down("combo[itemId='from']").getValue();
		s.toId = this.down("combo[itemId='to']").getValue();
		s.memo = this.down("textfield[itemId='memo']").getValue();
		return s;
	}
});