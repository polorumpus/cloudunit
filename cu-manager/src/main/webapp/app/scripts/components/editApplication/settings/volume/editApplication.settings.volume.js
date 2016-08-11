/*
 * LICENCE : CloudUnit is available under the Affero Gnu Public License GPL V3 : https://www.gnu.org/licenses/agpl-3.0.html
 *     but CloudUnit is licensed too under a standard commercial license.
 *     Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 *     If you are not sure whether the GPL is right for you,
 *     you can always test our software under the GPL and inspect the source code before you contact us
 *     about purchasing a commercial license.
 *
 *     LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
 *     or promote products derived from this project without prior written permission from Treeptik.
 *     Products or services derived from this software may not be called "CloudUnit"
 *     nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 *     For any questions, contact us : contact@treeptik.fr
 */

(function () {
  'use strict';
  angular
    .module('webuiApp.editApplication')
    .component('volumeComponent', volumeComponent());

  function volumeComponent(){
    return {
      templateUrl: 'scripts/components/editApplication/settings/volume/editApplication.settings.volume.html',
      bindings: {
        application: '<app'
      },
      controller: [
        '$stateParams',
        '$q',
        'ApplicationService',
        'ErrorService',
        volumeCtrl
      ],
      controllerAs: 'volume',
    }
  }

  function volumeCtrl($stateParams, $q, ApplicationService, ErrorService) {

    var vm = this;
    vm.volumes = [];
    vm.containers = [];
    vm.myContainer = {};
    vm.isLoading = true;
    vm.pageSize = 5;
    vm.currentPage = 1;
    vm.volumeName = '';
    vm.volumePath = '';
    vm.addNoticeMsg = '';
    vm.addErrorMsg = '';
    vm.manageNoticeMsg = '';
    vm.manageErrorMsg = '';

    vm.predicate = 'name';
    vm.reverse = false;
    vm.order = order;
    
    vm.addVolume = addVolume;
    vm.editVolume = editVolume;
    vm.deleteVolume = deleteVolume;

    vm.$onInit = function() {  
      getContainers()
      .then(function() {
       getListVolume();
      })
      .catch(function(response) {
         ErrorService.handle(response);
      });
    }

    ////////////////////////////////////////////////

    function getListVolume() {
      ApplicationService.getListSettingsVolume($stateParams.name, vm.myContainer.id)
        .then(function(response) {
          vm.volumes = response;
        })
        .catch(function(response) {
          ErrorService.handle(response);
        });
    }

    function getContainers (selectedContainer) {
      var deferred = $q.defer ();
      vm.isLoading = true;
      ApplicationService.listContainers ( $stateParams.name )
        .then ( function ( containers ) {
          vm.containers = containers;
          vm.myContainer = selectedContainer || containers[0];
          vm.isLoading = false;
          deferred.resolve ( containers );
        } )
        .catch ( function ( response ) {
          deferred.reject ( response );
        } );
        return deferred.promise;
    }

    function deleteVolume (volume) {
      console.log('deleteVolume');
      ApplicationService.deleteVolume (  $stateParams.name, vm.myContainer.id, volume.id )
        .then ( function() {
          cleanMessage();
          vm.volumes.splice(vm.volumes.indexOf(volume), 1);
          vm.manageNoticeMsg = 'The volume has been removed!'
        } )
        .catch (errorManageVolume);
    }
    
    function editVolume (volumeID, volumeName, volumePath) {
      console.log('editVolume');
      ApplicationService.editVolume ( $stateParams.name, vm.myContainer.id, volumeID, volumeName, volumePath )
        .then(function(volume) {
          cleanMessage();
          var elementPos = vm.volumes.map(function(x) {return x.id; }).indexOf(volumeID);
          vm.volumes[elementPos] = volume;
          vm.manageNoticeMsg = 'The volume has been edited!'
        })
        .catch ( function(response) {
          getListVolume();
          errorManageVolume(response);
        } );
    }

    function addVolume (volumeName, volumePath) {
      console.log(volumePath);
      ApplicationService.addVolume (  $stateParams.name, vm.myContainer.id, volumeName, volumePath )
        .then ( function(volume) {
          cleanMessage();
          vm.volumes.push(volume);
          vm.volumeName = '';
          vm.volumePath = '';
          vm.addNoticeMsg = 'volume successfully created !';
        } )
        .catch (errorAddVolume);
    }

    function errorAddVolume (res) {
      cleanMessage();
      if(res.data.message) {
        vm.addErrorMsg = res.data.message;
      } else {
        vm.addErrorMsg = 'An error has been encountered !';
      }
    }

    function errorManageVolume (res) {
      cleanMessage();
      if(res.data.message) {
        vm.manageErrorMsg = res.data.message;
      } else {
        vm.manageErrorMsg = 'An error has been encountered !';
      };
    }

    function cleanMessage() {
      vm.addErrorMsg = '';
      vm.addNoticeMsg = '';
      vm.manageErrorMsg = '';
      vm.manageNoticeMsg = '';
    }

    function order (predicate) {
      vm.reverse = (vm.predicate === predicate) ? !vm.reverse : false;
      vm.predicate = predicate;
    }
  }
})();
