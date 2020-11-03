(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('partyTypeItemEdit', partyTypeItemEditDirective);

  /** @ngInject */
  function partyTypeItemEditDirective(deleteEnabled) {
    var directive = {
      restrict: 'A',
      require: ['partyTypeItemEdit', '^^crudListView', 'crudItem', '^^crudListViewItem'],
      template: '<form class="form" role="form">\
                      <div class="form-group form-inline">\
                        <label for="name">Name:</label>\
                        <input type="text" class="form-control" ng-model="crudListViewItem.item.name" id="name">\
                      </div>\
                      <div class="form-group">\
                      <button type="submit" class="btn btn-primary" ng-click="partyCtrl.save(crudListViewItem.item)">Save</button>\
                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>\
                      </div>\
                    </form>',
      scope: {
        isActive: '=',
        crudEndpoint: '@'
      },
      controller: PartyTypeItemEditDirectiveCtrl,
      controllerAs: 'partyCtrl',
      bindToController: true,
      link: function (scope, element, attrs, controllers) {
        scope.deleteEnabled = deleteEnabled;
        controllers[0].controllers = controllers;
        scope.crudListViewItem = controllers[3];
      }
    };

    return directive;

    /** @ngInject */
    function PartyTypeItemEditDirectiveCtrl($scope) {

      this.addExternalId = function (party) {
        if (!party.externalIds) {
          party.externalIds = []
        }
        party.externalIds.push("")
      };

      this.cleanExternalIds = function (party) {
        if (party.externalIds) {
          for (var i = party.externalIds.length - 1; i >= 0; i--) {
            if (party.externalIds[i] == null || party.externalIds[i].length < 1) {
              party.externalIds.splice(i, 1);
            }
          }
        }
      };

      this.save = function (party) {
        this.cleanExternalIds(party);
        $scope.crudListViewItem.save(party);
      }

    }
  }

})();
