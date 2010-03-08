package comunication;

import app.FApp;
import midlet.FMIDlet;
import utils.Utils;

import java.util.Timer;
import java.util.TimerTask;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import java.util.Vector;


/**
 * Discovers devices by .searchDevices and compares each one
 * with the constructor received Bluetooth Adresses (BTA) <br>
 * Needs to override two methods: <code>friendDevice()</code> and <code>notFriendDevice()</code>
 * with the code to execute when a device is one of the expected (given in contructor)
 * and when it is not one of them.
 * @author fernando
 */
public abstract class FBluetooth implements DiscoveryListener {

    //object used for waiting
    private static Object lock=new Object();


    public static String message;
    /*Vector<String> with BTA of devices we won't process*/
    public static Vector skipDevices;
    /*Vector<String> with all the knowed friendly devices BTA*/
    public static Vector knownDevices;
    public static int knownDevicesSize = 0;
    /*Vector<String> contains the discovered devices BTA*/
    public static Vector discoveredBTA;
    /*the local device initialized at constructor*/
    public static LocalDevice localDevice;
    /*
     *some constants to control bluetooth states
     */
    /**-1*/
    public static final byte STATE_NONE_                     =-1;
    /**0*/
    public static final byte STATE_INITIALIZED_              = 0;
    /**1*/
    public static final byte STATE_INQUIRY_STARTED_          = 1;
    /**2*/
    public static final byte STATE_DEVICE_DISCOVERED_        = 2;
    /**3*/
    public static final byte STATE_INQUIRY_COMPLETED_        = 3;
    /**4*/
    public static final byte STATE_SERVICES_DISCOVERED_      = 4;
    /**5*/
    public static final byte STATE_SERVICE_SEARCH_COMPLETED_ = 5;
    /**no devices discovered at inquiry end. value 6*/
    public static final byte STATE_NOTHING_DISCOVERED_       = 6;
    /**any exception; detailed info stored at <code>FBluetooth.message</code>, value 7*/
    public static final byte STATE_EXCEPTION_                = 7;
    /**user disabled bluetooth, value 8*/
    public static final byte STATE_EXCEPTION_USER_DISABLED_  = 8;
    /**bluetooth is busy, value 9*/
    public static final byte STATE_EXCEPTION_BUSY_           = 9;
    /**device not support JSR082, value 10*/
    public static final byte STATE_EXCEPTION_NOT_JSR082_SUPPORT_ = 10;
    /**device disabled bluetooth by software, value 11*/
    public static final byte STATE_DISABLED_BY_APP_            = 11;
    /**device disabled bluetooth by software, value 11*/
    public static final byte STATE_WAITING_            = 12;
    /*has the last state of bluetooth*/
    public static byte state = STATE_NONE_;
    public static boolean activated = false;

    public static void resetSkipped() {
        if (Utils.isNull(FBluetooth.skipDevices)) {
            FBluetooth.skipDevices = new Vector(1, 1);
        }
        FBluetooth.skipDevices.removeAllElements();
    }

    /**
     * Need to receive a Vector of knowed devices. Will be used like satellites.
     * @param satellites Vector<String> all the satellite devices
     */
    public FBluetooth(Vector satellites) {
        init(satellites);
    }

    public void init(Vector satellites) {
        checkCompatibility();
        knownDevices = satellites;
        knownDevicesSize = satellites.size();
        discoveredBTA = new Vector(1, 1);
        skipDevices = new Vector(1, 1);
        switch (getState()) {
            case STATE_NONE_:
                start();
                break;
            default:
                break;
        }
    }

