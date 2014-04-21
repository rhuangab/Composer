(function(ko) {

	// Check local storage support
	var storage = (function() {
      var uid = new Date,
          storage,
          result;
      try {
        (storage = window.localStorage).setItem(uid, uid);
        result = storage.getItem(uid) == uid;
        storage.removeItem(uid);
        return result && storage;
      } catch(e) {}
    }());

	var persistExtender = function(target, options) {
		options = options || {};
		var key = options.key;
		var serializer = options.serialize || ko.extenders.localPersist.serialize;
		var deserializer = options.deserialize || ko.extenders.localPersist.deserialize;
		if (options.key) {	
			if(storage.getItem(key) !== null) {
			//if (Object.prototype.hasOwnProperty.call(storage, key)) {
			//if (storage.hasOwnProperty(key)) {
				try	{
					initialValue = deserializer(storage.getItem(key));
					target(ko.utils.unwrapObservable(initialValue));
				}catch(e){};
			}
			
			// Subscribe to all changes
			ko.computed(function() {
				ko.toJS(target);	// make dependencies
				return target();
			}).subscribe(function(newValue) {
			   storage.setItem(key, serializer(newValue));
			});
		}
		
		return target;
	};
		
	ko.extenders.localPersist = (storage ? persistExtender : function(target) { return target; });
	
	// Default serialize function
	ko.extenders.localPersist.serialize = function(data) {
		return ko.toJSON(data);
	};
	
	// Default deserialize function
	ko.extenders.localPersist.deserialize = function(data) {
		return JSON.parse(data);
	};
	
})(ko);
