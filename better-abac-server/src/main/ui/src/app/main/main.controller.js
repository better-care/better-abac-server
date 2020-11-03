(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .controller('MainCtrl', MainCtrl);

  /** @ngInject */
  function MainCtrl($location, $q, Restangular) {
    var _this = this;
    var endpointPath = $location.path().substr(1);
    _this.selectedEndpoint = endpointPath;

    var getPartyIds = function (partyId) {
      return $q(function (resolve, reject) {
        Restangular.one('party', partyId).get().then(function (sourceItem) {
          resolve(sourceItem);
        });
      });
    };

    _this.partyTitleGenerators = {
      fullName: function (item) {
        if (!item.fullName) {
          return item.externalIds ? item.externalIds.join(', ') : 'no external ids'
        }
        return item.fullName;
      }
    };

    _this.createPartyRelationTitleGenerators = {

      target: function (item) {
        var retStrTitle = '';
        if (item && item.target) {
          return $q(function (resolve) {
            _this.createPartyRelationTitleGenerators.titleForPartyId(item.target, resolve);
          });
        }
        return retStrTitle;
      },

      titleForPartyId: function (forPartyId, resolve) {
        getPartyIds(forPartyId).then(function (p) {
          var retStrTitle = p.type;
          if (p.externalIds != null) {
            if (p.fullName) {
              retStrTitle += ' ' + p.fullName;
            } else {
              retStrTitle += ' ' + p.externalIds ? p.externalIds.join(', ') : '';
            }
          }
          resolve(retStrTitle)
        });
      },

      source: function (item) {
        var retStrTitle = '';
        if (item && item.source) {
          return $q(function (resolve) {
            _this.createPartyRelationTitleGenerators.titleForPartyId(item.source, resolve);
          });
        }
        return retStrTitle;
      }
    }
  }
})();
