package items;

import utils.Utils;
import action.FInteraction;
import action.FInteractionUtils;

import app.FApp;
import utils.Products;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class FSuperItem {

    public static final byte TYPE_UNKNOW = 0;
    public static final byte TYPE_IMAGE = 1;
    public static final byte TYPE_TEXT = 2;
    /** interactive values */
    private boolean focusable = true;
    private boolean focus = false;
    public FInteraction interaction;
    /** data */
    protected Object content;
    public String description;
    public int x = 0, y = 0, w = 0, h = 0;
    protected int anchor = Graphics.LEFT | Graphics.TOP;
    /** frame values */
    public int fstartX, fstartY, fwidth, fheight;
    /** next state to jump when key pressed, used in combination with FInteraction */
    public byte next = FInteractionUtils.ACTION_NONE;
    /**var to store more data than showed, use manually*/
    public String id;

    /**
     * Creates a new {@link FSuperItem} setting his title.
     * By default can be focused. @see setFocusable, 
     * @param title the title
     */
    public FSuperItem(String title) {
        setDescription(title);
    }

    /**
     * Creates a new instance of a FItem.
     * @param content the Object content for this Item:
     * can be Command, Image, String or Object
     * @param contentType can be Unkown, Image, Text or Command.
     * If contentType is Command it allways is interactive.
     * @param description a string describing this item, can be null.
     * @param posx initial x position
     * @param posy initial y position
     * @param width of this item, if data is a Image can be GET_FROM_IMAGE
     * @param height of this item, if data is a Image can be GET_FROM_IMAGE
     * @param initialState can be showed, hided, selected,
     * unselected, pressed or released; see constants
     */
    public FSuperItem(Object data, byte contentType, String description,
            int posx, int posy,
            int width, int height) {
        setDescription(description);

        setX(posx);
        setY(posy);
        w = width;
        h = height;

        if (Utils.isNotNull(data)) {
            switch (contentType) {
                case TYPE_IMAGE:
                    setContent((Image) data);
                    break;
                case TYPE_TEXT:
                    setContent((String) data);
                    break;
                case TYPE_UNKNOW:
                    setContent(data);
                    break;
                default:
                    setContent(data);
                    break;
            }
        }
    }

    public abstract void paint(Graphics g);

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
        boolean ret = true;
        if (Utils.isNotNull(interaction)) {
            if (this.hasFocus()) {
                byte action;
                if (interaction.isAnyKey()) {
                    action = interaction.getAnykeyAction();
                } else {
                    action = interaction.getAction(key);
                }
                ret = execute(action);
            }
        }
        return ret;
    }

    /**
     * Executes an action for this item when is focused.
     * You can define some actions from FInteractionUtils or create your own.
     * Its necesary to override this method to allow any action from any item.
     */
    public boolean execute(byte action) {
        boolean ret = true;
        switch (action) {
            case FInteractionUtils.ACTION_EXIT:
                FInteractionUtils.exit();
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
                case FInteractionUtils.ACTION_SECONDARY_MENU:
                FInteractionUtils.setSecondaryMenu();
                break;
            case FInteractionUtils.ACTION_PLATFORM_REQUEST:
                System.out.println(FApp._theCanvas.focusedItem.description);
                if (FApp._last == FApp.STATE_SCREEN_PRODUCTS_SPLASH) {
                    //TODO get the current uri
                    FInteractionUtils.platformRequest(FApp._theCanvas.focusedItem.description);
                }
                break;
            case FInteractionUtils.ACTION_NONE:
                break;
            default:
                ret = FApp._theCanvas.execute(action);
                break;
        }
        return ret;
    }

    /**
     * Sets if this item can get the focus or will be never focused
     * @param canHasFocus true or false
     */
    public final void setFocusable(boolean canHasFocus) {
        this.focusable = canHasFocus;
    }

    public final boolean isFocusable() {
        return focusable;
    }

    /**
     * Just can be focused if <code>focusable</code> is true.
     * Performs an action if it has a focusAction
     * @param value
     */
    public void setFocus(boolean value) {
        if (isFocusable() && value != hasFocus()) {
            //System.out.print(focus + " -> " + value);
            this.focus = value;
            //System.out.println(" Focus to " + FApp._theCanvas.focusIndex);
            runFocusAction();
        }
    }

    public final boolean hasFocus() {
        return focus;
    }

    /**
     * @return the content
     */
    public final Object getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public final void setContent(Object content) {
        this.content = content;
    }

    /**
     * @return the description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param x the x to set
     */
    public final void setX(int x) {
        //if (x == FSuperItem.GET_FROM_INNER_PROPERTIES) {
        //    this.x = 0;
        //} else {
            this.x = x;
        //}
    }

    /**
     * @param y the y to set
     */
    public final void setY(int y) {
        //if (y == FSuperItem.GET_FROM_INNER_PROPERTIES) {
        //    this.y = 0;
        //} else {
            this.y = y;
        //}
    }

    public final int getAnchor() {
        return anchor;
    }

    public final void setAnchor(int anchor) {
        this.anchor = anchor;
    }

    private void runFocusAction() {
        if (Utils.isNotNull(this.interaction) && this.interaction.isFocusKey()) {
            switch (this.interaction.focusKey) {
                case FInteraction.FOCUS_ACTION_GET_FOCUS:
                    if (hasFocus()) {
                        execute(this.interaction.getFocusAction());
                    }
                    break;
                case FInteraction.FOCUS_NO_ACTION:
                default:
                    return;
            }
        }
    }

    public String toString() {
        return description;
    }

}
