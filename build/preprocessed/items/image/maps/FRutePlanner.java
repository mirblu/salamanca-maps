package items.image.maps;

import java.io.InputStream;
import utils.Utils;
import action.FInteractionUtils;
import app.FApp;
//import com.mirblu.j2me.fui.comunication.FBluetooth;
import items.FSuperItem;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Reads and controls a rute.
 * @author fernando
 */
public class FRutePlanner extends FSuperItem {

    protected static final String STRING_ELEVATOR_DOWN_INFO = "Ascensor, baje en la planta ";
    protected static final String STRING_ELEVATOR_UP_INFO = "Ascensor, suba hasta la planta ";
    protected static final String STRING_STAIRS_DOWN_INFO = "Escaleras, baje en la planta ";
    protected static final String STRING_STAIRS_UP_INFO = "Escaleras, suba hasta la planta ";
    protected static final String STRING_ZONE_UNION_INFO = "Unión entre zonas";
    protected static final String STRING_ELEVATOR_INFO = "Ascensor en planta ";
    protected static final String STRING_STAIRS_INFO = "Escaleras en planta ";

    public static String RUTES_FILENAME = "/rutasFI2.txt";
    public static final char CHAR_SPACE = ' ';
    public static final String STRING_RUTE_SEPARATOR = "|";
    public static final char CHAR_RUTE_SEPARATOR = '|';
    public static final char CHAR_RUTE_COORD_SEPARATOR = 'x';
    public static final String STRING_RUTE_COORD_SEPARATOR = "x";
    public static final String CHAR_END_OF_LINE = "@";
    public static final String SATELLITE_START_STRING = "$";
    public static final String STRING_ELEVATOR = "A";
    public static final String STRING_STAIRS = "E";
    public static final String STRING_ZONE = "e";
    protected static StringBuffer auxsb;
    protected static String rute;
    /** has a bound of values and constants that defines some points, floor intersections and other info about balizas <br>
     * sourceid destinationid x1 y1 baliza floor x2 y2 floor x3 y3 floor x4 y4 elevator floor 0 0 floor x5 y5<br>
     * 19 3 4 164 105 $0017E3D7B05A 4 212 128 4 346 134 A 1 0 0 1 132 38 1 161 50 1 139 76 1 61 43 1 35 29 @
     */
    protected static String[] ruteData;
    /** has a element for each rute to perform, one rute is one floor rute, @see FMap
     */
    public static String[] rutes;
    public static int currentRute;
    /*Vector<String> contains the bluetooth addresses of satellites readed from hotspot*/
    public static Vector satellites = new Vector(1, 1);
    /*Vector<Integer> contains the hotspots ID for each satellite if any id*/
    public static Vector satellitesIds = new Vector(1, 1);
    protected static boolean bluetoothMode = true;

    public final static void setBluetoothMode(boolean state) {
        FRutePlanner.bluetoothMode = state;
    }

    /**
     *
     * @return true if bluetooth mode is on
     */
    public final static boolean getBluetoothMode() {
        return bluetoothMode;
    }

    /**
     * Checks if current device is a satellite (one of the FBluetooth constructor BTA values).
     * @return
     */
    public static final boolean iAmASatellite() {
        return false;
    }
    /*    boolean ret = false;
        int size = satellites.size();
        if (getBluetoothMode() && Utils.isNotNull(satellites) && size > 0 && Utils.isNotNull(FBluetooth.localDevice)) {
            int i = 0;
            String localBTA = FBluetooth.localDevice.getBluetoothAddress();
            for (; i < size; i++) {
                if (localBTA.equals(satellites.elementAt(i))) {
                    return true;
                }
            }
        }
        return ret;
    }*/

