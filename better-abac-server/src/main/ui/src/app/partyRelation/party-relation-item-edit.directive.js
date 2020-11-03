(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('partyRelationItemEdit', partyRelationItemEditDirective);

  /** @ngInject */
  function partyRelationItemEditDirective(deleteEnabled, Restangular) {
    var directive = {
      restrict: 'A',
      require: ['partyRelationItemEdit', '^^crudListView', 'crudItem', '^^crudListViewItem'],
      template: '<form class="form" role="form">\
                      <div class="form-group form-inline">\
                        <label for="source">Source:</label>\
                        <select class="form-control" ng-options="sourceParty.id as (sourceParty.type +\' | ids: \'+ sourceParty.externalIds.join(\', \')) for sourceParty in partyRelationCtrl.parties" ng-model="crudListViewItem.item.source"></select>\
                      </div>\
                      <div class="form-group form-inline">\
                        <label for="relationType">Relation Type:</label>\
                        <select class="form-control" ng-options="relationType.name as relationType.name for relationType in partyRelationCtrl.relationTypes" ng-model="crudListViewItem.item.relationType"></select>\
                      </div>\
                      <div class="form-group form-inline">\
                        <label for="target">Target:</label>\
                        <select class="form-control" ng-options="sourceParty.id as (sourceParty.type +\' | ids: \'+ sourceParty.externalIds.join(\', \')) for sourceParty in partyRelationCtrl.parties" ng-model="crudListViewItem.item.target"></select>\
                        <select class="form-control" ng-options="sourceParty.id as (sourceParty.type +\' | ids: \'+ sourceParty.externalIds.join(\', \')) for sourceParty in partyRelationCtrl.parties" ng-model="crudListViewItem.item.target"></select>\
                      </div>\
                      <div class="form-group form-inline">\
                        <label class="control-label"><i class="fa fa-calendar"></i> Valid Until:</label><br>\
                        <div class="form-group">\
                          <input type="text" size="10" class="form-control" ng-model="crudListViewItem.item.validUntil" data-autoclose="1" placeholder="Date" bs-datepicker>\
                        </div>\
                        <div class="form-group">\
                          <input type="text" size="8" class="form-control" ng-model="crudListViewItem.item.validUntil" data-time-format="h:mm:ss a" data-autoclose="1" placeholder="Time" bs-timepicker>\
                        </div>\
                      </div>\
                      <div class="form-group">\
                      <button type="submit" class="btn btn-primary" ng-click="partyRelationCtrl.save(crudListViewItem.item)">Save</button>\
                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>\
                      </div>\
                    </form>',
      scope: {
        isActive: '=',
        relationTypeEndpoint: '@',
        partyEndpoint: '@'
      },
      controller: PartyRelationItemEditDirectiveCtrl,
      controllerAs: 'partyRelationCtrl',
      bindToController: true,
      link: function (scope, element, attrs, controllers) {
        scope.deleteEnabled = deleteEnabled;
        controllers[0].controllers = controllers;
        scope.crudListViewItem = controllers[3];
      }
    };

    return directive;

    /** @ngInject */
    function PartyRelationItemEditDirectiveCtrl($scope) {

      this.parties = Restangular.all(this.partyEndpoint).getList().$object;
      this.relationTypes = Restangular.all(this.relationTypeEndpoint).getList().$object;

      this.addExternalId = function (partyRelation) {
        if (!partyRelation.externalIds) {
          partyRelation.externalIds = []
        }
        partyRelation.externalIds.push("")
      };

      this.cleanExternalIds = function (partyRelation) {
        if (partyRelation.externalIds) {
          for (var i = partyRelation.externalIds.length - 1; i >= 0; i--) {
            if (partyRelation.externalIds[i] == null || partyRelation.externalIds[i].length < 1) {
              partyRelation.externalIds.splice(i, 1);
            }
          }
        }
      };

      this.save = function (partyRelation) {
        this.cleanExternalIds(partyRelation);
        $scope.crudListViewItem.save(partyRelation);
      }

    }
  }

})();
