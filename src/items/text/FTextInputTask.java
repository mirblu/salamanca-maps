/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package items.text;

import items.image.FCanvas;
import utils.Utils;
import java.util.TimerTask;

/**
 * Controls cursor blink.
 * @author fernando
 */
public class FTextInputTask extends TimerTask {

    public FTextInputTask() {
        super();
    }

    public void run() {
        if (FTextInput.blink) {
            FTextInput.blink = false;
        } else if (!FTextInput.blink) {
            FTextInput.blink = true;
        }
        Utils.callRepaint(
                0,
                FTextInput.cursorY - FTextInput.cursorH - 10,
                FCanvas.canvasWidth,
                FTextInput.cursorY + FTextInput.cursorH + 10);
    }
}

