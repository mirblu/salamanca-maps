package app;

import java.util.Vector;

import javax.microedition.lcdui.Image;

import action.FInteractionUtils;
import items.image.FCanvas;
import midlet.FMIDlet;
import utils.Utils;

public abstract class FApp implements FAppInterface {

    public static byte _current = STATE_NONE;
    public static byte _last = STATE_NONE;
    public static byte _numberOFScreens = 0;
    public static byte _x = STATE_NONE;
    /* Vector<Vector> */
    public static Vector _items;//its a matrix
    /* Vector<String> */
    public static Vector _titles;
    public static FCanvas _theCanvas;
    public static FMIDlet _theMIDlet;
    public static Image arrowRight;
    public static Image arrowLeft;
    public static FAppInterface _app = null;

    /**
     * Private constructor loads some tipical resources
     */
    protected FApp() {
        try {
            arrowRight = Image.createImage("/arrow_r.png");
            arrowLeft = Image.createImage("/arrow_l.png");
        } catch (Throwable e) {
            //Utils.debugModePrintStack(e, "FApp", "private constructor can't create arrow images");
        }
    }

    /**
     * Creates a new instance of FApp if not created yet.
     * Needs to call to mainScreen(...) to initialize it.
     * @param midlet the midlet
     * @param canvas the canvas
     * @see FApp.mainScreen
     * @return the unique instance of FApp
     */
    public abstract FAppInterface newInstance(FMIDlet midlet, FCanvas canvas);

    /**
     * Called before close app.
     * Can be used to clean resources and/or store needed data.
     */
    public abstract void closeClean();

    /**
     * Changes the current state to the new given one
     * @param newState the state to change to
     */
    public abstract void changeStateTo(byte newState);

    /**
     * Prepares the canvas to be started at startState
     * @param startState the state to start at
     */
    public void startAt(byte startState) {
        FApp._current = startState;
        FApp._theCanvas = prepareCanvas();
    }
    /**
     * Initialize this app.
     * <br>Sets the main screen for this app and all the elements.
     * <br>Main screen has EXIT action when KEY_EXIT
     */
    public final void intro(String string, Vector introItems) {
        _items = new Vector(INITIAL_CAPACITY, CAPACITY_INCREMENT);
        _items.setSize(INITIAL_CAPACITY);//avoids ArrayOutOfBoundsExceptions
        _titles = new Vector(INITIAL_CAPACITY, CAPACITY_INCREMENT);
        _titles.setSize(INITIAL_CAPACITY);//avoids ArrayOutOfBoundsExceptions
        _current = STATE_SPLASH_SCREEN;
        addScreen(string, introItems, STATE_SPLASH_SCREEN);
    }
    /**
     * Initialize this app.
     * <br>Sets the main screen for this app and all the elements.
     * <br>Main screen has EXIT action when KEY_EXIT
     */
    public final void mainScreen(String string, Vector mainScreenItems) {
        _items = new Vector(INITIAL_CAPACITY, CAPACITY_INCREMENT);
        _items.setSize(INITIAL_CAPACITY);//avoids ArrayOutOfBoundsExceptions
        _titles = new Vector(INITIAL_CAPACITY, CAPACITY_INCREMENT);
        _titles.setSize(INITIAL_CAPACITY);//avoids ArrayOutOfBoundsExceptions
        _current = STATE_MAIN_SCREEN;
        addScreen(string, mainScreenItems, STATE_MAIN_SCREEN);
    }

    /**
     * Adds a new screen at position only if not previously inserted.
     * @param title the canvas title, must be not null and not ""
     * @param items the matrix of FSuperItems that the screen has,
     * must be not null and not be empty
     * @param statePosition the constant that will reffer this screen.
     * @see removeScreen
     */
    public void addScreen(String title, Vector elements, byte statePosition)
    {
        if (_items.indexOf(elements, statePosition) == -1) {
            _numberOFScreens++;
            if (Utils.isNotNull(title) && !title.equals("")) {
                _titles.insertElementAt(title, statePosition);
            }
            if (Utils.isNotNull(elements) && elements.size() != 0) {
                _items.insertElementAt(elements, statePosition);
            }
        }
    }

    /**
     * Deletes a screen from this app.
     * Note that its better not to have uncontinuos screens, use it to
     * free a position and use it again.
     * @param statePosition the position that references the screen
     * @see addScreen
     */
    public void removeScreen() {
        //some operations for each state if needed
        switch (_current) {
            case STATE_RELOAD_CURRENT:
            case STATE_MAIN_SCREEN:
            case STATE_LOADING:
                /*
                 * these states will not be removed until program exit
                 */
                return;
            case STATE_SCREEN_SHOW_IMAGE:
                FInteractionUtils.imageToShow = "";
            default:
                try {
                    //remove current screen elements
                    _numberOFScreens--;
                    _titles.removeElementAt(_current);
                    _items.removeElementAt(_current);
                } catch (Throwable e) {
                    //Utils.debugModePrintStack(e, "FApp", "removeScreen(" + _current + "). Try incrementing INITIAL_CAPACITY");
                }
                break;
        }
    }

    public Vector getItems() {
        Vector ret = null;
        if (_items.size() > _current) {
            ret = (Vector) _items.elementAt(_current);
        }
        return ret;
    }

    public String getTitle() {
        String ret = null;
        if (_titles.size() > _current) {
            ret = (String) _titles.elementAt(_current);
        }
        return ret;
    }

    /**
     * Prepare the FCanvas for the current state.
     * Loads title and all elements to use with this screen <br>
     * Set fullScreenMode to true
     */
    public FCanvas prepareCanvas() {

        if (_theCanvas.hasHoverMenu()) {
            _theCanvas = new FCanvas(getItems(), getTitle(), true, FCanvas.hoverMenuAt);
        } else {
            _theCanvas = new FCanvas(getItems(), getTitle(), true, Utils.FOCUS_FIRST_ELEMENT);
        }
        return _theCanvas;
    }

}