    /**
     * Inits the local device and sets as General/Unlimited Inquiry Access Code (GIAC)
     */
    public void start() {
        switch (state) {
            case STATE_EXCEPTION_BUSY_:
            case STATE_EXCEPTION_:
            case STATE_INQUIRY_STARTED_:
            case STATE_EXCEPTION_USER_DISABLED_:
            case STATE_DISABLED_BY_APP_:
                default:
                    //do noting
                break;
            case STATE_NONE_:
            case STATE_NOTHING_DISCOVERED_:
            case STATE_INQUIRY_COMPLETED_:
            case STATE_SERVICE_SEARCH_COMPLETED_:
            case STATE_WAITING_:
                try {
                    if (Utils.isNull(localDevice)) {
                        localDevice = LocalDevice.getLocalDevice();
                        //General/Unlimited Inquiry Access Code (GIAC)
                        localDevice.setDiscoverable(DiscoveryAgent.GIAC);
                    }
                    setState(STATE_INITIALIZED_);
                    activated = true;
                } catch (Throwable e) {
                    activated = false;
                    message = e.toString();
                    if (e.getMessage().indexOf("off") != -1 || e.getMessage().indexOf("denied") != -1) {
                        //user swiched off bluetooth or denied access
                        //Utils.debugModePrintStack(e, "FBluetooth", "STATE_EXCEPTION_USER_DISABLED");
                        setState(STATE_EXCEPTION_USER_DISABLED_);
                    } else {
                        //Utils.debugModePrintStack(e, "FBluetooth", "initializing local device");
                        setState(STATE_EXCEPTION_);
                    }
                }
                break;
        }
        
    }

    public void searchDevices() {//1,3,
        switch (getState()) {
            case STATE_INQUIRY_STARTED_:
            case STATE_EXCEPTION_:
            case STATE_EXCEPTION_USER_DISABLED_:
            case STATE_NONE_:
            case STATE_EXCEPTION_BUSY_:
                default:
                break;
            case STATE_INITIALIZED_:
            //case STATE_NOTHING_DISCOVERED: //ask user
            //case STATE_INQUIRY_COMPLETED: //ask user
                try {
                    localDevice.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
                    setState(STATE_INQUIRY_STARTED_);
                    synchronized(lock){
                        lock.wait();
                    }
                } catch (Throwable e) {
                    message = e.toString();
                    //Utils.debugModePrintStack(e, "FBluetooth(" + state + ")", "searchDevices");
                    if (message.indexOf("active") != -1
                            || message.indexOf("running") != -1
                            || message.indexOf(/*B*/"usy") != -1) {
                        setState(STATE_EXCEPTION_BUSY_);
                    } else {
                        setState(STATE_EXCEPTION_);
                    }
                }
                break;
        }
    }
    
