/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package items.image;

import items.FSuperItem;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import utils.Utils;
/**
 *
 * @author Administrator
 */
public class NImage extends FSuperItem{

    protected int textColor;
    protected Font textFont;
    private Image img;


    public NImage(Image img, String title, Font textFont, int color, int posx, int posy) {
        super(title);
        this.content = img;
        this.textColor = color;
        this.textFont = textFont;
        this.x =  posx;
        this.y = posy;

    }

    public void paint(Graphics g) {
        if (Utils.isNotNull(this.getContent())) {
            int oldColor = g.getColor();
            int currentColor = textColor;
            img = (Image) this.getContent();
            g.setColor(currentColor);
          
            if (hasFocus()) {
                int offsetRec = 5;
                g.fillRoundRect(this.x-offsetRec , this.y-offsetRec, this.img.getWidth()+offsetRec*2,
                        this.img.getHeight()+offsetRec*2,
                        Utils.IMAGE_ROUND_RECTANGLE, Utils.IMAGE_ROUND_RECTANGLE);
                g.drawImage(img, this.x, this.y, anchor);
            }else{
                g.drawImage(img, this.x, this.y, anchor);
            }
        }
    }



}
