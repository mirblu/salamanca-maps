/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package items.image;

/**
 * A touch FCanvas
 * @author fernando
 */
public class FTouchCanvas extends FCanvas{

    public FTouchCanvas() {
        super();
        checkPointerCapabilities();
    }

    private void checkPointerCapabilities() {
        hasPointerEvents();
        hasPointerMotionEvents();
    }



}
