package a;

import java.io.IOException;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDletStateChangeException;

import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
//import com.mirblu.j2me.fui.comunication.FBluetooth;
import items.FSuperItem;
import items.image.FCanvas;
import items.image.FImage;
import items.image.FImageGrid;
import items.image.FScrollableImage;
import items.image.FSelectBox;
import items.image.NImage;
import items.image.maps.FMap;
import items.image.maps.FRutePlanner;
import items.menu.FHoverMenu;
import items.text.FText;
import items.text.FTextInput;
import java.util.Timer;
import javax.microedition.lcdui.Font;
import midlet.FMIDlet;
import utils.Categories;
import utils.Products;
import utils.Utils;

public class MirbluMapsMIDlet extends FMIDlet {

    /**
     * shows or hide a secondary menu
     */
    public static boolean secondaryMenu = false;
    private Timer t;
    private static boolean DestinyAllReadySelected=false;

    /**
     * Load needed resources for this application.
     */
    public MirbluMapsMIDlet() {
        super();
        //load images
        try {
            //Load images largest to smallest.
            //This helps with heap fragmentation
            home=Image.createImage("/h.png");
            categories=Image.createImage("/cat.png");
            orDot=Image.createImage("/or.png");
            destDot=Image.createImage("/des.png");
            backgroundImg = Image.createImage("/b.png");
            backgroundAbout=Image.createImage("/logoBack.png");
            advertisementImg = Image.createImage("/oager.png");
            checkBoxYesImg = Image.createImage("/cy.png");
            createImg = Image.createImage("/cr.png");
            bluetoothImg = Image.createImage("/bt.png");
            aboutImg = Image.createImage("/a.png");
            exploreImg = Image.createImage("/e.png");
            checkBoxNoImg = Image.createImage("/cn.png");
            exitImg = Image.createImage("/x.png");
            floorImageImg = Image.createImage("/f.png");
        } catch (Throwable e) {
            //Utils.debugModePrintStack(e, "MirbluMapsMIDlet", "constructor loading images");
        }
        hotspotsList = new Utils().readFile(Utils.HOT_SPOTS_FILENAME);
        hotspotsFiltered = hotspotsList;
    }

    protected void startApp() {
        try {
            Utils.debugMode = false;
            Utils.debugPaintOnCanvas = false;
            FApp._app = new FAppImpl().newInstance(this, new FCanvas());
            //initBluetooth();
            /** where to start */
            
            FCanvas.backgroundColor = mainBackgroundColor;
            FCanvas.textColor = mainTextColor;
            FApp._app.intro(" ", get3DSplashScreen());
            FApp._app.startAt(FApp.STATE_SPLASH_SCREEN);
            Display.getDisplay(FApp._theMIDlet).setCurrent(FApp._theCanvas);                                    
            

            //FApp._app.mainScreen("Mapa Turístico de Salamanca", getMainMenu());
            //FApp._app.startAt(FApp.STATE_MAIN_SCREEN);
            //FCanvas.backgroundColor = mainBackgroundColor;
            //FCanvas.textColor = mainTextColor;
            //Display.getDisplay(FApp._theMIDlet).setCurrent(FApp._theCanvas);

        } catch (Throwable e) {
            Utils.debugModePrintStack(e, "MirbluMIDlet", "startApp");
        }
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        notifyDestroyed();
    }

    public void initBluetooth() {
        return;
    }
          /*  getSatellites();
            bluetooth = new FBluetooth(FRutePlanner.satellites) {

                int satId;

                public void friendDeviceAction(String bluetoothAddress) {
                    //Utils.debugMode(bluetoothAddress, "MirbluMapsMIDlet", "friendDeviceAction", Utils.DEBUG_INFO);

                    if (FRutePlanner.getBluetoothMode()) {
                        switch (FApp._current) {
                            case FApp.STATE_SCREEN_RUTE_PLANNER:
                                //check if device is in rute, just rute planner state
                                if (!FRutePlanner.isSatelliteInRute(bluetoothAddress)) {
                                    //FInteraction.cancelTask();//cancel bluetooth
                                    //wrong path, recalculate rute
                                    satId = FRutePlanner.getSatelliteHotspotId(bluetoothAddress);
                                    FInteractionUtils.sourceHotspotId = String.valueOf(satId);
                                    FInteractionUtils.sourceHotspotName =
                                            FRutePlanner.getSatelliteHotspotName(satId, bluetoothAddress);
                                    //destination will no change
                                    //init & processRute
                                    new FRutePlanner(FRutePlanner.RUTES_FILENAME,
                                            FInteractionUtils.sourceHotspotId,
                                            FInteractionUtils.destinationHotspotId);
                                    //add this satellite to skipped to not be processed twice
                                    FBluetooth.skipDevices.addElement(bluetoothAddress);
                                }
                                break;
                            case FApp.STATE_SCREEN_TRACEME:
                                if (!FRutePlanner.iAmASatellite()) {
                                    //set source to current satellite
                                    satId = FRutePlanner.getSatelliteHotspotId(bluetoothAddress);
                                    //System.out.println("satellite id = " + satId);
                                    FInteractionUtils.sourceHotspotId = String.valueOf(satId);
                                    FInteractionUtils.sourceHotspotName =
                                            FRutePlanner.getSatelliteHotspotName(satId, bluetoothAddress);
                                    //show destinations
                                    FApp._app.changeStateTo(FApp.STATE_SCREEN_DESTINATIONS);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    Utils.callRepaint();
                }

                /**
                 * If is not in satellite list will be skipped
                 /
                public void notFriendDeviceAction(String bluetoothAddress) {
                    FBluetooth.skipDevices.addElement(bluetoothAddress);
                }

                public void setState(byte newState) {
                    if (FBluetooth.state == newState && FBluetooth.STATE_EXCEPTION_BUSY_ == newState) {
                        return;
                    }
                    FBluetooth.state = newState;
                    switch (FApp._current) {
                        case FApp.STATE_SCREEN_TRACEME:
                            FApp._app.changeStateTo(FAppImpl.STATE_RELOAD_CURRENT);
                            break;
                        case FApp.STATE_SCREEN_RUTE_PLANNER:
                            switch (FBluetooth.state) {
                                case FBluetooth.STATE_INQUIRY_COMPLETED_:
                                case FBluetooth.STATE_NOTHING_DISCOVERED_:
                                case FBluetooth.STATE_EXCEPTION_BUSY_:
                                case FBluetooth.STATE_EXCEPTION_:
                                    await(30000);
                                    break;
                            }
                            break;
                        default:
                            switch (FBluetooth.state) {
                                case FBluetooth.STATE_EXCEPTION_BUSY_:
                                    await(3000);
                            }
                            break;
                    }

                }
            };
            //bluetooth.checkCompatibility();
    }*/
    public static Vector traceMeItems = new Vector();
    public static int step = 0;
    private static final int maxInfo = 10;