    /**
     * Checks if ruteData contains the satellite bluetooth address
     * @param satelliteBTA the bluetooth address
     * @return true if ruteData has this bluetooth address
     * TODO this method doesn't check if we are at different floor!!!
     * FIXME
     */
    public static boolean isSatelliteInRute(String satelliteBTA) {
        if (Utils.isNotNull(ruteData)) {
            int i = ruteData.length - 1;
            for (; i > 0; i--) {
                if (((String) ruteData[i]).indexOf(satelliteBTA) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * if satellite is in hotspot file, get the id associated
     * @param satelliteBTA the Bluetooth Address of satellite we try to found
     * @return the hotspot id for satellite given; -1 if not found
     */
    public static final int getSatelliteHotspotId(String satelliteBTA) {
        int ret = -1;
        int satPos = satellites.indexOf(satelliteBTA);
        if (satPos != -1) {
            ret = ((Integer) satellitesIds.elementAt(satPos)).intValue();
        }
        return ret;
    }

    /**
     * if satellite is in hotspot file, get another hotspot name
     * to know where we are. If can't found one returns default String
     * @param satId the hotspot id to search
     * @return the hotspot name for this satellite
     */
    public static final String getSatelliteHotspotName(int satId, String bta) {
        StringBuffer temp = new StringBuffer().append(satId).append(
                Utils.HOTSPOTS_FILE_DIVIDE_CHAR).append(SATELLITE_START_STRING).append(bta);
        //System.out.println(temp.toString());
        int pos = FApp._theMIDlet.hotspotsList.indexOf((String) temp.toString());
        //System.out.println("hotspotsList.indexOf = " + pos);
        if (pos > 0) {
            //found satellite at hotspot, get the previously hotspot name
            temp = new StringBuffer(Utils.split(
                    (String) FApp._theMIDlet.hotspotsList.elementAt(pos - 1),
                    Utils.HOTSPOTS_FILE_DIVIDE_CHAR,
                    Utils.HOTSPOTS_FILE_ARRAY_SIZE)[Utils.HOTSPOTS_FILE_TEXT_POS]);
        } else {
            //not found a hotspot with same satellite id
            temp = new StringBuffer("Usted esta aqui");
        }
        return temp.toString();
    }
    
    private static Integer satelliteId;
    /**
     * Adds a satellite if not added before
     * @param bluetoothAddress the BTA for this satellite
     * @param id the satellite hotspot id
     */
    public static final void addSatellite(String bluetoothAddress, int id) {
        if (!satellites.contains(bluetoothAddress)) {
            satellites.addElement(bluetoothAddress);
            satelliteId = new Integer(id);
            satellitesIds.addElement(satelliteId);
            //Utils.debugMode("Added a satellite: " + bluetoothAddress + ", hotspot id = " + id,
            //        "FRutePlanner", "addSatellite", Utils.DEBUG_INFO);
        }
        satelliteId = null;
    }

    /**
     * Creates a rute planner
     * @param rutesFilename the file with the rute data
     * @param sourceId the hotspot id when we start the rute
     * @param destinationId the hotspot id end of rute
     */
    public FRutePlanner(String rutesFilename, String sourceId, String destinationId) {
        super(rutesFilename);
        init(sourceId, destinationId);
    }

    /**
     * Read the routes file looking for the sourceId and destinationId
     * @return a string like: floor coordX coordY, floor ...
     */
    public final void readRuteFrom(String filename) {
        InputStream is = null;
        StringBuffer sb = null;
        String match = null;
        try {
            sb = new StringBuffer();
            is = this.getClass().getResourceAsStream(filename);
            int c;
            int counter = 0;
            int size = auxsb.length();
            match = auxsb.toString();
            while ((c = is.read()) != -1) {
                //while reading a line
                if ((char) c != '\n') {
                    sb.append((char) c);
                    counter++;
                    if (counter == size && sb.toString().equals(match)) {
                        //founded the soruceid and destinationid we want
                        while (((c = is.read()) != -1 && (char) c != '\n')) {
                            sb.append((char) c);
                        }
                        rute = sb.toString();
                        return;
                    }
                } else {
                    counter = 0;
                    sb = new StringBuffer();
                }
            }
        } catch (Throwable ex) {
            //Utils.debugModePrintStack(ex, "FRutePlanner", "readRuteFrom(" + filename + ")");
        } finally {
            try {
                match = null;
                sb = null;
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                //Utils.debugModePrintStack(e, "FRutePlanner", "readRuteFrom(" + filename + ")");
            } finally {
                is = null;
            }
        }
    }
    public static final byte RUTE_SOURCE_NAME = 0;
    public static final byte RUTE_DESTINATION_NAME = 1;
    public static final byte RUTE_FLOOR = 2;
    public static final byte RUTE_START_X = 3;
    public static final byte RUTE_START_Y = 4;
    private static final byte FLOOR_31 = 2;
    private static final byte FLOOR_32 = 3;

    /**
     * Process the data readed for current full rute and divide it by floors.<br>
     * For each floor result will only have a:<br>
     * <code>sourceName destinationName floor x1xy1 x2xy2 x3xy3 ...</code><br>
     * extracted from something like<br>
     * <code>sourceid destinationid floor x1 y1 baliza floor x2 y2 floor x3 y3 floor x4 y4 elevator floor 0 0 floor x5 y5<br>
     * 19 3 4 164 105 $0017E3D7B05A 4 212 128 4 346 134 A 1 0 0 1 132 38 1 161 50 1 139 76 1 61 43 1 35 29 @</code> <br>
     * @return a String[] with one String for each floor rute
     */
    public static String[] processRuteData() {
        Vector retV = new Vector();
        StringBuffer sb = new StringBuffer();
        String string;
        String temp = new String();
        sb.append(FInteractionUtils.sourceHotspotName);
        sb.append(STRING_RUTE_SEPARATOR);
        sb.append(FInteractionUtils.destinationHotspotName);
        sb.append(STRING_RUTE_SEPARATOR);
        int floor = Integer.parseInt(ruteData[RUTE_FLOOR]);
        //parse the 31 & 32 to map number
        sb.append(fixFloor(floor));
        sb.append(STRING_RUTE_SEPARATOR);
        sb.append(ruteData[RUTE_START_X]);
        sb.append(CHAR_RUTE_COORD_SEPARATOR);
        sb.append(ruteData[RUTE_START_Y]);
        sb.append(STRING_RUTE_SEPARATOR);
        int i = RUTE_START_Y + 1;
        for (; i < ruteData.length; i++) {
            string = ruteData[i];
            //process zone, stairs and elevator names
            if ((string.equals(STRING_ZONE)) || (string.equals(STRING_STAIRS)) || (string.equals(STRING_ELEVATOR))) {
                i++;
                floor = Integer.parseInt(ruteData[i]);
                if (string.equals(STRING_ELEVATOR)) {
                    
                    if (Integer.parseInt(ruteData[RUTE_FLOOR]) > Integer.parseInt(fixFloor(floor))) {
                        temp = STRING_ELEVATOR_DOWN_INFO + fixFloor(floor);
                    } else if (Integer.parseInt(ruteData[RUTE_FLOOR]) < Integer.parseInt(fixFloor(floor))) {
                        temp = STRING_ELEVATOR_UP_INFO + fixFloor(floor);
                    }
                } else if (string.equals(STRING_STAIRS)) {
                    if (Integer.parseInt(ruteData[RUTE_FLOOR]) > Integer.parseInt(fixFloor(floor))) {
                        temp = STRING_STAIRS_DOWN_INFO + fixFloor(floor);
                    } else if (Integer.parseInt(ruteData[RUTE_FLOOR]) < Integer.parseInt(fixFloor(floor))) {
                        temp = STRING_STAIRS_UP_INFO + fixFloor(floor);
                    }
                } else if (string.equals(STRING_ZONE)) {
                    temp = STRING_ZONE_UNION_INFO;
                }
                //TODO solve this from the rute text file
                //skip all 0x0 values after found an 'A' 'E' 'e'
                i++;
                i++;
                //get the map floor and insert to last rute position
                //XxYxACTIONxFLOORreplac
                appendAction(sb, FMap.ACTION_LOAD_MAP);
                sb.append(STRING_RUTE_COORD_SEPARATOR);
                sb.append(fixFloor(floor));
                //insert elevator note
                sb = Utils.replaceText(FInteractionUtils.destinationHotspotName,
                        temp, sb.toString());
                retV.addElement(sb.toString());
                sb = new StringBuffer();
                //insert the names for the new rute
                sb.append(temp);//the next source is the current destination
                sb.append(STRING_RUTE_SEPARATOR);
                sb.append(FInteractionUtils.destinationHotspotName);
                sb.append(STRING_RUTE_SEPARATOR);
                //parse the 31 & 32 to real map number
                sb.append(fixFloor(floor));
                sb.append(STRING_RUTE_SEPARATOR);

                //change source name
                if (sb.toString().startsWith(STRING_ELEVATOR_DOWN_INFO)) {
                    sb = Utils.replaceText(STRING_ELEVATOR_DOWN_INFO, STRING_ELEVATOR_INFO, sb.toString());
                }
                if (sb.toString().startsWith(STRING_ELEVATOR_UP_INFO)) {
                    sb = Utils.replaceText(STRING_ELEVATOR_UP_INFO, STRING_ELEVATOR_INFO, sb.toString());
                }
                if (sb.toString().startsWith(STRING_STAIRS_DOWN_INFO)) {
                    sb = Utils.replaceText(STRING_STAIRS_DOWN_INFO, STRING_STAIRS_INFO, sb.toString());
                }
                if (sb.toString().startsWith(STRING_STAIRS_UP_INFO)) {
                    sb = Utils.replaceText(STRING_STAIRS_UP_INFO, STRING_STAIRS_INFO, sb.toString());
                }

                //} else if (string.startsWith(STRING_START_BALIZA_ID)) {
                //TODO process a baliza??
            } else if (string.startsWith(CHAR_END_OF_LINE)) {
                //add an action to last point
                //appendAction(sb, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
                appendAction(sb, FInteractionUtils.ACTION_NONE);
                //add satellites if any
                retV.addElement(sb.toString());
                sb = null;
                break;
            } else {
                //data must be a floor
                if (String.valueOf(floor).equals(string)) {
                    //insert a coordenate
                    i++;
                    sb.append(ruteData[i]);
                    sb.append(CHAR_RUTE_COORD_SEPARATOR);
                    i++;
                    sb.append(ruteData[i]);
                    sb.append(STRING_RUTE_SEPARATOR);
                }
            }
        }
        String[] retA = new String[retV.size()];
        retV.copyInto(retA);
        retV = null;
        temp = null;
        string = null;
        return retA;
    }

    /**
     * Action just can be added to the last point
     * @param sb
     * @param action
     */
    private static void appendAction(StringBuffer sb, byte action) {
        sb.deleteCharAt(sb.length() - 1);
        sb.insert(sb.length(), CHAR_RUTE_COORD_SEPARATOR);
        sb.append(action);
    }

    /**
     * Paints the current map and point
     * @param g
     */
    public void paint(Graphics g) {
        ((FSuperItem) this.content).paint(g);
    }

    public boolean execute(byte action) {
        return ((FMap) this.content).execute(action);
    }

    public boolean execute(int key) {
        return ((FMap) this.content).execute(key);
    }

    /**
     * Loads a new map for current rute.
     * @param currentRute the current rute, one for each floor
     */
    protected void loadMap(int currentRute) {
        System.gc();
        this.content = new FMap(rutes[currentRute]);
        this.interaction = ((FMap) this.content).interaction;
    }

    /**
     * Changes a integer floor to String floor
     * @param floor can be 31 or 32 or 0..6
     * @return 2 or 3, or floor
     */
    protected static String fixFloor(int floor) {
        if (floor == 31) {
            //floor = 3;
            return String.valueOf(FLOOR_31);
        } else if (floor == 32) {
            //floor = 4;
            return String.valueOf(FLOOR_32);
        }
        return String.valueOf(floor);
    }

    /**
     * Process all rute data for given hotspots ids
     * @param sourceId the start hotspot
     * @param destinationId the end hotspot
     */
    private final void init(String sourceId, String destinationId) {
        currentRute = 0;
        auxsb = new StringBuffer();
        auxsb.append(sourceId).append(CHAR_SPACE).append(destinationId).append(CHAR_SPACE);
        readRuteFrom(this.description);
        System.gc();
        ruteData = Utils.split(rute, CHAR_SPACE, Utils.getOccurrences(rute, " ").size() + 1);
        rutes = processRuteData();
        //change the source name when is a elevator or stairs

        if (sourceId.equals(destinationId)) {
            //source = destination; load last rute
            currentRute = rutes.length - 1;
            loadMap(currentRute);
        } else {
            //show the first rute and wait for the end of it to show the next rute
            loadMap(currentRute);
            //forces current point to be the last one
            //FMap.currentPoint = ruteData.length - 3;
        }
    }

	public FMap getMap() {
		return (FMap)content;
	}
}