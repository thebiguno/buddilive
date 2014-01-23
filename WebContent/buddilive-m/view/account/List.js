Ext.define("BuddiLive.view.account.List", {
	"extend": "Ext.dataview.List",
	"alias": "widget.accountlist",
	"requires": [
		"BuddiLive.store.account.List"
	],
	
	"config": {
		"fullscreen": true,
		"store": Ext.create("BuddiLive.store.account.List"),
		"grouped": true,
		"itemTpl": "<div>{name}</div>",
		"items": [
			{
				"xtype": "titlebar",
				"docked": "top",
				"height": 50,
				"items": [
					{
						"xtype": "button",
						"text": "Logout",
						"itemId": "logout",
						"align": "right"
					}
				]
			}
		]
	}
});
