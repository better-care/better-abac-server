(function () {
  'use strict';

  var module = angular
    .module('betterAbacUi', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'ngMessages', 'ngAria', 'restangular', 'ngRoute', 'mgcrea.ngStrap', 'toastr', 'ui.ace']);

  angular.element(document).ready(function ($log, $window) {
    var keycloakInterval = setInterval(function () {
      if (typeof Keycloak !== 'undefined') {
        clearInterval(keycloakInterval);
        var keycloakAuth = new Keycloak('keycloak.json');
        //auth.loggedIn = true;

        keycloakAuth.init({onLoad: 'check-sso'}/*{ onLoad: 'login-required' }*/).success(function (authenticated) {

          module.factory('Auth', function ($window, $q) {
            var auth = {
              loggedIn: authenticated,
              authz: keycloakAuth,
              logout: function () {
                auth.authz.logout({
                  redirectUri: $window.location
                });
              },
              login: function () {
                $log('*** LOGIN');

                auth.authz.login().success(function () {
                  auth.loggedIn = true;
                  auth.authz = keycloakAuth;
                  auth.logoutUrl = keycloakAuth.authServerUrl + "/realms/" + keycloakAuth.realm + "/tokens/logout?redirect_uri=" + $window.location.href;
                  module.factory('Auth', function () {
                    return auth;
                  });
                }).error(function () {
                  $log('*** LOGIN-error');
                });
              },
              refreshAccessToken: function () {
                var tokenSet = auth.authz.token != null;
                return $q(function (resolve, reject) {
                  auth.authz.updateToken()
                    .success(function (newToken) {
                      resolve(auth.authz.token);
                    })
                    .error(function () {
                      if (!tokenSet || confirm("Could not update token. You need to authenticate again.")) {
                        reject();
                        auth.login();
                      }
                    });
                });
              }
            };
            return auth;
          });

          angular.bootstrap(document, ['betterAbacUi']);
        }).error(function () {
          $window.location.reload();
        });

        keycloakAuth.onAuthSuccess = function () {
          /*alert('onAuthSuccess');*/
        };
        keycloakAuth.onAuthRefreshSuccess = function () {
          /*$log('onAuthRefreshSuccess');*/
        };
      }
    }, 50);
  });
})();
