package View;

public class ColorManager {

        public static final String RESET = "\u001B[0m";
        public static final String BOLD = "\u001B[1m";
        public static final String ITALIC = "\u001B[3m";
        public static final String UNDERLINE = "\u001B[4m";
        public static final String RED = "\u001B[31m";
        public static final String BG_RED = "\u001B[41m";
        public static final String GREEN = "\u001B[32m";
        public static final String BG_GREEN = "\u001B[42m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BG_YELLOW = "\u001B[43m";
        public static final String BLUE = "\u001B[34m";
        public static final String BG_BLUE = "\u001B[44m";
        public static final String PURPLE = "\u001B[35m";
        public static final String BG_PURPLE = "\u001B[45m";
        public static final String CYAN = "\u001B[36m";
        public static final String BG_CYAN = "\u001B[46m";
        public static final String WHITE = "\u001B[37m";
        public static final String BG_WHITE = "\u001B[47m";

        public static String getColoredText(String color, String text) {

                String colorCode = switch (color.toLowerCase()) {
                    case "red" -> RED;
                    case "bg_red" -> BG_RED;
                    case "green" -> GREEN;
                    case "bg_green" -> BG_GREEN;
                    case "yellow" -> YELLOW;
                    case "bg_yellow" -> BG_YELLOW;
                    case "blue" -> BLUE;
                    case "bg_blue" -> BG_BLUE;
                    case "purple" -> PURPLE;
                    case "bg_purple" -> BG_PURPLE;
                    case "cyan" -> CYAN;
                    case "bg_cyan" -> BG_CYAN;
                    case "bold" -> BOLD;
                    case "italic" -> ITALIC;
                    case "underline" -> UNDERLINE;
                    case "white" -> WHITE;
                    case "bg_white" -> BG_WHITE;
                    default -> RESET; // Falls eine unbekannte Farbe übergeben wird.
                };
            return colorCode + text + RESET;
        }
}
