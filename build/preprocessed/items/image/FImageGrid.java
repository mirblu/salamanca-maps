package items.image;

import action.FInteraction;
import action.FInteractionUtils;
import app.FApp;
import items.FSuperItem;
import utils.Utils;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * create a real-time grid image from a tiled image
 * @author fernando
 */
public class FImageGrid extends FSuperItem {

    private static final byte IMAGEGRID_LIMIT_SIZE = 10;
    private static final byte IMAGEGRID_FILE_ARRAY_SIZE = 4;
    private static final byte IMAGEGRID_MAP_FOLDER_POS = 0;
    private static final byte IMAGEGRID_IMAGE_WIDTH_POS = 1;
    private static final byte IMAGEGRID_IMAGE_HEIGHT_POS = 2;
    private static final byte IMAGEGRID_IMAGE_MATRIX_POS = 3;
    private static final char IMAGEGRID_IMAGE_MATRIX_SEPARATOR = ' ';
    //start position
    public static final byte IMAGEGRID_START_CUSTOM = 11;
    public static final byte IMAGEGRID_START_CENTERED = 12;
    public static final byte IMAGEGRID_START_TOP_LEFT = 13;
    public static final byte IMAGEGRID_START_BOTTOM_LEFT = 14;
    public static final byte IMAGEGRID_START_TOP_RIGHT = 15;
    public static final byte IMAGEGRID_START_BOTTOM_RIGHT = 16;
    private static StringBuffer auxsb;
    private static String mapFolder;
    private static int[][] matrix;
    /**the map pixel coordinates*/
    public static int currMatrixX = 0, currMatrixY = 0;
    /**the matrix map coordinates*/
    private static int currTileX = 0, currTileY = 0;
    /**the canvas offset for current tile*/
    private static int currOffsetX = 0, currOffsetY = 0;
    public static int scrollIncrement = 20;
    private static boolean scroll;
    private static int mapWidth, mapHeight, nTilesHeight, nTilesWidth, tileH, tileW;
    /**bigger pool if bigger screen*/
    private static int poolWidth = 0, poolHeight = 0;
    /**has the loaded images, it's size is poolHeigth, poolWidth*/
    private static Image[][] poolImg;
    /** same size than poolimg but just with the index of the loaded image*/
    private static int[][] poolIdx;

    /**
     * A tiled layer image. It's a image divided in tiles that controls key
     * interaction, scroll and dynamic image loading
     * @param title the title for this element
     * @param indexFilePath the path to the index text file formated as follow:
     * <br> One line for each tiled image.
     * <br><code>0|1024|640|0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39</code>
     * <br>Fields are:
     * <br><code>image index|full image width|full image height|matrix of each tile image name</code>
     * @param imgFolder the folder that has all the tiles to create the image
     * @param tileWidth the width of one tile
     * @param tileHeight the height of one tile
     * @param startX pixel width to start load. from the full image. If is
     * bigger than full image size it is set to full image width.
     * @param startY pixel height to start load. from the full image.
     * If is bigger than full image size it is set to full image height.
     * @param startPos a constant indicating the starting position:<br>
     * IMAGEGRID_START_CUSTOM needed to use startX && startY parameters, will center that point on the screen. <br>
     * IMAGEGRID_START_CENTERED center the map on the screen<br>
     * IMAGEGRID_START_TOP_LEFT<br>
     * IMAGEGRID_START_BOTTOM_LEFT <br>
     * IMAGEGRID_START_TOP_RIGHT<br>
     * IMAGEGRID_START_BOTTOM_RIGHT<br>
     * @param scrollInc the increment for user key scroll
     * <br><br>
     * Example usage: <br>
     * <code>
     * FSuperItem grid = new FImageGrid(" ", "/maps/index", mapPos,
    128, 128, -1, -1, FImageGrid.IMAGEGRID_START_CENTERED, 20);
     * </code>
     */
    public FImageGrid(String title, String indexFilePath, int imgFolder,
            int tileWidth, int tileHeight,
            int startX, int startY, byte startPos, int scrollInc) {
        super(title);
        init(indexFilePath, imgFolder, tileWidth, tileHeight,
        		startX, startY, startPos, scrollInc);
    }

