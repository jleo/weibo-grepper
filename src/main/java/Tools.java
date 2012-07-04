import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created with IntelliJ IDEA.
 * User: shang
 * Date: 12-7-4
 * Time: 上午8:36
 * To change this template use File | Settings | File Templates.
 */
public class Tools {



        public static String stringToUnicode(String s) {
            String str = "";
            for (int i = 0; i < s.length(); i++) {
                int ch = (int) s.charAt(i);
                if (ch > 255)
                    str += "\\u" + Integer.toHexString(ch);
                else
                    str += "\\" + Integer.toHexString(ch);
            }
            return str;
        }

        public static String unicodeToString(String str) {
            Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
            Matcher matcher = pattern.matcher(str);
            char ch;
            while (matcher.find()) {
                ch = (char) Integer.parseInt(matcher.group(2), 16);
                str = str.replace(matcher.group(1), ch + "");
            }
            return str;
        }


}
