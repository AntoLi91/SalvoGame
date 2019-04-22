package com.codeoftheweb.salvo;

import java.util.*;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date finishDate;

    private double score;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")

    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")

    private Player player;


    //CONTRUCTORES

    public Score() {
    }

    public Score(Game game,Player player,double score,Date finishDate) {
        this.score = score;
        this.setGame(game);
        this.setPlayer(player);
        this.finishDate= finishDate;
    }

    //METODOS

    public long getId() {
        return id;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

