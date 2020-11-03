(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .run(runBlock);

  /** @ngInject */
  function runBlock($log, Restangular, Auth, $http, $rootScope) {

    if (ace) {
      ace.config.set('basePath', 'aceFiles');
    }

    function getAuthorizationProp(token) {
      return {Authorization: 'Bearer ' + token};
    }

    $rootScope.$watch(function () {
      return Auth.authz.token;
    }, function (newToken) {
      if (newToken) {
        Restangular.setDefaultHeaders(getAuthorizationProp(newToken));
      }
    });

    Restangular.setErrorInterceptor(function (response, deferred, responseHandler) {

      if (response.status === 401 || response.status === -1) {
        Auth.refreshAccessToken().then(function (newToken) {
          angular.merge(response.config.headers, getAuthorizationProp(newToken));
          $http(response.config).then(responseHandler, deferred.reject);
        });
        return false; // error handled
      }

      return true; // error not handled
    });
  }

})();
