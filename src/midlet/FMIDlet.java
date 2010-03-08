package midlet;

//import com.mirblu.j2me.fui.comunication.FBluetooth;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import items.image.m3g.Cube;
import utils.Utils;

public abstract class FMIDlet extends MIDlet implements FMIDletInterface {

    public static Font mainTextFont = Font.getFont(
            Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    public static int mainTextColor = Utils.COLOR_TANGO_SKYBLUE1;
    public static int mainBackgroundColor = Utils.COLOR_WHITE;//focus color
    public static int cursorColor = Utils.COLOR_TANGO_CHAMELEON1;
    public static boolean _exploring = false;
    public TimerTask aTask;
    public static Cube cube;
    public static Image home;
    public static Image categories;
    public static Image createImg;
    public static Image bluetoothImg;
    public static Image exploreImg;
    public static Image aboutImg;
    public static Image exitImg;
    public static Image checkBoxYesImg;
    public static Image checkBoxNoImg;
    public static Image advertisementImg;
    public static Image map;
    public static Image orDot;
    public static Image destDot;
    public static Image floorImageImg;
    public static Image backgroundImg;//menu background
    public static Image backgroundAbout;//menu background
    public static Vector hotspotsList;
    public static Vector hotspotsFiltered;
//    public static FBluetooth bluetooth;
    public static StringBuffer bluetoothInfo;

    public FMIDlet() {
        super();
    }

    protected abstract void startApp() throws MIDletStateChangeException;

    protected abstract void pauseApp();

    protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    public abstract Vector getMainMenu();

    public abstract Vector getSourceList();

    public abstract Vector getSourceList(boolean DestinyAllReadySelectedParam);

    public abstract Vector getDestinyList();

    public abstract Vector getAbout();

    public abstract Vector getSelectMapScreen();

    public abstract Vector getShowImage();

    public abstract Vector getShowImage(String pathPic);

    public abstract Vector getExploreMap();

    public abstract Vector getRutePlanner();

    public abstract Vector getTraceMe();

    public abstract Vector get3DSplashScreen();
}
