Ext.application({
	"name": "BuddiLive",
	"appFolder": "buddilive-m",
	
	"css": [
		{
			"path": "resources/css/cupertino.css",
			"platform": ["ios"],
			"theme": "cupertino"
		}
	],

	"controllers": [
		"account.List"
	],

	"launch": function() {
		BuddiLive.app = this;
		Ext.widget('accountlist');
	}
});
