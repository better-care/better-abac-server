(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('partyItemEdit', partyItemEditDirective);

  /** @ngInject */
  function partyItemEditDirective(deleteEnabled, Restangular) {
    var directive = {
      restrict: 'A',
      require: ['partyItemEdit', '^^crudListView', 'crudItem', '^^crudListViewItem'],
      template: '<form class="form" role="form">\
                      <div class="form-group form-inline">\
                        <label for="type">Type:</label>\
                        <select class="form-control" ng-options="partyType.name as partyType.name for partyType in partyCtrl.partyTypes" ng-model="crudListViewItem.item.type"></select>\
                      </div>\
                      <div class="form-group form-inline">\
                        <label for="exId">External Ids:</label>\
                        <input type="text" class="form-control" ng-repeat="exId in crudListViewItem.item.externalIds track by $index" ng-model="crudListViewItem.item.externalIds[$index]" ng-blur="partyCtrl.cleanExternalIds(crudListViewItem.item)">\
                        <button type="button" ng-click="partyCtrl.addExternalId(crudListViewItem.item)" class="btn btn-success">+</button> \
                      </div>\
                      <div class="form-group">\
                      <button type="submit" class="btn btn-primary" ng-click="partyCtrl.save(crudListViewItem.item)">Save</button>\
                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>\
                      </div>\
                    </form>',
      scope: {
        partyTypeEndpoint: '@'
      },
      controller: PartyItemEditDirectiveCtrl,
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
    function PartyItemEditDirectiveCtrl($scope) {

      this.partyTypes = Restangular.all(this.partyTypeEndpoint).getList().$object;

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
