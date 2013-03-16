Ext.define("BuddiLive.view.Viewport", {
	"extend": "Ext.container.Viewport",
	"alias": "widget.buddiviewport",
	
	"requires": [
		"BuddiLive.view.account.Tree",
		"BuddiLive.view.budget.Panel",
		"BuddiLive.view.budget.Tree",
		"BuddiLive.view.preferences.Editor",
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
				"itemId": "budditabpanel",
				"layout": "fit",
				"region": "west",
				"dockedItems": [
					{
						"xtype": "toolbar",
						"dock": "top",
						"items": [
							{
								"tooltip": "${translation("NEW_ACCOUNT")?json_string}",
								"icon": "img/bank--plus.png",
								"itemId": "addAccount"
							},
							{
								"tooltip": "${translation("MODIFY_ACCOUNT")?json_string}",
								"icon": "img/bank--pencil.png",
								"itemId": "editAccount",
								"disabled": true
							},
							{
								"tooltip": "${translation("DELETE_ACCOUNT")?json_string}",
								"icon": "img/bank--minus.png",
								"itemId": "deleteAccount",
								"disabled": true
							},
							{
								"tooltip": "${translation("NEW_BUDGET_CATEGORY")?json_string}",
								"icon": "img/table--plus.png",
								"itemId": "addCategory",
								"hidden": true
							},
							{
								"tooltip": "${translation("MODIFY_BUDGET_CATEGORY")?json_string}",
								"icon": "img/table--pencil.png",
								"itemId": "editCategory",
								"disabled": true,
								"hidden": true
							},
							{
								"tooltip": "${translation("DELETE_BUDGET_CATEGORY")?json_string}",
								"icon": "img/table--minus.png",
								"itemId": "deleteCategory",
								"disabled": true,
								"hidden": true
							},
							"->",
							{
								"tooltip": "${translation("PREFERENCES")?json_string}",
								"icon": "img/gear.png",
								"itemId": "preferences"
							},
							{
								"text": "${translation("LOGOUT")?json_string}",
								"tooltip": "${translation("LOGOUT")?json_string} ${plaintextIdentifier}",
								"icon": "img/door-open-out.png",
								"itemId": "logout"
							}
						]
					}
				],
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
								"width": 300,
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
	},
	
	"reload": function(){
		//Reload the entire page
		location.reload(true);
	}
});