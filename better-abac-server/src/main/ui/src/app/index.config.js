(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .config(config);

  /** @ngInject */
  function config($logProvider, toastrConfig, RestangularProvider, $navbarProvider) {
    // Enable log
    $logProvider.debugEnabled(true);
    var baseUrl = 'rest/v1/admin';
    if (location.host == "localhost:3000") {
      baseUrl = 'http://localhost:8080' + baseUrl;
    }
    RestangularProvider.setBaseUrl(baseUrl);
    angular.extend($navbarProvider.defaults, {
      activeClass: 'in'
    });
  }

})();
