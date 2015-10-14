package itbenevides.com.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BluetoothUtil {

    private static BluetoothAdapter bluetoothAdapter;

    public static  String TIPO_SERVIDOR="BTSERVER";
    public static  String TIPO_CLIENTE="BTCLIENT";
    public static  String STATUS_DESCONECTADO="BT0";
    public static  String STATUS_CONECTANDO="BT1";
    public static  String STATUS_CONECTADO="BT2";
    public static  String STATUS_PAREANDO="BT3";
    private  AlertDialog.Builder builder;
    private  AlertDialog dialog;


    private  List<BluetoothDevice> devices;
    private static BluetoothDevice device;
    private static  UUID UUIDBT= UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66") ;
    private static final String NAME = "BluetoothChatInsecure";
    private  Activity activity;
    private  String tipoUser="";

    private String statusstr;

    private InputStream  mmInStream = null;
    private OutputStream mmOutStream = null;

    private Handler handler  =null;







    public BluetoothUtil(Activity activity,String tipoUser,Handler handler) throws Exception{


        this.activity=activity;
        this.tipoUser=tipoUser;
        this.handler=handler;
        devices = new ArrayList<>();



        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        activity.registerReceiver(mReceiver, intent);


        iniciaBluetoothAdapter();
        enviaHandler(activity.getString(R.string.status_iniciabt),STATUS_DESCONECTADO);


        if(!suportaBluetooth())
            throw new Exception(activity.getString(R.string.alerta_celular_nao_suportado));
        enviaHandler(activity.getString(R.string.status_validandobt),STATUS_DESCONECTADO);

        if(!ativaBluetooh(activity))
            throw new Exception(activity.getString(R.string.alerta_ativar_bluetooth));
        enviaHandler(activity.getString(R.string.status_verificandobt),STATUS_DESCONECTADO);


        ativaDescoberta(activity);


       carregaTipoUser(tipoUser, activity);




    }




    public String getStatus(){
        return statusstr;
    }

    public void ativaDescoberta(Activity activity){

        if(!bluetoothAdapter.isDiscovering()){
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            activity.startActivity(discoverableIntent);
        }




    }


    private  void carregaTipoUser(String tipoUser,Activity activity){

        if(tipoUser.equals(BluetoothUtil.TIPO_CLIENTE)){
            enviaHandler(activity.getString(R.string.status_carregandotipoclientbt),STATUS_DESCONECTADO);
            listaDispositivos(activity);

        }else if(tipoUser.equals(BluetoothUtil.TIPO_SERVIDOR)){
            enviaHandler(activity.getString(R.string.status_carregandotiposerverbt),STATUS_DESCONECTADO);
            iniciaServer();

        }

    }
    public  void listaDispositivos(Activity activity){

        try{

            enviaHandler(activity.getString(R.string.status_aguardandodevicebt),STATUS_DESCONECTADO);
            Set<BluetoothDevice> devicesSet = carregaPareados();



            if(devicesSet!=null){
                devices = adicionaDispositivos(devicesSet);
            }

            mostraDisponiveis(devices, activity);
        }catch (Exception    e){

        }

    }



    public  BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    private  void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        BluetoothUtil.bluetoothAdapter = bluetoothAdapter;
    }

    private  void iniciaBluetoothAdapter(){
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }catch (Exception e){

        }

    }

     public  Boolean suportaBluetooth(){

        try {
            if(bluetoothAdapter==null){
                iniciaBluetoothAdapter();
            }

            if (bluetoothAdapter == null) {
                return false;
            }
            return true;
        }catch (Exception e){

        }
        return false;

    }

    private  List<BluetoothDevice> adicionaDispositivos(Set<BluetoothDevice> devicesSet){

        for(BluetoothDevice device:devicesSet){
            for(BluetoothDevice device2:devices){
                if(device.getAddress().equals(device2.getAddress())){
                    devices.remove(device2);
                    break;
                }

            }
        }
        if(devicesSet!=null){
            devices.addAll(devicesSet);
        }


        return devices;

    }


    private  List<BluetoothDevice> adicionaDispositivos(List<BluetoothDevice> devicesSet){

        for(BluetoothDevice device:devicesSet){
            for(BluetoothDevice device2:devices){
                if(device.getAddress().equals(device2.getAddress())){
                    devices.remove(device2);
                    break;
                }

            }
        }
        if(devicesSet!=null){
            devices.addAll(devicesSet);
        }


        return devices;

    }
    private  List<BluetoothDevice> adicionaDispositivos(BluetoothDevice device){


            for(BluetoothDevice device2:devices){
                if(device.getAddress().equals(device2.getAddress())){
                    devices.remove(device2);
                    break;
                }

            }

        if(device!=null){
            devices.add(device);
        }


        return devices;

    }

    private  void procuraDispositivos(Activity activity){

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);


            activity.getApplicationContext().registerReceiver(mReceiver, filter);
            if(bluetoothAdapter==null){
                iniciaBluetoothAdapter();
            }

        }
        catch (Exception e){

        }
    }

    private  Boolean ativaBluetooh(final Activity activity){

        try {
            if(bluetoothAdapter==null){
                iniciaBluetoothAdapter();
            }
            activity.getApplicationContext().registerReceiver(mReceiverStatusChange, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                return false;
            }
            bluetoothAdapter.startDiscovery();
            return true;



        }catch (Exception e){

        }

        return false;

    }

    private  Set<BluetoothDevice> carregaPareados(){
        Set<BluetoothDevice> pareados =null;

        try {
            pareados=bluetoothAdapter.getBondedDevices();
        }catch (Exception e){

        }


        return pareados;

    }


    private  void mostraDisponiveis(final List<BluetoothDevice> bluetoothDevices, final Context activity){

        try {
            if(builder==null)
            builder = new AlertDialog.Builder(activity);

           if(dialog!=null&&dialog.isShowing()){



               dialog.dismiss();
            }



            List<String> nomesString = new ArrayList<>();
            for(BluetoothDevice bluetoothDevice:bluetoothDevices){
                nomesString.add(bluetoothDevice.getName());
            }
            if(nomesString.isEmpty()){
                nomesString.add(activity.getString(R.string.alerta_zero_dipositivos_encontrado));
            }
            CharSequence[] csNomes = nomesString.toArray(new CharSequence[nomesString.size()]);
            builder.setTitle(R.string.titulo_lista_disponiveis)
                    .setItems(csNomes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            onclickList(dialog,which,bluetoothDevices);
                        }
                    });


            dialog = builder.create();
            dialog.show();
        }catch (Exception e){

        }



    }


    private void onclickList(DialogInterface dialog, int which,final List<BluetoothDevice> bluetoothDevices){

        if(bluetoothDevices.isEmpty()){
            Toast.makeText(activity, "Invalido",Toast.LENGTH_LONG).show();
            device=null;
        }else{
            Toast.makeText(activity, bluetoothDevices.get(which).getName(),Toast.LENGTH_LONG).show();
            device=bluetoothDevices.get(which);

            Set<BluetoothDevice> btpaired=carregaPareados();
            boolean tem = false;
            for(BluetoothDevice device2:btpaired){
                if(device.getAddress().equals(device2.getAddress())){
                    tem=true;
                    break;
                }
            }
            if(!tem){
                enviaHandler(activity.getString(R.string.status_pareandobt)+device.getName(),STATUS_PAREANDO);
                parearDevice(device);


            }else{
                enviaHandler(activity.getString(R.string.status_conectandobt)+device.getName(),STATUS_CONECTANDO);
                iniciaCliente(device);
            }
            return;
        }



    dialog.dismiss();
    }

    private  void mostraDisponiveis(final List<BluetoothDevice> bluetoothDevices, final Activity activity){

        try {

            if(builder==null)
            builder = new AlertDialog.Builder(activity);


            if(dialog!=null&&dialog.isShowing()){
                dialog.dismiss();
            }

            List<String> nomesString = new ArrayList<>();
            for(BluetoothDevice bluetoothDevice:bluetoothDevices){
                nomesString.add(bluetoothDevice.getName());
            }
            if(nomesString.isEmpty()){
                nomesString.add(activity.getString(R.string.alerta_zero_dipositivos_encontrado));
            }
            CharSequence[] csNomes = nomesString.toArray(new CharSequence[nomesString.size()]);
            builder.setTitle(R.string.titulo_lista_disponiveis)
                    .setItems(csNomes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onclickList(dialog, which, bluetoothDevices);
                        }
                    });


            dialog = builder.create();
            dialog.show();
        }catch (Exception e){

        }

        procuraDispositivos(activity);

    }

    public  void parearDevice(BluetoothDevice device,Activity activity){
        try {
            String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
            Intent intent = new Intent(ACTION_PAIRING_REQUEST);
            String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
            intent.putExtra(EXTRA_DEVICE, device);
            String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
            int PAIRING_VARIANT_PIN = 0;
            intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }catch (Exception e){

        }

    }
    public  void parearDevice(BluetoothDevice device){
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void iniciaServer(){

        new AcceptThreadServer().start();

    }

    public  void iniciaCliente(BluetoothDevice bluetoothDevice){


        new ConnectThreadClient(bluetoothDevice).start();
    }


    private  class AcceptThreadServer extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThreadServer() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,UUIDBT);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {

                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    private  class ConnectThreadClient extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThreadClient(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUIDBT);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection

            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private  class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;


            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/






                    String readMessage = new String(buffer,0,bytes);

                    enviaHandler(readMessage,STATUS_CONECTADO);



                } catch (IOException e) {
                    break;
                }
            }

        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/



                    String readMessage = new String(buffer,0,bytes);

                    enviaHandler(readMessage,STATUS_CONECTADO);



                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    private  void manageConnectedSocket(BluetoothSocket bluetoothSocket){

        // new ConnectedThread(bluetoothSocket).start();




        BluetoothSocket mmSocket = bluetoothSocket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) { }

          mmInStream = tmpIn;
         mmOutStream = tmpOut;
        byte[] buffer = new byte[8192];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        enviaHandler(activity.getString(R.string.status_conectadobt),STATUS_CONECTADO);
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/



                String readMessage = new String(buffer,0,bytes);

                enviaHandler(readMessage,STATUS_CONECTADO);


            } catch (IOException e) {
                break;
            }
        }

    }



    private  final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // Quando um dispositivo for encontrado


            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    enviaHandler(activity.getString(R.string.status_conectandobt)+device.getName(),STATUS_CONECTANDO);
                    iniciaCliente(device);

                }

            }else  if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Obter o BluetoothDevice vindo pela Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Adicionar o nome e endere�o ao array adapter para mostrar na ListView



                if(device!=null){
                    devices = adicionaDispositivos(device);
                    mostraDisponiveis(devices, context);
                }




            }


        }
    };
    private  final BroadcastReceiver mReceiverStatusChange = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // Quando um dispositivo for encontrado
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){

                if(ativaBluetooh(activity)){
                    enviaHandler(activity.getString(R.string.status_verificandobt)+device.getName(),STATUS_DESCONECTADO);
                    carregaTipoUser(tipoUser, activity);
                }






            }
        }
    };
    public  BluetoothDevice getDevice() {
        return device;
    }

    private  void setDevice(BluetoothDevice device) {
        BluetoothUtil.device = device;
    }

    public void enviaDado(String dado){

        if(mmOutStream==null)return;

        try {

            mmOutStream.write(dado.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Handler getHandler() {
        return handler;
    }
    private void enviaHandler(String str,String status){

        Message msg = handler.obtainMessage();
        String[] messageString = new String[2];
        messageString[0]=str;
        messageString[1]=status;
        msg.obj=messageString;
        handler.sendMessage(msg);
    }
};




