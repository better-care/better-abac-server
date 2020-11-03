(function() {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('crudListView', crudListViewDirective);

  /** @ngInject */
  function crudListViewDirective() {
    var directive = {
      restrict: 'E',
      transclude:true,
      template: '<div ng-model="panels.activePanel" role="tablist" aria-multiselectable="true" bs-collapse>\
                  <div class="panel-group">\
                    <div crud-list-view-item crud-item item="crudListViewCtrl.controllers[1].newItem" is-active="panels.activePanel===0">\
                      <div ng-transclude></div>\
                    </div>\
                  </div>\
                  <div class="panel">\
                    <div class="panel-heading column-titles">\
                      <div class="row">\
                        <div class="col-xs-4">{{crudListViewCtrl.colTitles[0]}}</div><div class="col-xs-4">{{crudListViewCtrl.colTitles[1]}}</div><div class="col-xs-4">{{crudListViewCtrl.colTitles[2]}}</div> \
                      </div>\
                    </div>\
                  </div>\
                  <div class="panel-group" ng-repeat="crudItem in crudListViewCtrl.controllers[1].crudList">\
                      <div crud-list-view-item crud-item item="crudItem" is-active="panels.activePanel===$index+1">\
                        <div ng-transclude></div>\
                      </div>\
                  </div>\
                 </div>',
      scope: {
        itemTitleGenerators:'='
      },
      require:['crudListView','^^crudList'],
      controller: CrudListViewDirectiveCtrl,
      controllerAs: 'crudListViewCtrl',
      bindToController: true,
      link: function (scope, element, attr, controllers) {
        controllers[0].controllers = controllers;
        controllers[1].clearNewItem();
        controllers[0].titleProps = attr.titleProps ? attr.titleProps.split(';') : [];
        ///controllers[0].itemTitleGenerators = controllers[0].itemTitleGenerators;
        controllers[0].colTitles = attr.columnTitles ? attr.columnTitles.split(';') : [];
      }
    };

    return directive;

    /** @ngInject */
    function CrudListViewDirectiveCtrl($scope) {
      this.closeSelected= function () {
        $scope.panels.activePanel=-1;
      }
    }
  }

})();
