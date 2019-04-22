$(function () {
    postShipLocations(makePostUrl);
    loadData();
        $('#createGame').click(function(){
        postSalvos();
      });
});

function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

function loadData() {

    $.post('/api/game_view/' + getParameterByName('gp'))
        .done(function (data) {
            var playerInfo;
            console.log(data.gamePlayers);
            if (data.gamePlayers.length > 1){
                if (data.gamePlayers[0].id == getParameterByName('gp'))
                    playerInfo = [data.gamePlayers[0].player, data.gamePlayers[1].player];
                else
                    playerInfo = [data.gamePlayers[1].player, data.gamePlayers[0].player];

                $('#playerInfo2').text(playerInfo[1].username).addClass('salvo-font2');
            }
            else playerInfo= [data.gamePlayers[0].player];

            $('#playerInfo1').text(playerInfo[0].username + ' (you)').addClass('salvo-font1');

            data.ships.forEach(function (shipPiece) {
                shipPiece.locations.forEach(function (shipLocation) {
                    if(isHit(shipLocation,data.salvoes,playerInfo[0].id))
                        $('#B_' + shipLocation).addClass('ship-piece-hited');
                    else
                        $('#B_' + shipLocation).addClass('ship-piece');
                });
            });
            data.salvoes.forEach(function (salvo) {
                if (playerInfo[0].id === salvo.player) {
                    salvo.locations.forEach(function (location) {
                        $('#S_' + location).addClass('salvo-player1');
                    });
                } else {
                    salvo.locations.forEach(function (location) {
                        $('#_' + location).addClass('salvo-player2');
                    });
                }
            });
        })
        .fail(function( jqXHR, textStatus ) {
            alert( "Failed: " + textStatus );
        });
}


function makePostUrl() {
    var gamePlayerID =  getParameterByName("gp");
    return '/api/games/players/' + gamePlayerID + '/ships';
}


function postSalvos () {
    $.post({
        url:'/api/games/players/' + getParameterByName("gp")+ '/salvos',
        data: JSON.stringify({turn: 5 , locationList: ["A1", "A2", "A3"]}),
        dataType: "text",
        contentType: "application/json"
    })
        .done(function (response) {
            console.log(response);
            console.log("salvo!");

        })
        .fail(function (response) {
            console.log(response);

        })
}


function postShipLocations (postUrl) {
    $.post({
        url: postUrl,
        data: JSON.stringify([{shipType: "destroyer", locationList: ["A1", "A2", "A3"]},{shipType: "destroyer", locationList: ["A1", "A2", "A3"]}]),
        dataType: "text",
        contentType: "application/json"
    })
        .done(function (response) {
            console.log(response);
            console.log("SHIPS!");

        })
        .fail(function (response) {
            console.log(response);

        })
}

function isHit(shipLocation,salvoes,playerId) {
    var hit = false;
    salvoes.forEach(function (salvo) {
        if(salvo.player != playerId)
            salvo.locations.forEach(function (location) {
                if(shipLocation === location)
                    hit = true;
            });
    });
    return hit;
}
