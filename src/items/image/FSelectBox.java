package items.image;

import utils.Utils;
import action.FInteraction;

import action.FInteractionUtils;
import app.FApp;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 *
 * @author fernando
 */
public class FSelectBox extends FImage {

    private Image imgSelected;
    private Image imgUnselected;
    public static final byte ACTION_CHANGE_IMAGES = 10;

    /**
     * A select text box.
     * @param text the text to show, MUST contain a '|' to divide data
     * @param posToShow the text array position to show text (rest of data is hidden to user)
     * @param textFont
     * @param textColor
     * @param imgPathSelected the image to show when selected is true
     * @param imgPathUnselected the image when unselected
     * @param selected if true shows the selected image
     * @param nextState the next state to load when right or select keys pressed
     */
    public FSelectBox(String text, Font textFont, int textColor,
            Image imgSelected, Image imgUnselected, boolean selected, byte nextState) {
        super(imgSelected, text, textFont, textColor);
        //size
        this.h = textFont.getHeight();
        this.w = textFont.charsWidth(text.toCharArray(), 0, text.length());
        //images
        this.imgSelected = imgSelected;
        this.imgUnselected = imgUnselected;
        if (selected) {
            this.content = this.imgSelected;
        } else if (!selected) {
            this.content = this.imgUnselected;
        }
        //actions
        this.next = nextState;
        interaction = FInteraction.newInstance();
        interaction.addInteraction(Utils.KEY_SELECT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        interaction.addInteraction(Utils.KEY_RIGHT, FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        interaction.addInteraction(Utils.KEY_LEFT, FInteractionUtils.ACTION_GO_BACK);
    }

    public void drawNavigationArrows(Graphics g) {
        if (this.interaction.getAction(Utils.KEY_RIGHT) != -1 || (this.interaction.getAction(Utils.KEY_SELECT) != -1)) {
            g.drawImage(FApp.arrowRight,
                    FCanvas.canvasWidth - FApp.arrowRight.getWidth(),
                    this.y,
                    Graphics.RIGHT | Graphics.TOP);
        }
        if (this.interaction.getAction(Utils.KEY_LEFT) != -1 || (this.interaction.getAction(Utils.KEY_CLEAR) != -1)) {
            g.drawImage(FApp.arrowLeft,
                    0,
                    this.y,
                    Graphics.LEFT | Graphics.TOP);
        }
    }

    public void setFocus(boolean value) {
        super.setFocus(value);
        try {
            if (value) {
                content = Image.createImage(imgSelected);
            } else {
                content = Image.createImage(imgUnselected);
            }
        } catch (Throwable e) {
            //Utils.debugModePrintStack(e, "FSelectBox", "setFocus(" + value + ")");
        }
    }

    public boolean execute(byte action) {
        return super.execute(action);
    }
}