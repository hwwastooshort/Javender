package View;

public class ColorManager {

        public static final String RESET = "\u001B[0m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
        public static final String PURPLE = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String WHITE = "\u001B[37m";

        public static String getColoredText(String color, String text) {
                String colorCode;
                switch (color.toLowerCase()) {
                        case "red":
                                colorCode = RED;
                                break;
                        case "green":
                                colorCode = GREEN;
                                break;
                        case "yellow":
                                colorCode = YELLOW;
                                break;
                        case "blue":
                                colorCode = BLUE;
                                break;
                        case "purple":
                                colorCode = PURPLE;
                                break;
                        case "cyan":
                                colorCode = CYAN;
                                break;
                        case "white":
                                colorCode = WHITE;
                                break;
                        default:
                                colorCode = RESET; // Falls eine unbekannte Farbe übergeben wird.
                }
                return colorCode + text + RESET;
        }
}
