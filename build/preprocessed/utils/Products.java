package utils;

import a.MirbluMapsMIDlet;
import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
import items.FSuperItem;
import items.image.FCanvas;
import items.image.FImage;
import items.image.FSplashScreen;
import items.text.FText;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 *
 * @author fernando
 */
public class Products {

    public static byte NEXT_SCREEN_SIN_PIC = FApp.STATE_SCREEN_PRODUCTS_SPLASH;
    public static byte NEXT_SCREEN_CON_PIC = FApp.STATE_SCREEN_PROPIC;
    private static final char NEW_LINE_CHAR = '^';  
    public static final char PROD_SEPARATOR = '|';
    public static final char URI_SEPARATOR = ' ';

    public static final byte ID = 0;
    public static final byte NAME = 1;
    public static final byte TITLE = 2;
    public static final byte DESCRIPTION = 3;
    public static final byte PIC = 4;
    public static final byte ICON = 5;
    public static final byte URI = 6;
    public static final byte HOTSPOT_ID = 7;
    public static final byte ATT_SIZE = 8;

    public static String productId;
    public static String productName;    

    private static String[] prod;
    private static Vector tot;
    private static Image img;

    private static int[] ids;
    private static String[] names;
    private static String[] titles;
    private static String[] descriptions;
    private static String[] pics; //NACHO almacena las rutas de la ruta de las imagenes
    private static String[] icons;
    private static String[] uris;
    private static int[] hotspotIds;

    private static Products p;
    private static FImage ret;
    private static FSplashScreen splash;

    public static int size = -1;

    /**
     * Initialize all products data to size
     * @param size the size of Vectors used
     * @return a empty instance of Products
     */
    public static final Products newInstance(int size) {
        if (p == null) {
            p = new Products();
        }
        Products.size = size;
        Products.ids = new int[size];
        Products.names = new String[size];
        Products.titles = new String[size];
        Products.descriptions = new String[size];
        Products.pics = new String[size];
        Products.icons = new String[size];
        Products.uris = new String[size];
        Products.hotspotIds = new int[size];
        return p;
    }

    /**
     * Adds a product to the Products
     * @param data the data arrat ina  constant order
     * @param pos
     */
    public static void addProduct(String[] data, int pos){
        ids[pos] = Integer.valueOf(data[ID]).intValue();
        names[pos] = data[NAME];
        titles[pos] = data[TITLE];
        descriptions[pos] = data[DESCRIPTION];
        pics[pos] = data[PIC];
        icons[pos] = data[ICON];
        uris[pos] = data[URI];
        hotspotIds[pos] = Integer.valueOf(data[HOTSPOT_ID].trim()).intValue();
    }

    /**
     * Show a product at console and checks some format
     * @param data the data array in constant order
     * @param pos the pos where it is going to be added
     */
    public static void showProduct(String[] data, int pos){
        System.out.println();
        System.out.println("    PRODUCT " + pos);
        System.out.println();
        System.out.println("id = " + data[ID]);
        ids[pos] = Integer.valueOf(data[ID]).intValue();
        System.out.println("name = " + data[NAME]);
        names[pos] = data[NAME];
        System.out.println("title = " + data[TITLE]);
        titles[pos] = data[TITLE];
        System.out.println("descrip = ");
        String[] des = Utils.split(data[DESCRIPTION], '^', Utils.count(data[DESCRIPTION], '^') + 1);
        for (int i = 0; i < des.length; i++) {
            System.out.println("    " + des[i]);
        }
        descriptions[pos] = data[DESCRIPTION];
        System.out.println("PIC = " + data[PIC]);
        pics[pos] = data[PIC];
        System.out.println("icon = " + data[ICON]);
        icons[pos] = data[ICON];
        
        if (data[URI].equals("") ||data[URI].startsWith("tel:")
                || data[URI].startsWith("fax:") || data[URI].startsWith("http://")) {
            System.out.println("uri = " + data[URI]);
            uris[pos] = data[URI];
        } else {
            System.out.println("malformed uri: " + data[URI]);
        }
        System.out.println("hotspot = " + data[HOTSPOT_ID]);
        //hotspotIds[pos] = Integer.valueOf(data[HOTSPOT_ID].trim()).intValue();
        System.out.println();
    }

    public static Vector getProductsForIds(int[] idArray) {
        Vector ret = new Vector(idArray.length);
        int j, i = 0;
        for (; i < ids.length; i++) {
            j = 0;
            for (; j < idArray.length; j++) {
                if (ids[i] == idArray[j]) {
                    ret.addElement(getProduct(idArray[j]));
                }
            }
        }
        return ret;
    }

    public static String[] getProduct(int id) {
        prod = new String[ATT_SIZE];
        prod[ID] = String.valueOf(ids[id]);
        prod[NAME] = names[id];
        prod[TITLE] = titles[id];
        prod[DESCRIPTION] = descriptions[id];
        prod[PIC] = pics[id];
        prod[ICON] = icons[id];
        prod[URI] = uris[id];
        prod[HOTSPOT_ID] = String.valueOf(hotspotIds[id]);
        return prod;
    }

    public static final String getProductProperty(int pos, byte property) {
        switch (property) {
            case Products.ID:
                return String.valueOf(ids[pos]);
            case Products.NAME:
                return names[pos];
            case Products.TITLE:
                return titles[pos];
            case Products.DESCRIPTION:
                return descriptions[pos];
            case Products.PIC:
                return pics[pos];
            case Products.ICON:
                return icons[pos];
            case Products.URI:
                return uris[pos];
            case Products.HOTSPOT_ID:
                return String.valueOf(hotspotIds[pos]);
                default:
                    return null;
        }
    }

