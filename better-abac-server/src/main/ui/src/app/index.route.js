(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .config(routeConfig);

  function routeConfig($routeProvider) {
    $routeProvider
      .when('/party', {
        templateUrl: 'app/main/main-list.html',
        controller: 'MainCtrl',
        controllerAs: 'mainCtrl'
      })
      .when('/partyType', {
        templateUrl: 'app/main/main-list.html',
        controller: 'MainCtrl',
        controllerAs: 'mainCtrl'
      })
      .when('/partyRelation', {
        templateUrl: 'app/main/main-list.html',
        controller: 'MainCtrl',
        controllerAs: 'mainCtrl'
      })
      .when('/relationType', {
        templateUrl: 'app/main/main-list.html',
        controller: 'MainCtrl',
        controllerAs: 'mainCtrl'
      })
      .when('/policy', {
        templateUrl: 'app/main/main-list.html',
        controller: 'MainCtrl',
        controllerAs: 'mainCtrl'
      })
      .otherwise({
        redirectTo: '/party'
      });
  }

})();
