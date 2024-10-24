package misc;

public class UserWrapper {
    private final User user;

    public UserWrapper(User user) {this.user = user;}

    public String name() {return user.name();}

    public String password() {return user.password();}
}
