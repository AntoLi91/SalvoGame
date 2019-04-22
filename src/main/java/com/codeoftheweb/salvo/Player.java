package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String password;

    private String name;

    private String userName;

    @OneToMany(mappedBy="player",fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="player",fetch=FetchType.EAGER)
    private Set<Score> scores;

    //CONTRUCTORES

    public Player() {
    }

    public Player(String userName, String password) {

        this.setPassword(password);
        //this.setName(name);
        this.userName = userName;

    }

    //METODOS


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonIgnore
    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        gamePlayers.add(gamePlayer);
    }


    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }



    public long getId() {
        return id;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }


    public long getWins(){

        return this.scores.stream().filter(score -> score.getScore() == 1).count();
    }

    public long getTies(){

        return this.scores.stream().filter(score -> score.getScore() == 0.5).count();
    }

    public long getLosses(){

        return this.scores.stream().filter(score -> score.getScore() == 0).count();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

