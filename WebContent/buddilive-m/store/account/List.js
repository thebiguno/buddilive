Ext.define("BuddiLive.store.account.List", {
	"extend": 'Ext.data.Store',
	"config": {
		"fields": [ "id", "name", "type" ],
		"autoLoad": true,
		
		"grouper": {
			"groupFn": function(record) {
				return record.get('type');
			}
		},

		"proxy": {
			"type": "ajax",
			"method": "GET",
			"url": "data/accounts-m.json",
			"reader": {
				"type": "json",
				"rootProperty": "data"
			}
		}
	}
});
