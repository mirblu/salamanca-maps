package items.image;

import app.FApp;
import items.FSuperItem;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import utils.Utils;

public class FImage extends FSuperItem {

    protected int textColor;
    protected Font textFont;
    private Image img;

    /**
     * Creates a {@link FImage} with one {@link Image} and a text
     * @param imgPath the relative path to the {@link Image}
     * @param title the title of this object
     * @param textFont the {@link Font} to use with the text
     * @param color the color of the text
     */
    public FImage(Image img, String title, Font textFont, int color) {
        super(title);
        this.content = img;
        this.textColor = color;
        this.textFont = textFont;
    }

    public void paint(Graphics g) {
        if (Utils.isNotNull(this.getContent())) {
            int oldColor = g.getColor();
            int currentColor = textColor;
            img = (Image) this.getContent();
            g.setColor(currentColor);
            fstartX = this.x + img.getWidth();
            fstartY = this.y;
            fwidth = FCanvas.canvasWidth;
            //paint text
            if (hasFocus()) {
                //box size
                g.setFont(FCanvas.bigFont);
                this.x = this.x + (FApp.arrowRight.getWidth() << 1);

                fheight = Utils.getWrappedTextWidthHeigh(description, FCanvas.bigFont,
                    this.x + Utils.IMAGE_RECT_OFFSET + (img.getWidth() << 1) - (Utils.IMAGE_RECT_OFFSET << 2),
                    FCanvas.canvasWidth - (Utils.IMAGE_RECT_OFFSET << 1), false, null, y)[1];

                this.x = this.x - (FApp.arrowRight.getWidth() << 1);

                g.fillRoundRect(this.x + Utils.IMAGE_RECT_OFFSET, this.y,
                        g.getClipWidth() - this.x - (Utils.IMAGE_RECT_OFFSET << 1),
                        fheight, Utils.IMAGE_ROUND_RECTANGLE, Utils.IMAGE_ROUND_RECTANGLE);

                currentColor = Utils.COLOR_WHITE;
                g.setColor(currentColor);
                //paint wrapped focus text
                //this.x = this.x + (FApp.arrowRight.getWidth() << 1);
                this.x=5;
                fheight = Utils.getWrappedTextWidthHeigh(description, FCanvas.bigFont,
                    this.x + Utils.IMAGE_RECT_OFFSET + (img.getWidth() << 1) - (Utils.IMAGE_RECT_OFFSET << 2),
                    FCanvas.canvasWidth - (Utils.IMAGE_RECT_OFFSET << 1), true, g, y)[1] + fstartY;
            } else {
                //paint wrapped unfocus text
                g.setFont(textFont);
                fheight = Utils.getWrappedTextWidthHeigh(description, textFont,
                    fstartX + Utils.IMAGE_RECT_OFFSET,
                    FCanvas.canvasWidth - (img.getWidth() << 1), true, g, fstartY)[1] + fstartY;
            }
            this.h = fheight - fstartY;
            g.drawImage(img, this.x, this.y,
                    Graphics.TOP | Graphics.LEFT);
            g.setColor(oldColor);
        }
    }

    public String toString() {
        return this.description;
    }

}
