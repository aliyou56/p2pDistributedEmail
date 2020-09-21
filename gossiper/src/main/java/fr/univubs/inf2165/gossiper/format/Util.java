package fr.univubs.inf2165.gossiper.format;

public class Util {

    /**
     * Check if an argument is null or not.
     *
     * @param name The name of the argument
     * @param obj The argument to be checked.
     */
    public static void checkNotNull(String name, Object obj) {
        if(obj == null) {
            throw new IllegalArgumentException(name +" == null");
        }
    }
}
