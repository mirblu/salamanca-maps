package items.text;

import items.FSuperItem;
import utils.Utils;
import action.FInteraction;
import app.FApp;

import items.image.FCanvas;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A Class to allow user text input.
 * Must implement the update method, its throwed when user inserts a new char
 * on the inputText or when a char is removed.
 * @author fernando
 */
public abstract class FTextInput extends FSuperItem {

    private static final byte ACTION_GET_CHAR = 0;
    private static final byte ACTION_DELETE_CHAR = 1;
    private static final byte ACTION_LOST_FOCUS = 2;
    private static final byte ACTION_GO_LEFT = 3;
    private static final byte ACTION_GO_RIGHT = 4;
    private static final byte ACTION_GIVE_WRITE_FOCUS = 5;
    public static final int CURSOR_FIRST_POS = -1;
    public static final int CURSOR_LAST_POS = -2;
    public String inputText;
    private static int maxChars = 0;
    private static int textColor = -1;
    private static int textColorInverse = -1;
    private static int focusColor = -1;
    private static Font textFont;
    public static boolean blink = false;
    public static final byte FOCUS_WRITE_DISABLED = 0;
    public static final byte FOCUS_WRITE_FIRST_TIME = -1;
    public static final byte FOCUS_WRITE_ENABLED = 1;
    public byte focusWrite = FOCUS_WRITE_FIRST_TIME;
    /** used to control timeout events */
    protected boolean timeout = false;
    protected long timeoutValue = 0;
    protected long timeoutSched = 500;
    protected long timeoutMax = 1500;
    private Timer tTimeout;
    private Timer tBlink;
    private FTextInputTask blinkTask;
    private static int cursor = 0;
    public static int cursorX = 0;
    public static int cursorY = 0;
    public static int cursorW = 0;
    public static int cursorH = 0;
    private int cursorColor = -1;

    /*used to know what char needs when keypressed*/
    private int key;
    private int lastKey;
    private int currCharPos;
    private String currChar = "";
    private Vector textInputValues = new Vector();

    /**
     * Creates a new FTextInput object. Its a text that can be editable by user.<br>
     * User can change cursor position by LEFT and RIGHT keys <br>
     * User can insert text using phone keyboard <br>
     * that means FInteraction is initialized with lot of keys and actions,
     * but can be overriden after calling this contructor.
     * @param defaultText the text to start with, will be empty when user starts to write
     * @param maxChars the maximun chars allowed to insert
     * @param font the Font for the text
     * @param textColor the color of the text
     * @param textInverseColor the background color or textColor inverse
     * @param focusColor the color for the focus
     * @param cursorStartPos can be:<br>
     * FTextInput.CURSOR_FIRST_POS to start at text begin <br>
     * FTextInput.CURSOR_LAST_POS to start at text end <br>
     * or any other integer less than text used as cursor start position
     * @param cursorColor the color for the cursor
     * @param maxTimeout sets the maximun time to wait when no key pressed
     * by user. Used to insert characters. If less than one, default value is used. (1500ms)
     * @param scheduleTime sets the time to check if user pressed a key.
     * Usefull if is a maxTimeout multiple. If less than one, default value is used.(500ms)
     */
    public FTextInput(String defaultText, int maxChars,
            Font font, int textColor, int textInverseColor, int focusColor,
            int cursorStartPos, int cursorColor,
            long maxTimeout, long scheduleTime) {

        super(defaultText);

        textInputValues.addElement(" 0+-*/");              //0
        textInputValues.addElement(".,-1?!@':;/\\()_^|");   //1
        textInputValues.addElement("abc2áä");
        textInputValues.addElement("def3éë");
        textInputValues.addElement("ghi4íï");
        textInputValues.addElement("jkl5");
        textInputValues.addElement("mno6ñóö");
        textInputValues.addElement("pqrs7");
        textInputValues.addElement("tuv8úü");
        textInputValues.addElement("wxyz9");

        FTextInput.maxChars = maxChars;
        FTextInput.textFont = font;
        FTextInput.textColor = textColor;
        FTextInput.textColorInverse = textInverseColor;
        FTextInput.focusColor = focusColor;

        //addInteraction to allow user write text
        this.interaction = FInteraction.newInstance();
        this.interaction.addInteraction(Utils.KEY_LEFT, ACTION_GO_LEFT);
        this.interaction.addInteraction(Utils.KEY_RIGHT, ACTION_GO_RIGHT);
        this.interaction.addInteraction(Utils.KEY_CLEAR, ACTION_DELETE_CHAR);
        this.interaction.addInteraction(Utils.KEY_0, ACTION_GET_CHAR);//0
        this.interaction.addInteraction(Utils.KEY_1, ACTION_GET_CHAR);//1
        this.interaction.addInteraction(Utils.KEY_2, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_3, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_4, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_5, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_6, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_7, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_8, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_9, ACTION_GET_CHAR);
        this.interaction.addInteraction(Utils.KEY_DOWN, ACTION_LOST_FOCUS);
        this.interaction.addInteraction(Utils.KEY_UP, ACTION_LOST_FOCUS);
        this.interaction.addInteraction(Utils.KEY_EXIT, ACTION_LOST_FOCUS);
        this.interaction.addInteraction(Utils.KEY_SELECT, ACTION_GIVE_WRITE_FOCUS);

        //text given
        this.inputText = new String(defaultText);

        //cursor stuff
        this.cursorColor = cursorColor;
        switch (cursorStartPos) {
            case CURSOR_FIRST_POS:
                cursor = 0;
                break;
            case CURSOR_LAST_POS:
                cursor = defaultText.length();
                break;
            default:
                if (cursorStartPos >= 0) {
                    cursor = cursorStartPos;
                    if (cursor > defaultText.length()) {
                        cursor = defaultText.length();
                    }
                }
                break;
        }

        //writting timeout
        if (maxTimeout > 0) {
            this.timeoutMax = maxTimeout;
        }
        if (scheduleTime > 0) {
            this.timeoutSched = scheduleTime;
        }
        if (this.hasFocusWrite() && this.hasFocus()) {
            startTimeout();
            //cursor blinks
            startBlink();
        }

        //call to update method
        update();
    }