    private void init(String indexFilePath, int imgFolder,
			int tileWidth, int tileHeight, int startX, int startY,
			byte startPos, int scrollInc) {
    	currMatrixX = 0;
        currMatrixY = 0;
        currTileX = 0;
        currTileY = 0;
        currOffsetX = 0;
        currOffsetY = 0;
        scrollIncrement = scrollInc;
        poolHeight = 0;
        poolWidth = 0;

        loadImageTiles(indexFilePath, imgFolder, tileWidth, tileHeight);

        auxsb = new StringBuffer();
        //calculate the pool size
        setPoolSize(tileHeight, tileWidth);
        poolImg = new Image[poolHeight][poolWidth];
        //initialize indexes to useless value
        poolIdx = new int[poolHeight][poolWidth];
        initIndexes();
        //calculate start tile and start pixel offset
        switch (startPos) {
            case IMAGEGRID_START_CENTERED:
                currMatrixX = -(mapWidth - FCanvas.canvasWidth) >> 1;
                currMatrixY = -(mapHeight - FCanvas.canvasHeight) >> 1;
                break;
            case IMAGEGRID_START_BOTTOM_LEFT:
                currMatrixX = 0;
                currMatrixY = -mapHeight + FCanvas.canvasHeight;
                break;
            case IMAGEGRID_START_BOTTOM_RIGHT:
                currMatrixX = -mapWidth + FCanvas.canvasWidth;
                currMatrixY = -mapHeight + FCanvas.canvasHeight;
                break;
            case IMAGEGRID_START_TOP_LEFT:
                currMatrixX = 0;
                currMatrixY = 0;
                break;
            case IMAGEGRID_START_TOP_RIGHT:
                currMatrixX = -mapWidth + FCanvas.canvasWidth;
                currMatrixY = 0;
                break;
            case IMAGEGRID_START_CUSTOM:
            	default:
                currMatrixX = -startX + (FCanvas.canvasWidth >> 1);
                currMatrixY = -startY + (FCanvas.canvasHeight >> 1);
                break;
        }
        //Utils.debugMode("started at: " + currMatrixX + "," + currMatrixY,
        //         "FImageGrid", "constructor", Utils.DEBUG_INFO);
        setCoords(0, 0);
        loadPool();

        this.interaction = FInteraction.newInstance();
        //add the scroll actions
        if (FApp._theCanvas.getHeight() < mapHeight || FApp._theCanvas.getWidth() < mapWidth) {
            scroll = true;
            //X scroll
            this.interaction.addInteraction(Utils.KEY_LEFT, FInteractionUtils.ACTION_SCROLL_LEFT);
            this.interaction.addInteraction(Utils.KEY_RIGHT, FInteractionUtils.ACTION_SCROLL_RIGHT);
            this.interaction.addInteraction(FCanvas.KEY_NUM4, FInteractionUtils.ACTION_SCROLL_LEFT);
            this.interaction.addInteraction(FCanvas.KEY_NUM6, FInteractionUtils.ACTION_SCROLL_RIGHT);
            //Y scroll
            this.interaction.addInteraction(Utils.KEY_UP, FInteractionUtils.ACTION_SCROLL_UP);
            this.interaction.addInteraction(Utils.KEY_DOWN, FInteractionUtils.ACTION_SCROLL_DOWN);
            this.interaction.addInteraction(FCanvas.KEY_NUM2, FInteractionUtils.ACTION_SCROLL_UP);
            this.interaction.addInteraction(FCanvas.KEY_NUM8, FInteractionUtils.ACTION_SCROLL_DOWN);
            //diagonals
            this.interaction.addInteraction(FCanvas.KEY_NUM1, FInteractionUtils.ACTION_SCROLL_UP_LEFT);
            this.interaction.addInteraction(FCanvas.KEY_NUM3, FInteractionUtils.ACTION_SCROLL_UP_RIGHT);
            this.interaction.addInteraction(FCanvas.KEY_NUM7, FInteractionUtils.ACTION_SCROLL_DOWN_LEFT);
            this.interaction.addInteraction(FCanvas.KEY_NUM9, FInteractionUtils.ACTION_SCROLL_DOWN_RIGHT);
        }
		
	}

