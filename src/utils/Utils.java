    package utils;

import app.FApp;
import items.image.FCanvas;

import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;

/**
 * Some utilities and constants
 * @author fer
 *
 */
public class Utils {

    /**
     *
     * COLOR CONSTANTS
     *
     */
    public static final int COLOR_WHITE = 0x00FFFFFF;
    public static final int COLOR_BLACK = 0x00000000;
    public static final int COLOR_GRAY = 0x00080808;
    public static final int COLOR_RED = 0x00AA0000;
    public static final int COLOR_GREEN = 0x0000AA00;
    public static final int COLOR_BLUE = 0x000000AA;
    public static final int COLOR_YELLOW = 0x00FFFF00;
    public static final int COLOR_GRAY_LIGHT = 0x00AAAAAA;
    public static final int COLOR_GRAY_DARK = 0x00555555;
    public static final int COLOR_DECREASE = 0x00111111;
    //tango icon theme colours
    public static final int COLOR_TANGO_BUTTER1 = 0x00FCE94F;
    public static final int COLOR_TANGO_CHAMELEON1 = 0x008AE234;
    public static final int COLOR_TANGO_ORANGE1 = 0x00FCAF3E;
    public static final int COLOR_TANGO_SKYBLUE1 = 0x00729FCF;
    //public static final int COLOR_TANGO_SKYBLUE1ALPHA = 0xFF729FCF;
    public static final int COLOR_TANGO_PLUM1 = 0x00AD7FA8;
    public static final int COLOR_TANGO_CHOCOLATE1 = 0x00E9B96E;
    public static final int COLOR_TANGO_SCARLETRED1 = 0x00EF2929;
    public static final int COLOR_TANGO_ALUMINIUM1 = 0x00EEEEEC;
    public static final int COLOR_TANGO_ALUMINIUM4 = 0x00888A85;
    /**
     * KEY CONSTANTS
     * TODO implement a not-device-specific key management
     */
    public static final byte KEY_UP = -1;
    public static final byte KEY_DOWN = -2;
    public static final byte KEY_LEFT = -3;
    public static final byte KEY_RIGHT = -4;
    public static final byte KEY_SELECT = -5;
    public static final byte KEY_CLEAR = -8;
    public static final byte KEY_EXIT = -11;
    public static final byte KEY_0 = 48;
    public static final byte KEY_1 = 49;
    public static final byte KEY_2 = 50;
    public static final byte KEY_3 = 51;
    public static final byte KEY_4 = 52;
    public static final byte KEY_5 = 53;
    public static final byte KEY_6 = 54;
    public static final byte KEY_7 = 55;
    public static final byte KEY_8 = 56;
    public static final byte KEY_9 = 57;
    /**
     *
     * SCREEN UTILS
     *
     */
    public static final byte SCROLL_OFFSET = 16;
    public static final int FOCUS_SELF_ELEMENT = -2;
    public static final int FOCUS_NONE_ELEMENT = -1;
    public static final int FOCUS_FIRST_ELEMENT = -3;
    public static final int IMAGE_ROUND_RECTANGLE = 30;
    public static final int IMAGE_RECT_OFFSET = 3;

    /**
     * Calls to Canvas repaint if not null
     * TODO repaint only the needed space of the canvas
     */
    public static void callRepaint() {
        if (isNotNull(FApp._theCanvas)) {
            FApp._theCanvas.repaint();
            FApp._theCanvas.serviceRepaints();
        }
    }

    /**
     * Calls to Canvas repaint if not null
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void callRepaint(int x, int y, int width, int height) {
        if (isNotNull(FApp._theCanvas)) {
            FApp._theCanvas.repaint(x, y, width, height);
            FApp._theCanvas.serviceRepaints();
        }
    }
    /**
     *
     * DEBUG UTILS
     * TODO delete debugMode method when distribute
     */
    /*
     * variable para controlar el modo debug
     * en lo moviles no debe llegar codigo que tenga llamadas a:
     * printStackTrace
     * System.out.println
     * debugMode
     */
    public static boolean debugMode = false;
    public static boolean debugPaintOnCanvas = false;
    public static final byte DEBUG_WARN = 0;
    public static final byte DEBUG_INFO = 1;
    public static final byte DEBUG_ERROR = 2;
    public static final byte DEBUG_TODO = 3;
    private static long freeMemory;
    private static long totalMemory;
    private static String data;

