package items.image;

import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
import utils.Utils;
import items.FSuperItem;
import items.menu.FHoverMenu;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * A GameCanvas that can auto paint FSuperItem objects
 * @author fernando
 */
public class FCanvas extends GameCanvas {

    private static final byte OFFSET = 5;
    /** 0x00RRGGBB, by default black */
    public static int backgroundColor = 0x00000000;
    /** 0x00RRGGBB, by default white */
    public static int textColor = 0x00FFFFFF;
    public static boolean isPointerScreen = false;
    /** current X and Y for drawing elements */
    int currX = 0, currY = 0;
    int freeW = getWidth(), freeH = getHeight();
    int pinX = 0, pinY = 0;
    public static int starty, startx;
    /** focus just can be on one FItem */
    public int focusIndex = Utils.FOCUS_FIRST_ELEMENT;
    private boolean focus = false;
    public FInteraction interaction;
    public byte next = FInteractionUtils.ACTION_NONE;
    /**Vector<FSuperItem> all FItems this Screen has */
    public Vector items = new Vector();
    /** title height when its painted */
    private int titleHeight = 0;
    /** activates the scroll */
    public static boolean scrollActivated = false;
    //public static boolean counterTitle = false;
    //public static int itemsSize = 0;
    public static int itemsToPaint = 0;
    public static byte HOVER_MENU_NONE = -1;
    /** the hover menu position at items vector */
    public static byte hoverMenuAt = HOVER_MENU_NONE;
    public static int canvasWidth, canvasHeight;
    public static FSuperItem focusedItem;
    public static final byte FOCUS_INC_NONE = -1;//negative values to allow item selection
    public static final byte FOCUS_INC_PLUS = -2;
    public static final byte FOCUS_INC_MINUS = -3;
    public static Font bigFont = Font.getFont(
            Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
    public static Font medFont = Font.getFont(
            Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static Font smallFont = Font.getFont(
            Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private static boolean firstTime = true;
    public static int hasTextAt;

    /**
     * Creates a screen with some elements
     * @param elements the {@link Vector} that contains all the
     * items to paint on the display
     * @param title the title for this canvas
     * @param fullScreen if true {@link GameCanvas} is setted
     * @param focusIndex: Utils.FOCUS_FIRST_ELEMENT, Utils.FOCUS_SELF_ELEMENT, Utils.FOCUS_NONE_ELEMENT, or the item index
     */
    public FCanvas(Vector elements, String title, boolean fullScreen, int focusIndex) {
        super(false);
        init(fullScreen);
        setTitle(title);
        if (elements != null) {
            items = elements;
            itemsToPaint = getNumberOfItemsFitCanvas(0);// - 1;
            scrollActivated = autoScroll();
            switch (focusIndex) {
                case Utils.FOCUS_FIRST_ELEMENT:
                    setFocusToItem((byte) 0);
                    break;
                case Utils.FOCUS_SELF_ELEMENT:
                    setFocusToCanvas();
                    break;
                case Utils.FOCUS_NONE_ELEMENT:
                    setFocusToItem(FOCUS_INC_NONE);
                    break;
                default:
                    if (hoverMenuAt == focusIndex) {
                        setFocusToHoverMenu(true);
                    } else {
                        setFocusToItem((byte) focusIndex);
                    }
                    break;

            }
        }
    }

    /**
     * Creates a FCanvas from another.
     * Sets FullScreenMode to true
     * @param c the FCanvas
     */
    public FCanvas(FCanvas c, boolean fullScreen) {
        super(false);
        init(fullScreen);
        setTitle(c.getTitle());
        items = c.items;
        itemsToPaint = getNumberOfItemsFitCanvas(0);
        scrollActivated = autoScroll();
    }

    public FCanvas() {
        super(false);
        init(false);
    }

    public void setTitle(String title) {
        super.setTitle(title);
        starty = Utils.getWrappedTextWidthHeigh(title, bigFont, 0, canvasWidth, false, null, 0)[1];        
        System.out.println(title + " y = " + starty);
    }

    private void init(boolean fullScreen) {
        repaint();
        setFullScreenMode(fullScreen);
        repaint();//needed two repaints in some devices.
        //hasTextAt = -1;
        if (isPointerScreen()) {
            isPointerScreen = true;
        }
        if (Utils.isNull(interaction)) {
            interaction = FInteraction.newInstance();
        }
        interaction.addInteraction(Utils.KEY_SELECT, FApp.STATE_RELOAD_CURRENT);
        interaction.addInteraction(Utils.KEY_CLEAR, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
    }

    /**
     * Paints all items that can fit canvas size.
     * If some trouble goes to main screen.
     * @param g
     */
    public void paint(Graphics g) {
        if (firstTime) {
            //avoids nokia bugs
            canvasWidth = this.getWidth();
            canvasHeight = this.getHeight();
            if (freeW > canvasWidth) {
                canvasWidth = freeW;
            }
            if (freeH > canvasHeight) {
                canvasHeight = freeH;
            }
        }
        int oldColor = g.getColor();
        try {
            drawBackground(g);
            drawTitle(g);
            if (scrollActivated) {
                itemsToPaint = getNumberOfItemsFitCanvas(focusIndex);
                paintIt(g, focusIndex, focusIndex + itemsToPaint);
            } else {
                //paints all items
                paint(g, 0, items.size());
            }
            //drawInfo(g);
            Utils.debugDrawMemUsage(g);
        } catch (Throwable e) {
            //Utils.debugModePrintStack(e, "FCanvas", "painting app state = " + FApp._current);
            FApp._app.changeStateTo(FApp.STATE_MAIN_SCREEN);
        }
        //g.drawString(String.valueOf(FBluetooth.state), canvasWidth, 0, Graphics.RIGHT | Graphics.TOP);
        g.setColor(oldColor);
    }

    /**
     * this method head is needed for childrens who doesn't override paint method
     */
    public void drawNavigationArrows(Graphics g) {
    }

    /**
     * Calls the paint method for one {@link FSuperItem}
     * @param g the {@link Graphics} where to paint
     * @param fi the {@link FSuperItem} to paint, must say us where to paint
     * next item
     */
    private void paint(Graphics g, FSuperItem fi) {
        try{
            NImage ni = (NImage) fi;
            ni.paint(g);

        }catch(Exception ex){
        fi.x = currX;
        fi.y = currY;
        fi.paint(g);
        //currX = fi.fwidth + OFFSET;
        currY = fi.fheight + OFFSET;
        //TODO fix totHeight operation, this is just a patch
        if (currY > canvasHeight) {
            scrollActivated = true;
        }
        }
    }

    /**
     * Paints all items this canvas contains from begin to end.<br>
     * begin must be less than end
     * @param g the Graphic object
     * @param begin the first object to paint, must be zero or greater than zero
     * @param end the last item to paint, must be equal or less than getItems().size()
     */
    private void paint(Graphics g, int begin, int end) {
        if (begin < end && begin >= 0 && end <= items.size()) {
            int i = begin;
            FSuperItem fi = null;
            for (; i < end; i++) {
                fi = ((FSuperItem) items.elementAt(i));
                if (Utils.isNotNull(fi)) {
                    paint(g, fi);
                    if (i == focusIndex) {//better than hasFocus()
                        drawNavigationArrows(g);
                    }
                }
            }
            fi = null;
        }
    }

    /**
     * Recalculates begin and end to fit items size and skip invalid values
     * @param g
     * @param begin the first element
     * @param end the last element
     */
    private void paintIt(Graphics g, int begin, int end) {
        if (begin > end) {
            int temp = end;
            end = begin;
            begin = temp;
        }
        if (begin < 0 || end < 0) {
            begin = 0;
            end = itemsToPaint;
        }
        if (end > items.size()) {
            begin = items.size() - itemsToPaint;
            end = items.size();
        }
        if (begin > items.size() || begin < 0) {
            begin = 0;
            end = items.size();
        }
        paint(g, begin, end);
    }

    /**
     * Controls key events for scroll (if needed) and sets focus.
     * <br> If FCanvas has interaction it is procesed before focus item interaction but is not exclusive.
     * <br> If focus item has ANYKEY interaction it is executed before FCanvas and is exclusive
     * <br> If both canvas and focus item have an action for that key both will be executed
     *
     * @param k the key received
     */
    //IF AND ONLY IF <code>focusedItem.execute(action)</code> returns <code>true</code> for that action
    public void keyPressed(int k) {
        if (items.size() > 0) {
            if (focusIndex < items.size()) {
                focusedItem = (FSuperItem) items.elementAt(focusIndex);
            }
            //execute any key action for selected item
            //si es un inputText solo ejecuta algo si se va a escribir
            if (Utils.isNotNull(focusedItem) && Utils.isNotNull(focusedItem.interaction)) {
                if (focusedItem.interaction.isAnyKey()) {
                    focusedItem.execute(k);
                    return;
                } else if (focusedItem.interaction.lenght() > 0) {
                    if (!focusedItem.execute(k)) {
                        return;
                    } else {
                    }
                }
            }
        }
        switch (k) {
            case UP:
            case Utils.KEY_UP:
                setFocusToItem(FOCUS_INC_MINUS);
                break;
            case FIRE:
            case Utils.KEY_SELECT:
                break;
            case DOWN:
            case Utils.KEY_DOWN:
                setFocusToItem(FOCUS_INC_PLUS);
                break;
            //the destroy key
            case Utils.KEY_EXIT:
                FInteraction.cancelTask();
                break;
            default:
                //this canvas will perform the action
                if (Utils.isNotNull(this.interaction) && this.interaction.lenght() > 0) {
                    //only focused items can execute actions
                    this.focus = true;
                    this.execute(k);
                    this.focus = false;
                }
                break;
        }
    }

    /**
     * Called when a key is repeated (held down).
     *
     * @param keyCode
     */
    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    /**
     * Executes the {@link FInteraction} asociated with key given.
     * <br>If {@link FInteraction} is ANYKEY key given is not used
     * <br>Its needed that <code>this.interaction != null</code> and
     * <code>this.hasFocus() = true</code>
     * <br>Tipical usage:<br>
     * <code>this.interaction = FInteraction.newInstance();<br>
    this.interaction.addInteraction(Utils.KEY_LEFT, ACTION_GO_LEFT);</code>
     * <br>
     * @param key the key pressed
     * @return <code>true</code> if canvas must execute another action or
     * <code>false</code> if canvas must not execute another action, <br>by default <code>true</code>.
     */
    public boolean execute(int key) {
        System.out.println("Canvas execute key: " + key);
        boolean ret = true;
        if (Utils.isNotNull(interaction)) {
            if (focus) {
                byte action;
                if (interaction.isAnyKey()) {
                    action = interaction.getAnykeyAction();
                } else {
                    action = interaction.getAction(key);
                }
                ret = execute(action);
            }
//            if (hasText()) {
//                FSuperItem textItem = (FSuperItem)getItems().elementAt(hasTextAt);
//                textItem.setFocus(true);
//                ret = textItem.execute(key);
//                textItem.setFocus(false);
//                textItem = null;
//            }
        }
        return ret;
    }

    /**
     * Executes an action for this item when is focused.
     * You can define some actions from FInteractionUtils or create your own.
     * Its necesary to override this method to allow any action from any item.
     */
    public boolean execute(byte action) {
        System.out.println("Canvas execute action: " + action);
        boolean ret = true;
        switch (action) {
            case FInteractionUtils.ACTION_EXIT:
                FInteractionUtils.exit();
                //FInteractionUtils.changeToMainMenu();
                break;
            case FInteractionUtils.ACTION_GO_BACK:
                FInteractionUtils.goBack();
                break;
            case FInteractionUtils.ACTION_JUMP_TO_SCREEN:
                if (next != FInteractionUtils.ACTION_NONE) {
                    FInteractionUtils.changeToXScreen(next);
                }
                break;
            case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                FInteractionUtils.changeToMainMenu();
                break;
            case FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU:
                FInteractionUtils.setFocusToHoverMenu();
                break;
            case FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM:
                FInteractionUtils.setFocusToNextItem();
                break;
            case FInteractionUtils.ACTION_RETRY_BLUETOOTH:
                FInteractionUtils.retryBluetooth();
                FApp._app.changeStateTo(FApp.STATE_RELOAD_CURRENT);
                break;
            case FInteractionUtils.ACTION_SECONDARY_MENU:
                FInteractionUtils.setSecondaryMenu();
                break;
            default:
                break;
        }
        return ret;
    }

    public void setFocusToCanvas() {
        removeFocusForAll();
        this.focus = true;
    }
    private static int deepness;
    private static FSuperItem auxItem;

    /**
     * Set the focus to <code>true</code> for one {@link FSuperItem}.
     * Do nothing if items is <code>null</code> <br>
     * Sets focus to this canvas if has no elements.
     * @param increment:
     * <br> FOCUS_INC_NONE increment is zero
     * <br> FOCUS_INC_PLUS give focus to next item
     * <br> FOCUS_INC_MINUS give focus to previous item
     * <br> other: give focus to expecified item
     * @see focusNumber var
     */
    public void setFocusToItem(byte increment) {

        if (increment != focusIndex && Utils.isNotNull(items)) {
            if (items.size() <= 0) {
                //has no items, this canvas received the focus
                setFocusToCanvas();
                return;
            }
            //remove focus for all items
            removeFocusForAll();
            switch (increment) {
                case FOCUS_INC_NONE:
                    break;
                case FOCUS_INC_PLUS:
                    focusIndex++;
                    break;
                case FOCUS_INC_MINUS:
                    focusIndex--;
                    break;
                default:
                    if (increment >= 0 && increment < items.size()) {
                        focusIndex = increment;
                    }
                    break;
            }
            //set focus limits
            if (focusIndex < 0) {
                focusIndex = items.size() - 1;//last one
            } else if (focusIndex > items.size() - 1) {
                focusIndex = 0;//first one
            }

            auxItem = (FSuperItem) items.elementAt(focusIndex);
            if (Utils.isNotNull(auxItem) && auxItem.isFocusable()) {
                auxItem.setFocus(true);
            } else {
                //avoid infite loop
                deepness++;
                if (deepness <= items.size()) {
                    switch (increment) {
                        case FOCUS_INC_MINUS:
                        case FOCUS_INC_PLUS:
                        case FOCUS_INC_NONE:
                        default:
                            setFocusToItem(increment);
                            break;
                    }
                    //setFocusToItem(increment);
                    deepness = 0;
                } else {
                    setFocusToCanvas();
                }
            }
            //set the unique focus item
            focusedItem = auxItem;
            auxItem = null;

            Utils.callRepaint();
        }
        //System.out.println("setFocusToItem " + increment + " and focusIndex= " + focusIndex + " and hoverAt " + hoverMenuAt);
    }
    private static final Font titleFont = Font.getFont(
            Font.FACE_SYSTEM,
            Font.STYLE_BOLD,
            Font.SIZE_LARGE);

    /**
     * Paints the title of this canvas.
     * @param g
     */
    private void drawTitle(Graphics g) {
        if (Utils.isNotNull(this.getTitle())) {
            currY = Utils.write(
                    g, this.getTitle(), 0, 0,
                    canvasWidth, titleFont, textColor);
            titleHeight = currY;
        }
    }

    private void drawBackground(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        //menu background
        switch (FApp._current) {
            case FApp.STATE_MAIN_SCREEN:
            case FApp.STATE_SCREEN_SELECT_MAPS:
            case FApp.STATE_SCREEN_SOURCES:
            case FApp.STATE_SCREEN_DESTINATIONS:
            case FApp.STATE_SCREEN_TRACEME:
            case FApp.STATE_SCREEN_CATEGORIES:
            case FApp.STATE_SCREEN_PRODUCTS:
            case FApp.STATE_SCREEN_PRODUCTS_SPLASH:
            case FApp.STATE_SCREEN_SHOW_URIS:
                g.drawImage(FApp._theMIDlet.backgroundImg,
                        canvasWidth, canvasHeight,
                        Graphics.RIGHT | Graphics.BOTTOM);
                break;
            case FApp.STATE_SCREEN_ABOUT:
                g.drawImage(FApp._theMIDlet.backgroundAbout,
                        canvasWidth, canvasHeight,
                        Graphics.RIGHT | Graphics.BOTTOM);
                break;
        }
    }

    public Vector getItems() {
        return items;
    }

    public void setItems(Vector items) {
        this.items = items;
        this.items.setSize(items.size());
    }

    /**
     * Calculates how many items can fit canvas height
     * @param begin the item number to start with
     * @return the number of items that can fit the screen height
     */
    private int getNumberOfItemsFitCanvas(int begin) {
        int ret = 0;
        int totHeight = titleHeight;
        int i = begin;
        FSuperItem fi = null;
        for (; i < items.size(); i++) {
            fi = (FSuperItem) items.elementAt(i);
            totHeight += (fi.h + OFFSET);
            if (totHeight > canvasHeight || (totHeight + fi.h) > canvasHeight) {
                fi = null;
                return ret;
            } else {
                ret++;
            }
        }
        if (begin > 0) {
            i = 0;
            for (; i < begin; i++) {
                fi = (FSuperItem) items.elementAt(i);
                totHeight += (fi.h + OFFSET);
                if (totHeight > canvasHeight || (totHeight + fi.h) > canvasHeight) {
                    fi = null;
                    return ret;
                } else {
                    ret++;
                }
            }
        }
        fi = null;
        return ret;
    }

    /**
     * Activates the autoscroll for this canvas
     * @return true if autoscroll will be on, <code>itemsSize <= itemsToPaint</code>
     */
    private boolean autoScroll() {
        boolean ret = false;
        if (items.size() <= itemsToPaint) {
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }

    /**
     *
     * HOVER MENU METHODS
     *
     */
    /**
     * @return true if hoverMenuAt is greater than HOVER_MENU_NONE
     */
    public boolean hasHoverMenu() {
        boolean ret = false;
        if (hoverMenuAt > 0 && hoverMenuAt <= items.size()) {
            ret = true;
        }
        //System.out.println("hasHoverMenu size:" + items.size() + " and focusIndex= " + focusIndex + " and hoverAt " + hoverMenuAt);
        return ret;
    }

    public void setHoverMenuAt(byte pos) {
        hoverMenuAt = pos;
    }

    public void removeHoverMenu() {
        if (hasHoverMenu()) {
            if (items.elementAt(hoverMenuAt).toString().equals(FHoverMenu.FHOVERMENU_CLASSNAME)) {
                items.removeElementAt(hoverMenuAt);
                //System.out.println("deleted menu at " + hoverMenuAt);
            }
            setHoverMenuAt(HOVER_MENU_NONE);
        }
    }

    /**
     * Gives focus to hover menu if this canvas has one (first sets focusable to true)
     */
    public void setFocusToHoverMenu(boolean focus) {
        if (hasHoverMenu()) {
            auxItem = ((FSuperItem) items.elementAt(hoverMenuAt));
            auxItem.setFocusable(focus);
            FHoverMenu.showAll();
            setFocusToItem(hoverMenuAt);
        }
    }

    private void removeFocusForAll() {
        for (int i = 0; i < items.size(); i++) {
            auxItem = (FSuperItem) items.elementAt(i);
            if (Utils.isNotNull(auxItem)) {
                auxItem.setFocus(false);
            }
        }
    }

    private boolean hasText() {
        if (hasTextAt > -1) {
            return true;
        } else {
            return  false;
        }
    }

    private boolean isPointerScreen() {
        if (hasPointerEvents()) {
            return true;
        }
        if (hasPointerMotionEvents()) {
            return true;
        }
        return false;
    }
}
