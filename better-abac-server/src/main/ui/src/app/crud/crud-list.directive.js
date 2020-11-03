(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('crudList', crudListDirective);

  /** @ngInject */
  function crudListDirective(Restangular) {
    var directive = {
      restrict: 'A',
      template: '',
      controller: CrudListDirectiveCtrl,
      controllerAs: 'crudListCtrl',
      bindToController: true,
      require: ['crudList'],//,'crudListView'],
      link: function (scope, element, attr, controllers) {
        controllers[0].controllers = controllers;
        if (controllers[0].crudList == null) {
          controllers[0].crudList = Restangular.all(controllers[0].crudEndpoint).getList().$object
        }
      },
      scope: {
        crudList: '=',
        crudEndpoint: '@'
      }

    };

    return directive;

    /** @ngInject */
    function CrudListDirectiveCtrl($scope, $element, deleteEnabled) {
      this.newItem;

      this.clearNewItem = function () {
        this.newItem = this.getNewItem(this.crudEndpoint);
      };

      this.addNewItem = function (item) {
        this.crudList.push(item);
      };

      this.getNewItem = function (endpoint) {
        return Restangular.one(endpoint, null);
      };

      this.removeItem = function (item) {
        if (!deleteEnabled) {
          alert("Removing item is not allowed.");
          return
        }
        for (var i = 0; i < this.crudList.length; i++) {
          var pt = this.crudList[i];
          if (pt.id == item.id) {
            this.crudList.splice(i, 1);
            break;
          }
        }
      }
    }
  }

})();