    public static final void debugDrawMemUsage(Graphics g) {
        if (!debugMode) {
            return;
        }
        freeMemory = Runtime.getRuntime().freeMemory();
        totalMemory = Runtime.getRuntime().totalMemory();
        g.setColor(Utils.COLOR_TANGO_SCARLETRED1);
        data = String.valueOf((freeMemory * 100) / totalMemory);
        g.drawString(data + "% free", 0, FCanvas.canvasHeight - 60,
                Graphics.LEFT | Graphics.TOP);
        g.setColor(Utils.COLOR_TANGO_ALUMINIUM4);
        data = String.valueOf(freeMemory);
        g.drawString("free: " + data, 0, FCanvas.canvasHeight - 40,
                Graphics.LEFT | Graphics.TOP);
        g.setColor(Utils.COLOR_TANGO_SKYBLUE1);
        data = String.valueOf(totalMemory);
        g.drawString(" tot: " + data, 0, FCanvas.canvasHeight - 20,
                Graphics.LEFT | Graphics.TOP);
    }
    /**
     * Prints a string in console output like:
     * <code>debugType: className.methodName - msg</code>
     * @param msg the message to print
     * @param className the class name
     * @param methodName the method
     * @param debugType one of: DEBUG_WARN, DEBUG_INFO, DEBUG_ERROR,
     * DEBUG_TODO or DEBUG_NO_PRINT
     */
    private static Alert a;

    public static void debugMode(String msg,
            String className, String methodName, byte debugType) {
        if (!debugMode) {
            return;
        }
        try {
            StringBuffer text = new StringBuffer();
            text.delete(0, text.length());
            switch (debugType) {
                case DEBUG_WARN:
                    text.append("WARNING");
                    break;
                case DEBUG_INFO:
                    text.append("INFO");
                    break;
                case DEBUG_ERROR:
                    text.append("ERROR");
                    break;
                case DEBUG_TODO:
                    text.append("TODO");
                    break;
                default:
                    break;
            }
            text.append(": ");
            text.append(className);
            text.append(".");
            text.append(methodName);
            text.append("\n --- ");
            text.append(msg);

            if (debugPaintOnCanvas) {
                a = new Alert("BUG:" + className + "." + methodName,
                        text.toString(), null, AlertType.WARNING);
                a.setTimeout(5000);
                Display.getDisplay(FApp._theMIDlet).setCurrent(a);
                a = null;
            }
            //System.out.println(text.toString());
        } catch (Throwable e) {
            if (debugPaintOnCanvas) {
                a = new Alert("BUG:",
                        className + "." + methodName, null, AlertType.ALARM);
                Display.getDisplay(FApp._theMIDlet).setCurrent(a);
                a = null;
            }
        }
    }

    /**
     * Prints the exception message into console and exception print stack
     * @param e the throwable
     * @param className
     * @param methodName
     */
    public static void debugModePrintStack(Throwable e,
            String className, String methodName) {
        if (!debugMode) {
            return;
        }
        a = new Alert("BUG:" + className + "." + methodName, "BUG:" + className + "." + methodName + e.toString(), null, AlertType.WARNING);
        a.setTimeout(Alert.FOREVER);
        Display.getDisplay(FApp._theMIDlet).setCurrent(a);
    }

