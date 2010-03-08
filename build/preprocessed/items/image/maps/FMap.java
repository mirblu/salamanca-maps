package items.image.maps;

import action.FInteractionUtils;
import items.image.FCanvas;
import items.image.FImageGrid;
import java.io.IOException;
import utils.Utils;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import midlet.FMIDlet;

/**
 *
 * @author fernando
 */
public class FMap extends FImageGrid {

    /** Some useful constants */
    public static final byte ACTION_NEXT_POINT = 77;
    public static final byte ACTION_PREV_POINT = 78;
    public static final byte ACTION_LOAD_MAP = 79;
    private static final byte RUTE_SOURCE = 0;
    private static final byte RUTE_DESTINATION = 1;
    private static final byte RUTE_FLOOR = 2;
    private static final byte RUTE_FIRST_POINT_XY = 3;
    private static final char CHAR_SPACE = ' ';
    public static final String PATH_TO_MAP_INDEXES = "/maps/index";
    //private static String route = null;
    private static StringBuffer auxsb;
    public static FImageGrid fi;
    private static String[] ruteData;
    private static int mapPos;
    public static int endPoint = 1;
    public static int firstPoint = 1;
    public static int currentPoint = 1;
    private static int[] point, prevPoint;
    /*contains the current action to perform*/
    public static byte currentPointAction = FInteractionUtils.ACTION_NONE;
    private static int nextFloor = -1;
    private static TimerTask tt;
    private static Timer t;
    //protected boolean timeout = false;
    protected static long timeoutValue = 0;
    protected long timeoutSched = 50;
    protected long timeoutMax = 150;
    //private static byte pointType = POINT_TYPE_STANDARD;

    /**
     * process the route data on a map
     * @param ruteData the String[] with one floor pair of coordinates to paint the route<br>
     * <code>source destination x1 y1 x2 y2 x3 y3</code><br>
     * <code>sourceName destinationName 571x386 629x357 677x319</code><br>
     */
    public FMap(String rute) {
        super(rute);
        init(rute);
    }

    private void init(String rute) {
        ruteData = Utils.split(rute, FRutePlanner.CHAR_RUTE_SEPARATOR,
                Utils.getOccurrences(rute, FRutePlanner.STRING_RUTE_SEPARATOR).size() + 1);
        System.gc();
        endPoint = ruteData.length - 3;
        currentPoint = 1;
        nextFloor = -1;

        //used to animate the map
        diff[0] = 0;
        diff[0] = 0;

        mapPos = Integer.parseInt(ruteData[RUTE_FLOOR]);//map
        //first point
        point = getPoint(ruteData[RUTE_FIRST_POINT_XY]);
        auxsb = new StringBuffer();
        //TODO here is not the best place; change some strings
        //replace source name
        /*if (FInteractionUtils.sourceHotspotName.startsWith(temp)) {
            ruteData[RUTE_SOURCE] = "Escaleras en planta " + FRutePlanner.fixFloor(Integer.valueOf(ruteData[RUTE_FLOOR]).intValue());
        }*/
        auxsb.append(ruteData[RUTE_SOURCE]).append(CHAR_SPACE).append(ruteData[RUTE_DESTINATION]).append(CHAR_SPACE);
        fi = new FImageGrid(auxsb.toString(), PATH_TO_MAP_INDEXES, mapPos,
                128, 128, point[0], point[1],
                FImageGrid.IMAGEGRID_START_CUSTOM, 20);
        prevPoint = point;
        runFocusPoint();//start to the first point
        setFocus(true);
        interaction = fi.interaction;//get scroll from FImageGrid
        //        this.interaction.addInteraction(Utils.KEY_SELECT, ACTION_NEXT_POINT);
        //        this.interaction.addInteraction(Utils.KEY_CLEAR, ACTION_PREV_POINT);
        interaction.addInteraction(Utils.KEY_SELECT, FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU);
        interaction.addInteraction(Utils.KEY_CLEAR, FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM);
    }

