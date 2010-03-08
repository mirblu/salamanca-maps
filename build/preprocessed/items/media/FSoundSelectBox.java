/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package items.media;

import action.FInteractionUtils;
import items.image.FSelectBox;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

/**
 *
 * @author fernando
 */
public class FSoundSelectBox extends FSelectBox {

    private FSound player = null;
    private Thread t;

    public FSoundSelectBox(String text, Font textFont, int textColor,
            Image imgSelected, Image imgUnselected, boolean selected, byte nextState,
            String soundFile) {
        super(text, textFont, textColor, imgSelected, imgUnselected, selected, nextState);
        init(soundFile);
    }

    /**
     * Creates the sounds and ad
     * ds an action to play it
     * @param soundFile
     */
    private void init(String soundFile) {
        player = new FSound(soundFile, FSound.MIMETYPE_DEFAULT, 99);
        t = new Thread(player);
        this.interaction.addFocusAction(FInteractionUtils.ACTION_PLAY_SOUND, true);
    }

    public boolean execute(byte action) {
        switch (action) {
            case FInteractionUtils.ACTION_NONE:
                break;
            case FInteractionUtils.ACTION_PLAY_SOUND:
                //System.out.println("playing a sound... state = " + player.state);
                t.run();
                break;
            default:
                super.execute(action);
                break;
        }
        return true;
    }
    
    
}
