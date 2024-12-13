package misc;

public class InjectTestClass {
    private String aString;
    private int anInt;
    private Double aDouble;
    private InjectTestClass nested;

    public InjectTestClass() {}

    public InjectTestClass(String aString, int anInt, Double aDouble, InjectTestClass nested) {
        this.aString = aString;
        this.anInt = anInt;
        this.aDouble = aDouble;
        this.nested = nested;
    }

    public String getAString() {
        return aString;
    }

    public int getAnInt() {
        return anInt;
    }

    public Double getADouble() {
        return aDouble;
    }

    public InjectTestClass getNested() {
        return nested;
    }

    public static class Controller {
        private InjectTestClass obj;

        public void init() {
            obj.aString = "done";
            obj.anInt = 1;
            obj.aDouble = Double.POSITIVE_INFINITY;
            obj.nested = new InjectTestClass();
        }
    }
}
