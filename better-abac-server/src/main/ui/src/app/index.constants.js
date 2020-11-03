/* global malarkey:false, moment:false */
(function () {
  'use strict';

  angular
    .module('betterAbacUi')
    .constant('malarkey', malarkey)
    .constant('moment', moment)
    .constant('deleteEnabled', true);

})();