    public Vector getTraceMe() {
        return null;
    }
    /*    FImage info = null;
        switch (bluetooth.getState()) {
            case FBluetooth.STATE_NOTHING_DISCOVERED_:
                info = new FImage(exitImg, step + ". No hay satelites cercanos. \n Bluetooh esta activado?",
                        mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_EXCEPTION_:
            case FBluetooth.STATE_EXCEPTION_USER_DISABLED_:
                info = new FImage(exitImg, step + ". Ha ocurrido un error: " + FBluetooth.message, mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_EXCEPTION_BUSY_:
                info = new FImage(bluetoothImg, step + ". Bluetooth ocupado.", mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_EXCEPTION_NOT_JSR082_SUPPORT_:
                info = new FImage(exitImg, step + ". Bluetooth no soportado." + FBluetooth.message, mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_INITIALIZED_:
                if (FRutePlanner.iAmASatellite()) {
                    //if is a satellite, stop bluetooth search
                    info = new FImage(bluetoothImg,
                            step + ". Soy una baliza en " + FRutePlanner.getSatelliteHotspotName(
                            FRutePlanner.getSatelliteHotspotId(FBluetooth.localDevice.getBluetoothAddress()),
                            FBluetooth.localDevice.getBluetoothAddress()), mainTextFont, mainTextColor);
                    FInteraction.cancelTask();
                    //FBluetooth.disable();
                } else {
                    info = new FImage(bluetoothImg, step + ". Bluetooth inicializado.", mainTextFont, mainTextColor);
                }
                runBluetooth();
                break;
            case FBluetooth.STATE_INQUIRY_COMPLETED_:
                info = new FImage(bluetoothImg, step + ". Busqueda terminada.", mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_INQUIRY_STARTED_:
                info = new FImage(aboutImg, step + ". Buscando satelites cercanos...", mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_NONE_:
                info = new FImage(exitImg, step + ". Bluetooth no se ha iniciado.", mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_SERVICES_DISCOVERED_:
            case FBluetooth.STATE_SERVICE_SEARCH_COMPLETED_:
                break;
            case FBluetooth.STATE_WAITING_:
                info = new FImage(bluetoothImg, step + ". Esperando... " + FBluetooth.message, mainTextFont, mainTextColor);
                break;
            case FBluetooth.STATE_DEVICE_DISCOVERED_:
                info = new FImage(checkBoxYesImg, step + ". Dispositivo encontrado. Comprobando...", mainTextFont, mainTextColor);
                //runBluetooth();
                break;
            default:
                if (Utils.isNotNull(FBluetooth.message)) {
                    info = new FImage(exitImg, step + ". Estado no controlado (" + FBluetooth.message + ")", mainTextFont, mainTextColor);
                } else {
                    info = new FImage(exitImg, step + ". Estado no controlado.", mainTextFont, mainTextColor);
                }
                //back to main menu
                //FApp._app.changeStateTo(FApp.STATE_BACK_TO_MAIN);
                break;
        }
        if (Utils.isNotNull(info)) {
            //unfocusables
            info.setFocusable(false);

            //remove first element
            if (traceMeItems.size() > maxInfo) {
                //traceMeItems.removeElementAt(traceMeItems.size() - 1);
                traceMeItems.removeElementAt(0);
            }
            //traceMeItems.insertElementAt(info, 0);
            traceMeItems.addElement(info);
            info = null;
        }
        /**
         * process hoverMenu, will be shown at all this states
         /
        switch (bluetooth.getState()) {
            case FBluetooth.STATE_EXCEPTION_BUSY_:
            case FBluetooth.STATE_EXCEPTION_NOT_JSR082_SUPPORT_:
            case FBluetooth.STATE_EXCEPTION_USER_DISABLED_:
            case FBluetooth.STATE_EXCEPTION_:
            case FBluetooth.STATE_NOTHING_DISCOVERED_:
            case FBluetooth.STATE_DISABLED_BY_APP_:
            case FBluetooth.STATE_INQUIRY_COMPLETED_:
            case FBluetooth.STATE_WAITING_:
                //FInteraction.cancelTask();
                //FBluetooth.disable();
                FApp._theCanvas.removeHoverMenu();
                //System.out.println("state: " + FApp._current );
                FHoverMenu menu = getHoverMenu();
                if (Utils.isNotNull(menu)) {
                    traceMeItems.addElement(menu);
                    FApp._theCanvas.hoverMenuAt = (byte) (traceMeItems.size() - 1);
                    //FApp._theCanvas.setFocusToHoverMenu(true);
                    //System.out.println(" hover at " + FApp._theCanvas.hoverMenuAt);
                    //System.out.println(" focus index " + FApp._theCanvas.focusIndex);
                }
                break;
            default:
                break;
        }


        step++;
        return traceMeItems;
    }
     */

