package common.city;

public class SimpleCity extends City{
    static SimpleCity c;
    SimpleCity() {
        buildCity(1);
    }

    public synchronized static SimpleCity getCity() {
        if (c==null) {
            c = new SimpleCity();
        }
        return c;
    }
}
