package utils;

import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
import items.image.FImage;
import javax.microedition.lcdui.Image;

/**
 *
 * @author fernando
 */
public class Categories {

    private static final byte NEXT_SCREEN = FApp.STATE_SCREEN_PRODUCTS;
    public static final char CAT_SEPARATOR = '|';
    public static final char PROD_LIST_SEPARATOR = ',';

    public static final byte ID = 0;
    public static final byte NAME = 1;
    public static final byte DESCRIPTION = 2;
    public static final byte ICON = 3;
    public static final byte PROD_LIST = 4;
    public static final byte ATT_SIZE = 5;

    private static int[] ids;
    private static String[] names;
    private static String[] description;
    private static String[] icons;
    public static String[] prodList;

    private static String[] cat;
    private static Image temp;
    private static String s;
    private static FImage ret;
    private static String[] products;

    public static String categoryId;
    public static String categoryName;

    public static Categories c;
    public static int size  = -1;

    public static final Categories newInstance(int size){
        if (size > -1) {
            if (c == null) {
                c = new Categories();
            }
            Categories.size = size;
            Categories.ids = new int[size];
            Categories.names = new String[size];
            Categories.description = new String[size];
            Categories.icons = new String[size];
            Categories.prodList = new String[size];
        }
        return c;
    }

    public static final String[] getCategoryForId(int id){
        int i = 0;
        for (; i < size; i++) {
            if (ids[i] == id) {
                cat = new String[ATT_SIZE];
                cat[ID] = String.valueOf(id);
                cat[NAME] = names[i];
                cat[DESCRIPTION] = description[i];
                cat[ICON] = icons[i];
                cat[PROD_LIST] = prodList[i];
                return cat;
            }
        }
        return null;
    }

    /**
     * Gets a property for one categry inside Categories array
     * @param pos the postition into array
     * @param property the property:  Categories.NAME, Categories.ICON, Categories.ID, Categories.DESCRIPTION. Categories.PROD_LIST
     * @return a String. If data returned is a Integer its parsed into a String. If not found returns null
     */
    public static final String getCategoryProperty(int pos, byte property) {
        switch (property) {
            case Categories.NAME:
                return names[pos];
            case Categories.ICON:
                return icons[pos];
            case Categories.ID:
                return String.valueOf(ids[pos]);
            case Categories.DESCRIPTION:
                return description[pos];
            case Categories.PROD_LIST:
                return prodList[pos];
            default:
                break;
        }
        return null;
    }

    
    public static final FImage toFImage(int pos){
        try {
            if (!icons[pos].equals("")) {
                temp = Image.createImage("/" + icons[pos]);
            } else {
                //if no icon, create the minimal image
                temp = Image.createImage(1, 1);
            }
            ret  = new FImage(temp, names[pos],
                    FApp._theMIDlet.mainTextFont,
                    FApp._theMIDlet.mainTextColor);
            //store the position
            ret.id = String.valueOf(pos);
            ret.next = NEXT_SCREEN;
            //add user interaction
            ret.interaction = FInteraction.newInstance();
            ret.interaction.addInteraction(Utils.KEY_SELECT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
            ret.interaction.addInteraction(Utils.KEY_RIGHT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
            ret.interaction.addInteraction(Utils.KEY_LEFT, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
            ret.interaction.addInteraction(Utils.KEY_CLEAR, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
        } catch (Throwable ex) {
            Utils.debugModePrintStack(ex, "Categories", "toFImage");
        } finally {
            temp = null;
        }
        return ret;
    }

    public static final void addCategory(int id, String name, String descrip, String icon, String products, int pos) {
        ids[pos] = id;
        names[pos] = name;
        if (Utils.isNotNull(descrip)) {
            description[pos] = descrip;
        } else {
            description[pos] = null;
        }
        if (Utils.isNotNull(icon)) {
            icons[pos] = icon;
        } else {
            icons[pos] = null;
        }
        if (Utils.isNotNull(products)) {
            prodList[pos] = products;
        } else {
            prodList[pos] = "";
        }
    }

    /**
     *
     * @param data String[] = {name, minId, maxId, icon}
     * @param pos
     */
    public static final void addCategory(String[] data, int pos) {
        /*
        System.out.println();
        System.out.println("    CATEGORY " + pos);
        System.out.println();
        System.out.println("id = " + data[ID]);
        System.out.println("name = " + data[NAME]);
        System.out.println("description = " + data[DESCRIPTION]);
        System.out.println("icon = " + data[ICON]);
        System.out.println("products = " + data[PROD_LIST]);
         */
        addCategory(Integer.parseInt(data[ID]),
                data[NAME],
                data[DESCRIPTION],
                data[ICON],
                data[PROD_LIST],
                pos);
    }

    /**
     * Gets all products ids for one category
     * @param catId the identifier of the category
     * @return a String[] of products ids attached to one category or null if the
     * caegory has no attached products.
     */
    public static final int[] getProductsIds(int catId) {
        s = getCategoryProperty(catId, PROD_LIST);
        if (Utils.isNull(s) || s.equals("")) {
            return null;
        }
        int prodSize = Utils.count(s, PROD_LIST_SEPARATOR) + 1;
        products = Utils.split(s, PROD_LIST_SEPARATOR, prodSize);
        s = null;
        int[] ret = new int[prodSize];
        int i = 0;
        for (; i < prodSize; i++) {
            ret[i] = Integer.parseInt(products[i]);
        }
        return ret;
    }

}
