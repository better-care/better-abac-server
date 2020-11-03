(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('acmeNavbar', acmeNavbar);

  /** @ngInject */
  function acmeNavbar(Auth) {
    var directive = {
      restrict: 'E',
      templateUrl: 'app/components/navbar/navbar.html',
      scope: {},
      controller: NavbarController,
      controllerAs: 'vm',
      bindToController: true
    };

    return directive;

    /** @ngInject */
    function NavbarController() {
      this.username = Auth.username;
      this.logout = Auth.logout;
    }
  }

})();