	public FImageGrid(String title) {
        super(title);
    }

    /**
     * moves the image to focus on some pixel and repaints
     * @param x
     * @param y
     */
    public static void centerMapTo(int x, int y){
        currMatrixX = -x + (FCanvas.canvasWidth >> 1);
        currMatrixY = -y + (FCanvas.canvasHeight >> 1);
        setCoords(0, 0);
        loadPool();
        Utils.callRepaint();
    }

    /**
     * paint starts painting the (0,0) image from the pool
     * must paint different for each of the canvasStates
     * @param g the Graphics
     */
    public void paint(Graphics g) {
        int i = 0;
        int j;
        //System.out.println("pool " + poolHeight + "x" + poolWidth);
        int tempx = currOffsetX, tempy = currOffsetY;
        for (; i < poolHeight; i++) {
            for (j = 0; j < poolWidth; j++) {
                //System.out.println("painting... pool[" + i + "," + j + "] at canvas " + tempx + "," + tempy);
                g.drawImage(poolImg[i][j], tempx, tempy, Graphics.LEFT | Graphics.TOP);
                //g.drawRect(tempx, tempy, tileW, tileH);//img border
                //g.drawString(i + "," + j, tempx, tempy, Graphics.LEFT | Graphics.TOP); //image pool pos
                tempx += tileW;
                if (tempx >= FCanvas.canvasWidth) {
                    break;
                }
            }
            tempx = currOffsetX;
            tempy += tileH;
            if (tempy >= FCanvas.canvasHeight) {
                break;
            }
        }
        //limits
        g.setColor(Utils.COLOR_TANGO_SCARLETRED1);
        if (-currMatrixX <= IMAGEGRID_LIMIT_SIZE) {
            g.fillRect(0, 0, IMAGEGRID_LIMIT_SIZE, FCanvas.canvasHeight);
        }
        if (-currMatrixX >= (mapWidth - FCanvas.canvasWidth) - IMAGEGRID_LIMIT_SIZE) {
            g.fillRect(
                    FCanvas.canvasWidth - IMAGEGRID_LIMIT_SIZE, 0,
                    IMAGEGRID_LIMIT_SIZE, FCanvas.canvasHeight);
        }
        if (-currMatrixY <= IMAGEGRID_LIMIT_SIZE) {
            g.fillRect(0, 0, FCanvas.canvasWidth, IMAGEGRID_LIMIT_SIZE);
        }
        if (-currMatrixY >= (mapHeight - FCanvas.canvasHeight) - IMAGEGRID_LIMIT_SIZE) {
            g.fillRect(0,
                    FCanvas.canvasHeight - IMAGEGRID_LIMIT_SIZE,
                    FCanvas.canvasWidth, IMAGEGRID_LIMIT_SIZE);
        }
    }

    public boolean execute(byte action) {
        boolean ret = false;
        switch (action) {
            case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                FInteractionUtils.goBack();
                ret = true;
                break;
        }
        if (scroll) {
            switch (action) {
                case FInteractionUtils.ACTION_SCROLL_LEFT:
                    setCoords(scrollIncrement, 0);
                    break;
                case FInteractionUtils.ACTION_SCROLL_RIGHT:
                    setCoords(-scrollIncrement, 0);
                    break;
                case FInteractionUtils.ACTION_SCROLL_UP:
                    setCoords(0, scrollIncrement);
                    break;
                case FInteractionUtils.ACTION_SCROLL_DOWN:
                    setCoords(0, -scrollIncrement);
                    break;
                case FInteractionUtils.ACTION_SCROLL_UP_LEFT:
                    setCoords(scrollIncrement, scrollIncrement);
                    break;
                case FInteractionUtils.ACTION_SCROLL_UP_RIGHT:
                    setCoords(-scrollIncrement, scrollIncrement);
                    break;
                case FInteractionUtils.ACTION_SCROLL_DOWN_LEFT:
                    setCoords(scrollIncrement, -scrollIncrement);
                    break;
                case FInteractionUtils.ACTION_SCROLL_DOWN_RIGHT:
                    setCoords(-scrollIncrement, -scrollIncrement);
                    break;
                case FInteractionUtils.ACTION_FOCUS_TO_HOVER_MENU:
                	FInteractionUtils.setFocusToHoverMenu();
                	break;
                default:
                    //setCoords(0, 0);
                    break;
            }
            //Utils.debugMode("executing an action", "FScrollableImage", "execute(" + action + ")", Utils.DEBUG_INFO);
            loadPool();
            Utils.callRepaint();
        } else {
            //if not scroll, canvas controls action received
            ret = FApp._theCanvas.execute(action);
        }
        return ret;
    }

