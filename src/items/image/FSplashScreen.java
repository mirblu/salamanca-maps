package items.image;

import action.FInteractionUtils;
import action.FInteraction;
import items.FSuperItem;
import utils.Utils;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A SplashScreen can be shown just for a while and removed by a keypress.
 * Allows user to access mobile browser and usually contains some ads.
 * @author fernando
 */
public class FSplashScreen extends FImage {

    private int backgroundColor = -1;
    private boolean is3D = false;
    private byte nextState = FInteractionUtils.ACTION_NONE;
    private String uri = "";
    private String helpText = "";
    private Font helpFont = Font.getFont(
            Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font uriFont = Font.getFont(
            Font.FACE_SYSTEM, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
    public Timer t;
    /** override tt to perform another task */
    public TimerTask tt = new TimerTask() {

        public void run() {
            execute(FInteractionUtils.ACTION_JUMP_TO_SCREEN);
        }
    };
    public long timeout;
    public long value;

    /**
     * A Splash screen is a image with some text and interaction.
     * Always have CLEAR key to return to main menu.
     * If has an URL, SELECT key is used to perform platform request.
     * @param imgPath the image to show centered on the screen
     * @param text the main text to use
     * @param help the help text, will be smaller font. If contains '^' that will be a line break
     * @param font the font fot the main text
     * @param textColor the text color for all texts that will be shown
     * @param backgroundColor the background color for this screen; if -1 screen will not be background filled
     * @param url the URI, TEL, FAX to throw when platform request. If empty
     * will do nothing.
     * <br> For example:
     * <br><code>tel:+03485551234567</code> will initialize a phone call and/or open contact list
     * <br><code>http://www.mirblu.com</code> will open the mobile web browser
     * <br><code>fax:+3585551234568</code> will send a fax
     * @param timeout if greater than zero will start a Timer to perfomr some action
     * @param nextAction its the action to perform when Utils.SELECT key pressed. see FInteractionUtils constants
     * @param nextState its the state to show if nextAction is FInteractionUtils.ACTION_JUMP_TO_SCREEN;
     * will remove the PLATFORM_REQUEST key action
     * @param notImage if true it will ask how to paint the contents. If false will try to paint like an Image
     */
    public FSplashScreen(Image img, String text, String help, Font font,
            int textColor, int backgroundColor,
            String url, long timeout, byte nextAction, byte nextState, boolean notImage) {
        super(img, text, font, textColor);
        init(help, font, backgroundColor, url, timeout, nextAction, nextState, notImage);
    }

    private void init(String help, Font font, int backgroundColor,
            String url, long timeout, byte nextAction, byte nextState, boolean notImage){
        this.helpText = help;
        this.backgroundColor = backgroundColor;

        this.interaction = FInteraction.newInstance();
        //common
        this.interaction.addInteraction(Utils.KEY_CLEAR,
                FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU);
        this.interaction.addInteraction(Utils.KEY_SELECT, nextAction);
        //action data
        if (nextAction == FInteractionUtils.ACTION_JUMP_TO_SCREEN) {
            this.next = nextState;
        } else if (nextAction == FInteractionUtils.ACTION_PLATFORM_REQUEST) {
            if (Utils.isNotNull(url) && !url.equals("")) {
                this.uri = url;
            } else {
                this.next = FInteractionUtils.ACTION_GO_BACK;
            }
        }

        //time task
        if (timeout > 0) {
            this.timeout = timeout;
            this.value = System.currentTimeMillis() + timeout;
            t = new Timer();
            t.schedule(tt, timeout);
        } else {
            this.timeout = -1;
        }
        is3D = notImage;
    }

    public void paint(Graphics g) {
        try {
            if (backgroundColor != -1) {
                g.setColor(this.backgroundColor);
                g.fillRect(0, 0, FCanvas.canvasWidth, FCanvas.canvasHeight);
            }
            if (Utils.isNotNull(content)) {
                if (!is3D) {
                    g.drawImage((Image) this.getContent(),
                            FCanvas.canvasWidth >> 1,
                            FCanvas.canvasHeight >> 1,
                            Graphics.HCENTER | Graphics.VCENTER);
                } else {
                    //try paint itself
                    try {
                        ((FSuperItem) this.getContent()).paint(g);
                    } catch (Throwable e) {}
                }
            }
            int currY = Utils.write(g, description,
                    10, 10, FCanvas.canvasWidth - 10, textFont, textColor);

            //split line breaks for each ^ found only for helpText
            int size = Utils.count(helpText, '^') + 1;
            String[] temp = Utils.split(helpText, '^', size);
            for (int i = 0; i < size; i++) {
                if (!temp[i].equals("")) {
                    //System.out.println(temp[i]);
                    currY = +Utils.write(
                            g, temp[i], 10, currY, FCanvas.canvasWidth - 10, helpFont, textColor);
                }
            }
            Utils.write(g, uri, 10,
                    FCanvas.canvasHeight - uriFont.getHeight() - 5,
                    FCanvas.canvasWidth - 10, uriFont, textColor);

            if (timeout > 0) {
                long now = System.currentTimeMillis();
                if (value - now > 0) {
                    g.drawString(String.valueOf(value - now), FCanvas.canvasWidth, 0,
                            Graphics.RIGHT | Graphics.TOP);
                } else if (nextState != FInteractionUtils.ACTION_NONE) {
                    //timeout end
                    tt.cancel();
                    t.cancel();
                    this.execute(nextState);
                }
            }
        } catch (Throwable e) {
            //e.printStackTrace();
        }
    }

    public boolean execute(byte action) {
        boolean ret = false;
        FInteraction.cancelTask();
        switch (action) {
            case FInteractionUtils.ACTION_PLATFORM_REQUEST:
                //System.out.println("platform request:" + this.uri);
                FInteractionUtils.platformRequest(this.uri);
                ret = false;
                break;
            case FInteractionUtils.ACTION_CHANGE_TO_MAIN_MENU:
                FInteractionUtils.changeToMainMenu();
                ret = false;
                break;
            case FInteractionUtils.ACTION_EXIT:
                FInteractionUtils.exit();
                ret = false;
                break;
            case FInteractionUtils.ACTION_JUMP_TO_SCREEN:
                FInteractionUtils.changeToXScreen(next);
                ret = false;
                break;
            default:
                break;
        }
        //System.out.println("Executed an splash action: " + action);
        return ret;
    }
}