    private static String bta = null;
    /**
     * Called when one device is discovered.
     * Stores the device BTA and checks if has to perform an action
     * or has to skip it.
     * @param remoteDevice the recently discovered device
     * @param deviceClass the device class will be not used
     */
    public final synchronized void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        try {
            bta = remoteDevice.getBluetoothAddress();
            //if in skipped, skip
            if (skipDevices.indexOf(bta) != -1) {
                return;
            } else {
                setState(STATE_DEVICE_DISCOVERED_);
                discoveredBTA.addElement(bta);
                action(remoteDevice);
            }
        } catch (Throwable e) {
            message = e.toString();
            //Utils.debugModePrintStack(e, "FBluetooth(" + state + ")", "deviceDiscovered");
            setState(STATE_EXCEPTION_);
        } finally {
            bta = null;
        }
    }

    /**
     * Current search finished. If no devices discovered, state will be set to STATE_NOTHING_DISCOVERED
     * @param discType
     */
    public final void inquiryCompleted(int discType) {
        try {
            if (discoveredBTA.size() == 0) {
                setState(STATE_NOTHING_DISCOVERED_);
            } else {
                setState(STATE_INQUIRY_COMPLETED_);
                try {
                        //TODO when completed, wait
                        Thread.currentThread().sleep(30000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
            }
            synchronized(lock){
                lock.notify();
            }

            /*null all data
            discoveredBTA = null;
            knownDevices = null;
            localDevice = null;
            skipDevices = null;*/

            
        } catch (Throwable e) {
            message = e.toString();
            //Utils.debugModePrintStack(e, "FBluetooth(" + state + ")", "inquiryCompleted");
            setState(STATE_EXCEPTION_);
        }
    }

    public static long timeToWait = 0;
    public static Timer t;
    /**
     * Starts a task to await some seconds, then restarts bluetooth
     */
    protected static final void await(long time) {
        timeToWait = time;
        TimerTask tt = new TimerTask() {
            public void run() {
                //FMIDlet.bluetooth.setState(STATE_WAITING_);
                long start = System.currentTimeMillis();
                long now = start;
                while (timeToWait != 0 && now - start < timeToWait) {
                    try {
                        message = String.valueOf(timeToWait - (now - start)) + " ms";
                        //System.out.println(message);
                        Thread.sleep(timeToWait / 10);
                        now = System.currentTimeMillis();
                    } catch (InterruptedException ex) {
                        //ex.printStackTrace();
                    }
                }
                //after wait, start bluetooth if routePlanner
                switch (FApp._current) {
                        case FApp.STATE_SCREEN_RUTE_PLANNER:
                  //          FMIDlet.bluetooth.start();
                            break;
                    }
            }
        };
        t = new Timer();
        t.schedule(tt, 0);
    }

    /**
     * Checks if received address is a satellite
     * @param bluetoothAddress the readed address
     * @return true if address is equal to some known device (balizas)
     */
    private static final boolean isFriend(String bluetoothAddress) {
        boolean ret = false;
        int i = 0;
        for (; i < knownDevicesSize; i++) {
            if (bluetoothAddress.equalsIgnoreCase((String)knownDevices.elementAt(i))) {
                return true;
            }
        }
        return ret;
    }

    /**
     * Performs an action, can be overrided.
     * By default it checks if discovered device is one of our known devices (by BTA) or not
     * and calls to friendDeviceAction or notFriendDeviceAction
     * @param rd the RemoteDevice found
     */
    public void action(RemoteDevice rd) {
        if (isFriend(rd.getBluetoothAddress())) {
            friendDeviceAction(rd.getBluetoothAddress());
        } else {
            notFriendDeviceAction(rd.getBluetoothAddress());
        }
    }

    /**
     * The action to perform when a discovered device is one of our known devices
     * @param bluetoothAddress the bluetooth address of the friend device
     */
    public abstract void friendDeviceAction(String bluetoothAddress);

    /**
     * The action to perform when a discovered device is a unkown device
     * @param bluetoothAddress the bluetooth address for the discovered device
     * that is not at our friend list (satellite list)
     */
    public abstract void notFriendDeviceAction(String bluetoothAddress);

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        setState(STATE_SERVICES_DISCOVERED_);
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        setState(STATE_SERVICE_SEARCH_COMPLETED_);
    }

    /**
     * Sets a new state for bluetooth 
     * @param newState the state to set
     * TODO state machine to control state flow
     */
    public void setState(byte newState) {
        state = newState;
    }

    public byte getState() {
        return state;
    }

    /**
     * Checks if JSR082 is supportted by <code>Utils.isCompatible("javax.bluetooth.LocalDevice");</code>
     */
    public final void checkCompatibility() {
        try {
            Utils.isCompatible(new String[]{"javax.bluetooth.LocalDevice"});
        } catch (ClassNotFoundException e) {
            activated = false;
            setState(STATE_EXCEPTION_NOT_JSR082_SUPPORT_);
            //Utils.debugModePrintStack(e, "FBluetooth", "checkCompatible");
        }
    }

    private static Vector storedSatellites;
    public void enable(){
        message = "FBluetooth enabled.";
        init(storedSatellites);
    }

    /**
     * Sets all data to <code>null<code> and 
     * <code>message = "FBluetooth disabled."<code> and state to STATE_DISABLED_BY_SW.
     * To use bluetooth again is needed to call to enable().
     * All satellites readed will be restored.
     */
    public static void disable(){
        state = STATE_DISABLED_BY_APP_;
        activated = false;
        message = "Bluetooth disabled.";
        bta = null;
        discoveredBTA = null;
        storedSatellites = knownDevices;
        knownDevices = null;
        localDevice = null;
        skipDevices = null;
    }
    
}