Ext.define("BuddiLive.view.Viewport", {
	"extend": "Ext.container.Viewport",
	"alias": "widget.buddiviewport",
	
	"requires": [
		"BuddiLive.view.account.Tree",
		"BuddiLive.view.budget.Panel",
		"BuddiLive.view.budget.Tree",
		"BuddiLive.view.component.SelfDocumentingField",
		"BuddiLive.view.preferences.Editor",
		"BuddiLive.view.report.Panel",
		"BuddiLive.view.scheduled.List",
		"BuddiLive.view.transaction.List",
		"BuddiLive.view.transaction.Editor"
	],	
	"initComponent": function() {
		this.layout = "border";
		this.height = "100%";
		this.width = "100%";
		this.items = [
			<#if !premium>
			{
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
						}, 1000 * 60 * 1);	//Reload every minute
					}
				}
			},
			</#if>
			{
				"xtype": "tabpanel",
				"itemId": "budditabpanel",
				"layout": "fit",
				"region": "center",
				"dockedItems": [
					{
						"xtype": "toolbar",
						"dock": "top",
						"items": [
							{
								"text": "${translation("NEW_ACCOUNT")?json_string}",
								"icon": "img/bank--plus.png",
								"itemId": "addAccount"
							},
							{
								"text": "${translation("MODIFY_ACCOUNT")?json_string}",
								"icon": "img/bank--pencil.png",
								"itemId": "editAccount",
								"disabled": true
							},
							{
								"text": "${translation("DELETE_ACCOUNT")?json_string}",
								"icon": "img/bank--minus.png",
								"itemId": "deleteAccount",
								"disabled": true
							},
							{
								"text": "${translation("NEW_BUDGET_CATEGORY")?json_string}",
								"icon": "img/table--plus.png",
								"itemId": "addCategory",
								"hidden": true
							},
							{
								"text": "${translation("MODIFY_BUDGET_CATEGORY")?json_string}",
								"icon": "img/table--pencil.png",
								"itemId": "editCategory",
								"disabled": true,
								"hidden": true
							},
							{
								"text": "${translation("DELETE_BUDGET_CATEGORY")?json_string}",
								"icon": "img/table--minus.png",
								"itemId": "deleteCategory",
								"disabled": true,
								"hidden": true
							},
							"->",
							<#if encrypted>
							{
								"icon": "img/lock.png",
								"overCls": "",
								"tooltip": "${translation("DATA_ENCRYPTED")?json_string}"
							},
							" ",
							</#if>
							{
								"text": "${translation("SCHEDULED_TRANSACTIONS")?json_string}",
								"icon": "img/gear.png",
								"itemId": "editScheduledTransactions"
							},
							{
								"text": "${translation("PREFERENCES")?json_string}",
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
							}
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
		location.reload();
	}
});