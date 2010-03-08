package action;

import app.FApp;
import items.image.FCanvas;
import items.image.maps.FMap;
import items.media.FSound;
import a.MirbluMapsMIDlet;
import java.util.Vector;

/**
 *
 * @author fernando@mirblu.com
 */
public class FInteractionUtils {

    public static final byte ACTION_NONE = -1;
    public static final byte ACTION_EXIT = 1;
    public static final byte ACTION_CHANGE_TO_MAIN_MENU = 2;
    public static final byte ACTION_LOAD_MAIN_MENU =4;
    public static final byte ACTION_GO_BACK = 3;
    ///** @deprecated  */
    //public static final byte ACTION_CHANGE_TO_NEXT_SCREEN = 4;
    public static final byte ACTION_JUMP_TO_SCREEN = 5;
    public static final byte ACTION_PLATFORM_REQUEST = 6;
    public static final byte ACTION_SAVE_CURRENT_DATA = 7;
    public static final byte ACTION_FOCUS_TO_HOVER_MENU = 8;
    public static final byte ACTION_FOCUS_TO_FIRST_ITEM = 9;
    public static final byte ACTION_RETRY_BLUETOOTH = 10;
    public static final byte ACTION_RELOAD_CURRENT = 11;
    public static final byte ACTION_SECONDARY_MENU = 14;
    public static final byte ACTION_SCROLL_LEFT = 51;
    public static final byte ACTION_SCROLL_RIGHT = 52;
    public static final byte ACTION_SCROLL_UP = 53;
    public static final byte ACTION_SCROLL_DOWN = 54;
    public static final byte ACTION_SCROLL_UP_LEFT = 55;
    public static final byte ACTION_SCROLL_UP_RIGHT = 56;
    public static final byte ACTION_SCROLL_DOWN_LEFT = 57;
    public static final byte ACTION_SCROLL_DOWN_RIGHT = 58;
    public static final byte ACTION_PLAY_SOUND = 59;

    //public static byte event = ACTION_NONE;
    public static final int TIMEOUT_DELAY = 0;
    /**
     * something like "0|Calle compañía" it stores the id "0"
     */
    public static String sourceHotspotId;
    public static String destinationHotspotId;
    public static String sourceHotspotName;
    public static String destinationHotspotName;

    /**
     * used to know what image show: for example: "/0.png"
     */
    public static String imageToShow = new String("");

    public static void platformRequest(String url) {
        try {
            FApp._theMIDlet.platformRequest(url);
        } catch (Throwable ex) {
            //Utils.debugModePrintStack(
            //        ex, "FInteractionUtils", "platformRequest(" + url + ")");
        }
    }
    private static Thread t;

    public static void playSound(FSound sound) {
        t = null;
        t = new Thread(sound);
        t.run();
    }

    /**
     *
     * @return true if <code>((FApp._current +1) < FApp._numberOFScreens)</code>
     */
    public static boolean changeToNextScreen() {
        boolean ret = false;
        if ((FApp._current + 1) < FApp._numberOFScreens) {
            FApp._app.changeStateTo((byte) (FApp._current + 1));
            ret = true;
        }
        return ret;
    }

    public static void changeToMainMenu() {
        FApp._app.changeStateTo(FApp.STATE_BACK_TO_MAIN);
    }

    public static void changeToXScreen(byte next) {
        System.out.println("next state: " + next);
        FApp._app.changeStateTo(next);
    }

    /**
     * Calls the application implementation of FApp.clean().
     * Then closes the application.
     */
    public static void exit() {
        FApp._app.closeClean();
        System.gc();
        FApp._theMIDlet.notifyDestroyed();
    }

    public static void goBack() {
        if ((FApp._last) >= FApp.STATE_MAIN_SCREEN) {
            FApp._app.changeStateTo(FApp._last);
        }
    }

    /**
     * If canvas contains a hover menu, give focus to it
     */
    public static void setFocusToHoverMenu() {
        FApp._theCanvas.setFocusToHoverMenu(true);
    }

    /**
     * If canvas contains more than one item, give focus to first one
     */
    public static void setFocusToNextItem() {
        if (FApp._theCanvas.getItems().size() > 1) {
            FApp._theCanvas.setFocusToItem(FCanvas.FOCUS_INC_MINUS);
        }
    }

    public static void setFocusToPrevItem() {
        if (FApp._theCanvas.getItems().size() - 1 > -1) {
            //System.out.println("FInteractionUtils focusToPrevItem");
            FApp._theCanvas.setFocusToItem(FCanvas.FOCUS_INC_PLUS);
        }
    }

    public static void retryBluetooth() {
        return;
    }
     /*   //FBluetooth.disable();
        ((MirbluMapsMIDlet) FApp._theMIDlet).traceMeItems = null;
        ((MirbluMapsMIDlet) FApp._theMIDlet).traceMeItems = new Vector();
        ((MirbluMapsMIDlet) FApp._theMIDlet).step = 0;
        FApp._theCanvas.removeHoverMenu();
        FApp._theMIDlet.bluetooth.start();
    }*/

    public static void nextPoint() {
        FMap.currentPoint++;
        if (FMap.currentPoint > FMap.endPoint) {
            FMap.currentPoint = FMap.endPoint;
        }
        FMap.startTimeout();
        FMap.runFocusPoint();
    }

    public static void prevPoint() {
        FMap.currentPoint--;
        if (FMap.currentPoint < FMap.firstPoint) {
            FMap.currentPoint = FMap.firstPoint;
        }
        FMap.startTimeout();
        FMap.runFocusPoint();
    }

    public static void setSecondaryMenu() {
        System.out.println("secondaryMenu: " + MirbluMapsMIDlet.secondaryMenu);
        MirbluMapsMIDlet.secondaryMenu = !MirbluMapsMIDlet.secondaryMenu;
        FApp._app.changeStateTo(FApp.STATE_RELOAD_CURRENT);
    }
    //TODO more tipical actions
}
