package org.sagemind.game.commands;

import org.sagemind.game.entities.User;

public class RegisterUserCommand implements ICommand{
    @Override
    public void run(String[] args) {
//        validate args
        User user = new User(Long.parseLong(args[0]), args[1], args[2], args[3]);
        System.out.println(user.toString());

    }
}
