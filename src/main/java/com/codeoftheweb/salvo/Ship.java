package com.codeoftheweb.salvo;

import java.util.*;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String shipType;


    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    // Aqui estoy utilizando la anotacion para no tener que armar una nueva clase pues
    // no es practico en caso contrario.
    @ElementCollection
    @Column(name="locationList")
    private List<String> locationList = new ArrayList<>();

    //CONTRUCTORES

    public Ship() {
    }

    public Ship(String shipType,GamePlayer gamePlayer,List<String> locationList) {

        this.shipType= shipType;
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

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public List<String> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<String> locationList) {
        this.locationList = locationList;
    }
}


