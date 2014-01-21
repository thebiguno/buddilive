Ext.application({
	"name": "BuddiLive",
	"appFolder": "buddilive-m",
	
	"views": ["login.Login"],
	"controllers": ["login.Login"],

	"launch": function() {
		BuddiLive.app = this;
		Ext.Viewport.add(Ext.create('BuddiLive.view.login.Login'));
	}
});