    /**
     *
     * @param id
     * @return
     * @deprecated
     */
    public static final int getPosFromId(int id){
        int i = 0;
        for (; i < ids.length; i++) {
            if (ids[i] == id)
                return i;
        }
        return -1;
    }

    public static final Vector uriToMenu(int pos){
        Vector ret = new Vector(1,1);
        FSuperItem fi = null;
        int urisize = Utils.count(uris[pos], URI_SEPARATOR);
        String[] urisDiv = new String[urisize + 1];
        urisDiv = Utils.split(uris[pos], URI_SEPARATOR, urisize + 1);
        if (urisDiv.length < 0) {
            fi = new FImage(FApp._theMIDlet.exitImg,
                    "No se encontró información de contacto",
                    FApp._theMIDlet.mainTextFont, FApp._theMIDlet.mainTextColor);
            fi.interaction = FInteraction.newInstance();
            fi.interaction.addAnyKeyAction(FInteractionUtils.ACTION_GO_BACK);
            ret.addElement(fi);
            return ret;
        }
        int i = 0;
        Image img = null;
        for (; i < urisize + 1; i++) {
            if (urisDiv[i].startsWith("tel:")) {
                img = FApp._theMIDlet.aboutImg;
            } else if (urisDiv[i].startsWith("fax:")) {
                img = FApp._theMIDlet.floorImageImg;
            } else if (urisDiv[i].startsWith("http:")) {
                img = FApp._theMIDlet.exploreImg;
            }
            //image item
            fi = new FImage(img, urisDiv[i], FApp._theMIDlet.mainTextFont, FApp._theMIDlet.mainTextColor);
            fi.next = FApp.STATE_SCREEN_PRODUCTS_SPLASH;
            fi.interaction = FInteraction.newInstance();
            fi.interaction.addInteraction(Utils.KEY_SELECT, FInteractionUtils.ACTION_PLATFORM_REQUEST);
            fi.interaction.addInteraction(Utils.KEY_LEFT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
            fi.interaction.addInteraction(Utils.KEY_CLEAR, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
            ret.addElement(fi);
        }
        fi = new FImage(FApp._theMIDlet.exitImg, "Volver", FApp._theMIDlet.mainTextFont, FApp._theMIDlet.mainTextColor);
        fi.next = FApp.STATE_SCREEN_PRODUCTS_SPLASH;
        fi.interaction = FInteraction.newInstance();
        fi.interaction.addInteraction(Utils.KEY_SELECT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        ret.addElement(fi);
        return ret;
    }

    /**
     * Converts a Product in a FImage
     * @param pos the position of the product
     * @return a new FImage
     */
    public static final FImage toFImage(int pos, Image img){
        ret  = new FImage(img, names[pos],
                FApp._theMIDlet.mainTextFont,
                FApp._theMIDlet.mainTextColor);
        //store the position
        ret.id = String.valueOf(pos);
        if (getProductProperty(pos, PIC).equals("")){
            ret.next = NEXT_SCREEN_SIN_PIC;
        }else ret.next = NEXT_SCREEN_CON_PIC;
        ret.interaction = FInteraction.newInstance();
        ret.interaction.addInteraction(Utils.KEY_SELECT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        ret.interaction.addInteraction(Utils.KEY_RIGHT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        ret.interaction.addInteraction(Utils.KEY_LEFT, FInteractionUtils.ACTION_GO_BACK);
        ret.interaction.addInteraction(Utils.KEY_CLEAR, FInteractionUtils.ACTION_GO_BACK);
        ret.interaction.addInteraction(Utils.KEY_EXIT, FInteractionUtils.ACTION_GO_BACK);
        return ret;
    }

    
    public static final FImage toFImage(int pos, String imagePath) {
        ret = null;
        try {
            if (imagePath.equals("")) {
                img = Image.createImage(1,1);
            } else {
                img = Image.createImage(imagePath);
            }
            ret = toFImage(pos, img);
        } catch (Throwable ex) {
            Utils.debugModePrintStack(ex, "Products", "toFImage");
        } finally {
            img = null;
        }
        return ret;
    }

    public static final FImage toFImage(String id, String imagePath){
        return toFImage(new Integer(Integer.parseInt(id)).intValue() , imagePath);
    }

    /**
     * Converts a Products into a new advertisement
     * @param pos the pos of the product
     * @return a new FSplashScreen
     */
    public static final Vector toAdvertisement(int pos) {
        Vector ret = null;
        try {
            ret = new Vector(1, 1);
            if (Utils.isNotNull(icons[pos]) && !icons[pos].equals("")) {
                img = Image.createImage(icons[pos]);
            } else {
                img = FApp._theMIDlet.aboutImg;
            }
            String description;
            if (Utils.isNotNull(titles[pos]) && !titles[pos].equals("")) {
                description = titles[pos];
            } else {
                description = "";
            }

            //
            FSuperItem fi = new FText(descriptions[pos],
                 "description", FApp._theMIDlet.mainTextColor, FApp._theMIDlet.mainBackgroundColor,
                 FApp._theMIDlet.mainTextFont, 3, FApp._theCanvas.starty,//x, y
                 FCanvas.canvasWidth, FCanvas.canvasHeight, 0);//w, h
            fi.interaction = FInteraction.newInstance();
            fi.interaction.addInteraction(Utils.KEY_DOWN, FInteractionUtils.ACTION_SCROLL_UP);
            fi.interaction.addInteraction(Utils.KEY_UP, FInteractionUtils.ACTION_SCROLL_DOWN);
            FCanvas.hasTextAt = 0;
            ret.addElement(fi);

            FApp._theCanvas.hoverMenuAt = (byte) (1);
            FSuperItem menu = MirbluMapsMIDlet.getHoverMenu();
            ret.addElement(menu);

        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            img = null;
        }
        return ret;
    }

}
