Ext.define("BuddiLive.controller.account.List", {
	"extend": "Ext.app.Controller",
	"requires": [
		"BuddiLive.store.account.List",
		"BuddiLive.view.account.List",
		"BuddiLive.store.transaction.List",
		"BuddiLive.view.transaction.List"
		
	],
	"config": {
		"refs": {
			"transactions": {
				"xtype": "transactionlist",
				"selector": "transactionlist",
				"autoCreate": true
			}
		},
		"control": {
			"accountlist": {
				"itemtap": function(list, index, target, record) {
					var transactions = this.getTransactions();
					transactions.getStore().load({"params": {"source": record.data.id}});
					Ext.Viewport.animateActiveItem(transactions, {
						"type": "slide",
						"direction": "left"
					});
				}
			},
			"button[itemId=logout]": {
				"tap": function() {
					Ext.Ajax.request({
						"url": "index",
						"method": "DELETE",
						"success": function() {
							window.location.reload();
						}
					});
				}
			}
		}
	}
});
