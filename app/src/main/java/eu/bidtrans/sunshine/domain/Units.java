package eu.bidtrans.sunshine.domain;

public enum Units {
    metric, imperial;

    public static Units fromString(String value) {
        return value.equalsIgnoreCase("metric") ? metric :
                value.equalsIgnoreCase("imperial") ? imperial :
                        null;
    }
}
