(function() {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('policyItemEdit', policyItemEditDirective);

  /** @ngInject */
  function policyItemEditDirective(deleteEnabled) {
    var directive = {
      restrict: 'A',
      require:['policyItemEdit','^^crudListView', 'crudItem', '^^crudListViewItem'],
      template: '<form class="form" role="form">\
                      <div class="form-group form-inline">\
                        <label for="name">Name:</label>\
                        <input type="text" class="form-control" ng-model="crudListViewItem.item.name" id="name">\
                      </div>\
                      <div class="form-group form-inline">\
                        <label for="policy">Policy:</label>\
                        <div class="form-control" ng-model="crudListViewItem.item.policy" id="policy" ui-ace="{useWrapMode : true,showGutter: true,theme:\'twilight\',mode: \'javascript\'}" style="height: 400px; width: 100%"></div>\
                      </div>\
                      <div class="form-group">\
                      <button type="submit" class="btn btn-primary" ng-click="policyCtrl.save(crudListViewItem.item)">Save</button>\
                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>\
                      </div>\
                    </form>',
      scope: {
        isActive:'=',
        crudEndpolicy:'@'
      },
      controller: PolicyItemEditDirectiveCtrl,
      controllerAs: 'policyCtrl',
      bindToController: true,
      link: function (scope, element, attrs, controllers) {
        scope.deleteEnabled = deleteEnabled;
        controllers[0].controllers = controllers;
        scope.crudListViewItem = controllers[3];
      }
    };

    return directive;

    /** @ngInject */
    function PolicyItemEditDirectiveCtrl($scope) {

      this.addExternalId = function (policy) {
        if(!policy.externalIds){
          policy.externalIds=[]
        }
        policy.externalIds.push("");
      };

      this.cleanExternalIds= function (policy) {
        if(policy.externalIds){
          for (var i = policy.externalIds.length-1; i >=0; i--) {
            if(policy.externalIds[i]==null || policy.externalIds[i].length<1) {
              policy.externalIds.splice(i,1);
            }
          }
        }
      };

      this.save= function (policy) {
        this.cleanExternalIds(policy);
        $scope.crudListViewItem.save(policy);
      }

    }
  }

})();