    public void setInputText(String text) {
        this.inputText = text;
    }

    public boolean hasFocusWrite() {
        boolean ret = false;
        if (focusWrite == FOCUS_WRITE_ENABLED) {
            ret = true;
        }
        if (focusWrite == FOCUS_WRITE_DISABLED) {
            ret = false;
        }
        return ret;
    }

    private void setFocusWrite(boolean activate) {
        if (focusWrite == FOCUS_WRITE_FIRST_TIME && activate) {
            inputText = "";
            cursor = 0;
            currChar = "";
            currCharPos = 0;
        }
        if (activate) {
            reStartTimeout();
            reStartBlink();
            focusWrite = FOCUS_WRITE_ENABLED;
        } else {
            cancelTask();
            cancelBlinkTask();
            focusWrite = FOCUS_WRITE_DISABLED;
        }
    }

    public void paint(Graphics g) {
        int curry = 20;
        int currx = 0;
        if (!hasFocus()) {
            Utils.write(g, inputText, 0, curry,
                    FCanvas.canvasWidth,
                    FTextInput.textFont, FTextInput.textColor);
        } else if (hasFocus() && !hasFocusWrite()) {
            //has no write focus but has a focus
            g.setColor(focusColor);
            int rectWidth = textFont.charsWidth(
                    inputText.toCharArray(), 0, inputText.length());
            //draw a rectangle
            g.fillRoundRect(currx, curry, rectWidth,
                    textFont.getHeight() + 1,
                    Utils.IMAGE_ROUND_RECTANGLE, Utils.IMAGE_ROUND_RECTANGLE);
            g.setColor(textColorInverse);
            Utils.write(g, inputText, currx, curry,
                    FCanvas.canvasWidth,
                    FTextInput.textFont, FTextInput.textColorInverse);
            currx = textFont.charsWidth(inputText.toCharArray(),
                    0, inputText.length()) + 1;
            curry = textFont.getHeight() + 1;
        } else if (hasFocus() && hasFocusWrite()) {
            //user is editing the text
            g.setColor(focusColor);
            int rectWidth = textFont.charsWidth(inputText.toCharArray(), 0, inputText.length());
            //draw a rectangle
            g.drawRect(currx, curry, rectWidth, textFont.getHeight() + 1);
            /**
             * divide text by cursor
             */
            //prepare strings
            String firstPart = new String();
            String secondPart = new String();
            if (inputText.equals("")) {
                firstPart = "";
                secondPart = "";
            } else if (cursor <= 0) {
                firstPart = "";
                secondPart = inputText;
            } else if (cursor >= inputText.length()) {
                firstPart = inputText;
                secondPart = "";
            } else {
                firstPart = inputText.substring(0, cursor);
                secondPart = inputText.substring(cursor, inputText.length());
            }
            //draw first part
            Utils.write(g, firstPart, currx, curry,
                    FCanvas.canvasWidth,
                    FTextInput.textFont, FTextInput.textColor);
            if (cursor >= 0 && !firstPart.equals("")) {
                currx = textFont.charsWidth(firstPart.toCharArray(), 0, cursor) + 1;
            } else {
                currx = 1;
            }
            //draws cursor
            if (blink) {
                drawCursor(g, currx, curry);
            }
            //draws current char
            if (!currChar.equals("")) {
                Utils.write(g, currChar, currx, curry,
                        FCanvas.canvasWidth,
                        FTextInput.textFont, FTextInput.textColor);
                //append a space to see what we are inserting
                secondPart = (new StringBuffer(secondPart).insert(0, " ")).toString();
            }
            //draw second part
            Utils.write(g, secondPart, currx, curry,
                    FCanvas.canvasWidth,
                    FTextInput.textFont, FTextInput.textColor);
        }
        this.fheight = FTextInput.textFont.getHeight() + curry;
        this.h = Utils.getWrappedTextHeigh(inputText, textFont,
                x, FApp._theCanvas.getWidth());
        this.fwidth = 0;
        //Utils.debugMode("inputText: " + inputText,
        //        "FTextInput", "paint", Utils.DEBUG_INFO);
    }

