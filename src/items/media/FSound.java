package items.media;

import utils.Utils;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

public class FSound implements PlayerListener, Runnable {

    /**"audio/amr"*/
    public static final String MIMETYPE_DEFAULT = "audio/amr";//"audio/x-wav";;

    public static final byte NONE = -1;
    public static final byte LOADING = 0;
    public static final byte LOADED = 1;
    public static final byte PLAYING = 2;
    public static final byte PLAYED = 3;
    public static final byte STOPPING = 4;
    public static final byte STOPPED = 5;
    public static final byte ERROR = 6;

    private static final String VOLUME_CONTROL = "VolumeControl";
    private static final String FILENAME_SLASH = "/";

    public byte state = NONE;
    public Player player;
    private VolumeControl volume;

    /**
     * Plays a sound file.
     * @param file the file to load from /res/ folder
     * @param mimetype the mimetype for that file
     * @param volumen the volume to set from 1 to 99
     */
    public FSound(String file, String mimetype, int volumen) {
        state = LOADING;
        if (!file.startsWith(FILENAME_SLASH)) {
            file = new String(FILENAME_SLASH + file);
        }
        createPlayer(file, mimetype, volumen);
        state = LOADED;
    }

    private void createPlayer(String filepath, String mimetype, int vol) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(filepath);
            player = Manager.createPlayer(is, mimetype);
            player.addPlayerListener(this);
            player.realize();
            volume = (VolumeControl) player.getControl(VOLUME_CONTROL);
            if(volume != null) {
                if (vol > 99) {
                    vol = 99;
                } else if (vol < 1) {
                    vol = 1;
                }
                volume.setLevel(vol);
            }
            if (player.getState() == Player.REALIZED) {
                player.prefetch();
            }
        } catch (Throwable e) {
            state = ERROR;
            /*if (e instanceof MediaException) {
                 System.out.println("FSound.createPlayer - The AMR_DECODER environment variable is not set.EMULATOR cannot create a Player for: 'audio/amr'");
                 e.printStackTrace();
                 return;
            }
            if (e instanceof IOException) {
                 System.out.println("FSound.createPlayer - the file can't be loaded: " + filepath);
                 e.printStackTrace();
                 return;
            }*/
            //Utils.debugModePrintStack(e, "FSound","createPlayer");
             
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    state = ERROR;
                } finally {
                    is = null;
                }
            }
        }
    }

    public void run() {
        state = PLAYING;
        while (state == PLAYING) {
            try {
                player.start();
                state = PLAYED;
            } catch (Throwable e) {
                state = ERROR;
                if (e instanceof  NullPointerException) {
                    //System.out.println("FSound.play - " + e.toString());
                    return;
                }
               // Utils.debugModePrintStack(e, "FSound", "play");
            }
        }
    }

    public void stop() {
        state = STOPPING;
        try {
            switch (player.getState()) {
                case Player.CLOSED:
                    break;
                case Player.PREFETCHED:
                case Player.REALIZED:
                case Player.STARTED:
                case Player.UNREALIZED:
                    player.stop();
                    state = STOPPED;
                    break;
            }
        } catch (Throwable e) {
            state = ERROR;
            if (e instanceof  NullPointerException) {
                //System.out.println("FSound.stop - " + e.toString());
                return;
            }
           // Utils.debugModePrintStack(e, "FSound", "stop");
        } finally {
            //player = null;
            //volume = null;
        }
    }

    public void delete(){
        try {
            if (player != null) {
                player.stop();
                player.deallocate();
            }
        } catch (Throwable e) {
        } finally {
            player = null;
            volume = null;
            state = NONE;
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        if (event.equals(PlayerListener.STOPPED) || event.equals(PlayerListener.CLOSED) || event.equals(PlayerListener.END_OF_MEDIA)) {
            stop();
        }
    }

}
