angular.module('webProjet', []).controller('mainController', function($scope,$http) {

	  $scope.test = "";
	  $scope.showTable = false;
	  $scope.showBookForm = false;
	  $scope.availableRooms = [];
	  $scope.sizeTable = 'col-md-12';
	  $scope.nbHours = "";
	  $scope.myResa = "";
	  $scope.showResa = false;
	  $scope.listRooms = [];
	  $scope.nbPers = 1;
	  $scope.showSpinner = true;
	 
	  
	  
	  $scope.mails = [];
	  $scope.addMail = function () {
	      $scope.mails.push('');
	  };

	  $scope.removeMail = function (z) {
		  $scope.mails.splice(z,1);
	  };
	  
	  
	  
	  
	  var today = new Date();

	  $scope.dateSearch = {
	        value: new Date(today.getFullYear(),today.getMonth(),today.getDate())
	      };
	  //value: new Date(today.getFullYear(), today.getMonth(), today.getDate(), today.getHours(), today.getMinutes())
	  $scope.rechercher   = '';  
	  
	  //initialisation de la liste de toute les salles existantes
	  $http({
		    url: '/_ah/api/monapi/v1/rooms/liste',
		    method: 'GET'
		}).success(function (data) {
			for(var i=0; i < data.items.length; i++){
				$scope.listRooms.push({
					"name" : data.items[i].properties.name,
					"nbPers" : 0,
					"timeTable": {
						"first":{
							"kind" : "empty",
							"nbPers" : 0
						},
						"second":{
							"kind" : "empty",
							"nbPers" : 0
						},
						"third":{
							"kind" : "empty",
							"nbPers" : 0
						},
						"fourth":{
							"kind" : "empty",
							"nbPers" : 0
						},
						"fifth":{
							"kind" : "empty",
							"nbPers" : 0
						},
						"sixth":{
							"kind" : "empty",
							"nbPers" : 0
						},
					}
				});
			}
		});
	  
	  
	  $scope.$watch("dateSearch.value", function() {
		  getSchedule();
		  
	  });
	  
	  $scope.book = function(name,when,index){
		  $scope.bookRoom = {
				  name:name,
				  when: when
		  };
		  $scope.showBookForm = true;
		  $scope.sizeTable = 'col-md-8';
	  }
	  
	  
	  
	  $scope.requestBook = function(){
		  var userId = $("#idUser").attr("data-variable");
		  var StringDate = ($scope.dateSearch.value).toISOString().split('.')[0]+"Z";
		  StringDate = StringDate.slice(0,-4);
		  console.log($scope.inputsMails);

		  $http({
			    url: '/_ah/api/monapi/v1/reserve',
			    method: 'GET',
			    params: {
			        bookingDate : StringDate,
			        room : $scope.bookRoom.name,
			        when : $scope.bookRoom.when,
			        mails : JSON.stringify($scope.mails),
			        userId : userId,
			        nbPers : $scope.nbPers
			    }
			}).success(function (data) {
				  $scope.sizeTable = 'col-md-12';
				  $scope.showBookForm = false;
				  
				  //display success alert
				  $("#successMessage").fadeIn();
				  window.setTimeout(function () {
					  $("#successMessage").fadeOut(300)
					}, 3000);
				  requestGetReservation();
				  getSchedule();
			}).error(function (data,err) {
				console.log(err);
				console.log(data);
				  //display error alert
				  $("#errorMessage").fadeIn();
				  window.setTimeout(function () {
					  $("#errorMessage").fadeOut(300)
					}, 3000);
				   
			});
	  }
	  
	  $scope.displayResa = function(){
		  if(!$scope.showResa){
			  requestGetReservation();
		  }else{
			  $scope.showResa = false;
		  }
	  }
	  
	  $scope.cancelResa = function(id,key){
		  $http.delete('/_ah/api/monapi/v1/cancelResa/' + id).success(function(response) {
			  $scope.myResa.splice(key,1);
			  initializeListRoom();
			  getSchedule();
		  }).error(function(error) {
			//display error alert
			  $("#errorMessage").fadeIn();
			  window.setTimeout(function () {
				  $("#errorMessage").fadeOut(300)
				}, 3000);
		  });
		  
	  }
	  
	  function requestGetReservation(){
		  var userId = $("#idUser").attr("data-variable");
		  $http({
			    url: '/_ah/api/monapi/v1/myResa/'+userId,
			    method: 'GET'
			}).success(function (data) {
				  console.log(data);
				  $scope.myResa = data.items;
				  $scope.showResa = true;
			})
	  }
	  
	  function initializeListRoom(){
		  for(var i=0; i < $scope.listRooms.length; i++){
			  $scope.listRooms[i].timeTable.first.kind = "empty";
			  $scope.listRooms[i].timeTable.second.kind = "empty";
			  $scope.listRooms[i].timeTable.third.kind = "empty";
			  $scope.listRooms[i].timeTable.fourth.kind = "empty";
			  $scope.listRooms[i].timeTable.fifth.kind = "empty";
			  $scope.listRooms[i].timeTable.sixth.kind = "empty";
			  
			  $scope.listRooms[i].timeTable.first.nbPers = 0;
			  $scope.listRooms[i].timeTable.second.nbPers = 0;
			  $scope.listRooms[i].timeTable.third.nbPers = 0;
			  $scope.listRooms[i].timeTable.fourth.nbPers = 0;
			  $scope.listRooms[i].timeTable.fifth.nbPers = 0;
			  $scope.listRooms[i].timeTable.sixth.nbPers = 0;
		  }
	  }
	  
	  function getSchedule(){
		  $scope.sizeTable = 'col-md-12';
		  $scope.showBookForm = false;
			initializeListRoom();
			console.log($scope.dateSearch.value);
			var StringDate = ($scope.dateSearch.value).toISOString().split('.')[0]+"Z";
			
			StringDate = StringDate.slice(0,-4);
			$http({
			    url: '/_ah/api/monapi/v1/rooms/eventsList/'+StringDate,
			    method: 'GET'
			}).success(function (data) {
				if(data)
				$scope.showTable = true;
				$scope.listEvents = data.items;
				
				if(data.items != undefined || data.items != null){
				
					for(var i=0; i < $scope.listRooms.length; i++){
						for(var j=0; j < data.items.length; j++){
							
							if(data.items[j].properties.location.includes($scope.listRooms[i].name)){
								var dateStart = new Date(+data.items[j].properties.start);
								var dateEnd = new Date(+data.items[j].properties.end);
								console.log(dateStart);
								console.log(dateEnd);
								
								var diff = Math.abs(dateEnd - dateStart);
								
								var dt = new Date(diff);
								var nbSlot = dt.getHours();
								var nbMinutes = dt.getMinutes();
								
								if(nbSlot == 1 && nbMinutes > 20 || nbSlot == 2 && nbMinutes > 50 || nbSlot == 4 && minutes > 0){
									
								}else{
									nbSlot = nbSlot -1;
								}
								
										
								var hours = dateStart.getHours();
								var minutes = dateStart.getMinutes();
								
								var kind = data.items[j].key.kind;
	
								var moreHour = false;
								
								if(hours == 8 || hours == 9 && minutes <=20 ){
				            		$scope.listRooms[i].timeTable.first.kind = kind;
									$scope.listRooms[i].timeTable.first.nbPers = parseInt($scope.listRooms[i].timeTable.first.nbPers) + parseInt(data.items[j].properties.nbPers);						
									nbSlot = nbSlot - 1;
									moreHour = true;
								}
								
								if(hours == 9 && minutes >20 || hours == 10 && minutes <=50 || nbSlot > 0 && moreHour){
				            		$scope.listRooms[i].timeTable.second.kind = kind;
									$scope.listRooms[i].timeTable.second.nbPers = parseInt($scope.listRooms[i].timeTable.second.nbPers) + parseInt(data.items[j].properties.nbPers);
									nbSlot = nbSlot - 1;
									moreHour = true;
								}
								
								if(hours == 10 && minutes > 50 || hours == 11 || hours == 12 || nbSlot > 0 && moreHour){
				            		$scope.listRooms[i].timeTable.third.kind = kind;
									$scope.listRooms[i].timeTable.third.nbPers = parseInt($scope.listRooms[i].timeTable.third.nbPers) + parseInt(data.items[j].properties.nbPers);
									nbSlot = nbSlot - 1;
									moreHour = true;
								}
	
								if(hours == 14 || hours == 13 || hours == 15 && minutes <= 20 || nbSlot > 0 && moreHour){
			            			$scope.listRooms[i].timeTable.fourth.kind = kind;
									$scope.listRooms[i].timeTable.fourth.nbPers = parseInt($scope.listRooms[i].timeTable.fourth.nbPers) + parseInt(data.items[j].properties.nbPers);
	
									console.log(nbSlot);
									nbSlot = nbSlot - 1;
									moreHour = true;
								}
								
								if(hours == 15 && minutes > 20 || hours == 16 && minutes <= 50 || nbSlot > 0 && moreHour){
			            			$scope.listRooms[i].timeTable.fifth.kind = kind;
									$scope.listRooms[i].timeTable.fifth.nbPers = parseInt($scope.listRooms[i].timeTable.fifth.nbPers) + parseInt(data.items[j].properties.nbPers);
									nbSlot = nbSlot - 1;
									moreHour = true;
								}
	
								if(hours == 16 && minutes > 50 || hours == 17 || hours == 18 || nbSlot > 0 && moreHour){
			            			$scope.listRooms[i].timeTable.sixth.kind = kind;
									$scope.listRooms[i].timeTable.sixth.nbPers = parseInt($scope.listRooms[i].timeTable.sixth.nbPers) + parseInt(data.items[j].properties.nbPers);				
									nbSlot = nbSlot - 1;
									moreHour = true;
								}
								
							}
						}
					}
				}
				console.log("spinnner: "+$scope.showSpinner);
				$scope.showSpinner = false;
				console.log("spinner:  "+$scope.showSpinner);
				$scope.showTable = true;
			});
	  }
});