    public boolean execute(int key) {
        //Utils.debugMode("Executing an action ",
        //        "FTextInput", "execute(" + key + ") ", Utils.DEBUG_INFO);
        this.key = key;
        return execute(interaction.getAction(key));
    }

    public boolean execute(byte action) {
        boolean moreActionsAfter = false;
        if (hasFocus()) {
            if (!hasFocusWrite()) {
                moreActionsAfter = true;
                switch (action) {
                    case FTextInput.ACTION_GIVE_WRITE_FOCUS:
                        setFocusWrite(true);
                        moreActionsAfter = false;
                        Utils.callRepaint();
                        break;
                    case FTextInput.ACTION_GET_CHAR:
                        setFocusWrite(true);
                        getNextChar();
                        moreActionsAfter = false;
                        break;
                    default:
                        //NACHO
                        //si entra aqui es que hemos pulsado una tecla que no realiza ninguna
                        //acción.Puede ser up, down, left or right
                        break;
                }
            } else if (hasFocusWrite()) {
                reStartTimeout();
                reStartBlink();
                switch (action) {
                    case FTextInput.ACTION_LOST_FOCUS:
                        insertCurrentChar();
                        setFocusWrite(false);
                        setFocus(false);
                        if (inputText.equals("")) {
                            inputText = this.description;
                            cursor = 0;
                            focusWrite = FOCUS_WRITE_FIRST_TIME;
                        } else {
                            cursor = inputText.length();
                        }
                        moreActionsAfter = true;
                        cancelBlinkTask();
                        cancelTask();
                        Utils.callRepaint();
                        break;
                    case FTextInput.ACTION_GO_LEFT:
                        insertCurrentChar();
                        cursor--;
                        if (cursor < 0) {
                            cursor = inputText.length();
                        }
                        moreActionsAfter = false;
                        Utils.callRepaint();
                        break;
                    case FTextInput.ACTION_GO_RIGHT:
                        insertCurrentChar();
                        cursor++;
                        if (cursor > this.inputText.length()) {
                            cursor = 0;
                        }
                        moreActionsAfter = false;
                        Utils.callRepaint();
                        break;
                    case FTextInput.ACTION_DELETE_CHAR:
                        removeChar();
                        moreActionsAfter = false;
                        Utils.callRepaint();
                        break;
                    case FTextInput.ACTION_GET_CHAR:
                        getNextChar();
                        moreActionsAfter = false;
                        Utils.callRepaint();
                        break;
                }
            } else {
                cancelTask();
                cancelBlinkTask();
                moreActionsAfter = true;
            }
        }
        //Utils.debugMode("focus? " + hasFocus() + " action? " + action, "FTextInput", "execute", Utils.DEBUG_INFO);
        return moreActionsAfter;
    }