    /**
     * Checks if something is not null.
     * @param data the Object to check
     * @return true if content is not null and false if content is null
     */
    public static final boolean isNotNull(Object data) {
        if (data == null || data.equals(null)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if data is null
     * @param data
     * @return true if data is null or false if has a not null value
     */
    public static final boolean isNull(Object data) {
        return !isNotNull(data);
    }

    /*
     *
     * TEXT UTILS
     *
     */
    static Font m_font = Font.getDefaultFont();
    static String m_txt = "";
    static int m_length = 0;
    static int m_width = 0;
    static int m_position = 0;
    static int m_start = 0;

    /**
     * Wraps and draw a {@link String} on the {@link Graphics}
     * @param g the {@link Graphics} where we want to paint
     * @param txt the text {@link String} to wrap
     * @param x the x coordenate for the text
     * @param y the y coord for the text
     * @param width the maximun width accepted (usually the canvas width)
     * @param font the {@link Font} used with the text
     * @param color the hex color for the text
     * @return the next y coordinate on the canvas,
     * which is the next location for a text to be written.
     * this is good for cases that you want to continue writing on the
     * canvas but with a different font or color.
     */
    public static int write(Graphics g, String txt,
            int x, int y, int width, Font font, int color) {
        if (!txt.equals("")) {
            m_font = font;
            m_txt = txt;
            m_length = txt.length();
            m_width = width - x;
            //		reseting
            m_position = 0;
            m_start = 0;

            int fontHight = m_font.getHeight() + 1;
            String s;
            g.setFont(m_font);
            g.setColor(color);
            do {
                s = nextLine();
                g.drawString(s, x, y, Graphics.TOP | Graphics.LEFT);
                y += fontHight;
            } while (hasMoreLines());
            s = null;
        }
        return y;
    }

    /**
     * @param txt
     * @param theFont
     * @param widthStart
     * @param widthEnd
     * @return
     */
    public static int getWrappedTextHeigh(String txt, Font theFont,
            int widthStart, int widthEnd) {
        int ret = 0;
        if (!txt.equals("")) {
            m_font = theFont;
            m_txt = txt;
            m_length = txt.length();
            m_width = widthEnd - widthStart;
            m_position = 0;
            m_start = 0;
            int fontHeight = m_font.getHeight() + 1;
            String s = new String();
            do {
                s = nextLine();
                ret += fontHeight;
            } while (hasMoreLines());
            s = null;
        }
        return ret;
    }

    /**
     *
     * @param txt
     * @param theFont
     * @param widthStart
     * @param widthEnd
     * @param paint
     * @param g
     * @param y
     * @return int[4] {w, h, x, y}
     */
    public static int[] getWrappedTextWidthHeigh(String txt, Font theFont,
            int widthStart, int widthEnd, boolean paint, Graphics g, int y) {
        int[] ret = new int[4];
        if (!txt.equals("")) {
            m_font = theFont;
            m_txt = txt;
            m_length = txt.length();
            m_width = widthEnd - widthStart;
            m_position = 0;
            m_start = 0;
            int fontHeight = m_font.getHeight() + 1;
            int txtWidth = 0;
            ret[0] = 0;
            ret[1] = 0;
            String s = new String();
            
            do {
                s = nextLine();
                txtWidth = m_font.charsWidth(s.toCharArray(), 0, s.length());
                if (txtWidth > ret[0]) {
                    ret[0] = txtWidth;
                }
                if (paint && isNotNull(g)) {
                    g.setFont(m_font);
                    g.drawString(s, widthStart, y, Graphics.TOP | Graphics.LEFT);
                }
                ret[1] += fontHeight;
                y += fontHeight;
            } while (hasMoreLines());
            s = null;
            ret[2] = widthStart;
            ret[3] = y - fontHeight;
        }
        return ret;
    }

    private static Vector lines;
    /**
     * Divide a text into lines fitting canvas width
     * @param txt the source text
     * @param theFont
     * @param widthStart
     * @param widthEnd
     * @param y
     * @return a Vector<String> with all lines of text fitting canvas sizes
     */
    public static Vector lines(String txt, Font theFont, int widthStart, int widthEnd, int y, char returnChar) {
        m_font = theFont;
        m_txt = txt;
        m_length = txt.length();
        m_width = widthEnd - widthStart;
        m_position = 0;
        m_start = 0;
        //int f_start = 0;
        int fontHeight = m_font.getHeight() + 1;
        String s = new String();
        lines = new Vector(3, 1);
        do {
            s = nextLine();
            int returnInt = s.indexOf(String.valueOf(returnChar));
            if (returnInt != -1) {
                //contains ^
                String[] parts = split(s, returnChar, count(s, returnChar) + 1);
                for (int i = 0; i < parts.length; i++) {
                    lines.addElement(parts[i]);
                }
            } else {
                lines.addElement(s);
            }
            y += fontHeight;
        } while (hasMoreLines());
        s = null;
        return lines;
    }

    private static boolean hasMoreLines() {
        return (m_position < (m_length));
    }

    private static String nextLine() {
        int maxLength = m_txt.length();
        int next = next();
        if (m_start >= maxLength || next > maxLength) {
            return null;
        }
        String s = m_txt.substring(m_start, next);
        m_start = next;
        if ((m_txt.length() - 1 > m_start) && ((m_txt.charAt(m_start) == '\n') ||
                (m_txt.charAt(m_start) == ' '))) {
            m_position++;
            m_start++;
        }
        return s;
    }

    private static int next() {
        int i = getNextWord(m_position);
        int lastBreak = -1;
        String line;
        line = m_txt.substring(m_position, i);
        int lineWidth = m_font.stringWidth(line);
        while (i < m_length && lineWidth <= m_width) {
            if (m_txt.charAt(i) == ' ') {
                lastBreak = i;
            } else if (m_txt.charAt(i) == '\n') {
                lastBreak = i;
                break;
            }
            if (++i < m_length) {
                i = getNextWord(i);
                line = m_txt.substring(m_position, i);
                lineWidth = m_font.stringWidth(line);
            }
        }
        if (i == m_length && lineWidth <= m_width) {
            m_position = i;
        } else if (lastBreak == m_position) {
            m_position++;
        } else if (lastBreak < m_position) {
            m_position = i;
        } else {
            m_position = lastBreak;
        }
        return m_position;
    }

    private static int getNextWord(int startIndex) {
        int space = m_txt.indexOf(' ', startIndex);
        int newLine = m_txt.indexOf('\n', startIndex);
        if (space == -1) {
            space = m_length;
        }
        if (newLine == -1) {
            newLine = m_length;
        }
        if (space < newLine) {
            return space;
        } else {
            return newLine;
        }
    }

    /**
     * checks if this mobile contains needed APIs
     * @param apis the string array we want to be compatible
     * @return true if mobile supports all libraries, false if othercase
     * @author fer
     * @throws ClassNotFoundException if one class was not found by <code>Class.forName</code>
     */
    public static boolean isCompatible(String[] apis) throws ClassNotFoundException {
        boolean ret = false;
        try {
            //System.out.print("This device is compatible with: ");
            if (apis.length > 0) {
                for (int i = 0; i < apis.length; i++) {
                    Class.forName(apis[i]);
                    ret = true;
                }
            } else {
                ret = true;
            }
        } catch (ClassNotFoundException e) {
            //some problem?
            ret = false;
            //Utils.debugModePrintStack(e, "MirbluMapsMIDlet", "isCompatible");
            throw new ClassNotFoundException(e.toString());
        } finally {
            if (!ret) {
                //Utils.debugMode(" -- Device NOT COMPATIBLE -- ", "MirbluMapsMIDlet",
                //        "isCompatible", Utils.DEBUG_ERROR);
            }
        }
        return ret;
    }

    /**
     * Counts the number of times that some value is found on the text
     * @param text the based text to search for value
     * @param value the value to search into the text
     * @return a {@link Vector<Integer>} with all indexes of value founded
     */
    public static final Vector getOccurrences(String text, String value) {
        Vector ret = new Vector();
        int index = text.indexOf(value);
        while (index != -1) {
            ret.addElement(new Integer(index));
            index = text.indexOf(value, index + 1);
        }
        return ret;
    }

    /**
     * Counts the number of times that some value is found on the text
     * @param text the based text to search for value
     * @param value the value to search into the text
     * @return a {@link Vector<Integer>} with all indexes of value founded
     */
    public static final int count(String text, char value) {
        int c = 0;
        if (!isNull(text) && !text.equals("")) {
            int index = text.indexOf(value);
            while (index != -1) {
                index = text.indexOf(value, index + 1);
                c++;
            }
        }
        return c;
    }
    
    //private static int[] rawInt;

    /**
     * Draws a rectangle filled with a color and with some alpha transparency
     * @param g the graphic to draw in
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     * @param alpha the transparency to apply: from 0 (fully transparent) to 255 (fully opaque)
     * @param color the color of the rectangle in hexadecimal
     */
    /*public static final void fillRoundRectAlpha(Graphics g, int x, int y, int width, int height, int alpha, int color) {
        rawInt = new int[width * height];
        //creates a white image
        Image ret = Image.createImage(width, height);
        ret.getRGB(rawInt, 0, width, 0, 0, width, height);
        blend(color, alpha);
        ret = Image.createRGBImage(rawInt, width, height, true);
        g.drawImage(ret, x, y, Graphics.LEFT | Graphics.TOP);
        ret = null;
    }*/

    /**
     * Applies a alpha value to all colors at rawInt[]
     * @param alphaValue the int alpha value to apply, from 0 (transparent) to 255 (opaque)
     */
    /*private static void blend(int color, int alphaValue) {
        int len = rawInt.length;
        int i = 0;
        int value = ((0x00333399 & color) + (alphaValue << 24));
        // Start loop through all the pixels in the image.
        for (; i < len; i++) {
            rawInt[i] += value;
            //rawInt[i] += ((rawInt[i] & color) + (alphaValue<<24));
        }
    }*/
    /**
     * FILE UTILS
     */
    public static final String HOT_SPOTS_FILENAME = "/hotspots.txt";
    public static final char HOTSPOTS_FILE_DIVIDE_CHAR = '|';
    public static final int HOTSPOTS_FILE_ARRAY_SIZE = 2;
    public static final int HOTSPOTS_FILE_ID_POS = 0;
    public static final int HOTSPOTS_FILE_TEXT_POS = 1;

    /**
     * Read a file an returns a String for each line
     * @param filename the filename to read from /res/ folder
     * @return a Vector<String> with the text data readed
     */
    public Vector readFile(String filename) {
        Vector ret = new Vector();
        InputStream is = null;
        StringBuffer sb = null;
        try {
            sb = new StringBuffer();
            is = this.getClass().getResourceAsStream(filename);
            int c;
            while ((c = is.read()) != -1) {
                if ((char) c != '\n') {
                    sb.append((char) c);
                } else {
                    ret.addElement(sb.toString());
                    sb = new StringBuffer();
                }
            }
        } catch (Throwable ex) {
            //Utils.debugModePrintStack(ex, "Utilities", "readFile(" + filename + ")");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                //Utils.debugModePrintStack(e, "Utilities", "readFile(" + filename + ")2");
            } finally {
                sb = null;
                is = null;
            }
        }
        return ret;
    }
    public static final byte FILTER_EXCLUDE = 1;
    public static final byte FILTER_INCLUDE = 2;

    /**
     * Filter a Vector<String> by filter given.
     * It searchs filter in the Vector to return all values including or excluding values like <code>filter</code>
     * <br><code>filter(Vector("abc", "bce", "cef"), "b", Utils.FILTER_EXCLUDE)</code>
     * <br> will return <code>Vector("abc","bce")</code>
     * <br>
     * <br><code>filter(Vector("1234", "5678", "90"), "5", Utils.FILTER_INCLUDE)</code>
     * <br> will return <code>Vector("1234","90")</code>
     * @param source the Vector<String> with all the data
     * @param filter the string filter; if "" will return the given Vector<String>
     * @param filterType the type of filter: <br> Can be <code>Utils.FILTER_EXCLUDE</code> or <code>Utils.FILTER_INCLUDE</code>
     * @return a new Vector<String> with all string that has one instance of filter.
     */
    public static Vector filter(Vector source, String filter, byte filterType) {
        if (filter.equals("")) {
            return source;
        }
        Vector ret = new Vector();
        int i = 0;
        int size = source.size();
        String current;
        filter = filter.toLowerCase();
        for (; i < size; i++) {
            current = ((String) source.elementAt(i));
            switch (filterType) {
                case FILTER_EXCLUDE:
                    //if not found, add
                    if (current.toLowerCase().indexOf(filter) != -1) {
                        ret.addElement(current);
                    }
                    break;
                case FILTER_INCLUDE:
                    //if found, not add
                    if (current.toLowerCase().indexOf(filter) == -1) {
                        ret.addElement(current);
                    }
                    break;
                default:
                    break;
            }
        }
        //Utils.debugMode("filterList return " + ret.size() + "/" + size + ". Filter was = '" + filter + "'",
        //        "Utilities", "filter", Utils.DEBUG_INFO);
        current = null;
        filter = null;
        return ret;
    }

    /**
     * Split a string into a string array dividing it by given char. <br>
     * Missed values will be a empty String
     * @param source the source string
     * @param divide is the char to found
     * @param arraySize the expected size of return array
     * @return a string array
     */
    public static final String[] split(String source, char divide, int arraySize) {
        String[] ret = null;
        ret = new String[arraySize];
        if (arraySize > 0) {
            try {
                int begin = 0;
                int end = source.indexOf(divide, begin);
                int i = 0;
                while (end != -1 || begin > source.length()) {
                    if (begin == end) {
                        ret[i] = "";//missed values will be empty
                    }
                    ret[i] = source.substring(begin, end);
                    begin = end + 1;
                    end = source.indexOf(divide, begin);
                    i++;
                }
                //add last part
                ret[i] = source.substring(begin, source.length());
            } catch (Throwable e) {
                //e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Replaces a String for another inside a text
     * @param replace the text to delete, can't be ""
     * @param replacement the text to put
     * @param text the String to modify
     * @return the new String with changes applied
     */
    public static StringBuffer replaceText(String replace, String replacement, String text) {
        if (replace.equals("")) {
            return new StringBuffer(text);
        }
        int begin = text.indexOf(replace);
        int end = begin + replace.length();
        StringBuffer ret = new StringBuffer(text.substring(0, begin));
        ret.append(replacement);
        ret.append(text.substring(end, text.length()));
        //Utils.debugMode(text, "Utils", "replaceText", Utils.DEBUG_INFO);
        //Utils.debugMode(ret.toString(), "Utilities", "replaceText", Utils.DEBUG_INFO);
        return ret;
    }

    /**
     * reads the list of categories and its min and Max ids
     * @param fileName
     * @return a Vector<String,int,int> like "name",mId, MId
     */
    public static void readCategories(String fileName) {
        Vector lines = new Utils().readFile(fileName);
        Categories.newInstance(lines.size());
        String line;
        String[] cat;
        int i = 0;
        for (; i < lines.size(); i++) {
            line = (String) lines.elementAt(i);
            if (count(line, Categories.CAT_SEPARATOR) + 1 != Categories.ATT_SIZE) {
                System.out.println("Category bad format: " + line);
            }
            cat = Utils.split(line, Categories.CAT_SEPARATOR, Categories.ATT_SIZE);
            Categories.addCategory(cat, i);
        }
        lines = null;
        line = null;
    }

    public static void readProducts(String fileName) {
        Vector lines = new Utils().readFile(fileName);
        Products.newInstance(lines.size());
        String line;
        int i = 0;
        for (; i < lines.size(); i++) {
            line = (String) lines.elementAt(i);
            int sepFound = count(line, Products.PROD_SEPARATOR);
            if (sepFound + 1 != Products.ATT_SIZE) {
                System.out.println("Product bad format: " + line);
                System.out.println("expected " + Products.ATT_SIZE + " found " + (sepFound + 1));
            } else {
                Products.addProduct(Utils.split(line, Products.PROD_SEPARATOR, Products.ATT_SIZE), i);
            }
        }
        line = null;
        lines = null;
    }

    public static int[] toIntArray(String source, char separator) {
        int[] ids = new int[count(source, separator) + 1];
        String[] ss = split(source, Categories.PROD_LIST_SEPARATOR, ids.length);
        int i = 0;
        for (; i < ss.length; i++) {
            ids[i] = Integer.valueOf(ss[i]).intValue();
        }

        return ids;
    }
    /*public static Products readProductsForCategory(String fileName, int catId){
    String productsIds = Categories.getCategoryProperty(catId, Categories.PROD_LIST);
    Products ret = Products.newInstance(count(productsIds, Products.PROD_SEPARATOR) + 1);
    Vector lines = new Utils().readFile(fileName);
    String line;
    StringBuffer id;
    int i = 0;
    for (; i < lines.size(); i++) {
    line = (String)lines.elementAt(i);
    //id is the first occurence of separator
    id = new StringBuffer(line.substring(0, line.indexOf(String.valueOf(Products.PROD_SEPARATOR))));
    id.append(Categories.PROD_LIST_SEPARATOR);
    if (productsIds.indexOf(id)) {

    }
    }
    line = null;
    id = null;
    productsIds = null;
    lines = null;
    return ret;
    }*/
}
