Ext.define('BuddiLive.view.budget.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.budgeteditor",
	"requires": [
		"BuddiLive.view.component.CurrencyField",
		"BuddiLive.view.budget.ParentCombobox"
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected
		var editor = this;
		
		this.title = (s ? "Edit Budget Category" : "Add Budget Category");	//TODO i18n
		this.layout = "fit";
		this.modal = true;
		this.width = 400;
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
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_NAME")?json_string}",
						"type": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "${translation("BUDGET_CATEGORY_NAME")?json_string}",
						"allowBlank": false,
						"enableKeyEvents": true,
						"emptyText": "Salary, Groceries, Auto Insurance, etc",	//TODO i18n
						"listeners": {
							"afterrender": function(field) {
								field.focus(false, 500);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_PARENT")?json_string}",
						"type": "parentcombobox",
						"itemId": "parent",
						"fieldLabel": "${translation("BUDGET_CATEGORY_PARENT")?json_string}",
						"emptyText": "Parent",
						"value": (s ? s.parent : null),
						"url": "data/categories/parents.json" + (s ? "?exclude=" + s.id : ""),
						"listeners": {
							"change": function(){
								var parent = editor.down("combobox[itemId='parent']");
								if (parent.getValue() != null && (parent.getValue() + "").length > 0){
									editor.down("combobox[itemId='periodType']").setValue(parent.getStore().findRecord("value", parent.getValue()).raw.periodType);
									editor.down("combobox[itemId='type']").setValue(parent.getStore().findRecord("value", parent.getValue()).raw.type);
								}
								editor.down("combobox[itemId='periodType']").setDisabled(parent.getValue() != null);
								editor.down("combobox[itemId='type']").setDisabled(parent.getValue() != null);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_PERIOD_TYPE")?json_string}",
						"type": "combobox",
						"itemId": "periodType",
						"value": "MONTH",
						"hidden": s != null,
						"fieldLabel": "${translation("BUDGET_CATEGORY_PERIOD_TYPE")?json_string}",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("BUDGET_CATEGORY_TYPE_WEEK")?json_string}", "value": "WEEK"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_SEMI_MONTH")?json_string}", "value": "SEMI_MONTH"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_MONTH")?json_string}", "value": "MONTH"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_QUARTER")?json_string}", "value": "QUARTER"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_SEMI_YEAR")?json_string}", "value": "SEMI_YEAR"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_YEAR")?json_string}", "value": "YEAR"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_TYPE")?json_string}",
						"type": "combobox",
						"itemId": "type",
						"value": "E",
						"hidden": s != null,
						"fieldLabel": "${translation("BUDGET_CATEGORY_TYPE")?json_string}",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "Income", "value": "I"},	//TODO i18n
								{"text": "Expense", "value": "E"}	//TODO i18n
							]
						}),
						"queryMode": "local",
						"valueField": "value"
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