    public void paint(Graphics g) {
        fi.paint(g);//paint the needed tiles
        if (ruteData[RUTE_SOURCE].equals(ruteData[RUTE_DESTINATION])) {
            //force route end if equal source - destination
            currentPoint = endPoint - 1;
            //int[] p = getPoint(currentPoint);
            //(p[0], p[1]);
        }
        paintRoute(g, ruteData, 0, currentPoint, Utils.COLOR_TANGO_SCARLETRED1); //paints until current point
        //Pintar el nombre de los hotspots impide ver la ruta.
        //paintHotspotName(g, ruteData[RUTE_SOURCE], 1);
        //paintHotspotName(g, "A.", 1);
        point = getPoint(1);
        int x = point[0] + currMatrixX + transformCoordenate(mapPos, X_COORD);
        int y = point[1] + currMatrixY + transformCoordenate(mapPos, Y_COORD);
        
        g.drawImage(FMIDlet.orDot, x, y, Graphics.TOP | Graphics.LEFT);
        if (currentPoint==1) paintSign(g, 1);

        if (currentPoint == endPoint || ruteData[RUTE_SOURCE].equals(ruteData[RUTE_DESTINATION])) {
            //se comenta por que no permite ver la ruta al usuario
        //paintHotspotName(g, ruteData[RUTE_DESTINATION], endPoint);
        //paintHotspotName(g, "B.", endPoint);
        point = getPoint(endPoint);
        x = point[0] + currMatrixX + transformCoordenate(mapPos, X_COORD);
        y = point[1] + currMatrixY + transformCoordenate(mapPos, Y_COORD);        
        g.drawImage(FMIDlet.destDot, x, y, Graphics.TOP | Graphics.LEFT);
        paintSign(g, endPoint);
        }
    }

