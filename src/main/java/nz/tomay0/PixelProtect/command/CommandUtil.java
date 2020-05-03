package nz.tomay0.PixelProtect.command;

import java.util.HashSet;
import java.util.Set;

public class CommandUtil {

    /**
     * Get a protection expansion from arguments in a string.
     *
     * @param args          arguments
     * @param startIndex    Where the arguments begin
     * @param allowNull     Allow certain directions to not be specified
     * @param allowNegative Allow negative directions
     * @return null if valid size formatting is not found
     */
    public static Integer[] getSize(String[] args, int startIndex, boolean allowNull, boolean allowNegative) {
        if (startIndex >= args.length) return null;

        Integer[] size = new Integer[]{null, null, null, null};  // west, east, north, south

        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i].toLowerCase();

            // check if only a number
            try {
                // valid, number specified
                int sizeParam = Integer.parseInt(arg);

                if (sizeParam < 0 && allowNegative) return null; // invalid size

                for (int j = 0; j < 4; j++) {
                    if (size[j] == null) size[j] = sizeParam;
                }

                continue;

            } catch (NumberFormatException e) {
            }

            // check if starting with S/N/E/W

            Set<Character> directions = new HashSet<>();
            int numStart = 0;
            for (char c : arg.toCharArray()) {
                if (c == 's' || c == 'n' || c == 'e' || c == 'w') {
                    numStart++;
                    directions.add(c);
                } else if (Character.isDigit(c) || c == '-') break;
                else return null; // character not a direction
            }

            if (numStart == 0) {
                return null; // must start with the direction
            }

            try {
                int sizeParam = Integer.parseInt(arg.substring(numStart));

                if (sizeParam < 0 && allowNegative) return null; // invalid size

                // set a size for specific directions
                for (char c : directions) {
                    size["wens".indexOf(c)] = sizeParam;
                }

            } catch (NumberFormatException e) {
                return null; // begins with direction but still has invalid characters
            }
        }

        // allow null, don't bother checking
        if (allowNull) return size;

        // don't allow null, check all
        for (int j = 0; j < 4; j++) {
            if (size[j] == null) return null; // found a null
        }

        // no nulls found
        return size;
    }
}
