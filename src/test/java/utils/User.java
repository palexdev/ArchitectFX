package utils;

public final class User {
    private String name;
    private String password;
    private Integer[] numbers;

    public User(Integer[] numbers) {
        this.name = "@placeholder";
        this.password = "@placeholder";
        this.numbers = numbers;
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(String name, String password, Integer[] numbers) {
        setData(name, password, numbers);
    }

    public void setData(String name, String password, Integer[] numbers) {
        this.name = name;
        this.password = password;
        this.numbers = numbers;
    }

    public String name() {return name;}

    public void setName(String name) {
        this.name = name;
    }

    public String password() {return password;}

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer[] numbers() {
        return numbers;
    }

    public void setNumbers(Integer[] numbers) {
        this.numbers = numbers;
    }
}
