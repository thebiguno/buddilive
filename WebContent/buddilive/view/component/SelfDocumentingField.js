Ext.define('BuddiLive.view.component.SelfDocumentingField', {
	"extend": "Ext.form.FieldContainer",
	"alias": "widget.selfdocumentingfield",
	
	"initComponent": function(){
		this.layout = "hbox";
		this.childItemId = this.itemId;
		var component = Ext.applyIf({
			"xtype": this.initialConfig.type,
			"flex": 1
		}, this.initialConfig);
		delete component.hidden;	//We hide the self documenting field, not the child component
		delete component.type;
		delete component.fieldLabel;
		delete this.itemId;
		delete this.listeners;
		delete this.disabled;
		
		var messageTitle = (this.initialConfig.messageTitle ? this.initialConfig.messageTitle : "${translation("WHAT_IS_THIS")?json_string}");
		var messageBody = this.initialConfig.messageBody;
		
		this.items = [
			component,
			{
				"xtype": "button",
				"icon": "img/question.png",
				"margin": "1 0 0 5",
				"tooltip": (this.initialConfig.helpButtonTooltip ? this.initialConfig.helpButtonTooltip : "${translation("WHAT_IS_THIS")?json_string}"),
				"tabIndex": -1,
				"listeners": {
					"click": function(){
						Ext.MessageBox.show({
							"title": messageTitle,
							"msg": messageBody,
							"buttons": Ext.MessageBox.OK
						});
					}
				}
			}
		];

		this.callParent(arguments);
	}
});