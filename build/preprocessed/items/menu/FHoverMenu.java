package items.menu;

import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
import items.FSuperItem;
import items.image.FCanvas;
import items.image.maps.FMap;
import items.image.maps.FRutePlanner;
import utils.Utils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A menu drawn over the rest of items. It will have some options like:
 * go back, go to main menu, give focus to current canvas and customized ones.
 * It is a container of some image items and its interactions.
 * By default is not focusable due to receive
 * focus by @link FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU
 * @author fernando
 */
public class FHoverMenu extends FSuperItem {

    public static final String FHOVERMENU_CLASSNAME = "FHoverMenu";

    /**21*/
    public static final byte ACTION_FOCUS_TO_LEFT = 21;
    /**22*/
    public static final byte ACTION_FOCUS_TO_RIGHT = 22;
    /**icon image separation has a value of 10*/
    public static final byte IMAGE_ICON_SEPARATION = 10;
    private static boolean showAll = true;
    /**Graphics.VCENTER | Graphics.HCENTER*/
    public static final byte IMAGE_ANCHOR = Graphics.VCENTER | Graphics.HCENTER;
    /**Graphics.HCENTER | Graphics.BASELINE*/
    public static final byte TEXT_ANCHOR = Graphics.HCENTER | Graphics.BASELINE;
    private static int focused = -1;
    private static int nItems = -1;
    private static String[] names;
    private static Image[] images;
    private static byte[] actions;
    private static String[] actionData;
    //TODO automate
    public static int hoverMenuHeight = 30;

    /**
     * Creates a menu with some 'buttons'
     * @param titles the buttons titles
     * @param icon the buttons icons
     * @param actions the action for each button to perform when key pressed is SELECT
     * @param actionData the action data if needed
     */
    public FHoverMenu(String[] titles, Image[] icon, byte[] actions, String[] actionData) {
        super(FHOVERMENU_CLASSNAME);
        init(titles, icon, actions, actionData);
    }

    private final void init(String[] titles, Image[] icon, byte[] actions, String[] actionData) {
        FHoverMenu.focused = 0;
        setFocusable(false);
        FHoverMenu.nItems = titles.length;
        FHoverMenu.names = titles;
        FHoverMenu.images = icon;
        FHoverMenu.actions = actions;
        FHoverMenu.actionData = actionData;
        hoverMenuHeight = icon[0].getHeight();
        this.interaction = FInteraction.newInstance();
        this.interaction.addInteraction(Utils.KEY_LEFT, ACTION_FOCUS_TO_LEFT);
        this.interaction.addInteraction(Utils.KEY_RIGHT, ACTION_FOCUS_TO_RIGHT);
    }

    /**
     * Removes data attached to this menu
     */
    public static final void delete() {
        focused = -1;
        nItems = -1;
        names = null;
        images = null;
        actions = null;
        actionData = null;
    }
    private static int offset;
    private static int yy, xx;

    public void paint(Graphics g) {
        //initialize variables
        //NACHO
        //offset = (FCanvas.canvasWidth / nItems);
        offset=images[0].getWidth()+10;
        xx = FCanvas.canvasWidth >> 1;
        yy = FCanvas.canvasHeight - IMAGE_ICON_SEPARATION - (images[focused].getHeight() >> 1);
        if (showAll) {
            paintTo(g, DIR_LEFT);
            paintTo(g, DIR_RIGHT);
        }
        //paint the focused item
        paint(g, focused, xx, yy);
    }

    /**
     * Only paints the name of the button at focused item
     * @param g
     * @param i
     * @param x
     * @param y
     */
    private void paint(Graphics g, int i, int x, int y) {
        try {
            if (isFocusable() && i == focused) {
                if (showAll) {
                    y -= images[i].getHeight() >> 1;
                    g.setFont(FApp._theCanvas.bigFont);
                    g.setColor(Utils.COLOR_TANGO_ALUMINIUM1);
                    g.drawString(names[i], x - 1, y - IMAGE_ICON_SEPARATION - 1, TEXT_ANCHOR);
                    g.setColor(FApp._theCanvas.textColor);
                    g.drawString(names[i], x, y - IMAGE_ICON_SEPARATION, TEXT_ANCHOR);
                    hoverMenuHeight = FCanvas.canvasHeight - y;
                }
            }
            g.drawImage(images[i], x, y, IMAGE_ANCHOR);
            //Utils.debugMode(" painting... '" + names[i] + "' @ (" + x + " , " + y + ")", "FHoverMenu", "paint", Utils.DEBUG_INFO);
        } catch (Throwable e) {
            //Utils.debugModePrintStack(e, "FHoverMenu", "paint");
        }
    }

