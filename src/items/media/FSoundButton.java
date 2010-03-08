package items.media;

import action.FInteraction;
import action.FInteractionUtils;
import items.image.FImage;
import utils.Utils;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

/**
 * FImage + FSound
 * @author fernando
 */
public class FSoundButton extends FImage {

    private String soundFile;
    private static FSound player;
    private static Thread t;

    public FSoundButton(Image image, String title, Font textfont, int color, String soundFile) {
        super(image, title, textfont, color);
        delete();
        init(soundFile);
    }

    /**
     * Creates the sounds and ad
     * ds an action to play it
     * @param soundFile
     */
    private void init(String soundFile) {
        this.soundFile = soundFile;
        this.interaction = FInteraction.newInstance();
        this.interaction.addFocusAction(FInteractionUtils.ACTION_PLAY_SOUND, true);
    }

    
    public boolean execute(byte action) {
        switch (action) {
            case FInteractionUtils.ACTION_NONE:
                break;
            case FInteractionUtils.ACTION_PLAY_SOUND:
                player = new FSound(soundFile, FSound.MIMETYPE_DEFAULT, 99);
                t = new Thread(player);
                t.run();
                break;
            default:
                super.execute(action);
                break;
        }
        return true;
    }

    private void delete() {
        try {
            if (Utils.isNotNull(player)) {
                player.stop();
                player.delete();
            }
            if (Utils.isNotNull(t)) {
                t = null;
            }
        } catch (Throwable e) {
        } finally {
            player = null;
            t = null;
        }
    }

}
