/**
 * action
 */
package action;

import java.util.Vector;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link FInteraction} its a user event controler.
 * It has a Vector of actions asociated to a Vector of keys that will throw them
 * <code>
 * FSuperItem.interaction = FInteraction.newInstance();<br>
 * FSuperItem.interaction.addInteraction(-5, FInteraction.ACTION_EXIT);<br>
 * </code>
 * @author fer
 */
public class FInteraction {

    private static final byte ANYKEY_POS = 0;
    /** when focused will not perform any action */
    public static final byte FOCUS_NO_ACTION  = 0;
    /** when gets focus will perform an action */
    public static final byte FOCUS_ACTION_GET_FOCUS  = 1;
    private boolean anyKey   = false;
    public byte focusKey = FOCUS_NO_ACTION;
    private byte focusAction;
    private Vector keys;
    private Vector actions;
    private static Timer t;

    private FInteraction() {
        keys = new Vector(1, 1);
        actions = new Vector(1, 1);
    }

    /**
     *
     * @return a new instance of FInteraction removing all previous
     * keys and actions
     */
    public static final FInteraction newInstance() {
        FInteraction ret = new FInteraction();
        return ret;
    }

    public int lenght() {
        return this.actions.size();
    }

    /**
     * Creates a ANYKEY interaction.
     * ANYKEY action has a priority bigger than FCanvas
     * ANYKEY action doesn't allow to use any other action.
     * @param anyKeyAction the action to perform
     */
    public FInteraction(byte anyKeyAction) {
        keys = null;
        anyKey = true;
        actions = new Vector(1);
        addAnyKeyAction(anyKeyAction);
    }

    /**
     * Creates a FOCUS interaction.
     * FOCUS action has a priority bigger than FCanvas
     * FOCUS action allows anyother tipe of action
     * @param focusAction the action to perform
     * @param focus perform action when gets focus <code>true</code> or when lost focus <code>false</code>
     */
    public FInteraction(byte focusAction, boolean whenFocused) {
        addFocusAction(focusAction, whenFocused);
    }

    /**
     * Creates a Timeout task to run. Is compatible with key press<br>
     * If delay is less than zero action will be performed just one time.
     * If delay is greater than zero it will be performed forever after delay.
     * @param theTask task to perform
     * @param delay the delay for this task
     * @param repeat the timeout for repeated tasks
     */
    public static void addTimeoutAction(TimerTask theTask, long delay, long repeat) {
        t = new Timer();
        t.schedule(theTask, delay, repeat);
    }
    
    /**
     * Cancels the previously created task.
     * Used in convination to <code>addTimeoutAction</code>
     */
    public static void cancelTask() {
        try {
            t.cancel();
        } catch (Throwable e) {
            if (t != null) {
                t.cancel();
                t = null;
            }
        } finally {
            t = null;
        }
    }

    public void addInteraction(int key, byte action, int index) {
        keys.insertElementAt(new Integer(key), index);
        actions.insertElementAt(new Byte(action), index);
    }

    public void addInteraction(Integer key, Byte action, int index) {
        keys.insertElementAt(key, index);
        actions.insertElementAt(action, index);
    }

    public void addInteraction(int key, byte action) {
        keys.addElement(new Integer(key));
        actions.addElement(new Byte(action));
    }

    public void addInteraction(Integer key, Byte action) {
        keys.addElement(key);
        actions.addElement(action);
    }

    /**
     * Deletes all keys and actions and adds just one action
     * that will be throwed by any key
     */
    public void addAnyKeyAction(byte action) {
        anyKey = true;
        actions.removeAllElements();
        actions.setSize(1);
        actions.insertElementAt(new Byte(action), ANYKEY_POS);
    }

    /**
     *
     * @param focusAction the action to execute when receive the focus
     * @param whenFocused always true
     */
    public void addFocusAction(byte focusAction, boolean whenFocused) {
        if (whenFocused) {
            focusKey = FOCUS_ACTION_GET_FOCUS;
        }
        //else {
        //    focusKey = FOCUS_ACTION_LOST_FOCUS;
        //}
        this.focusAction = focusAction;
    }

    public byte getAnykeyAction() {
        byte ret = -1;
        if (anyKey) {
            ret = ((Byte) actions.elementAt(ANYKEY_POS)).byteValue();
        }
        return ret;
    }

    public byte getFocusAction() {
        byte ret = FInteractionUtils.ACTION_NONE;
        switch (focusKey) {
            case FOCUS_ACTION_GET_FOCUS:
                ret = focusAction;
                break;
            //case FOCUS_ACTION_LOST_FOCUS:
            //case FOCUS_NO_ACTION:
            //default:
            //    break;
        }
        return ret;
    }

    private Integer theKey;
    public byte getAction(int key) {
        byte ret = -1;
        int size = keys.size();
        theKey = new Integer(key);
        for (int i = 0; i < size; i++) {
            if (keys.elementAt(i).equals(theKey)) {
                ret = ((Byte) actions.elementAt(i)).byteValue();
            }
        }
        theKey = null;
        return ret;
    }

    public int getKey(byte action) {
        int ret = -1;
        int size = actions.size();
        Byte theAction = new Byte(action);
        for (int i = 0; i < size; i++) {
            if (actions.elementAt(i).equals(theAction)) {
                ret = ((Integer) keys.elementAt(i)).intValue();
            }
        }
        theAction = null;
        return ret;
    }

    public final boolean isAnyKey() {
        return anyKey;
    }

    public final boolean isFocusKey() {
        switch (focusKey) {
            case FOCUS_ACTION_GET_FOCUS:
                return true;
            case FOCUS_NO_ACTION:
            //case FOCUS_ACTION_LOST_FOCUS:
            default:
                return false;
        }
    }
}