    public Vector getRutePlanner() {
        Vector v = new Vector(1, 1);
        FSuperItem fi = new FRutePlanner(FRutePlanner.RUTES_FILENAME,
                FInteractionUtils.sourceHotspotId,
                FInteractionUtils.destinationHotspotId);
        v.addElement(fi);
        //TODO automate this
        v.addElement(getHoverMenu());
        FApp._theCanvas.hoverMenuAt = (byte) (v.size() - 1);
        return v;
    }

    public Vector getExploreMap() {
        int mapPos = Integer.parseInt(FCanvas.focusedItem.id);
        //TODO Bug due to maps are less than elements
        FImageGrid grid = new FImageGrid(" ", "/maps/index", mapPos,
                128, 128, -1, -1, FImageGrid.IMAGEGRID_START_CENTERED, 20);
        //TODO automate this
        FApp._theCanvas.hoverMenuAt = (byte) (1);
        if (FApp._theCanvas.hasHoverMenu()) {
            grid.interaction.addInteraction(Utils.KEY_SELECT,
                    FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU);
            grid.interaction.addInteraction(Utils.KEY_CLEAR,
                    FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU);
        } else {
            //add the back action
            grid.interaction.addInteraction(Utils.KEY_CLEAR,
                    FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
        }
        Vector v = new Vector(2, 1);
        v.addElement(grid);
        v.addElement(getHoverMenu());

        return v;
    }

    public Vector getShowUris() {
        return Products.uriToMenu(Integer.parseInt(Products.productId));
    }

    public Vector getMainMenu() {
        int colNum=0;
        int rowNum=0;
        int numElements=11;
        int iconWidth=this.categories.getWidth();
        int totalWidth=numElements*iconWidth;
        int numElementsCapacityCOL = FApp._theCanvas.getWidth()/iconWidth;
        int numElementsCapacityROW = FApp._theCanvas.getHeight()/iconWidth;

        if (numElementsCapacityCOL<numElementsCapacityROW) numElementsCapacityROW=numElementsCapacityCOL;
        if (numElementsCapacityCOL>numElementsCapacityROW) numElementsCapacityCOL=numElementsCapacityROW;
        boolean exit = false;
        int r=numElementsCapacityROW;
        int c=numElementsCapacityCOL;
        int totalIconsCapa = r*c;
        while(!exit){            
            if (totalIconsCapa>numElements){
                c=c-1;
                totalIconsCapa=r*c;
                if (totalIconsCapa>numElements){
                    r=r-1;
                } 
            }
            totalIconsCapa=r*c;
            if (totalIconsCapa<numElements){
                r=r+1;
                exit=true;
            }
        }
        colNum=c;
        rowNum=r;

        int xColMod=FApp._theCanvas.getWidth()/colNum+1;
        int yRowMod=FApp._theCanvas.getHeight()/rowNum+1;
        int toptFrameWidth=yRowMod - iconWidth/2  ;
        int leftFrameWidth=xColMod - iconWidth/2  ;
        int gapCOL = 0;   gapCOL=xColMod-iconWidth;
        int gapROW = 0;   gapROW=yRowMod-iconWidth;
        int startX = leftFrameWidth;
        int startY = toptFrameWidth+20; //20 por el título

        Vector v = new Vector();
        //Object data, byte contentType, String description, int posx, int posy, int width, int height
        FSuperItem ii2 = new NImage(createImg, "Crear ruta",
                mainTextFont, mainTextColor,
                startX,startY);

        ii2.next = FApp.STATE_SCREEN_SOURCES;
        ii2.interaction = FInteraction.newInstance();
        ii2.interaction.addInteraction(
                Utils.KEY_RIGHT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        ii2.interaction.addInteraction(
                Utils.KEY_SELECT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        
        FSuperItem ii4 = new NImage(
                exploreImg, "Explorar mapas",
                mainTextFont, mainTextColor,
                startX+gapCOL, startY);
        ii4.next = FApp.STATE_SCREEN_SELECT_MAPS;
        ii4.interaction = FInteraction.newInstance();
        ii4.interaction.addInteraction(
                Utils.KEY_RIGHT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        ii4.interaction.addInteraction(
                Utils.KEY_SELECT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        
        FSuperItem ii6 = new NImage(
                categories, "Categorías",
                mainTextFont, mainTextColor,
                startX, startY+gapROW);
        ii6.next = FApp.STATE_SCREEN_CATEGORIES;
        ii6.interaction = FInteraction.newInstance();
        ii6.interaction.addInteraction(Utils.KEY_SELECT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        
        FSuperItem ii5 = new NImage(
                aboutImg, "Acerca de...",
                mainTextFont, mainTextColor,
                startX+gapCOL, startY+gapROW);
        //TODO ii5.setFocusable(true); //add interaction and screen items
        ii5.next= FApp.STATE_SCREEN_ABOUT;
        ii5.interaction = FInteraction.newInstance();
        ii5.interaction.addInteraction(
                Utils.KEY_RIGHT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        ii5.interaction.addInteraction(
                Utils.KEY_SELECT,
                FInteractionUtils.ACTION_JUMP_TO_SCREEN);

        //ii5.setFocusable(false);
        
        FSuperItem ii1 = new NImage(
                exitImg, "Salir",
                mainTextFont, mainTextColor,
                startX, startY+gapROW*2);
        ii1.interaction = FInteraction.newInstance();
        ii1.interaction.addInteraction(Utils.KEY_SELECT,
                FInteractionUtils.ACTION_EXIT);

        

        v.addElement(ii2);
        v.addElement(ii6);
        //v.addElement(ii3);
        v.addElement(ii4);
        v.addElement(ii5);
        v.addElement(ii1);

        ii1 = null;
        ii5 = null;
        ii4 = null;        
        ii6 = null;
        ii2 = null;

        return v;
    }


    /**
     * Gets the hover menu for one state.
     * @return null if state has not a hover menu
     */
    public static FHoverMenu getHoverMenu() {
        String[] names = null;
        Image[] images = null;
        byte[] actions = null;
        String[] data = null;

        switch (FApp._current) {
            case FApp.STATE_SCREEN_RUTE_PLANNER:
                if (false) {
                    names = new String[]{
                                "Punto siguiente",
                                "Explorar", // focus to map
                                "Crear ruta desde aquí",//trace_me: encuentra baliza ? "destinos"; "debe introducir el origen manualmente"
                                "Menú principal",//main_menu
                                "Punto anterior"
                            };
                    images = new Image[]{
                                FApp.arrowRight, exploreImg, bluetoothImg, home, FApp.arrowLeft
                            };
                    actions = new byte[]{
                                FMap.ACTION_NEXT_POINT,
                                FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM,
                                FInteractionUtils.ACTION_JUMP_TO_SCREEN,
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU,
                                FMap.ACTION_PREV_POINT
                            };
                    data = new String[]{
                                "", "", String.valueOf(FApp.STATE_SCREEN_TRACEME), "", ""
                            };
                } else {
                    names = new String[]{
                                "Punto siguiente",
                                "Explorar", // focus to map
                                "Menú principal",//main_menu
                                "Punto anterior"
                            };
                    images = new Image[]{
                                FApp.arrowRight, exploreImg, home, FApp.arrowLeft
                            };
                    actions = new byte[]{
                                FMap.ACTION_NEXT_POINT,
                                FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM,
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU,
                                FMap.ACTION_PREV_POINT
                            };
                    data = new String[]{
                                "", "", "", ""
                            };
                }
                break;
            case FApp.STATE_SCREEN_EXPLORE_MAP:
                if (false) {
                //if (FBluetooth.activated) {
                    names = new String[]{
                                "Explorar", // focus to map
                                "Crear ruta desde aquí",//trace_me
                                "Menú principal"//main_menu
                            };
                    images = new Image[]{
                                exploreImg, bluetoothImg, home
                            };
                    actions = new byte[]{
                                FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM,
                                FInteractionUtils.ACTION_JUMP_TO_SCREEN,
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU
                            };
                    data = new String[]{
                                "", String.valueOf(FApp.STATE_SCREEN_TRACEME), ""
                            };
                } else {
                    names = new String[]{
                                "Explorar", // focus to map
                                "Menú principal"//main_menu
                            };
                    images = new Image[]{
                                exploreImg, home
                            };
                    actions = new byte[]{
                                FInteractionUtils.ACTION_FOCUS_TO_FIRST_ITEM,
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU
                            };
                    data = new String[]{
                                "", ""
                            };
                }
                break;
            case FApp.STATE_SCREEN_TRACEME:
                if (false) {
                    names = new String[]{
                                "Reintentar",
                                "Crear ruta manual",
                                "Cancelar"
                            };
                    images = new Image[]{
                                bluetoothImg, createImg, home
                            };
                    actions = new byte[]{
                                FInteractionUtils.ACTION_RETRY_BLUETOOTH,
                                FInteractionUtils.ACTION_JUMP_TO_SCREEN,
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU,};
                    data = new String[]{
                                "", String.valueOf(FApp.STATE_SCREEN_SOURCES), ""
                            };
                } else {
                    names = new String[]{
                                "Cancelar"
                            };
                    images = new Image[]{
                                home
                            };
                    actions = new byte[]{
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU
                            };
                    data = new String[]{
                                ""
                            };
                }

                break;
            case FApp.STATE_SCREEN_PRODUCTS_SPLASH:
                names = new String[]{
                            "Como Llegar aquí",
                            "Contactar",
                            "Menú principal",
                            "Volver"
                        };
                images = new Image[]{
                            bluetoothImg,
                            createImg,
                            home,
                            FApp.arrowLeft
                        };
                actions = new byte[]{
                            FInteractionUtils.ACTION_JUMP_TO_SCREEN,
                            FInteractionUtils.ACTION_JUMP_TO_SCREEN,
                            FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU,
                            FInteractionUtils.ACTION_JUMP_TO_SCREEN
                        };
                data = new String[]{
                            String.valueOf(FApp.STATE_SCREEN_SOURCES),
                            String.valueOf(FApp.STATE_SCREEN_SHOW_URIS),
                            "",
                            String.valueOf(FApp.STATE_SCREEN_PRODUCTS)
                        };
                break;
            case FApp.STATE_SCREEN_ABOUT:
                     names = new String[]{
                                "Menú principal"//main_menu
                            };
                    images = new Image[]{
                                home
                            };
                    actions = new byte[]{
                                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU
                            };
                    data = new String[]{
                                ""
                            };
                break;
             case FApp.STATE_SCREEN_PROPIC:
                     names = new String[]{
                                "Información",
                                "Menú principal",
                                "Volver"
                            };
                    images = new Image[]{
                                FApp.arrowRight,
                                home,
                                FApp.arrowLeft
                            };
                    actions = new byte[]{
                            FInteractionUtils.ACTION_JUMP_TO_SCREEN,
                            FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU,
                            FInteractionUtils.ACTION_JUMP_TO_SCREEN
                            };
                    data = new String[]{
                                String.valueOf(FApp.STATE_SCREEN_PRODUCTS_SPLASH),
                                "",
                                String.valueOf(FApp.STATE_SCREEN_PRODUCTS)
                            };
                break;
            default:
                break;
        }
        FHoverMenu menu = null;
        if (Utils.isNotNull(names) && Utils.isNotNull(images) && Utils.isNotNull(actions) && Utils.isNotNull(data)) {
            menu = new FHoverMenu(
                    names, images, actions, data);
        }
        return menu;
    }

    public Vector getSourceList(boolean DestinyAllReadySelectedParam) {
        Vector v = new Vector();
        DestinyAllReadySelected = DestinyAllReadySelectedParam;
        //Supongo que esto es para que no de la opcion de escribir si es un
        //mÃƒÂ³vil con pantalla tÃƒÂ¡ctil
        if (!FCanvas.isPointerScreen) {
            v.addElement(getTextInput());
        }
        v = getFilteredListOfFSelectBox(v, "", "");
        return v;
    }

    public Vector getDestinyList() {
        Vector v = new Vector();        
        if (!FCanvas.isPointerScreen) {
            v.addElement(getTextInput());
        }
        v = getFilteredListOfFSelectBox(v, "", FInteractionUtils.sourceHotspotId);
        runBluetooth();
        return v;
    }
    //NACHO
    public final Vector getShowImage(String path) {
        Vector v = new Vector(1, 1);
        FSuperItem fi = null;
        
        try {
                Image pic = Image.createImage(path);
                int starty=(FApp._theCanvas.canvasHeight-pic.getHeight())/2;
                int startx=(FApp._theCanvas.canvasWidth-pic.getWidth())/2;
                fi = new FScrollableImage(pic, "Map", startx, starty, 20);
                v.addElement(fi);
        } catch (Throwable ex) {
            Utils.debugModePrintStack(ex,
                    "MirbluMapsMIDlet", "getScrollableImage(" + FInteractionUtils.imageToShow + ")");
        }
        fi = null;
        FApp._theCanvas.hoverMenuAt = (byte) (1);
        v.addElement(getHoverMenu());
        return v;
    }
    //NACHO
    public Vector getAbout() {
        Vector ret = new Vector(1, 1);
        int lowLimitMap = 0;
        int startY = 0;
      

        try {
            FSuperItem fi = null;
            map = Image.createImage("/logo.png");
            startY=+(FApp._theCanvas.canvasHeight/2)-map.getHeight()*2;
            lowLimitMap=startY+map.getHeight()+10;
            //Image centered
            //if(FApp._theCanvas.canvasWidth>map.getWidth())
                fi = new FScrollableImage(map, "Logo", (FApp._theCanvas.canvasWidth-map.getWidth())/2, startY, 500);
            //else
            //    fi = new FScrollableImage(map, "Logo", 0, startY, 20);
            
            ret.addElement(fi);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String message = "Desarrollado por mirblu S.L.^Contacto: www.mirblu.com";
        FText ft = new FText(message, "", mainTextColor, mainBackgroundColor,
                Font.getDefaultFont(), 0, lowLimitMap + 10, FApp._theCanvas.canvasWidth, lowLimitMap+ 500, 0, false);
      
        ret.addElement(ft);
        FApp._theCanvas.hoverMenuAt = (byte) (2);
        ret.addElement(getHoverMenu());
        return ret;
    }

    /**
     * The result is the hotspotList without all elements that not contains
     * the filter string ignoring case
     * @param v the vector where we will add
     * @param filter the filter string
     * @return a Vector<FSelectBox>
     */
    public static final Vector getFilteredListOfFSelectBox(Vector v, String filter, String skipId) {
        String[] hotspot = null;
        String object = null;
        FSelectBox fi = null;
        int i = 0;
        hotspotsFiltered = Utils.filter(hotspotsList, filter, Utils.FILTER_EXCLUDE);

        for (; i < hotspotsFiltered.size(); i++) {
            object = (String) hotspotsFiltered.elementAt(i);
            object = (String)object.substring(0,object.length()-1 );
            hotspot = Utils.split(
                    object,
                    Utils.HOTSPOTS_FILE_DIVIDE_CHAR,
                    Utils.HOTSPOTS_FILE_ARRAY_SIZE);
            if (Utils.isNotNull(skipId) && !skipId.equals("") && skipId.equals(hotspot[Utils.HOTSPOTS_FILE_ID_POS])) {
                //we must skip some values
            } else {
                //skip satellites
                if (hotspot[Utils.HOTSPOTS_FILE_TEXT_POS].indexOf(FRutePlanner.SATELLITE_START_STRING) == -1) {
                    //switch between sources and destinations
                    byte temp;
                    switch (FApp._current) {
                        case FApp.STATE_SCREEN_SOURCES:
                            temp = FApp.STATE_SCREEN_DESTINATIONS;
                            if (DestinyAllReadySelected)  temp = FApp.STATE_SCREEN_RUTE_PLANNER;
                            break;
                        case FApp.STATE_SCREEN_DESTINATIONS:
                            temp = FApp.STATE_SCREEN_RUTE_PLANNER;
                            break;
                        case FApp.STATE_NONE:
                            temp = FApp.STATE_NONE;
                            break;
                        default:
                            temp = FApp.STATE_BACK_TO_MAIN;
                            break;
                    }

                    fi = new FSelectBox(hotspot[Utils.HOTSPOTS_FILE_TEXT_POS],
                            mainTextFont, mainTextColor, checkBoxYesImg, checkBoxNoImg, false, temp);
                    fi.id = hotspot[Utils.HOTSPOTS_FILE_ID_POS];

                    switch (FApp._current) {
                        case FApp.STATE_SCREEN_SOURCES:
                            temp = FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU;
                            break;
                        case FApp.STATE_SCREEN_DESTINATIONS:
                            temp = FInteractionUtils.ACTION_GO_BACK;
                            break;
                        case FApp.STATE_NONE:
                            temp = FInteractionUtils.ACTION_NONE;
                            break;
                        default:
                            temp = FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU;
                            break;
                    }
                    fi.interaction.addInteraction(Utils.KEY_CLEAR, temp);
                    v.addElement(fi);
                }
                //skip satellites
				/*if (hotspot[Utils.HOTSPOTS_FILE_TEXT_POS].indexOf(FRutePlanner.SATELLITE_START_STRING) == -1) {
                fi = new FSelectBox(hotspot[Utils.HOTSPOTS_FILE_TEXT_POS],
                mainTextFont, mainTextColor, checkBoxYesImg, checkBoxNoImg, false, FApp.STATE_SCREEN_DESTINATIONS);
                fi.id = hotspot[Utils.HOTSPOTS_FILE_ID_POS];
                fi.interaction.addInteraction(Utils.KEY_CLEAR,
                FInteractionUtils.ACTION_GO_BACK);
                v.addElement(fi);
                }*/
            }
        }
        fi = null;
        object = null;
        hotspot = null;
        return v;
    }
    //each description is asociated with the map to show.

    //TODO: Esto no puede quedar asÃƒÂ­. Tiene que ser independiente del edificio en que se implante
    public static final String[] descriptions = {
        "Mapa del casco antíguo de Salamanca"
        //"Planta baja de CompaÃƒÂ±ÃƒÂ­a",
        //"Primera planta Serranos",
        //"Tercera planta Serranos",
        //"Tercera planta CompaÃƒÂ±ÃƒÂ­a",
        //"Cuarta planta EUI",
        //"Quinta planta EUI",
        //"Sexta planta EUI"
    };

    public final Vector getSelectMapScreen() {
        Vector v = new Vector(1, 1);
        FSuperItem fi = null;
        for (int i = 0; i < descriptions.length; i++) {
            fi = new FImage(
                    floorImageImg, descriptions[i], mainTextFont, mainTextColor);
            //image folder name
            fi.next = FApp.STATE_SCREEN_EXPLORE_MAP;
            fi.id = String.valueOf(i);
            fi.interaction = FInteraction.newInstance();
            fi.interaction.addInteraction(
                    Utils.KEY_SELECT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
            fi.interaction.addInteraction(
                    Utils.KEY_CLEAR, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
            v.addElement(fi);
        }
        fi = null;
        return v;
    }

    /* public Vector getLoadingScreen() {
    Vector v = new Vector(1, 1);

    FSuperItem fi = new FSplashScreen(exit, "Cargando...",
    "preparando recursos necesarios", mainTextFont, mainTextColor, mainBackgroundColor,
    null, 5000, FInteractionUtils.ACTION_JUMP_TO_SCREEN, false);
    fi.next = FApp.STATE_SCREEN_SHOW_IMAGE;

    aTask = new TimerTask() {
    public void run() {
    if (Utils.isNull(map)
    && Utils.isNotNull(FInteractionUtils.imageToShow)
    && !FInteractionUtils.imageToShow.equals(""))
    {
    try {
    map = Image.createImage(FInteractionUtils.imageToShow);
    FApp.changeStateTo(FApp.STATE_SCREEN_SHOW_IMAGE);
    } catch (IOException ex) {
    ex.printStackTrace();
    }
    }
    Utils.callRepaint();
    }
    };
    v.addElement(fi);
    fi = null;
    return v;
    }*/
    public final Vector getShowImage() {
        Vector v = new Vector(1, 1);
        FSuperItem fi = null;
        try {
            if (Utils.isNotNull(FInteractionUtils.imageToShow) && !FInteractionUtils.imageToShow.equals("")) {
                map = Image.createImage(FInteractionUtils.imageToShow);
                //start at top left
                fi = new FScrollableImage(map, "Map", 0, 0, 20);
                v.addElement(fi);
            }
        } catch (Throwable ex) {
            //Utils.debugModePrintStack(ex,
            //        "MirbluMapsMIDlet", "getScrollableImage(" + FInteractionUtils.imageToShow + ")");
        }
        fi = null;
        return v;
    }

    /**
     * Creates a 3D image and shows on the screen
     * @return the Vector with all the elements

    public final Vector get3DSplashScreen() {
    Vector v = new Vector(1, 1);
    //TODO read from config file
    String title = "Universidad Pontificia de Salamanca";
    String text = "Bienvenidos a la Universidad Pontificia de Salamanca (UPSA)." +
    "Con esta aplicaciÃƒÂ³n podrÃƒÂ¡s utilizar mapas para navegar por la Universidad" +
    " sin perderte nunca. " +
    "Si quieres mÃƒÂ¡s informaciÃƒÂ³n pulsa SELECT para llamar o guardar el nÃƒÂºmero de contacto.";
    FSplashScreen splash = new FSplashScreen(
    advertisementImg, title, text, mainTextFont, mainTextColor,
    mainBackgroundColor, "tel:+34923277100", 15000,
    FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU, true);
    cube = new Cube(advertisementImg);
    splash.setContent(cube);

    aTask = new TimerTask() {

    public void run() {
    //TODO repaint just where needed
    Utils.callRepaint();
    }
    };
    splash.interaction = FInteraction.newInstance();
    FInteraction.addTimeoutAction(aTask, 0, 40);
    splash.interaction.addInteraction(
    Utils.KEY_SELECT, FInteractionUtils.ACTION_PLATFORM_REQUEST);
    splash.interaction.addInteraction(
    Utils.KEY_CLEAR, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);

    v.addElement(splash);
    splash = null;
    return v;
    } */
    private static Vector items;

    private static Object getTextInput() {
        FTextInput ti = new FTextInput("Escribe aquí...", 20,
                mainTextFont, mainTextColor, mainBackgroundColor, mainTextColor,
                200, cursorColor, 1500, 500) {

            public void update() {
                //only do it on sources and destinations
                switch (FApp._current) {
                    case FApp.STATE_SCREEN_SOURCES:
                    case FApp.STATE_SCREEN_DESTINATIONS:
                        //reset the list adding this object at first
                        items = new Vector();
                        items.addElement(this);
                        if (!this.getDescription().equals(inputText)) {
                            items = getFilteredListOfFSelectBox(
                                    items, inputText, FInteractionUtils.sourceHotspotId);
                        } else {
                            items = getFilteredListOfFSelectBox(
                                    items, "", FInteractionUtils.sourceHotspotId);
                        }
                        //set the number of items at title
                        StringBuffer temp = new StringBuffer();
                        if (Utils.isNotNull(FApp._theCanvas)) {
                            String title;
                            int end = FApp._theCanvas.getTitle().indexOf(" (");
                            //if title has the counter
                            if (end <= -1) {
                                title = FApp._theCanvas.getTitle();
                            } else {
                                title = FApp._theCanvas.getTitle().substring(0, end);
                            }
                            temp.append(title);
                            temp.append(" (").append(items.size() - 1).append(")");
                        }
                        //control when nothing found
                        if (items.size() - 1 <= 0) {//only has textInput
                            FImage notFound = new FImage(aboutImg, "<<ningún resultado>>", mainTextFont, mainTextColor);
                            notFound.setFocusable(false);
                            items.addElement(notFound);
                            notFound = null;
                        }
                        FApp._theCanvas.setTitle(temp.toString());
                        FApp._theCanvas.setItems(items);
                        temp = null;
                        items = null;
                        Utils.callRepaint();
                        break;
                }
            }
        };
        ti.focusWrite = FTextInput.FOCUS_WRITE_FIRST_TIME;
        return ti;
    }

    public static void runBluetooth() {
        return;
    }
     /*   TimerTask tt = new TimerTask() {

            public void run() {
                if (FRutePlanner.getBluetoothMode()) {
                    switch (FBluetooth.state) {
                        case FBluetooth.STATE_EXCEPTION_BUSY_:
                            break;
                        default:
                            bluetooth.searchDevices();
                            break;
                    }
                }
            }
        };
        FInteraction.addTimeoutAction(tt, 0, 100);
    }*/

    /*public void exit() {
    try {
    bluetooth.disable();
    FInteraction.cancelTask();
    //TODO free more resources and stop anything

    } catch (Throwable ex) {
    Utils.debugModePrintStack(ex, "MirbluMapsMIDlet", "exit");
    } finally {
    try {
    //tipical exit

    //destroyApp(true);
    } catch (Throwable ex) {
    Utils.debugModePrintStack(ex, "MirbluMapsMIDlet", "exit");
    }
    }
    }*/

    //intro de la aplicacion
    public Vector get3DSplashScreen() {    
        Vector v = new Vector(1,1);
        FSuperItem fi = null;
        try {                
                Image pic = Image.createImage(advertisementImg);
                //int starty=(FApp._theCanvas.getHeight()-pic.getHeight())/2;
                int starty=40;
                int startx=(FApp._theCanvas.getWidth()-pic.getWidth())/2;
                fi = new FScrollableImage(pic, "Map", startx , starty, 20);
                fi.interaction.addAnyKeyAction(FInteractionUtils.ACTION_LOAD_MAIN_MENU);
                v.addElement(fi);                
                int y = starty + 50 + pic.getHeight();
                
                pic = Image.createImage("/logo2.png");
                int x=(FApp._theCanvas.getWidth()-pic.getWidth())/2;
                fi = new FScrollableImage(pic, "Map", x , y, 20);
                v.addElement(fi);
                String message = "Press Any Key";
                FText ft = new FText(message, "", mainTextColor, mainBackgroundColor,Font.getDefaultFont(),
                50,FApp._theCanvas.getHeight()-10,
                500, 1000, 1000, false);
                v.addElement(ft);
        } catch (Throwable ex) {
            Utils.debugModePrintStack(ex,
                    "MirbluMapsMIDlet", "getScrollableImage(" + FInteractionUtils.imageToShow + ")");
        }
       fi = null;
       return v;
    }

    /**
     * Sets the satellites inside FRutePlanner
     */
    public void getSatellites() {
        if (Utils.isNull(FRutePlanner.satellites) || FRutePlanner.satellites.size() < 1) {
            Vector temp = Utils.filter(hotspotsList, "$", Utils.FILTER_EXCLUDE);
            String[] s;
            for (int i = 0; i < temp.size(); i++) {
                s = Utils.split((String) temp.elementAt(i),
                        Utils.HOTSPOTS_FILE_DIVIDE_CHAR,
                        Utils.HOTSPOTS_FILE_ARRAY_SIZE);
                //add removing '$'
                FRutePlanner.addSatellite(s[Utils.HOTSPOTS_FILE_TEXT_POS].substring(1),
                        Integer.valueOf(s[Utils.HOTSPOTS_FILE_ID_POS]).intValue());

            }
        }
    }

    public static final Vector getCategories() {
        Vector ret = new Vector(1, 1);
        Utils.readProducts("/products.txt");
        Utils.readCategories("/categories.txt");
        int i = 0;
        for (; i < Categories.size; i++) {
            ret.addElement(Categories.toFImage(i));
        }
        return ret;
    }

    public static final Vector getProducts() {
        Vector ret = new Vector(1, 1);      
        Utils.readProducts("/products.txt");
        int[] ids = Categories.getProductsIds(Integer.valueOf(Categories.categoryId).intValue());
        if (ids == null) {
            FImage item = new FImage(exitImg, "No hay productos en esta categoría", mainTextFont, mainTextColor);
            item.interaction = FInteraction.newInstance();
            item.interaction.addAnyKeyAction(FInteractionUtils.ACTION_GO_BACK);
            ret.addElement(item);
            item = null;
        } else {
            //Vector<String[]>
            Vector products = Products.getProductsForIds(ids);
            int i = 0;
            for (; i < products.size(); i++) {
                //NACHO
                //id and icon
                //ret.addElement(Products.toFImage(((String[]) products.elementAt(i))[Products.ID],
                //        ((String[]) products.elementAt(i))[Products.ICON]));
                ret.addElement(Products.toFImage(((String[]) products.elementAt(i))[Products.ID], ("") ));
            }
            products = null;
        }
        return ret;
    }

    public Vector getSourceList() {
        Vector v = new Vector();
        this.DestinyAllReadySelected = DestinyAllReadySelected;
        //Supongo que esto es para que no de la opcion de escribir si es un
        //mÃƒÂ³vil con pantalla tÃƒÂ¡ctil
        if (!FCanvas.isPointerScreen) {
            v.addElement(getTextInput());
        }
        v = getFilteredListOfFSelectBox(v, "", "");
        return v;
    }
    
}