    public boolean execute(byte action) {
        //animating doesn't allow to perform user actions
        if (diff[0] != 0 || diff[1] != 0) {
            return false;
        } else {
            getPoint(currentPoint);
            if (currentPointAction == FInteractionUtils.ACTION_NONE) {
                switch (action) {
                    case ACTION_NEXT_POINT:
                        currentPoint++;
                        if (currentPoint > endPoint) {
                            currentPoint = endPoint;
                        }
                        startTimeout();
                        runFocusPoint();
                        break;
                    case ACTION_PREV_POINT:
                        currentPoint--;
                        if (currentPoint < firstPoint) {
                            currentPoint = firstPoint;
                        }
                        startTimeout();
                        runFocusPoint();
                        break;
                    case FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU:
                        FInteractionUtils.setFocusToHoverMenu();
                        break;
                    case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                        FMap.cancelTask();
                        FInteractionUtils.changeToMainMenu();
                        break;
                    default:
                        FMap.cancelTask();
                        //add the scroll
                        fi.execute(action);
                        break;
                }
            } else {
                FMap.cancelTask();
                //execute the point action
                switch (currentPointAction) {
                    case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                        FInteractionUtils.changeToMainMenu();                        
                        break;
                    case ACTION_LOAD_MAP:
                        FRutePlanner.currentRute++;
                        if (FRutePlanner.currentRute >= FRutePlanner.rutes.length) {
                            return this.execute(FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
                        }
                        init(FRutePlanner.rutes[FRutePlanner.currentRute]);
                        break;
                }
            }
        }
        return false;
    }
    public static int[] diff = new int[]{0, 0};

    /**
     * executes an action for current point.
     * can be loading a new map or anyelse
     */
    public static void runFocusPoint() {
        //center map to current point
        point = getPoint(currentPoint);

        if (prevPoint[0] != point[0]) {
            if (prevPoint[0] > point[0]) {
                diff[0] = prevPoint[0] - point[0];
                prevPoint[0] -= FImageGrid.scrollIncrement;
            } else {
                diff[0] = point[0] - prevPoint[0];
                prevPoint[0] += FImageGrid.scrollIncrement;
            }
        }
        if (prevPoint[1] != point[1]) {
            diff[1] = prevPoint[1] - point[1];
            if (prevPoint[1] > point[1]) {
                diff[1] = prevPoint[1] - point[1];
                prevPoint[1] -= FImageGrid.scrollIncrement;
            } else {
                diff[1] = point[1] - prevPoint[1];
                prevPoint[1] += FImageGrid.scrollIncrement;
            }
        }
        if (diff[0] > 0 || diff[1] > 0) {
            diff[0] -= FImageGrid.scrollIncrement;
            diff[1] -= FImageGrid.scrollIncrement;
            if (diff[0] <= FImageGrid.scrollIncrement) {
                diff[0] = 0;
                prevPoint[0] = point[0];
            }
            if (diff[1] <= FImageGrid.scrollIncrement) {
                diff[1] = 0;
                prevPoint[1] = point[1];
            }
            //System.out.print("prev[" + prevPoint[0] + "," + prevPoint[1] + "] | ");
            //System.out.print("next[" + point[0] + "," + point[1] + "] | ");
            //System.out.println("diff[" + diff[0] + "," + diff[1] + "]");
            centerMapTo(prevPoint[0] + transformCoordenate(mapPos, X_COORD),
                    prevPoint[1] + transformCoordenate(mapPos, Y_COORD));
        } else {
            diff = new int[]{0, 0};
            if (currentPoint - 1 > 0) {
                prevPoint = getPoint(currentPoint - 1);
            } else {
                prevPoint = point;
            }
            centerMapTo(point[0] + transformCoordenate(mapPos, X_COORD),
                    point[1] + transformCoordenate(mapPos, Y_COORD));
        }
    }

    /**
     * Transform a "123x456x57x1" into a int[] {123, 456, 57, 1}
     * @param coords
     * @return
     */
    public static int[] getPoint(String coords) {
        int[] ret = new int[2];
        String[] temp = Utils.split(coords, FRutePlanner.CHAR_RUTE_COORD_SEPARATOR, 4);
        if (Utils.isNotNull(temp) && Utils.isNotNull(temp[0]) && Utils.isNotNull(temp[1])) {
            ret[0] = Integer.parseInt(temp[0]);
            ret[1] = Integer.parseInt(temp[1]);
            if (temp.length >= 3 && Utils.isNotNull(temp[2])) {
                currentPointAction = Byte.parseByte(temp[2]);
                if (currentPointAction == ACTION_LOAD_MAP && temp.length == 4 && Utils.isNotNull(temp[3])) {
                    nextFloor = Integer.parseInt(temp[3]);
                }

            } else {
                currentPointAction = FInteractionUtils.ACTION_NONE;
            }
        }
        temp = null;
        return ret;
    }

    /**
     * Gets the x,y values for a point, applying transformation for each floor
     * @param point the point to search, starting at 1
     * @return a int[2] array, pos 0 is x and 1 is y
     */
    private static final int[] getPoint(int point) {
        int[] ret = new int[2];
        if (point >= firstPoint && point <= endPoint) {
            ret = getPoint(ruteData[point + 2]);//ruteData[point + 2]);
        }
        return ret;
    }

    /**
     * Paints a information sign centered on the screen
     * @param g
     */
    private void paintSign(Graphics g,  int pointParam ) {
        point = getPoint(pointParam);
        int[] maxWH = new int[2];
        int x = (FCanvas.canvasWidth - maxWH[0]) >> 1;
        int y = (FCanvas.canvasHeight - maxWH[1]) >> 1;
        if (ruteData[RUTE_SOURCE].equals(ruteData[RUTE_DESTINATION])) {
            auxsb = new StringBuffer("Encontrada baliza de '");
            auxsb.append(FInteractionUtils.destinationHotspotName);
            auxsb.append("'. Ha llegado a su destino");
            //auxsb.append(FInteractionUtils.sourceHotspotName);
        } else {
            if (pointParam==endPoint){
                auxsb = new StringBuffer("Ha llegado a '");
                auxsb.append(ruteData[RUTE_DESTINATION]);
                auxsb.append("' desde '");
                //TODO i don't like FMap to FRutePlanner communication
                if (FRutePlanner.rutes.length - 1 == FRutePlanner.currentRute) {
                    auxsb.append(FInteractionUtils.sourceHotspotName);
                } else {
                    auxsb.append(ruteData[RUTE_SOURCE]);
                }
                auxsb.append("'");
            }
            if (pointParam ==1){
                auxsb = new StringBuffer("Salimos desde: '");
                auxsb.append(ruteData[RUTE_SOURCE]);
            }
        }
        maxWH = Utils.getWrappedTextWidthHeigh(auxsb.toString(), FCanvas.bigFont,
                0, FCanvas.canvasWidth, false, null, y);
        x = (FCanvas.canvasWidth - maxWH[0]) >> 1;
        //y = (FCanvas.canvasHeight - maxWH[1]) >> 1;
        y = 5;
        g.setColor(Utils.COLOR_TANGO_BUTTER1);
        g.fillRoundRect(x - 3, y - 3, maxWH[0] + 6, maxWH[1] + 6, 6, 6);
        g.setColor(Utils.COLOR_TANGO_SKYBLUE1);
        g.fillRoundRect(x, y, maxWH[0], maxWH[1], 6, 6);
        g.setColor(Utils.COLOR_TANGO_ALUMINIUM1);
        Utils.getWrappedTextWidthHeigh(auxsb.toString(), FCanvas.bigFont,
                x, FCanvas.canvasWidth, true, g, y);
    }

    /**
     * Paints the hotspot name
     * @param g where to paint
     * @param name the text to paint
     * @param attPoint the point that has attached text, related to full image size
     */
    private void paintHotspotName(Graphics g, String name, int attPoint) {
        point = getPoint(attPoint);
        int[] maxWH = new int[2];
        int x = point[0] + currMatrixX + transformCoordenate(mapPos, X_COORD);
        int y = point[1] + currMatrixY + transformCoordenate(mapPos, Y_COORD);
        maxWH = Utils.getWrappedTextWidthHeigh(name, FCanvas.bigFont,
                x, FCanvas.canvasWidth, false, null, y);
        //draw a point attached to name
        g.setColor(Utils.COLOR_TANGO_BUTTER1);
        g.fillRoundRect(x - 6, y - 6, 12, 12, 6, 6);
        g.setColor(Utils.COLOR_TANGO_SCARLETRED1);
        g.fillRoundRect(x - 3, y - 3, 6, 6, 3, 3);
        //check canvas limits and recalculate if needed
        if (FCanvas.canvasWidth < x + maxWH[0]) {
            x = x - maxWH[0];
            maxWH = Utils.getWrappedTextWidthHeigh(name, FCanvas.bigFont,
                x, FCanvas.canvasWidth, false, null, y);
        }
        if (FCanvas.canvasHeight < y + maxWH[1]) {
            y = y - maxWH[1];
            maxWH = Utils.getWrappedTextWidthHeigh(name, FCanvas.bigFont,
                x, FCanvas.canvasWidth, false, null, y);
        }
        g.setColor(Utils.COLOR_TANGO_SKYBLUE1);
        g.fillRoundRect(x - 3, y - 3, maxWH[0] + 3, maxWH[1] + 3, 3, 3);
        //Utils.fillRoundRect(g, x - 3, y - 3, maxWH[0] + 3, maxWH[1] + 3, Utils.COLOR_TANGO_SKYBLUE1, 0xAA);
        g.setColor(Utils.COLOR_TANGO_ALUMINIUM1);
        Utils.getWrappedTextWidthHeigh(name, FCanvas.bigFont,
                x, FCanvas.canvasWidth, true, g, y);
    }

    /**
     * Paints a bound of lines corresponding the rute of current map
     * @param g
     * @param ruteData the data of each line to paint. Pattern:<br>
     * <code>sourceName destinationName floor x1 y1 x2 y2 x3 y3</code> ...
     * @param map the map number
     * @param begin the starting point to paint
     * @param end the end point to paint
     * @param color the line color
     */
    public void paintRoute(Graphics g, String[] ruteData, int begin, int end, int color) {
        //System.out.println("painting rute");
        end += RUTE_FLOOR + 1;
        begin += RUTE_FLOOR + 1;
        if (begin > end || end > ruteData.length || begin < 0 || g == null || ruteData == null) {
            return;
        }
        g.setColor(color);
        //paint the rute
        int x = 0, y = 0, lastx = 0, lasty = 0, i = begin;
        int floor = Integer.parseInt(ruteData[RUTE_FLOOR]);
        for (; i < end; i++) {
            //System.out.println(ruteData[i]);
            point = getPoint(ruteData[i]);
            x = point[0] + currMatrixX + transformCoordenate(floor, X_COORD);
            y = point[1] + currMatrixY + transformCoordenate(floor, Y_COORD);
            //dot
            g.fillRoundRect(x - 5, y - 5, 10, 10, 5, 5);
            //line
            if (!(lastx == 0 && lasty == 0)) {
                g.drawLine(x - 1, y - 1, lastx - 1, lasty - 1);
                g.drawLine(x, y - 1, lastx, lasty - 1);
                g.drawLine(x - 1, y, lastx - 1, lasty);
                g.drawLine(x, y, lastx, lasty);
                g.drawLine(x, y + 1, lastx, lasty + 1);
                g.drawLine(x + 1, y, lastx + 1, lasty);
                g.drawLine(x + 1, y + 1, lastx + 1, lasty + 1);
            }
            lastx = x;
            lasty = y;
            //System.out.println("point " + (i-begin) + ": " + x + "," + y + " floor " + floor);
        }
    }
    private static final byte X_COORD = 53;
    private static final byte Y_COORD = 54;

    /**
     * For current floor decrements values to fit map image
     * @param floor
     * @param axis: X_COORD || Y_COORD
     * @return the transformed value.
     * TODO remove this method use.
     */
    public static int transformCoordenate(int floor, byte axis) {
        switch (floor) {
            case 0:
                switch (axis) {
                    case X_COORD:
                        return 0;
                    case Y_COORD:
                        return 0;
                }
                break;
            case 1:
                return 0;
            case 2:
                switch (axis) {
                    case X_COORD:
                        return -321;
                    case Y_COORD:
                        return -23;
                }
                break;
            case 3:
                switch (axis) {
                    case X_COORD:
                        return -137;
                    case Y_COORD:
                        return -241;
                }
                break;
            case 4:
                switch (axis) {
                    case X_COORD:
                        return 0;
                    case Y_COORD:
                        return -13;
                }
                break;
            case 5:
                switch (axis) {
                    case X_COORD:
                        return 0;
                    case Y_COORD:
                        return -90;
                }
                break;
            case 6:
                switch (axis) {
                    case X_COORD:
                        return -116;
                    case Y_COORD:
                        return -223;
                }
                break;
        }
        return 0;
    }

    /**
     * Launch a timer task to perform animations
     */
    public static void startTimeout() {
        t = new Timer();
        tt = new TimerTask() {

            public void run() {
                while (true) {
                    if (diff[0] > 0 || diff[1] > 0) {
                        try {
                            runFocusPoint();
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                            //ex.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
                cancelTask();
                diff[0] = 0;
                diff[1] = 0;
            }
        };
        t.scheduleAtFixedRate(tt, 0, 50);
    }

    /**
     * cancels and starts the timer task
     */
    private void reStartTimeout() {
        cancelTask();
        startTimeout();
    }

    /**
     * Cancels the timer task
     */
    public static void cancelTask() {
        try {
            if (Utils.isNotNull(tt)) {
                tt.cancel();
            }
            if (Utils.isNotNull(t)) {
                t.cancel();
            }
        } catch (Throwable e) {
            //Utils.debugModePrintStack(e, "FMap", "cancelTask");
        } finally {
            tt = null;
            t = null;
        }
        timeoutValue = 0;
    }
}
