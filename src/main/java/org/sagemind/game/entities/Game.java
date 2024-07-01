package org.sagemind.game.entities;

import lombok.Data;

import java.util.List;
@Data
public class Game {

    private long id;

    private List<User> players;

}
