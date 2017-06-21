
 <%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="com.google.appengine.api.users.*" %>

<% UserService userService = UserServiceFactory.getUserService(); %>

<!DOCTYPE html>
<html lang="en">
<head>
  <!-- Theme Made By www.w3schools.com - No Copyright -->
  <title>Réserver un salle</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <link href="https://fonts.googleapis.com/css?family=Lato" rel="stylesheet" type="text/css">
  <link href="https://fonts.googleapis.com/css?family=Montserrat" rel="stylesheet" type="text/css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
   <link rel="stylesheet" type="text/css" href="css/style.css">
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.6/angular.min.js"></script>



	
</head>
<body ng-app="webProjet" ng-controller="mainController" id="myPage" data-spy="scroll" data-target=".navbar" data-offset="50">

<nav class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>                        
      </button>
      <a class="navbar-brand" href="#myPage">FST</a>
    </div>
    <div class="collapse navbar-collapse" id="myNavbar">
      <ul class="nav navbar-nav navbar-right">
      <% if (userService.getCurrentUser() != null) { %>
      	<li><a href="<%= userService.createLogoutURL("/") %>">Se déconnecter</a></li>
        <li><a href="#book">Réserver</a></li>
        <li><a href="#github">Github</a></li>
        <li><a href="#" ng-click="displayResa()">Mes Réservations</a></li>
      <% } %>
        <li><a href="#band">Band</a></li>
      </ul>
    </div>
  </div>
</nav>


<!-- Container (TOUR Section) -->
<div id="book" class="bg-1" hidden>
  <div class="container">
    <h3 class="text-center">Charger les données</h3>
  
    <div class="row text-center">
      <a class="btn btn-primary" href="test?choix=loadDatas" >Charger</a>
    </div>
  </div>
  
</div>

