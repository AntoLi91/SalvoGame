package com.codeoftheweb.salvo;

import java.util.*;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private int turn;


    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    // Aqui estoy utilizando la anotacion para no tener que armar una nueva clase pues
    // no es practico en caso contrario.
    @ElementCollection
    @Column(name="locationList")
    private List<String> locationList = new ArrayList<>();

    //CONTRUCTORES

    public Salvo() {
    }

    public Salvo(int turn,GamePlayer gamePlayer,List<String> locationList) {

        this.turn =turn;
        this.gamePlayer = gamePlayer;
        this.locationList=locationList;
    }

    //METODOS

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<String> locationList) {
        this.locationList = locationList;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}