    /**
     * Changes the current matrix x and y.
     * @param xInc the increment
     * @param yInc the increment
     */
    protected static void setCoords(int xInc, int yInc) {
        currMatrixX += xInc;
        currMatrixY += yInc;
        if (currMatrixX > 0) {
            currMatrixX = 0;
        } else if (-currMatrixX > mapWidth - FCanvas.canvasWidth) {
            currMatrixX = -mapWidth + FCanvas.canvasWidth;
        }
        if (currMatrixY > 0) {
            currMatrixY = 0;
        } else if (-currMatrixY > mapHeight - FCanvas.canvasHeight) {
            currMatrixY = -mapHeight + FCanvas.canvasHeight;
        }
    }

    private static void getMatrixValuesFrom(String matrixString) {
        String[] temp = Utils.split(matrixString,
                IMAGEGRID_IMAGE_MATRIX_SEPARATOR, nTilesHeight * nTilesWidth);
        int i = 0;
        int j;
        int pos;
        for (; i < nTilesHeight; i++) {
            for (j = 0; j < nTilesWidth; j++) {
                pos = j + (i * nTilesWidth);
                matrix[i][j] = Byte.parseByte(temp[pos]);
            }
        }
        temp = null;
    }

    /**
     * Calculates the canvas area we are working for different loading strategies
     */
    private final static void loadPool() {
        setTileAndOffsetX();
        setTileAndOffsetY();
        /*
        System.out.println("--------------------------");
        System.out.println("tile   " + currTileX + "," + currTileY);
        System.out.println("offset " + currOffsetX + "," + currOffsetY);
        System.out.println("matrix " + currMatrixX + "," + currMatrixY);
        System.out.println("--------------------------");
         */
        loadImages(currTileX, currTileY);
        System.gc();

    }

    /**
     * loads all the images needed starting at some value.
     * <br>Controls what images we need based on current position, canvas size
     * and map size.
     * @param jStart the first tile height coord to load, <= nTilesWidth
     * @param iStart the first tile width coord to load, <= nTilesheight
     */
    private final static void loadImages(int jStart, int iStart) {
        int i;
        int j;
        int pooly = 0;
        int poolx = 0;
        //calc the real first tile to load
        int inii = iStart, inij = jStart;
        for (i = inii; i < inii + poolHeight; i++) {
            for (j = inij; j < inij + poolWidth; j++) {
                try {
                    //skip if loaded at same position
                    if (poolIdx[pooly][poolx] != matrix[i][j]) {
                        poolImg[pooly][poolx] = loadImage(i, j);
                        poolIdx[pooly][poolx] = matrix[i][j];
                    }
                } catch (IOException ex) {
                    //Utils.debugModePrintStack(ex, "FScrollableImageGrid", "loadImages");
                }
                poolx++;
            }
            pooly++;
            poolx = 0;
        }
    }

    private synchronized static Image loadImage(int i, int j) throws IOException {
        auxsb.delete(0, auxsb.length());
        auxsb.append(mapFolder);
        auxsb.append("/");
        auxsb.append(String.valueOf(matrix[i][j]));
        auxsb.append(".png");
        return Image.createImage(auxsb.toString());
    }

