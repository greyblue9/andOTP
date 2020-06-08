package org.shadowice.flocke.andotp.Utilities;


import org.shadowice.flocke.andotp.Database.Entry;

import android.util.Log;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgentV2;
import com.samsung.android.sdk.accessory.SAAuthenticationToken;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;


import javax.crypto.SecretKey;

public class Tizen extends SAAgentV2 {

    private static final String TAG = "Tizen (tizOTP/link)";
    private static final Class<ServiceConnection> SASOCKET_CLASS = ServiceConnection.class;
    private ServiceConnection mConnectionHandler = null;
    private Handler mHandler = new Handler();
    private Context mContext = null;

    public Tizen(Context context) {
        super(TAG, context, SASOCKET_CLASS);
        mContext = context;

        SA mAccessory = new SA();
        try {
            mAccessory.initialize(mContext);
        } catch (SsdkUnsupportedException e) {
            // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service threads, close UI thread, etc.)
             */
        }
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        Log.d(TAG, "onFindPeerAgentResponse : result =" + result);
    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        if (peerAgent != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Connection accepted", Toast.LENGTH_SHORT).show();
                }
            });
            acceptServiceConnectionRequest(peerAgent);
        }
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result) {
        if (result == SAAgentV2.CONNECTION_SUCCESS) {
            if (socket != null) {
                mConnectionHandler = (ServiceConnection) socket;
            }
        } else if (result == SAAgentV2.CONNECTION_ALREADY_EXIST) {
            Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
        }
    }

    @Override
    protected void onAuthenticationResponse(SAPeerAgent peerAgent, SAAuthenticationToken authToken, int error) {
        /*
         * The authenticatePeerAgent(peerAgent) API may not be working properly depending on the firmware
         * version of accessory device. Please refer to another sample application for Security.
         */
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

    public class ServiceConnection extends SASocket {
        public ServiceConnection() {
            super(ServiceConnection.class.getName());
        }

        @Override
        public void onError(int channelId, String errorMessage, int errorCode) {
        }

        @Override
        public void onReceive(int channelId, byte[] data) {
            if (mConnectionHandler == null) {
                return;
            }
            String receivedCommand= new String(data);
            Log.e(TAG, receivedCommand);
            if (!receivedCommand.equals("Hello Dududu! Gimme some s**t" )) {
                Log.e(TAG, "dropping query");
                return;
            }
            Calendar calendar = new GregorianCalendar();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd aa hh:mm:ss.SSS");
            String timeStr = " " + dateFormat.format(calendar.getTime());
            //String strToUpdateUI = new String(data);
            String strToUpdateUI = getTizenString(); // getTizenTransfer();
            System.out.println(strToUpdateUI);
            final String message = strToUpdateUI;
            ///final String message = strToUpdateUI.concat(timeStr);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        mConnectionHandler.send(getServiceChannelId(0), message.getBytes());
                        //mConnectionHandler.send(getServiceChannelId(0), getTizenData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        protected void onServiceConnectionLost(int reason) {
            mConnectionHandler = null;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Connection Terminated", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String transferstring;

    //public static byte[] transferdata;
 /*   public static void setTizenTransfer(String datastring) {
        transferstring = datastring;
        System.out.println("Tizen set called. System out ");
        Log.e(TAG, "Tizen set called");
    }*/
    public static void setTizenData(byte[] databytes) {
    /*    String datastring = null;
        try {
            datastring = new String(databytes, "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Exception"+e);
        }*/
        String hexdata = bytesToHex(databytes);
        //System.out.println(hexdata);

        //transferdata = databytes;
        transferstring = hexdata;
        //System.out.println("Tizen set databytes called. System out ");
        Log.e(TAG, "Tizen set called bytes: "+transferstring);
    }
  /*  public static byte[] getTizenData() {
        Log.e(TAG, "Tizen get Data called");
        if (transferdata==null) {
            transferdata = "No Datab".getBytes();
        }
        System.out.println(transferdata.toString());
        return transferdata;
    }
*/    public static String getTizenString() {
        Log.e(TAG, "Tizen get called");
        if (transferstring==null) {
            transferstring = "No Data";
        }
        return transferstring;
    }

    public static boolean backupToTizen(String password, ArrayList<Entry> entries)
    {
        //ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);
        String plain = DatabaseHelper.entriesToString(entries);

        try {
            int iter = EncryptionHelper.generateRandomIterations();
            byte[] salt = EncryptionHelper.generateRandom(Constants.ENCRYPTION_IV_LENGTH);

            SecretKey key = EncryptionHelper.generateSymmetricKeyPBKDF2(password, iter, salt);
            byte[] encrypted = EncryptionHelper.encrypt(key, plain.getBytes(StandardCharsets.UTF_8));

            byte[] iterBytes = ByteBuffer.allocate(Constants.INT_LENGTH).putInt(iter).array();
            byte[] data = new byte[Constants.INT_LENGTH + Constants.ENCRYPTION_IV_LENGTH + encrypted.length];

            System.arraycopy(iterBytes, 0, data, 0, Constants.INT_LENGTH);
            System.arraycopy(salt, 0, data, Constants.INT_LENGTH, Constants.ENCRYPTION_IV_LENGTH);
            System.arraycopy(encrypted, 0, data, Constants.INT_LENGTH + Constants.ENCRYPTION_IV_LENGTH, encrypted.length);

            //StorageAccessHelper.saveFile(context, uri, data);
            System.out.println(data.toString());
            Tizen.setTizenData(data);
            System.out.println("Set Tizen Data");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}


