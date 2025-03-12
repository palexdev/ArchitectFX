package misc;

import io.github.palexdev.architectfx.backend.model.Initializable;

public class InjectTestClass {
    public String aString;
    public int anInt;
    public Double aDouble;
    public InjectTestClass nested;

    public InjectTestClass() {}

    public InjectTestClass(String aString, int anInt, Double aDouble, InjectTestClass nested) {
        this.aString = aString;
        this.anInt = anInt;
        this.aDouble = aDouble;
        this.nested = nested;
    }

    public static class Controller implements Initializable {
        protected InjectTestClass obj;

        @Override
        public void initialize() {
            obj.aString = "done";
            obj.anInt = 1;
            obj.aDouble = Double.POSITIVE_INFINITY;
            obj.nested = new InjectTestClass();
        }
    }
}
