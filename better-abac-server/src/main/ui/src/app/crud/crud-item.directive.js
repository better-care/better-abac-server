(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .directive('crudItem', crudItemDirective);

  /** @ngInject */
  function crudItemDirective() {
    var directive = {
      restrict: 'A',
      template: '',
      controller: CrudItemDirectiveCtrl,
      controllerAs: 'crudItemCtrl',
      bindToController: true,
      require: ['crudItem', '^^crudList'],
      link: function (scope, el, attr, controllers) {
        controllers[0].controllers = controllers;
      }
    };

    return directive;

    /** @ngInject */
    function CrudItemDirectiveCtrl($scope, $element, $timeout, $q, deleteEnabled) {
      var _this = this;
      this.status = '';
      this.STATUS_SAVED = 'saved';
      this.STATUS_SAVING = 'saving';
      this.STATUS_ERROR = 'error';
      this.STATUS_IDLE = '';
      this.statusTmt;


      this.save = function (crudItem) {
        this.status = this.STATUS_SAVING;
        var isNew = !crudItem.id;
        return $q(function (resolve, reject) {
          crudItem.save().then(
            function (res) {
              if (isNew) {
                _this.controllers[1].addNewItem(res);
              }
              setStatusTimeout(_this.STATUS_SAVED);
              resolve(res, _this.STATUS_SAVED);
            },
            function () {
              setStatusTimeout(_this.STATUS_ERROR);
              reject(_this.STATUS_ERROR)
            });
        });
      };

      this.delete = function (crudItem) {
        if (crudItem.id) {
          if (!deleteEnabled) {
            alert("Deleting is not allowed.");
            return
          }
          crudItem.remove().then(function () {
            _this.controllers[1].removeItem(crudItem);
          }, function () {
            setStatusTimeout(_this.STATUS_ERROR)
          });
        } else {
          this.controllers[1].clearNewItem();
        }
      };

      var setStatusTimeout = function (status) {
        _this.status = status;
        $timeout.cancel(_this.statusTmt);
        _this.statusTmt = $timeout(function () {
          _this.status = '';
        }, 2000);
      }

    }
  }

})();
