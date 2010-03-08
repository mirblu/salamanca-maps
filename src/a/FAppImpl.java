package a;

import midlet.FMIDlet;
import javax.microedition.lcdui.Display;

import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
import app.FAppInterface;
//import com.mirblu.j2me.fui.comunication.FBluetooth;
import items.image.FCanvas;
import items.image.maps.FRutePlanner;
import items.text.FText;
import utils.Categories;
import utils.Products;
import utils.Utils;
import java.util.Vector;

public class FAppImpl extends FApp {

    public FAppImpl() {
        super();
    }

    public void changeStateTo(byte newState) {

        if (newState != _current && newState != STATE_NONE) {
            _last = _current;
            _theCanvas.removeHoverMenu();
            //before exit current state
            switch (_current) {
                case STATE_SCREEN_CATEGORIES:
                    //save selected category data
                    Categories.categoryId = Categories.getCategoryProperty(
                            Integer.valueOf(FCanvas.focusedItem.id).intValue(),
                            Categories.ID);
                    Categories.categoryName = Categories.getCategoryProperty(
                            Integer.valueOf(FCanvas.focusedItem.id).intValue(),
                            Categories.NAME);
                    break;
                case STATE_SCREEN_PRODUCTS:
                    if (Utils.isNotNull(FCanvas.focusedItem.id)) {
                        Products.productId = Products.getProductProperty(
                                Integer.valueOf(FCanvas.focusedItem.id).intValue(),
                                Products.ID);
                        Products.productName = Products.getProductProperty(
                                Integer.valueOf(FCanvas.focusedItem.id).intValue(),
                                Products.NAME);
                        
                    }
                    break;
                case STATE_SCREEN_PRODUCTS_SPLASH:
                    //NACHO
                    //FInteractionUtils.sourceHotspotId = Products.getProductProperty(Integer.parseInt(Products.productId),Products.HOTSPOT_ID );
                    //FInteractionUtils.sourceHotspotName = Products.productName;
                    FInteractionUtils.destinationHotspotId= Products.getProductProperty(Integer.parseInt(Products.productId),Products.HOTSPOT_ID );
                    FInteractionUtils.destinationHotspotName = Products.productName;
                    switch (newState) {
                        //NACHO
                        //case STATE_SCREEN_DESTINATIONS:
                        case STATE_SCREEN_SOURCES:
                        case STATE_SCREEN_SHOW_URIS:                        
                            //don't delete data
                            break;
                        default:
                           //NACHO
                           // He comentado estas lineas por que no permitia refrescar la
                            // descripcion selecionada cuando veias una descripcion y
                            // volvias a productos y seleccionabas otro , se mantenia en
                            // el anterior
                           // FCanvas.hasTextAt = -1;
                           // FText.lines.removeAllElements();
                           // FText.linesToFitCanvas = -1;
                           // FText.firstLine = 0;
                           // FText.msg = "";
                           // break;
                    }
                    break;
                case STATE_SPLASH_SCREEN:
                    //FInteraction.cancelTask();
                    //FMIDlet.cube = null;
                    break;
                case STATE_SCREEN_SOURCES:
                    FInteractionUtils.sourceHotspotId = FCanvas.focusedItem.id;
                    FInteractionUtils.sourceHotspotName = FCanvas.focusedItem.description;
                    break;
                case STATE_SCREEN_DESTINATIONS:
                    FInteractionUtils.destinationHotspotId = FCanvas.focusedItem.id;
                    FInteractionUtils.destinationHotspotName = FCanvas.focusedItem.description;
                    //from destinations back to sources
                    switch (newState) {
                        case STATE_SCREEN_SOURCES:
                            FInteractionUtils.sourceHotspotId = "";
                            FInteractionUtils.sourceHotspotName = "";
                            FApp._theMIDlet.hotspotsFiltered = FApp._theMIDlet.hotspotsList;
                            break;
                    }
                    break;
                case STATE_SCREEN_SELECT_MAPS:
                    FInteractionUtils.imageToShow = FCanvas.focusedItem.id;
                    break;
                case STATE_SCREEN_TRACEME:
                    //delete traceme items if not reloading state
                    switch (newState) {
                        case STATE_RELOAD_CURRENT:
                            break;
                        default:
                            //FBluetooth.resetSkipped();
                            MirbluMapsMIDlet.traceMeItems = new Vector();
                            MirbluMapsMIDlet.step = 0;
                            ((MirbluMapsMIDlet)(FApp._theMIDlet)).getSatellites();
                            //FApp._theMIDlet.bluetooth.init(FRutePlanner.satellites);
                            break;
                    }
                    //FBluetooth.skipDevices = new Vector();
                    break;
                
                case STATE_LOADING:
                    FInteraction.cancelTask();//cancel bluetooth
                    break;
                case STATE_SCREEN_RUTE_PLANNER:
                    FInteractionUtils.sourceHotspotId = "";
                    FInteractionUtils.sourceHotspotName = "";
                    FInteractionUtils.destinationHotspotId = "";
                    FInteractionUtils.destinationHotspotName = "";
                    //FBluetooth.timeToWait = 0;
                    //FApp._theMIDlet.bluetooth.start();
                    break;
                default:
                    break;
            }

            //exit current state
            switch (newState) {
                case STATE_NONE:
                    break;
                /*case FApp.STATE_SCREEN_TRACEME:
                    FBluetooth.resetSkipped();
                    break;*/
                default:
                    removeScreen();
                    break;
            }
            //cleaning
            System.gc();
            //change current state
            _current = newState;
            //prepare new state before entering it
            switch (newState) {
                case STATE_RELOAD_CURRENT:
                    changeStateTo(_last);
                    return;         
                case STATE_BACK_TO_MAIN:
                    //NACHO
                    addScreen("Plano Turístico de Salamanca", _theMIDlet.getMainMenu(), STATE_MAIN_SCREEN);
                    _current = STATE_MAIN_SCREEN;
                    break;
                case STATE_BACK:
                    _current = _last;
                    break;
                case STATE_NEXT_SCREEN:
                    if (_current < _numberOFScreens) {
                        _current++;
                    }
                    break;
                case STATE_NEXT_X_SCREEN:
                    changeStateTo(_x);
                    return;
                case STATE_EXIT_PROGRAM:
                    //exit before change state
                    FInteractionUtils.exit();
                    return;                
                case STATE_SCREEN_CATEGORIES:
                    addScreen("Categorias",
                            MirbluMapsMIDlet.getCategories(),
                            FApp.STATE_SCREEN_CATEGORIES);
                    break;
                case STATE_SCREEN_PRODUCTS:
                    addScreen(Categories.categoryName,
                            MirbluMapsMIDlet.getProducts(),
                            FApp.STATE_SCREEN_PRODUCTS);
                    break;
                case STATE_SCREEN_PRODUCTS_SPLASH:
                    addScreen(Products.productName,
                            Products.toAdvertisement(Integer.parseInt(Products.productId)),
                            FApp.STATE_SCREEN_PRODUCTS_SPLASH);
                    break;
                case STATE_SCREEN_SHOW_URIS:
                    addScreen(Products.productName,
                            Products.uriToMenu(
                            Integer.valueOf(Products.productId).intValue()),
                            FApp.STATE_SCREEN_SHOW_URIS);
                    break;
                case STATE_SCREEN_SOURCES:
                    //reset hotspot list
                    boolean destinyAllReadySelected=false;
                    _theMIDlet.hotspotsFiltered = _theMIDlet.hotspotsList;
                    if (_last != STATE_SCREEN_PRODUCTS_SPLASH){
                    addScreen("Origenes", _theMIDlet.getSourceList(destinyAllReadySelected),
                            FApp.STATE_SCREEN_SOURCES);
                    }else{
                        destinyAllReadySelected = true;
                        addScreen("Origenes", _theMIDlet.getSourceList(destinyAllReadySelected),
                            FApp.STATE_SCREEN_SOURCES);
                    }

                    break;
                case STATE_SCREEN_DESTINATIONS:
                    addScreen("Destinos", _theMIDlet.getDestinyList(),
                            FApp.STATE_SCREEN_DESTINATIONS);
                    break;
                case STATE_SCREEN_SELECT_MAPS:
                    addScreen("Escoje un mapa", _theMIDlet.getSelectMapScreen(),
                            STATE_SCREEN_SELECT_MAPS);
                    break;
                case STATE_SCREEN_SHOW_IMAGE:
                    addScreen(" ", _theMIDlet.getShowImage(),
                            STATE_SCREEN_SHOW_IMAGE);
                    break;
                case STATE_SCREEN_EXPLORE_MAP:
                    addScreen(" ", _theMIDlet.getExploreMap(), 
                    		STATE_SCREEN_EXPLORE_MAP);
                    break;
                case STATE_SCREEN_RUTE_PLANNER:
                    FRutePlanner.currentRute = 0;
                    /*if (Utils.isNotNull(FApp._theMIDlet.bluetooth)) {
                        switch (FApp._theMIDlet.bluetooth.getState()) {
                            case FBluetooth.STATE_DISABLED_BY_APP_:
                            case FBluetooth.STATE_EXCEPTION_:
                            case FBluetooth.STATE_EXCEPTION_BUSY_:
                            case FBluetooth.STATE_EXCEPTION_NOT_JSR082_SUPPORT_:
                            case FBluetooth.STATE_EXCEPTION_USER_DISABLED_:
                            case FBluetooth.STATE_WAITING_:
                                break;
                            default:
                                FInteractionUtils.retryBluetooth();
                                break;
                        }
                    }*/
                    addScreen(" ", _theMIDlet.getRutePlanner(), STATE_SCREEN_RUTE_PLANNER);
                    break;
                case STATE_SCREEN_TRACEME:
                    addScreen(" ", _theMIDlet.getTraceMe(), STATE_SCREEN_TRACEME);
                    break;
                /*case STATE_LOADING:
                addScreen("Loading...", FApp._theMIDlet.getLoadingScreen(),
                STATE_LOADING);
                break;
                 */
                case STATE_SPLASH_SCREEN:
                    addScreen("Advertisement", _theMIDlet.get3DSplashScreen(),
                            STATE_SPLASH_SCREEN);
                    break;
                case STATE_SCREEN_ABOUT:
                    addScreen("Acerca de ...",_theMIDlet.getAbout(),
                            STATE_SCREEN_ABOUT);
                    break;
                //NACHO
                case STATE_SCREEN_PROPIC:
                    addScreen("Foto!",_theMIDlet.getShowImage(Products.getProductProperty(Integer.parseInt(Products.productId),Products.PIC)),
                            STATE_SCREEN_PROPIC);
                    break;

                default:
                    break;
            }
            //prepare the canvas
            prepareCanvas();
            //recently entered the new state
            switch (_current) {
                case STATE_LOADING:
                    FInteraction.addTimeoutAction(_theMIDlet.aTask, 2000, 4000);
                    break;
            }
  
        } 
        //show it
        Display.getDisplay(_theMIDlet).setCurrent(_theCanvas);
    }

    public FAppInterface newInstance(FMIDlet m, FCanvas c) {
        if (Utils.isNull(FApp._app)) {
            _app = new FAppImpl();
            FApp._theCanvas = c;
            FApp._theMIDlet = m;
        }
        return _app;
    }
    
    /**
     * Process all data before exit this application.
     * Stops threads, clean memory, save user data, close connections, ...
     * Must not call to notifyDestroy or exit
     */
    public void closeClean() {
        try {
            //FBluetooth.disable();
            FInteraction.cancelTask();
            //TODO free more resources and stop anything
        } catch (Throwable ex) {
            //Utils.debugModePrintStack(ex, "MirbluMapsMIDlet", "exit");
        }
    }

}