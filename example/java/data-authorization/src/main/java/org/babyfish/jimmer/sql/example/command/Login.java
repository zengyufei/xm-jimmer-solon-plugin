package org.babyfish.jimmer.sql.example.command;

import org.babyfish.jimmer.sql.example.command.common.Command;

import java.util.List;
import java.util.Set;

public class Login extends Command {

    @Override
    public void execute(Set<Character> flags, List<String> args) {
        String nickName = arg(args, 0);
        if (USER_SERVICE.login(nickName)) {
            System.out.println("Logged successfully");
        } else {
            System.out.println("Logged failed");
        }
    }

    @Override
    public boolean isUserRequired() {
        return false;
    }
}