    private void decreaseCursor() {
        cursor--;
        if (cursor < 0) {
            cursor = 0;
        }
    }

    /**
     * Paints a cursor in the middle of the text
     * @param g the Graphics where we want to paint
     * @param cX the current X coordinate
     * @param cY the current Y coordinate
     */
    private void drawCursor(Graphics g, int cX, int cY) {
        cursorX = cX;
        cursorY = cY;
        cursorW = 2;
        cursorH = textFont.getHeight();
        g.setColor(this.cursorColor);
        g.fillRect(cursorX, cursorY, cursorW, cursorH);
    }

    private void increaseCursor() {
        cursor++;
        if (cursor > inputText.length()) {
            cursor = inputText.length();
        }
    }

    /** checks the array of letters and get the char needed for that key*/
    private void getNextChar() {
        try {
            int keyIntValue = Integer.parseInt(FApp._theCanvas.getKeyName(key));
            if (keyIntValue >= 0 && keyIntValue < textInputValues.size()) {
                String array = (String) textInputValues.elementAt(keyIntValue);
                if (key == lastKey && !currChar.equals("")) {
                    currCharPos++;
                    if (currCharPos >= array.length()) {
                        currCharPos = 0;
                    }
                } else {
                    //if different key insert character
                    insertCurrentChar();
                }
                currChar = String.valueOf(array.charAt(currCharPos));
            }
        } catch (Throwable e) {
            currChar = "";
            //Utils.debugModePrintStack(e, "FTextInput", "getNextChar");
        }
        //Utils.debugMode("key: " + key + " lastkey: " + lastKey + " cChar(" + currCharPos + "): '" + currChar + "'",
        //        "FTextInput", "getNextChar", Utils.DEBUG_INFO);
        lastKey = key;
    }

    /**
     * Inserts the currect character into the input text
     * and increases cursor position only if maxChars allow it
     */
    private void insertCurrentChar() {
        if (!currChar.equals("") && maxChars > inputText.length()) {
            StringBuffer sb = new StringBuffer(inputText);
            sb.insert(cursor, currChar);
            inputText = sb.toString();
            currChar = "";
            currCharPos = 0;
            update();
            increaseCursor();
        }
    }

    /**
     * This method is called when the inputText is updated adding chars or
     * removing them. It react to user interaction and can be used for any
     * purpouse and needs to be implemented by developer.
     */
    public abstract void update();

    /**
     * Deletes the character before current cursor position
     */
    private void removeChar() {
        StringBuffer sb = new StringBuffer(this.inputText);
        if (sb.length() > 0) {
            if (cursor > 0 && cursor < (sb.length())) {
                sb.deleteCharAt(cursor - 1);
            } else if (cursor == sb.length()) {
                sb.deleteCharAt(sb.length() - 1);
            } else if (cursor == 0) {
                //do nothing
            }
            this.inputText = sb.toString();
            decreaseCursor();
            update();
        }
        currChar = "";
        currCharPos = 0;

    }

    /**
     * Starts a scheduled task controling cursor blink
     */
    private void startBlink() {
        //blinkTask = null;
        //t = null;
        blinkTask = new FTextInputTask();
        tBlink = new Timer();
        tBlink.schedule(blinkTask, 500, 500);
        blink = true;
    }


    /*
     * Controls a timeout when no key is pressed.
     *
     */
    private void startTimeout() {
        tTimeout = new Timer();
        TimerTask tt = new TimerTask() {

            public void run() {
                if (timeoutValue >= timeoutMax) {
                    timeout = true;
                    //insert char when timeout
                    if (!currChar.equals("")) {
                        insertCurrentChar();
                    }
                } else {
                    timeoutValue += timeoutSched;
                    timeout = false;
                }
            }
        };
        tTimeout.schedule(tt, timeoutSched, timeoutSched);
    }

    private void reStartTimeout() {
        cancelTask();
        startTimeout();
    }

    private void reStartBlink() {
        cancelBlinkTask();
        startBlink();
    }

    private void cancelBlinkTask() {
        if (Utils.isNotNull(tBlink)) {
            tBlink.cancel();
        }
        blink = false;
    }

    private void cancelTask() {
        if (Utils.isNotNull(tTimeout)) {
            tTimeout.cancel();
        }
        timeout = false;
        timeoutValue = 0;
    }
}