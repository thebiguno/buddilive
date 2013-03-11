Ext.define("BuddiLive.view.Viewport", {
	"extend": "Ext.container.Viewport",
	"alias": "widget.buddiviewport",
	
	"requires": [
		"BuddiLive.view.account.Tree",
		"BuddiLive.view.budget.Panel",
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
				"dockedItems": [{
					"xtype": "toolbar",
					"dock": "top",
					"items": [
						"->",
						{
							"tooltip": "${translation("LOGOUT")?json_string}",
							"icon": "img/door-open-out.png",
							"itemId": "logout"
						}
					]
				}],
				"items": [
					{
						"xtype": "panel",
						"layout": "border",
						"title": "${translation("MY_ACCOUNTS")?json_string}",
						"itemId": "myAccounts",
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
							}/*
							,{
								"xtype": "panel",
								"region": "north",
								"height": 60,
								"border": false,
								"html": "<iframe id='adsensetop' src='buddilive/view/ads/top.html' scrolling='no' width='468' height='60' marginheight='0' marginwidth='0' seamless='seamless' frameborder='0'></iframe>",
								"listeners": {
									"afterrender": function(){
										window.setInterval(function(){
											var iframe = document.getElementById('adsensetop');
											if (iframe != null) iframe.src += "";
										}, 5000);
									}
								}
							}*/
						]
					},
					{
						"xtype": "budgetpanel",
						"title": "${translation("MY_BUDGET")?json_string}",
						"itemId": "myBudget"
					}
				]
			}
		];
		
		this.callParent();
	}
});