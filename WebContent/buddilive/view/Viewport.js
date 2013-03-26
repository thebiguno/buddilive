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
						],
						"dockedItems": this.getDockedItems("accounts")
					},
					{
						"xtype": "panel",
						"layout": "fit",
						"region": "center",
						"title": "${translation("MY_BUDGET")?json_string}",
						"items": [
							{
								"xtype": "budgetpanel",
								"itemId": "myBudget"
							}
						],
						"dockedItems": this.getDockedItems("categories")
					}
				]
			}
		];
		
		this.callParent();
	},
	
	"reload": function(){
		//Reload the entire page
		location.reload();
	},
	
	"getDockedItems": function(type){
		var items = [];
		
		if (type == "accounts"){
			items.push(
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
				}
			);
		}
		else if (type == "categories"){
			items.push(
				{
					"text": "${translation("NEW_BUDGET_CATEGORY")?json_string}",
					"icon": "img/table--plus.png",
					"itemId": "addCategory"
				},
				{
					"text": "${translation("MODIFY_BUDGET_CATEGORY")?json_string}",
					"icon": "img/table--pencil.png",
					"itemId": "editCategory",
					"disabled": true
				},
				{
					"text": "${translation("DELETE_BUDGET_CATEGORY")?json_string}",
					"icon": "img/table--minus.png",
					"itemId": "deleteCategory",
					"disabled": true
				}
			);
		}
		else if (type == "scheduled"){
			items.push(
				{
					"text": "${translation("NEW_SCHEDULED_TRANSACTION")?json_string}",
					"icon": "img/alarm-clock--plus.png",
					"itemId": "addScheduled"
				},
				{
					"text": "${translation("MODIFY_SCHEDULED_TRANSACTION")?json_string}",
					"icon": "img/alarm-clock--pencil.png",
					"itemId": "editScheduled",
					"disabled": true
				},
				{
					"text": "${translation("DELETE_SCHEDULED_TRANSACTION")?json_string}",
					"icon": "img/alarm-clock--minus.png",
					"itemId": "deleteScheduled",
					"disabled": true
				}
			);
		}
		
		items.push(
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
				"icon": "img/alarm-clock.png",
				"itemId": "showScheduled"
			},
			{
				"text": "${translation("PREFERENCES")?json_string}",
				"icon": "img/gear.png",
				"itemId": "showPreferences"
			},
			{
				"text": "${translation("LOGOUT")?json_string}",
				"tooltip": "${translation("LOGOUT")?json_string} ${plaintextIdentifier}",
				"icon": "img/door-open-out.png",
				"itemId": "logout"
			}
		);
		
		return [
			{
				"xtype": "toolbar",
				"dock": "top",
				"items": items
			}
		];
	}
});