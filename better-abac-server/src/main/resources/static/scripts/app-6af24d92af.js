!function(){"use strict";var t=angular.module("betterAbacUi",["ngAnimate","ngCookies","ngTouch","ngSanitize","ngMessages","ngAria","restangular","ngRoute","mgcrea.ngStrap","toastr","ui.ace"]);angular.element(document).ready(function(e,i){var keycloakInterval = setInterval(function() {if(typeof Keycloak !== 'undefined'){clearInterval(keycloakInterval);var r=new Keycloak("keycloak.json");r.init({onLoad:"check-sso"}).success(function(i){t.factory("Auth",["$window","$q",function(n,l){var a={loggedIn:i,authz:r,logout:function(){a.authz.logout({redirectUri:n.location})},login:function(){e("*** LOGIN"),a.authz.login().success(function(){a.loggedIn=!0,a.authz=r,a.logoutUrl=r.authServerUrl+"/realms/"+r.realm+"/tokens/logout?redirect_uri="+n.location.href,t.factory("Auth",function(){return a})}).error(function(){e("*** LOGIN-error")})},refreshAccessToken:function(){var t=null!=a.authz.token;return l(function(e,i){a.authz.updateToken().success(function(t){e(a.authz.token)}).error(function(){(!t||confirm("Could not update token. You need to authenticate again."))&&(i(),a.login())})})}};return a}]),i?r.loadUserProfile().success(function(t){var e=angular.bootstrap(document,["betterAbacUi"]);e.invoke(function(e){e.username=t.username})}):angular.bootstrap(document,["betterAbacUi"])}).error(function(){i.location.reload()}),r.onAuthSuccess=function(){},r.onAuthRefreshSuccess=function(){}}}, 50);})}(),function(){"use strict";function t(t){function e(){this.username=t.username,this.logout=t.logout}var i={restrict:"E",templateUrl:"app/components/navbar/navbar.html",scope:{},controller:e,controllerAs:"vm",bindToController:!0};return i}t.$inject=["Auth"],angular.module("betterAbacUi").directive("acmeNavbar",t)}(),function(){"use strict";function t(t,e){function i(t){this.partyTypes=e.all(this.partyTypeEndpoint).getList().$object,this.addExternalId=function(t){t.externalIds||(t.externalIds=[]),t.externalIds.push("")},this.cleanExternalIds=function(t){if(t.externalIds)for(var e=t.externalIds.length-1;e>=0;e--)(null==t.externalIds[e]||t.externalIds[e].length<1)&&t.externalIds.splice(e,1)},this.save=function(e){this.cleanExternalIds(e),t.crudListViewItem.save(e)}}i.$inject=["$scope"];var r={restrict:"A",require:["relationTypeItemEdit","^^crudListView","crudItem","^^crudListViewItem"],template:'<form class="form" role="form">                      <div class="form-group form-inline">                        <label for="name">Name:</label>                        <input type="text" class="form-control" ng-model="crudListViewItem.item.name" id="name">                      </div>                      <div class="form-group form-inline">                        <label for="allowedSourcePartyType">Allowed Source Party Type:</label>                        <select class="form-control" ng-options="partyType.name as partyType.name for partyType in relationTypeCtrl.partyTypes" ng-model="crudListViewItem.item.allowedSourcePartyType"></select>                      </div>                      <div class="form-group form-inline">                        <label for="allowedTargetPartyType">Allowed Target Party Type:</label>                        <select class="form-control" ng-options="partyType.name as partyType.name for partyType in relationTypeCtrl.partyTypes" ng-model="crudListViewItem.item.allowedTargetPartyType"></select>                      </div>                      <div class="form-group">                      <button type="submit" class="btn btn-primary" ng-click="relationTypeCtrl.save(crudListViewItem.item)">Save</button>                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>                      </div>                    </form>',scope:{partyTypeEndpoint:"@"},controller:i,controllerAs:"relationTypeCtrl",bindToController:!0,link:function(e,i,r,n){e.deleteEnabled=t,n[0].controllers=n,e.crudListViewItem=n[3]}};return r}t.$inject=["deleteEnabled","Restangular"],angular.module("betterAbacUi").directive("relationTypeItemEdit",t)}(),function(){"use strict";function t(t){function e(t){this.addExternalId=function(t){t.externalIds||(t.externalIds=[]),t.externalIds.push("")},this.cleanExternalIds=function(t){if(t.externalIds)for(var e=t.externalIds.length-1;e>=0;e--)(null==t.externalIds[e]||t.externalIds[e].length<1)&&t.externalIds.splice(e,1)},this.save=function(e){this.cleanExternalIds(e),t.crudListViewItem.save(e)}}e.$inject=["$scope"];var i={restrict:"A",require:["policyItemEdit","^^crudListView","crudItem","^^crudListViewItem"],template:'<form class="form" role="form">                      <div class="form-group form-inline">                        <label for="name">Name:</label>                        <input type="text" class="form-control" ng-model="crudListViewItem.item.name" id="name">                      </div>                      <div class="form-group form-inline">                        <label for="policy">Policy:</label>                        <div class="form-control" ng-model="crudListViewItem.item.policy" id="policy" ui-ace="{useWrapMode : true,showGutter: true,theme:\'twilight\',mode: \'javascript\'}" style="height: 400px; width: 100%"></div>                      </div>                      <div class="form-group">                      <button type="submit" class="btn btn-primary" ng-click="policyCtrl.save(crudListViewItem.item)">Save</button>                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>                      </div>                    </form>',scope:{isActive:"=",crudEndpolicy:"@"},controller:e,controllerAs:"policyCtrl",bindToController:!0,link:function(e,i,r,n){e.deleteEnabled=t,n[0].controllers=n,e.crudListViewItem=n[3]}};return i}t.$inject=["deleteEnabled"],angular.module("betterAbacUi").directive("policyItemEdit",t)}(),function(){"use strict";function t(t){function e(t){this.addExternalId=function(t){t.externalIds||(t.externalIds=[]),t.externalIds.push("")},this.cleanExternalIds=function(t){if(t.externalIds)for(var e=t.externalIds.length-1;e>=0;e--)(null==t.externalIds[e]||t.externalIds[e].length<1)&&t.externalIds.splice(e,1)},this.save=function(e){this.cleanExternalIds(e),t.crudListViewItem.save(e)}}e.$inject=["$scope"];var i={restrict:"A",require:["partyTypeItemEdit","^^crudListView","crudItem","^^crudListViewItem"],template:'<form class="form" role="form">                      <div class="form-group form-inline">                        <label for="name">Name:</label>                        <input type="text" class="form-control" ng-model="crudListViewItem.item.name" id="name">                      </div>                      <div class="form-group">                      <button type="submit" class="btn btn-primary" ng-click="partyCtrl.save(crudListViewItem.item)">Save</button>                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>                      </div>                    </form>',scope:{isActive:"=",crudEndpoint:"@"},controller:e,controllerAs:"partyCtrl",bindToController:!0,link:function(e,i,r,n){e.deleteEnabled=t,n[0].controllers=n,e.crudListViewItem=n[3]}};return i}t.$inject=["deleteEnabled"],angular.module("betterAbacUi").directive("partyTypeItemEdit",t)}(),function(){"use strict";function t(t,e){function i(t){this.parties=e.all(this.partyEndpoint).getList().$object,this.relationTypes=e.all(this.relationTypeEndpoint).getList().$object,this.addExternalId=function(t){t.externalIds||(t.externalIds=[]),t.externalIds.push("")},this.cleanExternalIds=function(t){if(t.externalIds)for(var e=t.externalIds.length-1;e>=0;e--)(null==t.externalIds[e]||t.externalIds[e].length<1)&&t.externalIds.splice(e,1)},this.save=function(e){this.cleanExternalIds(e),t.crudListViewItem.save(e)}}i.$inject=["$scope"];var r={restrict:"A",require:["partyRelationItemEdit","^^crudListView","crudItem","^^crudListViewItem"],template:'<form class="form" role="form">                      <div class="form-group form-inline">                        <label for="source">Source:</label>                        <select class="form-control" ng-options="sourceParty.id as (sourceParty.type +\' | ids: \'+ sourceParty.externalIds.join(\', \')) for sourceParty in partyRelationCtrl.parties" ng-model="crudListViewItem.item.source"></select>                      </div>                      <div class="form-group form-inline">                        <label for="relationType">Relation Type:</label>                        <select class="form-control" ng-options="relationType.name as relationType.name for relationType in partyRelationCtrl.relationTypes" ng-model="crudListViewItem.item.relationType"></select>                      </div>                      <div class="form-group form-inline">                        <label for="target">Target:</label>                        <select class="form-control" ng-options="sourceParty.id as (sourceParty.type +\' | ids: \'+ sourceParty.externalIds.join(\', \')) for sourceParty in partyRelationCtrl.parties" ng-model="crudListViewItem.item.target"></select>                        <select class="form-control" ng-options="sourceParty.id as (sourceParty.type +\' | ids: \'+ sourceParty.externalIds.join(\', \')) for sourceParty in partyRelationCtrl.parties" ng-model="crudListViewItem.item.target"></select>                      </div>                      <div class="form-group form-inline">                        <label class="control-label"><i class="fa fa-calendar"></i> Valid Until:</label><br>                        <div class="form-group">                          <input type="text" size="10" class="form-control" ng-model="crudListViewItem.item.validUntil" data-autoclose="1" placeholder="Date" bs-datepicker>                        </div>                        <div class="form-group">                          <input type="text" size="8" class="form-control" ng-model="crudListViewItem.item.validUntil" data-time-format="h:mm:ss a" data-autoclose="1" placeholder="Time" bs-timepicker>                        </div>                      </div>                      <div class="form-group">                      <button type="submit" class="btn btn-primary" ng-click="partyRelationCtrl.save(crudListViewItem.item)">Save</button>                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>                      </div>                    </form>',scope:{isActive:"=",relationTypeEndpoint:"@",partyEndpoint:"@"},controller:i,controllerAs:"partyRelationCtrl",bindToController:!0,link:function(e,i,r,n){e.deleteEnabled=t,n[0].controllers=n,e.crudListViewItem=n[3]}};return r}t.$inject=["deleteEnabled","Restangular"],angular.module("betterAbacUi").directive("partyRelationItemEdit",t)}(),function(){"use strict";function t(t,e){function i(t){this.partyTypes=e.all(this.partyTypeEndpoint).getList().$object,this.addExternalId=function(t){t.externalIds||(t.externalIds=[]),t.externalIds.push("")},this.cleanExternalIds=function(t){if(t.externalIds)for(var e=t.externalIds.length-1;e>=0;e--)(null==t.externalIds[e]||t.externalIds[e].length<1)&&t.externalIds.splice(e,1)},this.save=function(e){this.cleanExternalIds(e),t.crudListViewItem.save(e)}}i.$inject=["$scope"];var r={restrict:"A",require:["partyItemEdit","^^crudListView","crudItem","^^crudListViewItem"],template:'<form class="form" role="form">                      <div class="form-group form-inline">                        <label for="type">Type:</label>                        <select class="form-control" ng-options="partyType.name as partyType.name for partyType in partyCtrl.partyTypes" ng-model="crudListViewItem.item.type"></select>                      </div>                      <div class="form-group form-inline">                        <label for="exId">External Ids:</label>                        <input type="text" class="form-control" ng-repeat="exId in crudListViewItem.item.externalIds track by $index" ng-model="crudListViewItem.item.externalIds[$index]" ng-blur="partyCtrl.cleanExternalIds(crudListViewItem.item)">                        <button type="button" ng-click="partyCtrl.addExternalId(crudListViewItem.item)" class="btn btn-success">+</button>                       </div>                      <div class="form-group">                      <button type="submit" class="btn btn-primary" ng-click="partyCtrl.save(crudListViewItem.item)">Save</button>                      <button ng-show="deleteEnabled" type="button" class="btn btn-default" ng-click="crudListViewItem.delete(crudListViewItem.item)"><span ng-show="crudListViewItem.item.id">Delete</span><span ng-show="!crudListViewItem.item.id">Clear</span></button>                      </div>                    </form>',scope:{partyTypeEndpoint:"@"},controller:i,controllerAs:"partyCtrl",bindToController:!0,link:function(e,i,r,n){e.deleteEnabled=t,n[0].controllers=n,e.crudListViewItem=n[3]}};return r}t.$inject=["deleteEnabled","Restangular"],angular.module("betterAbacUi").directive("partyItemEdit",t)}(),function(){"use strict";function t(t,e,i){var r=this,n=t.path().substr(1);r.selectedEndpoint=n;var l=function(t){return e(function(e,r){i.one("party",t).get().then(function(t){e(t)})})};r.partyTitleGenerators={fullName:function(t){return t.fullName?t.fullName:t.externalIds?t.externalIds.join(", "):"no external ids"}},r.createPartyRelationTitleGenerators={target:function(t){var i="";return t&&t.target?e(function(e){r.createPartyRelationTitleGenerators.titleForPartyId(t.target,e)}):i},titleForPartyId:function(t,e){l(t).then(function(t){var i=t.type;null!=t.externalIds&&(i+=t.fullName?" "+t.fullName:t.externalIds.join(", ")),e(i)})},source:function(t){var i="";return t&&t.source?e(function(e){r.createPartyRelationTitleGenerators.titleForPartyId(t.source,e)}):i}}}t.$inject=["$location","$q","Restangular"],angular.module("betterAbacUi").controller("MainCtrl",t)}(),function(){"use strict";function t(t){function e(e,i,r){this.newItem,this.clearNewItem=function(){this.newItem=this.getNewItem(this.crudEndpoint)},this.addNewItem=function(t){this.crudList.push(t)},this.getNewItem=function(e){return t.one(e,null)},this.removeItem=function(t){if(!r)return void alert("Removing item is not allowed.");for(var e=0;e<this.crudList.length;e++){var i=this.crudList[e];if(i.id==t.id){this.crudList.splice(e,1);break}}}}e.$inject=["$scope","$element","deleteEnabled"];var i={restrict:"A",template:"",controller:e,controllerAs:"crudListCtrl",bindToController:!0,require:["crudList"],link:function(e,i,r,n){n[0].controllers=n,null==n[0].crudList&&(n[0].crudList=t.all(n[0].crudEndpoint).getList().$object)},scope:{crudList:"=",crudEndpoint:"@"}};return i}t.$inject=["Restangular"],angular.module("betterAbacUi").directive("crudList",t)}(),function(){"use strict";function t(){function t(t){this.closeSelected=function(){t.panels.activePanel=-1}}t.$inject=["$scope"];var e={restrict:"E",transclude:!0,template:'<div ng-model="panels.activePanel" role="tablist" aria-multiselectable="true" bs-collapse>                  <div class="panel-group">                    <div crud-list-view-item crud-item item="crudListViewCtrl.controllers[1].newItem" is-active="panels.activePanel===0">                      <div ng-transclude></div>                    </div>                  </div>                  <div class="panel">                    <div class="panel-heading column-titles">                      <div class="row">                        <div class="col-xs-4">{{crudListViewCtrl.colTitles[0]}}</div><div class="col-xs-4">{{crudListViewCtrl.colTitles[1]}}</div><div class="col-xs-4">{{crudListViewCtrl.colTitles[2]}}</div>                       </div>                    </div>                  </div>                  <div class="panel-group" ng-repeat="crudItem in crudListViewCtrl.controllers[1].crudList">                      <div crud-list-view-item crud-item item="crudItem" is-active="panels.activePanel===$index+1">                        <div ng-transclude></div>                      </div>                  </div>                 </div>',scope:{itemTitleGenerators:"="},require:["crudListView","^^crudList"],controller:t,controllerAs:"crudListViewCtrl",bindToController:!0,link:function(t,e,i,r){r[0].controllers=r,r[1].clearNewItem(),r[0].titleProps=i.titleProps?i.titleProps.split(";"):[],r[0].colTitles=i.columnTitles?i.columnTitles.split(";"):[]}};return e}angular.module("betterAbacUi").directive("crudListView",t)}(),function(){"use strict";function t(t){function e(){var e=this;this.title,this.itemTitles=[];var i=function(i){return t(function(t){if(e.controllers[1].itemTitleGenerators&&angular.isObject(e.controllers[1].itemTitleGenerators)&&angular.isFunction(e.controllers[1].itemTitleGenerators[i])){var r=e.controllers[1].itemTitleGenerators[i],n=r(e.item);angular.isString(n)?t(n):n&&angular.isFunction(n.then)?n.then(function(e){t(e)}):t(null)}else t(null)})};this.resetTitle=function(){if(this.item&&this.item.id){var t="";!t&&angular.isArray(e.controllers[1].titleProps)&&e.controllers[1].titleProps.forEach(function(t,r){var t=t.trim();i(t).then(function(i){null==i&&(i=e.item[t]),null==i&&(i=t),e.itemTitles[r]=i})})}},this["delete"]=function(t){t.id?(this.controllers[2]["delete"](t),this.controllers[1].closeSelected()):this.controllers[3].clearNewItem()},this.save=function(){e.resetTitle(),this.controllers[2].save(this.item).then(function(t){t.id==e.item.id&&(e.item=t),e.controllers[3].clearNewItem(),e.resetTitle()},function(){})}}var i={restrict:"A",require:["crudListViewItem","^^crudListView","crudItem","^^crudList"],transclude:!0,template:'<div class="panel "  ng-class="{\'panel-info\':!crudItemViewCtrl.isActive && !crudItemViewCtrl.controllers[2].status, \'panel-primary\':crudItemViewCtrl.isActive && (!crudItemViewCtrl.controllers[2].status || crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_SAVING), \'panel-success\':crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_SAVED, \'panel-error\':crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_ERROR}">                    <div class="panel-heading cursor-pointer" role="tab" bs-collapse-toggle>                      <div class="row" ng-show="crudItemViewCtrl.item.id">                        <div class="col-xs-4" ng-bind-html="crudItemViewCtrl.itemTitles[0]"></div><div class="col-xs-4" ng-bind-html="crudItemViewCtrl.itemTitles[1]"></div><div class="col-xs-4" ng-bind-html="crudItemViewCtrl.itemTitles[2]"></div>                       </div>                      <h4 class="panel-title">                          <button class="btn btn-default" ng-show="crudItemViewCtrl.newItemTitle">{{crudItemViewCtrl.newItemTitle}}</button>                           <a ng-show="!crudItemViewCtrl.newItemTitle">                            <!--<span class="badge">{{ crudItemViewCtrl.item.id }}</span>-->                            <span ng-show="crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_SAVED" class="glyphicon glyphicon-ok" aria-hidden="true"></span>                             <!--<span ng-bind-html="crudItemViewCtrl.title"></span>-->                             <!--<span class="glyphicon glyphicon-menu-right"></span>-->                             {{crudItemViewCtrl.newItemTitle}} <!--{{crudItemViewCtrl.controllers[3].crudEndpoint}}-->                             <span ng-show="crudItemViewCtrl.controllers[2].status==crudItemViewCtrl.controllers[2].STATUS_ERROR"> | item was not updated</span>                          </a>                      </h4>                    </div>                    <div class="panel-collapse" role="tabpanel" bs-collapse-target>                      <div class="panel-body">                      <div ng-if="crudItemViewCtrl.isActive"><div ng-transclude></div>                      </div>                                                </div>                    </div>                </div>',scope:{item:"=",isActive:"="},controller:e,controllerAs:"crudItemViewCtrl",bindToController:!0,link:function(t,e,i,r){r[0].controllers=r,r[0].item&&r[0].item.id||(r[0].newItemTitle=" create new "),r[0].resetTitle()}};return i}t.$inject=["$q"],angular.module("betterAbacUi").directive("crudListViewItem",t)}(),function(){"use strict";function t(){function t(t,e,i,r,n){var l=this;this.status="",this.STATUS_SAVED="saved",this.STATUS_SAVING="saving",this.STATUS_ERROR="error",this.STATUS_IDLE="",this.statusTmt,this.save=function(t){this.status=this.STATUS_SAVING;var e=!t.id;return r(function(i,r){t.save().then(function(t){e&&l.controllers[1].addNewItem(t),a(l.STATUS_SAVED),i(t,l.STATUS_SAVED)},function(){a(l.STATUS_ERROR),r(l.STATUS_ERROR)})})},this["delete"]=function(t){if(t.id){if(!n)return void alert("Deleting is not allowed.");t.remove().then(function(){l.controllers[1].removeItem(t)},function(){a(l.STATUS_ERROR)})}else this.controllers[1].clearNewItem()};var a=function(t){l.status=t,i.cancel(l.statusTmt),l.statusTmt=i(function(){l.status=""},2e3)}}t.$inject=["$scope","$element","$timeout","$q","deleteEnabled"];var e={restrict:"A",template:"",controller:t,controllerAs:"crudItemCtrl",bindToController:!0,require:["crudItem","^^crudList"],link:function(t,e,i,r){r[0].controllers=r}};return e}angular.module("betterAbacUi").directive("crudItem",t)}(),function(){"use strict";function t(t,e,i,r,n){function l(t){return{Authorization:"Bearer "+t}}ace&&ace.config.set("basePath","aceFiles"),n.$watch(function(){return i.authz.token},function(t){t&&e.setDefaultHeaders(l(t))}),e.setErrorInterceptor(function(t,e,n){return 401===t.status||-1===t.status?(i.refreshAccessToken().then(function(i){angular.merge(t.config.headers,l(i)),r(t.config).then(n,e.reject)}),!1):!0})}t.$inject=["$log","Restangular","Auth","$http","$rootScope"],angular.module("betterAbacUi").run(t)}(),function(){"use strict";function t(t){t.when("/party",{templateUrl:"app/main/main-list.html",controller:"MainCtrl",controllerAs:"mainCtrl"}).when("/partyType",{templateUrl:"app/main/main-list.html",controller:"MainCtrl",controllerAs:"mainCtrl"}).when("/partyRelation",{templateUrl:"app/main/main-list.html",controller:"MainCtrl",controllerAs:"mainCtrl"}).when("/relationType",{templateUrl:"app/main/main-list.html",controller:"MainCtrl",controllerAs:"mainCtrl"}).when("/policy",{templateUrl:"app/main/main-list.html",controller:"MainCtrl",controllerAs:"mainCtrl"}).otherwise({redirectTo:"/party"})}t.$inject=["$routeProvider"],angular.module("betterAbacUi").config(t)}(),function(){"use strict";angular.module("betterAbacUi").constant("malarkey",malarkey).constant("moment",moment).constant("deleteEnabled",!0)}(),function(){"use strict";function t(t,e,i,r){t.debugEnabled(!0);var n="rest/v1/admin";"localhost:3000"==location.host&&(n="http://localhost:8080"+n),i.setBaseUrl(n),angular.extend(r.defaults,{activeClass:"in"})}t.$inject=["$logProvider","toastrConfig","RestangularProvider","$navbarProvider"],angular.module("betterAbacUi").config(t)}(),angular.module("betterAbacUi").run(["$templateCache",function(t){t.put("app/main/main-list.html",'<div class="container"><div><acme-navbar></acme-navbar></div><div ng-if="mainCtrl.selectedEndpoint==\'party\'" crud-list="mainCtrl.partyList" crud-endpoint="{{mainCtrl.selectedEndpoint}}"><h2>Party</h2><crud-list-view title-props="id;fullName;type" item-title-generators="mainCtrl.partyTitleGenerators" column-titles="Id;Name;Type"><div party-item-edit="" crud-item="" party-type-endpoint="partyType"></div></crud-list-view></div><div ng-if="mainCtrl.selectedEndpoint==\'partyType\'" crud-list="mainCtrl.partyTypeList" crud-endpoint="{{mainCtrl.selectedEndpoint}}"><h2>Party Type</h2><crud-list-view title-props="id; name" column-titles="Id;Name"><div party-type-item-edit="" crud-item=""></div></crud-list-view></div><div ng-if="mainCtrl.selectedEndpoint==\'policy\'" crud-list="mainCtrl.partyTypeList" crud-endpoint="{{mainCtrl.selectedEndpoint}}"><h2>Policy</h2><crud-list-view title-props="name"><div policy-item-edit="" crud-item=""></div></crud-list-view></div><div ng-if="mainCtrl.selectedEndpoint==\'relationType\'" crud-list="mainCtrl.partyTypeList" crud-endpoint="{{mainCtrl.selectedEndpoint}}"><h2>Relation Type</h2><crud-list-view title-props="id; name" column-titles="Id;Name"><div relation-type-item-edit="" crud-item="" party-type-endpoint="partyType"></div></crud-list-view></div><div ng-if="mainCtrl.selectedEndpoint==\'partyRelation\'" crud-list="mainCtrl.partyTypeList" crud-endpoint="{{mainCtrl.selectedEndpoint}}"><h2>Party Relation</h2><crud-list-view title-props="source;relationType; target" item-title-generators="mainCtrl.createPartyRelationTitleGenerators" column-titles="Source;Relation Type; Target"><div party-relation-item-edit="" crud-item="" party-endpoint="party" relation-type-endpoint="relationType"></div></crud-list-view></div></div>'),t.put("app/components/navbar/navbar.html",'<nav class="navbar navbar-default" role="navigation" bs-navbar=""><div class="navbar-header"><a class="navbar-brand" href="#">Better ABAC</a></div><ul class="nav navbar-nav navbar-left"><li data-match-route="/party"><a href="#/party">Party</a></li><li data-match-route="/partyRelation"><a href="#/partyRelation">Party Relation</a></li><li data-match-route="/policy"><a href="#/policy">Policy</a></li><li data-match-route="/partyType"><a href="#/partyType">Party Type</a></li><li data-match-route="/relationType"><a href="#/relationType">Relation Type</a></li></ul><ul class="nav navbar-nav navbar-right"><li class="navbar-username"><a ng-click="vm.logout()" class="navbar-link"><span class="glyphicon glyphicon glyphicon-off alert"></span>{{vm.username}}</a></li></ul></nav>')}]);
//# sourceMappingURL=../maps/scripts/app-6af24d92af.js.map