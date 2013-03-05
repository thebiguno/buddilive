Ext.define('BuddiLive.view.budget.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.budgeteditor",
	"requires": [
		"BuddiLive.view.budget.ParentCombobox"
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected

		this.title = (s ? "Edit Budget Category" : "Add Budget Category");
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
						"fieldLabel": "Name",
						"allowBlank": false,
						"enableKeyEvents": true,
						"emptyText": "Salary, Groceries, Auto Insurance, etc"
					},
					{
						"xtype": "parentcombobox",
						"itemId": "parent",
						"fieldLabel": "Parent Category",
						"emptyText": "Parent",
						"value": (s ? s.parent : null),
						"url": "gui/categories/parents.json" + (s ? "?exclude=" + s.id : "")
					},
					{
						"xtype": "combobox",
						"itemId": "periodType",
						"value": "MONTH",
						"hidden": s != null,
						"fieldLabel": "Period Type",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "Week", "value": "WEEK"},
								{"text": "Semi Month", "value": "SEMI_MONTH"},
								{"text": "Month", "value": "MONTH"},
								{"text": "Quarter", "value": "QUERTER"},
								{"text": "Semi Year", "value": "SEMI_YEAR"},
								{"text": "Year", "value": "YEAR"}
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
						"fieldLabel": "Type",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "Income", "value": "I"},
								{"text": "Expense", "value": "E"}
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
				"text": "OK",
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "Cancel",
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});