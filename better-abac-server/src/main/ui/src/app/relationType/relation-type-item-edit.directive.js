(function() {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('relationTypeItemEdit', relationTypeItemEditDirective);

  /** @ngInject */
  function relationTypeItemEditDirective(deleteEnabled, Restangular) {
    var directive = {
      restrict: 'A',
      require:['relationTypeItemEdit','^^crudListView', 'crudItem', '^^crudListViewItem'],
      template: '<form class="form" role="form">\
                      <div class="form-group form-inline">\
                        <label for="name">Name:</label>\
                        <input type="text" class="form-control" ng-model="crudListViewItem.item.name" id="name">\
                      </div>\
                      <div class="form-group form-inline">\
                        <label for="allowedSourcePartyType">Allowed Source Party Type:</label>\
                        <select class="form-control" ng-options="partyType.name as partyType.name for partyType in relationTypeCtrl.partyTypes" ng-model="crudListViewItem.item.allowedSourcePartyType"></select>\
                      </div>\
                      <div class="form-group form-inline">\
                        <label for="allowedTargetPartyType">Allowed Target Party Type:</label>\
                        <select class="form-control" ng-options="partyType.name as partyType.name for partyType in relationTypeCtrl.partyTypes" ng-model="crudListViewItem.item.allowedTargetPartyType"></select>\
                      </div>\
                      <div class="form-group">\
                      <button type="submit" class="btn btn-primary" ng-click="relationTypeCtrl.save(crudListViewItem.item)">Save</button>\
                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>\
                      </div>\
                    </form>',
      scope: {
        partyTypeEndpoint:'@'
      },
      controller: RelationTypeItemEditDirectiveCtrl,
      controllerAs: 'relationTypeCtrl',
      bindToController: true,
      link: function (scope, element, attrs, controllers) {
        scope.deleteEnabled = deleteEnabled;
        controllers[0].controllers = controllers;
        scope.crudListViewItem = controllers[3];
      }
    };

    return directive;

    /** @ngInject */
    function RelationTypeItemEditDirectiveCtrl($scope) {

      this.partyTypes = Restangular.all(this.partyTypeEndpoint).getList().$object;

      this.addExternalId = function (relationType) {
        if(!relationType.externalIds){
          relationType.externalIds=[]
        }
        relationType.externalIds.push("")
      };

      this.cleanExternalIds= function (relationType) {
        if(relationType.externalIds){
          for (var i = relationType.externalIds.length-1; i >=0; i--) {
            if(relationType.externalIds[i]==null || relationType.externalIds[i].length<1) {
              relationType.externalIds.splice(i,1);
            }
          }
        }
      };

      this.save= function (relationType) {
        this.cleanExternalIds(relationType);
        $scope.crudListViewItem.save(relationType);
      }

    }
  }

})();
