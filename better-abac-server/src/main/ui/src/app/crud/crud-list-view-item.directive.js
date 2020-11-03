(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('crudListViewItem', crudListViewItemDirective);

  /** @ngInject */
  function crudListViewItemDirective($q) {
    var directive = {
      restrict: 'A',
      require: ['crudListViewItem', '^^crudListView', 'crudItem', "^^crudList"],
      transclude: true,
      template: '<div class="panel "  ng-class="{\'panel-info\':!crudItemViewCtrl.isActive && !crudItemViewCtrl.controllers[2].status, \'panel-primary\':crudItemViewCtrl.isActive && (!crudItemViewCtrl.controllers[2].status || crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_SAVING), \'panel-success\':crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_SAVED, \'panel-error\':crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_ERROR}">\
                    <div class="panel-heading cursor-pointer" role="tab" bs-collapse-toggle>\
                      <div class="row" ng-show="crudItemViewCtrl.item.id">\
                        <div class="col-xs-4" ng-bind-html="crudItemViewCtrl.itemTitles[0]"></div><div class="col-xs-4" ng-bind-html="crudItemViewCtrl.itemTitles[1]"></div><div class="col-xs-4" ng-bind-html="crudItemViewCtrl.itemTitles[2]"></div> \
                      </div>\
                      <h4 class="panel-title">\
                          <button class="btn btn-default" ng-show="crudItemViewCtrl.newItemTitle">{{crudItemViewCtrl.newItemTitle}}</button> \
                          <a ng-show="!crudItemViewCtrl.newItemTitle">\
                            <!--<span class="badge">{{ crudItemViewCtrl.item.id }}</span>-->\
                            <span ng-show="crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_SAVED" class="glyphicon glyphicon-ok" aria-hidden="true"></span> \
                            <!--<span ng-bind-html="crudItemViewCtrl.title"></span>--> \
                            <!--<span class="glyphicon glyphicon-menu-right"></span>--> \
                            {{crudItemViewCtrl.newItemTitle}} <!--{{crudItemViewCtrl.controllers[3].crudEndpoint}}--> \
                            <span ng-show="crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_ERROR"> | item was not updated</span>\
                          </a>\
                      </h4>\
                    </div>\
                    <div class="panel-collapse" role="tabpanel" bs-collapse-target>\
                      <div class="panel-body">\
                      <div ng-if="crudItemViewCtrl.isActive"><div ng-transclude></div>\
                      </div>\
                          \
                      </div>\
                    </div>\
                </div>',
      scope: {
        item: '=',
        isActive: '='
      },
      controller: CrudItemViewDirectiveCtrl,
      controllerAs: 'crudItemViewCtrl',
      bindToController: true,
      link: function (scope, element, attrs, controllers) {

        controllers[0].controllers = controllers;

        if (!controllers[0].item || !controllers[0].item.id) {
          controllers[0].newItemTitle = " create new ";
        }
        controllers[0].resetTitle();
      }
    };

    return directive;

    /** @ngInject */
    function CrudItemViewDirectiveCtrl() {

      var _this = this;

      this.title;
      this.itemTitles = [];

      var getTitleFromGenerator = function (titlePropKey) {
        return $q(function (resolve) {
          if (_this.controllers[1].itemTitleGenerators && angular.isObject(_this.controllers[1].itemTitleGenerators) && angular.isFunction(_this.controllers[1].itemTitleGenerators[titlePropKey])) {
            var titGenFn = _this.controllers[1].itemTitleGenerators[titlePropKey];
            //_this.controllers[1].itemTitleGenerators.forEach(function (titGenFn, ind) {
            var titGenRes = titGenFn(_this.item);
            if (angular.isString(titGenRes)) {
              resolve(titGenRes);
            } else if (titGenRes && angular.isFunction(titGenRes.then)) {
              titGenRes.then(function (promisedTitle) {
                resolve(promisedTitle);
              })
            }else{
              resolve( null );
            }
            //});
            /*var titleRes = _this.controllers[1].itemTitleGenerators(this.item);
             if (angular.isString(titleRes)) {
             titleStr = titleRes;
             } else if (titleRes && angular.isFunction(titleRes.then)) {
             titleRes.then(function (promisedTitle) {
             _this.title = promisedTitle;
             })
             }*/
          }else{
            resolve( null );
          }
        })
      };

      this.resetTitle = function () {
        if (this.item && this.item.id) {
          var titleStr = '';
          /*if (_this.controllers[1].itemTitleGenerators && angular.isArray(_this.controllers[1].itemTitleGenerators)) {
            _this.controllers[1].itemTitleGenerators.forEach(function (titGenFn, ind) {
              var titGenRes = titGenFn(this.item, ind);
              if (angular.isString(titGenRes)) {
                _this.itemTitles.push(titGenRes);
              } else if (titleRes && angular.isFunction(titleRes.then)) {
                titleRes.then(function (promisedTitle) {
                  _this.itemTitles[ind] = promisedTitle;
                })
              }
            });
            /!*var titleRes = _this.controllers[1].itemTitleGenerators(this.item);
            if (angular.isString(titleRes)) {
              titleStr = titleRes;
            } else if (titleRes && angular.isFunction(titleRes.then)) {
              titleRes.then(function (promisedTitle) {
                _this.title = promisedTitle;
              })
            }*!/
          }*/

          if (!titleStr && angular.isArray(_this.controllers[1].titleProps)) {
            _this.controllers[1].titleProps.forEach(function (propName, index) {
                /*if (propName.indexOf(';') >= 0) {
                  propName.split(';').forEach(function (p) {
                    var itmVal = _this.item[p.trim()];
                    if (itmVal == null) {
                      itmVal = '';
                    }
                    _this.itemTitles.push(itmVal);
                    titleStr += itmVal + ' ';
                  });
                } else {*/

                  var propName = propName.trim();
                  getTitleFromGenerator(propName).then(function(resTitle){
                    if(resTitle==null){
                      resTitle = _this.item[propName];
                    }
                    if (resTitle == null) {
                      resTitle = propName;
                    }
                    _this.itemTitles[index]=resTitle
                  });

                  ///titleStr += itmVal + ' ';
                //}
              }
            );
          }

          ///_this.title = titleStr;
        }

      }
      ;

      this.delete = function (item) {
        if (item.id) {
          this.controllers[2].delete(item);
          this.controllers[1].closeSelected();
        } else {
          this.controllers[3].clearNewItem();
        }
      };

      this.save = function () {
        _this.resetTitle();
        this.controllers[2].save(this.item).then(function (resObj) {
          if (resObj.id == _this.item.id) {
            _this.item = resObj;
          }
          _this.controllers[3].clearNewItem();
          _this.resetTitle();
        }, function () {
          //leave form on error
        });
      }

    }
  }

})
();