    private static void setTileAndOffsetX() {
        //origin
        if (-currMatrixX <= 0) {
            currMatrixX = 0;
            currTileX = 0;
            currOffsetX = 0;
            //medium
        } else if (-currMatrixX > 0 && -currMatrixX <= (mapWidth - FCanvas.canvasWidth)) {
            currTileX = -currMatrixX / tileW;
            //limits
            if (currTileX > (nTilesWidth - poolWidth) && currTileX <= (nTilesWidth)) {
                if (nTilesWidth - poolWidth < 0) {
                    currTileX = poolWidth - nTilesWidth;
                } else {
                    currTileX = nTilesWidth - poolWidth;
                }
                currOffsetX = currMatrixX - (tileW * (int) (currMatrixX / tileW)) - tileW;//TODO must be related with canvas
                return;
            } else {
                currOffsetX = currMatrixX - (tileW * (int) (currMatrixX / tileW));
            }
        }
    }

    private static void setTileAndOffsetY() {
        //origin
        if (-currMatrixY <= 0) {
            currMatrixY = 0;
            currTileY = 0;
            currOffsetY = 0;
            //medium
        } else if (-currMatrixY > 0 && -currMatrixY <= (mapHeight - FCanvas.canvasHeight)) {
            currTileY = -currMatrixY / tileH;
            //limits
            if (currTileY > (nTilesHeight - poolHeight) && currTileY <= (nTilesHeight)) {
                if (nTilesHeight - poolHeight < 0) {
                    currTileY = poolHeight - nTilesHeight;
                } else {
                    currTileY = nTilesHeight - poolHeight;
                }
                currOffsetY = currMatrixY - (tileH * (int) (currMatrixY / tileH)) - tileH;//TODO must be related with canvas
                return;
            } else {
                currOffsetY = currMatrixY - (tileH * (int) (currMatrixY / tileH));
            }
        }
    }

    /**
     * Used to initialize indexes matrix to useless value (-1)
     */
    private void initIndexes() {
        int i = 0;
        int j;
        for (; i < poolHeight; i++) {
            for (j = 0; j < poolWidth; j++) {
                poolIdx[i][j] = -1;
            }
        }
    }

    /**
     * Calculate the needed tiles size
     * @param indexFilePath the path to image matrix file
     * @param imgFolder the image folder with all the tiles
     * @param tileWidth sets the expected tile width
     * @param tileHeight sets the expected tile height
     */
    public void loadImageTiles(String indexFilePath, int imgFolder, int tileWidth, int tileHeight) {
        String line = (String) new Utils().readFile(
                indexFilePath.trim()).elementAt(imgFolder);
        String[] data = Utils.split(line,
        		Utils.HOTSPOTS_FILE_DIVIDE_CHAR,
                IMAGEGRID_FILE_ARRAY_SIZE);

        mapWidth = Integer.valueOf(data[IMAGEGRID_IMAGE_WIDTH_POS]).intValue();
        mapHeight = Integer.valueOf(data[IMAGEGRID_IMAGE_HEIGHT_POS]).intValue();
        nTilesHeight = mapHeight / tileHeight;
        nTilesWidth = mapWidth / tileWidth;
        tileH = tileHeight;
        tileW = tileWidth;

        matrix = new int[nTilesHeight][nTilesWidth];
        getMatrixValuesFrom(data[IMAGEGRID_IMAGE_MATRIX_POS]);
        mapFolder = "/maps/" + data[IMAGEGRID_MAP_FOLDER_POS];

        line = null;
        data = null;

    }

    private void setPoolSize(int tileHeight, int tileWidth) {
        int size = FCanvas.canvasHeight / tileHeight;
        while ((size * tileHeight) - FCanvas.canvasHeight < tileHeight) {
            size++;
        }
        //poolheight
        if (size > nTilesHeight) {
            size = nTilesHeight;
        }
        if (FImageGrid.poolHeight < size) {
            FImageGrid.poolHeight = size;
        }

        size = FCanvas.canvasWidth / tileWidth;
        while ((size * tileWidth) - FCanvas.canvasWidth < tileWidth) {
            size++;
        }
        //poolWidth
        if (size > nTilesWidth) {
            size = nTilesWidth;
        }
        if (FImageGrid.poolWidth < size) {
            FImageGrid.poolWidth = size;
        }
    }
}
