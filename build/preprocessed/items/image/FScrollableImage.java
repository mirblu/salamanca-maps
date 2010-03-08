package items.image;

import action.FInteraction;
import action.FInteractionUtils;
import items.FSuperItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import utils.Utils;

public class FScrollableImage extends FSuperItem {


    private int scrollIncrement = 10;
    protected int textColor;
    //private Image theImage;
    private int widthMax;
    private int heightMax;
    private boolean scrollInUse = false;
    private int currX = 0;
    private int currY = 0;
    //public int endX = 0;
    //public int endY = 0;
    //private TimerTask repaintTask;

    /**
     *
     * @param img the image to show
     * @param title @deprecated use " "
     * @param startX the x start coord, usually zero
     * @param startY the y start coord, usually zero
     * @param scrollIncrement the scroll increment, scroll is apply on user key press
     * and only if canvas size is less than image size
     */
    public FScrollableImage(Image img, String title,
            int startX, int startY, int scrollIncrement) {
        super(title);
        this.content = img;
        //set sizes
        widthMax = img.getWidth();
        heightMax = img.getHeight();

        //NACHO
        //He comentado las lineas con menos por que no me dejaban
        //poner una imgen donde yo quería
        if (startX > 0) {
            //currX = -startX;
            currX = startX;
        } else {
            currX = startX;

        }
        if (startY > 0) {
            //currY = -startY;
            currY = startY;
        } else {
            currY = startY;
        }
        this.anchor = Graphics.LEFT | Graphics.TOP;
        //add the back action
        this.interaction = FInteraction.newInstance();
        interaction.addInteraction(Utils.KEY_CLEAR, FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
        if (FCanvas.canvasWidth < widthMax || FCanvas.canvasHeight < heightMax) {
            scrollInUse = true;
            this.scrollIncrement = scrollIncrement;
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

    public void paint(Graphics g) {
        g.drawImage((Image)content, currX, currY, anchor);
    }

    public String toString() {
        return this.description;
    }

    public boolean execute(byte action) {
        boolean ret = false;
        switch (action) {
            case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                FInteractionUtils.goBack();
                ret = true;
                break;
            case FInteractionUtils.ACTION_LOAD_MAIN_MENU:
                FInteractionUtils.changeToMainMenu();
                ret = true;
                break;
        }
        if (scrollInUse) {
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
                default:
                    break;
            }
            //Utils.debugMode(currX + " , " + currY, "FScrollableImage", "execute(" + action + ")", Utils.DEBUG_INFO);
            Utils.callRepaint();
        } else {
            //if not scroll, canvas controls action received
            ret = true;
        }
        return ret;
    }

    private void setCoords(int xInc, int yInc) {
        currX += xInc;
        currY += yInc;
        if (currX > 0) {
            currX = 0;
        } else if (-currX > widthMax - FCanvas.canvasWidth) {
            currX = -widthMax + FCanvas.canvasWidth;
        }
        if (currY > 0) {
            currY = 0;
        } else if (-currY > heightMax - FCanvas.canvasHeight) {
            currY = -heightMax + FCanvas.canvasHeight;
        }
    }
}
