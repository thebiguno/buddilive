Ext.define("BuddiLive.view.component.GenericStoreBackedCombobox", {
	"extend": "Ext.form.field.ComboBox",
	
	"initComponent": function(){
		var combo = this;
		Ext.applyIf(this, this.initialConfig);

		this.forceSelection = true;
		this.displayField = "text";
		this.valueField = "value";
		this.enableKeyEvents = true;
		this.editable = false;
		this.triggerAction = "all";

		this.listConfig = {
			"itemTpl": "<div style='{style}'>{text}</div>"
		};
	
		this.callParent(arguments);
		
		this.addListener("select", function(){
			//Don't let users select the separators.
			if (combo.getValue() == ""){
				combo.setValue();
			}
		});
	}
});