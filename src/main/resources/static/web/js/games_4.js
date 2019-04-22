$(function() {

    loadData();

    $("#login-btn").click(function(){
        login();

    });

    $('.join-btn').click(function(){

        joinGame();

    });

  $("#sign-up-btn").click(function(){
        signUp();
    });

  $("#logout-btn").click(function(){
    logout();
  });
});

function updateViewGames(data) {
  var userData = data.player;
  var htmlList = data.games.map(function (games) {
      return getGameItem(games,userData);
  }).join('');
  $("#game-list").html(htmlList);
  if(userData!="Guest"){
    $("#user-info").text('Hello ' + userData.name + '!');
    showLogin(false);
  }
}

function getGameItem(gameData, userData){

    var joinButton = null;

    var item = new Date(gameData.creationDate).toLocaleString() + ' ' + gameData.gamePlayers.map(function(p) { return p.player.username}).join(', ');
    var idPlayerInGame = isPlayerInGame(userData.id,gameData);

    if (idPlayerInGame != -1) {

        item = '<a href="game_3.html?gp='+ idPlayerInGame + '">'+ new Date(gameData.creationDate).toLocaleString() + ' ' + gameData.gamePlayers.map(function(p) { return p.player.username}).join(', ')  +'</a>';
    }
    else{

        if(gameData.gamePlayers.length == 1)joinButton ='<button id="join-btn" type="button" class="joinGameButton btn btn-info">JOIN GAME</button> ';

        //if(joinButton != null) $(joinButton).appendTo(row);

        else joinButton='';
    }

    return '<li class="list-group-item">'+item+ joinButton + '<li>';
}

function isPlayerInGame(idPlayer,gameData){
    var isPlayerInGame = -1;
    gameData.gamePlayers.forEach(function (game){
        if(idPlayer === game.player.id)
            isPlayerInGame = game.id;
    });
    return isPlayerInGame;
}

function updateViewLBoard(data) {
  var htmlList = data.map(function (score) {
      return  '<tr><td>' + score.name + '</td>'
              + '<td>' + score.score.total + '</td>'
              + '<td>' + score.score.won + '</td>'
              + '<td>' + score.score.lost + '</td>'
              + '<td>' + score.score.tied + '</td></tr>';
  }).join('');
  document.getElementById("leader-list").innerHTML = htmlList;
}

function joinGame(){
    logout();
}

function loadData() {
  $.post("/api/games")
    .done(function(data) {
      updateViewGames(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
  
  $.get("/api/leaderboard")
    .done(function(data) {
      updateViewLBoard(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
}

function login(){
    $.post("/api/login", { username: $("#username").val(), password: $("#password").val()})
        .done(function() {
            loadData(),
                showLogin(false);
        })
        .fail(function() {

            $("#system-message").text('Login failed. Please check your password and username').show("slow").delay(2000).hide("slow");

        })
}

function logout(){
  $.post("/api/logout")
    .done(function() {
      loadData();
      showLogin(true);
    });
}

function signUp(){

    $.post("/api/players",
        { username: $("#username").val(),
            password: $("#password").val() })


        .done(function() {
            console.log("ok");
            $("#system-message").text('Sign up successful !').show("slow").delay(2000).hide("slow");

        })
        .fail(function() {
            console.log("signup failed");
            $("#system-message").text('Sorry, sign up failed. Try again').show("slow").delay(2000).hide("slow");
            // console.log(data);
            $("#username").val("");
            $("#password").val("");
            $("#username").focus();

        })
        .always(function() {

        });

}


function showLogin(show){
  if(show){
    $("#login-panel").show();
    $("#user-panel").hide();
  } else {
    $("#login-panel").hide();
    $("#user-panel").show();
  }
}

function fetchJson(url) {
    return fetch(url, {
        method: 'GET',
        credentials: 'include'
    }).then(function (response) {
        if (response.ok) {
            return response.json();
        }
        throw new Error(response.statusText);
    });
}

function updateJson() {
    fetchJson('/api/games').then(function (json) {
        // do something with the JSON
        data = json;
        gamesData = data.games;
        updateView();
    }).catch(function (error) {
        // do something getting JSON fails
    });
}