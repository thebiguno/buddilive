Ext.define('BuddiLive.view.budget.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.budgeteditor",
	"requires": [
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
						"xtype": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "Name",	//TODO i18n
						"allowBlank": false,
						"enableKeyEvents": true,
						"emptyText": "Salary, Groceries, Auto Insurance, etc"	//TODO i18n
					},
					{
						"xtype": "parentcombobox",
						"itemId": "parent",
						"fieldLabel": "Parent Category",	//TODO i18n
						"emptyText": "Parent",
						"value": (s ? s.parent : null),
						"url": "gui/categories/parents.json" + (s ? "?exclude=" + s.id : ""),
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
						"xtype": "combobox",
						"itemId": "periodType",
						"value": "MONTH",
						"hidden": s != null,
						"fieldLabel": "Period Type",	//TODO i18n
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("BUDGET_CATEGORY_TYPE_WEEK")?json_string}", "value": "WEEK"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_SEMI_MONTH")?json_string}", "value": "SEMI_MONTH"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_MONTH")?json_string}", "value": "MONTH"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_QUARTER")?json_string}", "value": "QUERTER"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_SEMI_YEAR")?json_string}", "value": "SEMI_YEAR"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_YEAR")?json_string}", "value": "YEAR"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "combobox",
						"itemId": "type",
						"value": "E",
						"hidden": s != null,
						"fieldLabel": "Type",	//TODO i18n
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
					},
				]
			}
		];
		this.buttons = [
			{
				"text": "OK",	//TODO i18n
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "Cancel",	//TODO i18n
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});