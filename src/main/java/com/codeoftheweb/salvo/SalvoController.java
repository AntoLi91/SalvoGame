package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

//estoy en develop!
@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    //autorizo el gamePlayer
    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Permite la ejecucion del codigo mediante la ruta especificada

    public List<Object> getAll() {
        return gameRepository
                .findAll()
                .stream()
                .map(game -> gameToDto(game))
                .collect(Collectors.toList());
    }

    @RequestMapping("/games")
    public Map<String,Object> makeDTO(Authentication authentication){
        Map<String,Object> dto= new LinkedHashMap<>();
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) dto.put("player","Guest");
        else dto.put("player",loggedPlayerDTO(playerRepository.findByUserName(authentication.getName())));
        dto.put("games",getAll());
        return dto;
    }

    @RequestMapping(path="/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> createGame(Authentication authentication){

        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        Game newGame = new Game();

        gameRepository.save(newGame);

        GamePlayer newGamePlayer = new GamePlayer(newGame,playerRepository.findByUserName(authentication.getName()));

        gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(value="/games/players/{id}/ships" , method=RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> addShip(Authentication authentication,@PathVariable long id, @RequestBody List<Ship> shipList) {

        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        if(gamePlayerRepository.findById(id) == null) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        GamePlayer gamePlayer=gamePlayerRepository.findById(id).get();

        if(gamePlayer.getPlayer().getUserName() != authentication.getName()) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        if(! gamePlayer.getShips().isEmpty()) return new ResponseEntity<>(makeMap("error","Forbidden"), HttpStatus.FORBIDDEN);

        shipList.forEach(ship -> save_ships(ship,gamePlayer));



        return new ResponseEntity<>(makeMap("OK","Ships placed!"), HttpStatus.CREATED);
    }

    private void save_ships(Ship ship, GamePlayer gp){

        gp.getShips().add(ship);
        ship.setGamePlayer(gp);
        shipRepository.save(ship);

    }

    @RequestMapping(value="/games/players/{id}/salvos" , method=RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> addSalvo(Authentication authentication,@PathVariable long id, @RequestBody Salvo salvo) {

        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        if(gamePlayerRepository.findById(id) == null) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        GamePlayer gamePlayer=gamePlayerRepository.findById(id).get();

        if(gamePlayer.getPlayer().getUserName() != authentication.getName()) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

        List<Salvo> salvoList= gamePlayer.getSalvoes().stream().filter(salvo2-> salvo2.getTurn()== salvo.getTurn()).collect(Collectors.toList());

        if(! salvoList.isEmpty()) return new ResponseEntity<>(makeMap("error","Forbidden"), HttpStatus.FORBIDDEN);

        gamePlayer.getSalvoes().add(salvo);

        salvo.setGamePlayer(gamePlayer);

        salvoRepository.save(salvo);

        return new ResponseEntity<>(makeMap("OK","Salvos Placed!"), HttpStatus.CREATED);

    }


    public Map<String,Object> loggedPlayerDTO(Player player){
        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("id",player.getId());
        dto.put("name",player.getUserName());
        return dto;
    }


    //Pathvariable lee cadena de caracteres.
    @RequestMapping("/game_view/{id}")
    public  Map<String,Object> getGameView(@PathVariable long id){
        return gameViewDTO(gamePlayerRepository.findById(id).get());
    }


    /*@RequestMapping("/game_view/{id}")
    public Map<String,Object> getGameView(@PathVariable long id) {

        return gameViewDTO(gamePlayerRepository.findById(id).get());

    }*/

    @RequestMapping(path = "/game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> createGame(
            Authentication auth, @PathVariable long id) {

        if(auth == null || auth instanceof AnonymousAuthenticationToken) return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);


        if (gameRepository.findById(id) == null ) {
            return new ResponseEntity<>(makeMap("error","Game not found"), HttpStatus.FORBIDDEN);
        }

        if (gameRepository.findById(id).get().getGamePlayers().size() < 2 ){

            GamePlayer gamePlayer = new GamePlayer(gameRepository.findById(id).get(),playerRepository.findByUserName(auth.getName()));
            gamePlayerRepository.save(gamePlayer);
            return new ResponseEntity<>(makeMap("gpid",gamePlayer.getId()), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

    }

    @RequestMapping(path = "/game_view/{id}", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> getGameView(
            Authentication auth, @PathVariable long id) {

        if (gamePlayerRepository.findById(id) == null ) {
            return new ResponseEntity<>(makeMap("error","Game not found"), HttpStatus.FORBIDDEN);
        }

        if (gamePlayerRepository.findById(id).get().getPlayer().getUserName() == auth.getName() ) {
            return new ResponseEntity<>(gameViewDTO(gamePlayerRepository.findById(id).get()), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);

    }

    private Map<String,Object> makeMap(String key, Object value){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put(key,value);
        return map;
    }


    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String username, @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(username) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping("/leaderboard")
    public List<Object> getLeaderboard() {

        return playerRepository
                .findAll()
                .stream()
                .map(player -> leaderboardDTO(player))
                .collect(Collectors.toList());

    }

    private Map<String,Object> leaderboardDTO(Player player){

        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("name",player.getUserName());
        dto.put("score",scoreToDto(player));
        return dto;
    }


    private Map<String,Object> gameViewDTO(GamePlayer gamePlayer) {

        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id",gamePlayer.getGame().getId());
        dto.put("created",gamePlayer.getGame().getCreationDate());
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put ("ships", getShipList(gamePlayer.getShips()));
        dto.put("salvoes",obtainAllSalvoes(gamePlayer));
        dto.put("hits",makeHitsDTO(gamePlayer,getEnemy(gamePlayer)));
        dto.put("gameState",getGameState(gamePlayer,getEnemy(gamePlayer)));
        return dto;
    }

    private GamePlayer getEnemy(GamePlayer gamePlayer){

        Set<GamePlayer> gps = gamePlayer.getGame().getGamePlayers();

        for (GamePlayer gp: gps) {
            if(gp.getId() != gamePlayer.getId()) return gp;

        }

        return gamePlayer;
    }

    private Map<String, Object> makeHitsDTO(GamePlayer selfGP, GamePlayer opponentGP){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self", getHits(selfGP,     opponentGP));
        dto.put("opponent", getHits(opponentGP, selfGP));
        return dto;
    }


    //creo la función de Mapeo para player
    private Map<String, Object> playerToDto(Player player) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("username", player.getUserName());
        dto.put("score",scoreToDto(player));
        return dto;
    }

    //creo la función de Mapeo para gamePlayer
    private Map<String, Object> gamePlayerToDto(GamePlayer gamePlayer) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", playerToDto(gamePlayer.getPlayer()));
        return dto;

    }

    //Creo la función de mapeo para game
    private Map<String, Object> gameToDto(Game game) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate().getTime());
        dto.put("gamePlayers", getGamePlayerList(game.getGamePlayers()));
        dto.put("scores",getScoreList(game.getScores()));
        return dto;
    }

    private List<Map<String, Object>> getScoreList(Set<Score> scores) {
        return scores.stream().map(score -> scoreToDto(score))
                .collect(Collectors.toList());
    }

    private Map<String, Object> scoreToDto(Score score){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID",score.getPlayer().getId());
        dto.put("score",score.getScore());
        return dto;
    }


    private Map<String, Object> shipToDto(Ship ship){

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type",ship.getShipType());
        dto.put("locations", ship.getLocationList());
        return dto;
    }

    private Map<String, Object> salvoToDto(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn",salvo.getTurn());
        dto.put("player",salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getLocationList());
        return dto;
    }

    private Map<String, Object> scoreToDto(Player player) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("total", player.getWins()+player.getTies()*0.5);
        dto.put("won", player.getWins());
        dto.put("lost", player.getLosses());
        dto.put("tied", player.getTies());
        return dto;
    }


    //Obtengo una lista a partir de un SET
    private List<Map<String, Object>> getGamePlayerList(Set<GamePlayer> gamePlayers) {
        return gamePlayers.stream().map(gamePlayer -> gamePlayerToDto(gamePlayer))
                .collect(Collectors.toList());
    }


    private List<Map<String, Object>> getShipList(Set<Ship> ships) {
        return ships.stream().map(ship -> shipToDto(ship))
                .collect(Collectors.toList());
    }


    /*private List<Map<String, Object>> obtainAllShips(GamePlayer gamePlayer){

            List<Map<String,Object>> shipList = new ArrayList<>();
            gamePlayer.getGame().getGamePlayers().forEach(gpl -> gpl.getShips().forEach(ship -> shipList.add(shipToDto(ship))));
            return shipList;
    }*/

    private List<Map<String, Object>> obtainAllSalvoes(GamePlayer gamePlayer){

        List<Map<String,Object>> salvoList = new ArrayList<>();
        gamePlayer.getGame().getGamePlayers().forEach(gpl -> gpl.getSalvoes().forEach(salvo -> salvoList.add(salvoToDto(salvo))));
        return salvoList;
    }

    private List<Map> getHits(GamePlayer gamePlayer, GamePlayer opponentGameplayer) {
        List<Map> hits = new ArrayList<>();
        Integer carrierDamage = 0;
        Integer battleshipDamage = 0;
        Integer submarineDamage = 0;
        Integer destroyerDamage = 0;
        Integer patrolboatDamage = 0;
        List <String> carrierLocation = new ArrayList<>();
        List <String> battleshipLocation = new ArrayList<>();
        List <String> submarineLocation = new ArrayList<>();
        List <String> destroyerLocation = new ArrayList<>();
        List <String> patrolboatLocation = new ArrayList<>();
        gamePlayer.getShips().forEach(ship -> {
            switch (ship.getShipType()) {
                case "carrier":
                    carrierLocation.addAll(ship.getLocationList());
                    break;
                case "battleship":
                    battleshipLocation.addAll(ship.getLocationList());
                    break;
                case "submarine":
                    submarineLocation.addAll(ship.getLocationList());
                    break;
                case "destroyer":
                    destroyerLocation.addAll(ship.getLocationList());
                    break;
                case "patrolboat":
                    patrolboatLocation.addAll(ship.getLocationList());
                    break;
            }
        });
        for (Salvo salvo : opponentGameplayer.getSalvoes()) {
            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;
            Integer missedShots = salvo.getLocationList().size();
            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
            List<String> salvoLocationsList = new ArrayList<>();
            List<String> hitCellsList = new ArrayList<>();
            salvoLocationsList.addAll(salvo.getLocationList());
            for (String salvoShot : salvoLocationsList) {
                if (carrierLocation.contains(salvoShot)) {
                    carrierDamage++;
                    carrierHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (battleshipLocation.contains(salvoShot)) {
                    battleshipDamage++;
                    battleshipHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (submarineLocation.contains(salvoShot)) {
                    submarineDamage++;
                    submarineHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (destroyerLocation.contains(salvoShot)) {
                    destroyerDamage++;
                    destroyerHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (patrolboatLocation.contains(salvoShot)) {
                    patrolboatDamage++;
                    patrolboatHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
            }
            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
            damagesPerTurn.put("carrier", carrierDamage);
            damagesPerTurn.put("battleship", battleshipDamage);
            damagesPerTurn.put("submarine", submarineDamage);
            damagesPerTurn.put("destroyer", destroyerDamage);
            damagesPerTurn.put("patrolboat", patrolboatDamage);
            hitsMapPerTurn.put("turn", salvo.getTurn());
            hitsMapPerTurn.put("hitLocations", hitCellsList);
            hitsMapPerTurn.put("damages", damagesPerTurn);
            hitsMapPerTurn.put("missed", missedShots);
            hits.add(hitsMapPerTurn);
        }
        return hits;
    }

    private String getGameState(GamePlayer selfGP, GamePlayer opponentGP) {
        Set<Ship> selfShips = selfGP.getShips();
        Set<Salvo> selfSalvoes = selfGP.getSalvoes();
        if (selfShips.size() == 0){
            return "PLACESHIPS";
        }
        if (opponentGP.getShips() == null){
            return "WAITINGFOROPP";
        }
        int turn = getCurrentTurn(selfGP, opponentGP);
        Set<Ship> opponentShips = opponentGP.getShips();
        Set<Salvo> opponentSalvoes = opponentGP.getSalvoes();
        if (opponentShips.size() == 0 || opponentGP.getId()==selfGP.getId()){
            return "WAIT";
        }
        if(selfSalvoes.size() == opponentSalvoes.size()){
            Player self = selfGP.getPlayer();
            Game game = selfGP.getGame();
            if (allPlayerShipsSunk(selfShips, opponentSalvoes) && allPlayerShipsSunk(opponentShips, selfSalvoes)){
                Score score = new Score(game,self, 0.5f, new Date());
                if(!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "TIE";
            }
            if (allPlayerShipsSunk(selfShips, opponentSalvoes)){
                Score score = new Score(game,self,  0, new Date());
                if(!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "LOST";
            }
            if(allPlayerShipsSunk(opponentShips, selfSalvoes)){
                Score score = new Score(game,self, 1, new Date());
                if(!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "WON";
            }
        }
        if (selfSalvoes.size() != turn){
            return "PLAY";
        }
        return "WAIT";
    }

    private int getCurrentTurn(GamePlayer selfGP, GamePlayer opponentGP){
        int selfGPSalvoes = selfGP.getSalvoes().size();
        int opponentGPSalvoes = opponentGP.getSalvoes().size();

        int totalSalvoes = selfGPSalvoes + opponentGPSalvoes;

        if(totalSalvoes % 2 == 0)
            return totalSalvoes / 2 + 1;

        return (int) (totalSalvoes / 2.0 + 0.5);
    }

    private boolean allPlayerShipsSunk(Set<Ship> selfShips, Set<Salvo> opponentSalvoes){
        List<String> carrierLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        int carrierDamage = 0;
        int battleshipDamage = 0;
        int patrolboatDamage = 0;
        int submarineDamage = 0;
        int destroyerDamage = 0;
        for(Ship ship : selfShips){
            if(ship.getShipType().equals("carrier")){
                carrierLocations.addAll(ship.getLocationList());
            }else if(ship.getShipType().equals("battleship")){
                battleshipLocations.addAll(ship.getLocationList());
            }else if(ship.getShipType().equals("patrolboat")){
                patrolboatLocations.addAll(ship.getLocationList());
            }else if(ship.getShipType().equals("submarine")){
                submarineLocations.addAll(ship.getLocationList());
            }else if(ship.getShipType().equals("destroyer")){
                destroyerLocations.addAll(ship.getLocationList());
            }
        }
        List<String> salvoesLocations = new ArrayList<>();
        for(Salvo salvo : opponentSalvoes){
            salvoesLocations.addAll(salvo.getLocationList());
        }
        for(String salvoLocation : salvoesLocations){
            if(existLocation(salvoLocation, carrierLocations)){
                carrierDamage++;
            }else if(existLocation(salvoLocation, battleshipLocations)) {
                battleshipDamage++;
            }else if(existLocation(salvoLocation, patrolboatLocations)){
                patrolboatDamage++;
            }else if(existLocation(salvoLocation, submarineLocations)){
                submarineDamage++;
            }else if(existLocation(salvoLocation, destroyerLocations)){
                destroyerDamage++;
            }
        }
        if(carrierDamage == 5 && battleshipDamage == 4 && destroyerDamage == 3 && submarineDamage == 3 && patrolboatDamage == 2){
            return true;
        }
        return false;
    }


    private boolean existLocation(String location, List<String> locations){
        for(String _location : locations){
            if(location.equals(_location)){
                return true;
            }
        }
        return false;
    }
    private boolean existSalvo(Salvo salvo, Set<Salvo> salvoes){
        for(Salvo s : salvoes){
            if(salvo.getTurn() == s.getTurn()){
                return true;
            }
        }
        return false;
    }
    private boolean existScore(Score score, Game game){
        Set<Score> scores = game.getScores();
        for(Score s : scores){
            if(score.getPlayer().getUserName().equals(s.getPlayer().getUserName())){
                return true;
            }
        }
        return false;
    }



}



