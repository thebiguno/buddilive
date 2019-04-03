Ext.define("BuddiLive.view.Viewport", {
	"extend": "Ext.container.Viewport",
	"alias": "widget.buddiviewport",
	
	"requires": [
		"BuddiLive.view.account.Tree",
		"BuddiLive.view.budget.Panel",
		"BuddiLive.view.budget.Tree",
		"BuddiLive.view.component.SelfDocumentingField",
		"BuddiLive.view.restore.Form",
		"BuddiLive.view.preferences.PreferencesEditor",
		"BuddiLive.view.preferences.ChangePasswordEditor",
		"BuddiLive.view.scheduled.List",
		"BuddiLive.view.transaction.List",
		"BuddiLive.view.transaction.Editor"
	],	
	"initComponent": function() {
		this.layout = "border";
		this.height = "100%";
		this.width = "100%";
		
		this.items = [
			<#if !((user.premium)!false)>
			{
				"xtype": "panel",
				"region": "north",
				"height": 60,
				"border": false,
				"html": "<iframe id='adsensetop' src='buddilive/view/ads/top.html' scrolling='no' width='468' height='60' marginheight='0' marginwidth='0' seamless='seamless' frameborder='0'></iframe><img src='img/logo-title-small.png' style='position: absolute; top: 2px; right: 30px'/>",
				"listeners": {
					"afterrender": function(){
						window.setInterval(function(){
							var iframe = document.getElementById('adsensetop');
							if (iframe != null) iframe.src += "";
						}, 1000 * 60 * 20);	//Reload every 20 minutes
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
			<#if (user.encrypted)!false>
			{
				"icon": "img/lock.png",
				"overCls": "",
				"tooltip": "${translation("DATA_ENCRYPTED")?json_string}"
			},
			" ",
			</#if>
			{
				"text": "${translation("REPORTS")?json_string}",
				"icon": "img/chart.png",
				"menu": [
					{
						"text": "${translation("REPORT_TABLE_INCOME_AND_EXPENSES_BY_CATEGORY")?json_string}",
						"icon": "img/table-sum.png",
						"itemId": "showIncomeAndExpensesByCategoryTable"
					},
					{
						"text": "${translation("REPORT_TABLE_AVERAGE_INCOME_AND_EXPENSES_BY_CATEGORY")?json_string}",
						"icon": "img/table-sum.png",
						"itemId": "showAverageIncomeAndExpensesByCategoryTable"
					},
					{
						"text": "${translation("REPORT_PIE_INCOME_BY_CATEGORY")?json_string}",
						"icon": "img/chart-pie.png",
						"itemId": "showIncomeByCategoryPie"
					},
					{
						"text": "${translation("REPORT_PIE_EXPENSES_BY_CATEGORY")?json_string}",
						"icon": "img/chart-pie.png",
						"itemId": "showExpensesByCategoryPie"
					},
					{
						"text": "${translation("REPORT_ACCOUNT_BALANCES_OVER_TIME")?json_string}",
						"icon": "img/chart-up.png",
						"itemId": "showAccountBalancesOverTimeLine"
					},
					{
						"text": "${translation("REPORT_NET_WORTH_OVER_TIME")?json_string}",
						"icon": "img/chart-up.png",
						"itemId": "showNetWorthOverTimeLine"
					}
				]
			},
			{
				"text": "${translation("SYSTEM")?json_string}",
				"icon": "img/switch.png",
				"menu": [
					{
						"text": "${translation("CHANGE_PASSWORD")?json_string}",
						"icon": "img/ui-text-field-password.png",
						"itemId": "changePassword"
					},
					{
						"text": "${translation("PREFERENCES")?json_string}",
						"icon": "img/gear.png",
						"itemId": "showPreferences"
					},
					{
						"text": "${translation("SCHEDULED_TRANSACTIONS")?json_string}",
						"icon": "img/alarm-clock.png",
						"itemId": "showScheduled"
					},
					"-",
					{
						"text": "${translation("BACKUP")?json_string}",
						"icon": "img/drive-download.png",
						"itemId": "backup"
					},
					{
						"text": "${translation("RESTORE")?json_string}",
						"icon": "img/drive-upload.png",
						"itemId": "restore"
					},
					"-",
					{
						"text": "${translation("DELETE_USER")?json_string}",
						"icon": "img/minus-octagon.png",
						"itemId": "deleteUser"
					},
					"-",
					{
						"text": "${translation("HELP_GETTING_STARTED_TITLE")?json_string}",
						"icon": "img/question.png",
						"itemId": "gettingStarted"
					},
					{
						"text": "${translation("DONATE_TITLE")?json_string}",
						"icon": "img/money-coin.png",
						"itemId": "donate"
					}
				]
			},
			{
				"text": "${translation("LOGOUT")?json_string}",
				"icon": "img/control-power.png",
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