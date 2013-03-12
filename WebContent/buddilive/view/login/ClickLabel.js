Ext.define('BuddiLive.view.login.ClickLabel', {
	"extend": "Ext.form.Label",
	"alias": "widget.clicklabel",
		
	"initComponent": function(){
		Ext.applyIf(this, this.initialConfig);
		if (!this.style) this.style = "color: #44a; text-decoration: underline; text-align: right; padding-top: 3px;";
		if (this.listeners != null && this.listeners.click != null){
			var clickListener = this.listeners.click;
			delete this.listeners.click;
			this.listeners.render = function(c){
				c.getEl().on('click', function(){
					clickListener(c);
				}, c);
			}
		}

		this.callParent(arguments);
	}
});