<div id="book" class="container text-center" >
<% if (userService.getCurrentUser() == null) { %>
			<p>Pour pouvoir réserver une salle veuillez vous authentifier : <br>
			<a href="<%= userService.createLoginURL("/") %>" class="btn btn-primary">Se connecter avec google</a></p>
		
  	<% }
	else { %>
		
  <div id="successMessage" class="alert alert-success" data-alert="alert">Réservation effectuée avec succès</div>
  <div id="errorMessage" class="alert alert-danger" data-alert="alert">Erreur interne</div>
  
  <div class="row">
    <div class="col-sm-12">
    	<div >   	
    		<div data-variable="<%= userService.getCurrentUser().getUserId() %>" id="idUser">
    		<h1>Bonjour <%= userService.getCurrentUser().getNickname() %>, choisissez la date de réservation :</h1>
    		<input type="date" name="date" ng-model="dateSearch.value"/>
    		

			
			<br><br>
			<img ng-show="showSpinner" src="/img/spinner.gif"/>
			<div ng-show="showTable">
	        	<form>
				    <div class="form-group">
				      <div class="input-group">
				        <div class="input-group-addon"><i class="fa fa-search"></i></div>
				        <input type="text" class="form-control" placeholder="Rechercher une salle" ng-model="rechercher">
				      </div>      
				    </div>
				</form>
				<div ng-class="sizeTable" >
					<table class="table table-hover" >
						<thead>
						    <tr>
						        <th >Nom</th>
						        <th class="cell" >8h - 9h20</th>
						        <th class="cell" >9h30 - 10h50</th>
						        <th class="cell" >11h - 12h20</th>
						        <th class="cell" >Pause</th>
						        <th class="cell" >14h - 15h20</th>
						        <th class="cell" >15h30 - 16h50</th>
						        <th class="cell" >17h - 18h20</th>
						    </tr>
					    </thead>
					    </tbody>
						    <tr ng-repeat="(key, val) in listRooms | filter:rechercher" >
						    	<td style="border:solid;">{{val.name}} (capa. 30)</td>
						    	<td style="border:solid;" ng-click="val.timeTable.first.kind == 'Event' || book(val.name,'first',$index)" ng-class="val.timeTable.first.kind == 'empty' ? 'greenBackground' : (val.timeTable.first.kind == 'Reservation' ? 'orangeBackground' : 'redBackground')"><p ng-show="val.timeTable.first.nbPers>0">{{val.timeTable.first.nbPers}}</p></td>
						    	<td style="border:solid;" ng-click="val.timeTable.second.kind == 'Event' || book(val.name,'second')" ng-class="val.timeTable.second.kind == 'empty' ? 'greenBackground' : (val.timeTable.second.kind == 'Reservation' ? 'orangeBackground' : 'redBackground')"><p ng-show="val.timeTable.second.nbPers>0">{{val.timeTable.second.nbPers}}</p></td>
						    	<td style="border:solid;" ng-click="val.timeTable.third.kind == 'Event' || book(val.name,'third')" ng-class="val.timeTable.third.kind == 'empty' ? 'greenBackground' : (val.timeTable.third.kind == 'Reservation' ? 'orangeBackground' : 'redBackground')"><p ng-show="val.timeTable.third.nbPers>0">{{val.timeTable.third.nbPers}}</p></td>
						    	<td style="border:solid; background-color:grey;" ></td>
						    	<td style="border:solid;" ng-click="val.timeTable.fourth.kind == 'Event' || book(val.name,'fourth')" ng-class="val.timeTable.fourth.kind == 'empty' ? 'greenBackground' : (val.timeTable.fourth.kind == 'Reservation' ? 'orangeBackground' : 'redBackground')"><p ng-show="val.timeTable.fourth.nbPers>0">{{val.timeTable.fourth.nbPers}}</p></td>
						    	<td style="border:solid;" ng-click="val.timeTable.fifth.kind == 'Event' || book(val.name,'fifth')" ng-class="val.timeTable.fifth.kind == 'empty' ? 'greenBackground' : (val.timeTable.fifth.kind == 'Reservation' ? 'orangeBackground' : 'redBackground')"><p ng-show="val.timeTable.fifth.nbPers>0">{{val.timeTable.fifth.nbPers}}</p></td>
						    	<td style="border:solid;" ng-click="val.timeTable.sixth.kind == 'Event' || book(val.name,'sixth')" ng-class="val.timeTable.sixth.kind == 'empty' ? 'greenBackground' : (val.timeTable.sixth.kind == 'Reservation' ? 'orangeBackground' : 'redBackground')"><p ng-show="val.timeTable.sixth.nbPers>0">{{val.timeTable.sixth.nbPers}}</p></td>
						    </tr>
						</tbody>
					</table>
				</div>
				
				<div class="col-lg-4" ng-show="showBookForm">
					
	    			<h3>{{bookRoom.name}}</h3></br>
	    			Nombre de personne : <input type="text" class="form-control" ng-model="nbPers" required/></br>
	    			Ajouter une adresse mail (optionnel) :
	    
				      <input type="button" class="btn btn-info" ng-click="addMail()" value="Ajouter">
				     
				     <fieldset data-ng-repeat="field in mails track by $index">
				        <input type="test" rows="4" ng-model=" mails[$index]"></input>
				        <button type="button" class="btn btn-default btn-sm" ng-click="removeMail($index)">
				          <span class="glyphicon glyphicon-minus"></span>
				        </button>
				
				      </fieldset>
				      
     				</br>
     				</br>
     				</br>
				    <a class="btn btn-primary" href="#" ng-click="requestBook()" >Book Now !</a>
				    
				</div>
			</div>
		</div>
    </div>
    <div ng-show="showResa" class="myResa">
					<table class="tableResa table-hover table-fixed" >
						<thead>
						    <tr>
						        <th class="cell">Mes réservations </th>
						    </tr>
					    </thead>
					    </tbody>
						    <tr ng-repeat="(key, val) in myResa">
						        <td class="cell" ><span class="glyphicon glyphicon-remove-circle" style="color:red;" ng-click="cancelResa(val.key.id,key)"></span>&nbsp&nbsp{{val.properties.location}} {{val.properties.start | date:"MM/dd/yyyy 'à' HH:mm"}}</td>    	
						    </tr>
						</tbody>
					</table>
				</div>
  </div>
  
    <% } %>
</div>

</div>

<!-- Container (The Band Section) -->
<div id="band" class="container text-center">
  <h3>THE BAND</h3>
  <div class="row">
    <div class="col-sm-4">
        <h2>Helbert Tanguy</h2>
    </div>
    <div class="col-sm-4">
        <h2>Piquet Fabien</h2>
    </div>
    <div class="col-sm-4">
        <h2>Pavageau Stanislas</h2>
    </div>
  </div>
</div>

<div id="github" class="container text-center">
  <h3>Github Link below :</h3>
  <div class="row" style="text-align:center;">
    <div class="col-sm-4"></div>
    <div class="col-sm-4">
        <h2><a href="https://github.com/staniskas/wheretowork">Github</a></h2>
    </div>
  </div>
</div>


<!-- Footer -->
<footer class="text-center">
  <a class="up-arrow" href="#myPage" data-toggle="tooltip" title="TO TOP">
    <span class="glyphicon glyphicon-chevron-up"></span>
  </a><br><br>
</footer>

 <script src="js/script.js"></script>
 <script src="js/angularjs.js"></script>

</body>
</html>

