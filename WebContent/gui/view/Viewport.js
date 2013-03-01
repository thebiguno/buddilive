Ext.define("BuddiLive.view.Viewport", {
	"extend": "Ext.container.Viewport",
	
	"requires": [
		"BuddiLive.view.account.Tree",
		"BuddiLive.view.budget.Tree",
		"BuddiLive.view.report.Panel",
		"BuddiLive.view.transaction.List",
		"BuddiLive.view.transaction.Editor"
	],	
	"initComponent": function() {
		this.layout = "fit";
		this.height = "100%";
		this.width = "100%";
		this.items = [
			{
				"xtype": "tabpanel",
				"layout": "fit",
				"region": "west",
				"items": [
					{
						"xtype": "panel",
						"layout": "border",
						"title": "My Accounts",
						"items": [
							{
								"xtype": "accounttree",
								"region": "west",
								"stateful": true,
								"stateId": "accounttree",
								"split": true
							},
							{
								"xtype": "transactionlist",
								"region": "center"
							}
						]
					},
					{
						"xtype": "panel",
						"layout": "border",
						"title": "My Budget",
						"items": [
							{
								"xtype": "budgettree",
								"region": "west"
							},
							{
								"xtype": "transactionlist",
								"region": "center"
							}
						]
					}
				]
			}
		];

		this.callParent();
	}
});