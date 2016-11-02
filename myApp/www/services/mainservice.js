var app = angular.module('testapp.services.main', []);

app.service("MainService", function($http, $q, url){
    var urlvalue = url.address;
    var self = {
      'datos':[],
      'getEntryG': function(direction) {
        var d = $q.defer();
        $http.get(urlvalue + "/Restful-HelloWorld/rest/SayHi/holas")
        .success(function success (data){
          self.datos = data;
          d.resolve(self.datos);
        })
        .error(function error (msg) {
          console.error(msg);
          d.reject("http request returned an error.")
        });
        return d.promise;
   }
  };

  return self;

});