    /**
     * Limit the keys that can be used to left, right and selection
     */
    public boolean execute(int key) {
        switch (key) {
            case Utils.KEY_SELECT:
            case Utils.KEY_LEFT:
            case Utils.KEY_RIGHT:
                super.execute(key);
                break;
            case Utils.KEY_UP:
            case Utils.KEY_DOWN:
                FApp._theCanvas.execute(key);
            default:
                break;
        }
        return false;
    }

    public boolean execute(byte action) {
        //execute menu actions
        switch (action) {
             
            case ACTION_FOCUS_TO_LEFT:
                focused--;
                if (focused < 0) {
                    focused = nItems - 1;
                }
                Utils.callRepaint();
                break;
            case ACTION_FOCUS_TO_RIGHT:
                focused++;
                if (focused > nItems - 1) {
                    focused = 0;
                }
                Utils.callRepaint();
                break;
            default:
                //execute current button action @see FSuperItem.execute(key)
                execute(actions[focused], actionData[focused]);
                break;
        }

                        
        return false;//TODO change me
    }

    /**
     * Executes an action using the data needed
     * @param action
     * @param data
     */
    private void execute(byte action, String data) {
        switch (action) {
            case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                FInteractionUtils.changeToMainMenu();
                break;
            case FInteractionUtils.ACTION_GO_BACK:
                FInteractionUtils.goBack();
                break;
            case FInteractionUtils.ACTION_EXIT:
                FInteractionUtils.exit();
                break;
            case FInteractionUtils.ACTION_PLATFORM_REQUEST:
                FInteractionUtils.platformRequest(data);
                break;
            case FInteractionUtils.ACTION_JUMP_TO_SCREEN:
                FInteractionUtils.changeToXScreen(Byte.parseByte(data));
                break;
            case FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM:
                hideAllExceptFocused();
                FInteractionUtils.setFocusToPrevItem();
                break;
            case FInteractionUtils.ACTION_RETRY_BLUETOOTH:
                FInteractionUtils.retryBluetooth();
                FApp._app.changeStateTo(FApp.STATE_RELOAD_CURRENT);
                break;
            case FMap.ACTION_NEXT_POINT:
            case FMap.ACTION_PREV_POINT:
                switch (FApp._current) {
                    case FApp.STATE_SCREEN_SHOW_IMAGE:
                        //TODO
                        break;
                    case FApp.STATE_SCREEN_RUTE_PLANNER:
                        //TODO wtf?
                        ((FRutePlanner) (FApp._theCanvas.getItems().elementAt(0))).getMap().execute(action);
                        break;
                    default:
                        break;
                }
            default:
                break;
        }
    }
    private static final byte DIR_RIGHT = 1;
    private static final byte DIR_LEFT = -1;
    private static int button;

    /**
     * Paints needed items from left of focused one.
     * @param focused
     */
    private void paintTo(Graphics g, byte dir) {
        button = focused;
        for (int i = 0; i < (nItems >> 1); i++) {
            button = button + dir;
            switch (dir) {
                case DIR_LEFT:
                    if (button < 0) {
                        button = nItems - 1;
                    }
                    break;
                case DIR_RIGHT:
                    if (button > nItems - 1) {
                        button = 0;
                    }
                    break;
            }
            paint(g, button, xx + dir * ((i + 1) * offset), yy);
        }
    }

    /**
     * Called when this menu performs an action to give focus to other element
     */
    private static void hideAllExceptFocused() {
        showAll = false;
    }

    /**
     * Called from FCanvas when hover menu receive the focus
     */
    public static void showAll() {
        showAll = true;
    }
}
