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
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.Toast;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.lang.reflect.Method;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Set;
        import java.util.UUID;

        import static java.lang.Thread.sleep;


public class BluetoothUtil {

    private static BluetoothAdapter bluetoothAdapter;

    //flag tipo para saber se e cliente ou servidor
    public static  String TIPO_SERVIDOR="BTSERVER";
    public static  String TIPO_CLIENTE="BTCLIENT";
    //flags com status de conexão
    public static  String STATUS_DESCONECTADO="BT0";
    public static  String STATUS_CONECTANDO="BT1";
    public static  String STATUS_CONECTADO="BT2";
    public static  String STATUS_PAREANDO="BT3";



    public static  String STATUS_COMUNICANDO="BT4";
    //dialog para criação de lista de dispositivos
    private  static AlertDialog.Builder builder;
    private static AlertDialog dialog;
    //lista de dispositivos encontrados pareados e/ou não
    private  List<BluetoothDevice> devices;
    private CharSequence[] csNomes;
    //dispositivo selecionado pelo cliente
    private static BluetoothDevice device;
    //uuid da aplicação
    private static  UUID UUIDBT= UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66") ;
    //name da aplicaçao
    private static final String NAME = "BluetoothChatInsecure";
    //atividade ativa
    private  Activity activity;
    //tipo de usuario selecionado
    private  String tipoUser="";

    
    //thread de coneções
    private ConnectThreadClient connectThreadClient=null;
    private AcceptThreadServer acceptThreadServer=null;
    
    //socket bluetooth 
    private BluetoothServerSocket mmServerSocket=null;

    
    // input e output de comunicação com bluetooth
    private InputStream  mmInStream = null;
    private OutputStream mmOutStream = null;

    //handler de comunicação da aplicação com a lib
    private Handler handler  =null;
    //flag para finalizar lib
    private  Boolean cancel=false;
    private String statusstr;





    //construtor 
    public BluetoothUtil(Activity activity,String tipoUser,Handler handler) throws Exception{


        this.activity=activity;
        this.tipoUser=tipoUser;
        this.handler=handler;
        devices = new ArrayList<>();
        cancel=false;



        iniciaBluetoothAdapter();

        enviaHandler("-> "+activity.getString(R.string.status_iniciabt),STATUS_DESCONECTADO);


        if(!suportaBluetooth())
            throw new Exception(activity.getString(R.string.alerta_celular_nao_suportado));
        enviaHandler("-> "+activity.getString(R.string.status_validandobt),STATUS_DESCONECTADO);

        if(!ativaBluetooh(activity))
            throw new Exception(activity.getString(R.string.alerta_ativar_bluetooth));
        enviaHandler("-> "+activity.getString(R.string.status_verificandobt),STATUS_DESCONECTADO);






        carregaTipoUser(tipoUser, activity);




    }




    public String getStatus(){
        return statusstr;
    }

    public void ativaDescoberta(Activity activity){

        try {

                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                activity.startActivity(discoverableIntent);

        }catch (Exception e){

        }






    }


    private  void carregaTipoUser(String tipoUser,Activity activity){
      //  ativaDescoberta(activity);
        if(tipoUser.equals(BluetoothUtil.TIPO_CLIENTE)){


            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver(mReceiverfound, filter);

            IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            activity.registerReceiver(mReceiver, intent);


            bluetoothAdapter.startDiscovery();

            enviaHandler("-> "+activity.getString(R.string.status_carregandotipoclientbt),STATUS_DESCONECTADO);
            listaDispositivos(activity);

        }else if(tipoUser.equals(BluetoothUtil.TIPO_SERVIDOR)) {
            ativaDescoberta(activity);
            enviaHandler("-> " + activity.getString(R.string.status_carregandotiposerverbt),STATUS_DESCONECTADO);

            iniciaServer();

        }

    }
    public  void listaDispositivos(Activity activity){

        try{


            enviaHandler("-> "+activity.getString(R.string.status_aguardandodevicebt),STATUS_DESCONECTADO);

            Set<BluetoothDevice> devicesSet = carregaPareados();



            if(devicesSet!=null){
                devices = adicionaDispositivos(devicesSet);
            }
            procuraDispositivos(activity);
            mostraDisponiveis(devices);
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

            activity.getApplicationContext().registerReceiver(mReceiverStatusChange, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

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

    private  void procuraDispositivos(Context activity){

        try {

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

            if (!bluetoothAdapter.isEnabled()) {

                bluetoothAdapter.enable();
                return false;
            }else{
                activity.getApplicationContext().unregisterReceiver(mReceiverStatusChange);
            }

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





    private  synchronized void mostraDisponiveis(final List<BluetoothDevice> bluetoothDevices){

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
            csNomes = nomesString.toArray(new CharSequence[nomesString.size()]);
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

        try {
            bluetoothAdapter.cancelDiscovery();
            activity.getApplicationContext().unregisterReceiver(mReceiverfound);
            activity.getApplicationContext().unregisterReceiver(mReceiverStatusChange);
            activity.getApplicationContext().unregisterReceiver(mReceiver);


        }catch (Exception e){

        }


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

                enviaHandler("-> "+activity.getString(R.string.status_pareandobt)+device.getName(),STATUS_PAREANDO);

                parearDevice(device);


            }else{

                enviaHandler("-> "+activity.getString(R.string.status_conectandobt)+device.getName(),STATUS_CONECTANDO);

                iniciaCliente(device);
            }

        }

        dialog.dismiss();


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

        if(acceptThreadServer!=null)acceptThreadServer.cancel();

        acceptThreadServer=new AcceptThreadServer();
        acceptThreadServer.start();

    }

    public  void iniciaCliente(BluetoothDevice bluetoothDevice){


        connectThreadClient=new ConnectThreadClient(bluetoothDevice);
        connectThreadClient.start();
    }


    private  class AcceptThreadServer extends Thread {


        public AcceptThreadServer() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final

        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned

            enviaHandler("-> "+activity.getString(R.string.status_conectando_clibt),STATUS_CONECTANDO);


            if(cancel)return;
            int tentativas =0;
            while (true) {
                if(cancel)break;

                BluetoothServerSocket tmp = null;
                try {
                    // MY_UUID is the app's UUID string, also used by the client code

                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,UUIDBT);
                } catch (IOException e) { }
                mmServerSocket = tmp;


                try {
                    if(mmServerSocket!=null){
                        socket = mmServerSocket.accept();

                    }else{
                        tentativas++;

                        enviaHandler("-> "+"conexão falhou, tentando conectar novamente...("+String.valueOf(tentativas)+")",STATUS_CONECTANDO);


                        continue;
                    }


                    tentativas=0;

                } catch (IOException e) {



                    tentativas++;


                    enviaHandler("-> "+"conexão falhou, tentando conectar novamente...("+String.valueOf(tentativas)+")",STATUS_CONECTANDO);


                    continue;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);

                    try {
                        if(mmServerSocket!=null)
                        mmServerSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                if(mmServerSocket!=null)
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    private  class ConnectThreadClient extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThreadClient(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final

            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice

        }

        public void run() {
            // Cancel discovery because it will slow down the connection

            if(cancel)return;
            int tentativas =0;


            while(true){
                if(cancel)break;

                try {

                    try {
                        try {
                            BluetoothSocket tmp = null;
                            // MY_UUID is the app's UUID string, also used by the server code
                            mmSocket = device.createRfcommSocketToServiceRecord(UUIDBT);
                        } catch (IOException e) { }

                        // Connect the device through the socket. This will block
                        // until it succeeds or throws an exception
                        if(mmSocket!=null){
                            mmSocket.connect();
                            tentativas=0;
                        }else{
                            enviaHandler("->"+"conexão falhou, tentando conectar novamente...("+String.valueOf(tentativas)+")",STATUS_CONECTANDO);
                            tentativas++;
                            continue;
                        }

                    } catch (IOException connectException) {
                        // Unable to connect; close the socket and get out

                        enviaHandler("-> "+"conexão falhou, tentando conectar novamente...("+String.valueOf(tentativas)+")",STATUS_CONECTANDO);

                        tentativas++;
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    if(dialog.isShowing()){
                        dialog.dismiss();
                    }
                    manageConnectedSocket(mmSocket);
                }catch (Exception e){

                }


            }



            // Do work to manage the connection (in a separate thread)

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }




    private  void manageConnectedSocket(BluetoothSocket bluetoothSocket){

        // new ConnectedThread(bluetoothSocket).start();

        int TENTATIVASMAX=2;
        if(cancel)TENTATIVASMAX=0;
        int tentativas =TENTATIVASMAX;
        while(tentativas>0){
            if(cancel)break;
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
            if(tentativas==TENTATIVASMAX)

            enviaHandler("-> "+activity.getString(R.string.status_conectadobt),STATUS_CONECTADO);
            else
                enviaHandler("-> "+activity.getString(R.string.status_reconectadobt),STATUS_CONECTANDO);




            while (true) try {
                // Read from the InputStream


                // Send the obtained bytes to the UI activity
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/


                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                try {

                    int tamanho = Integer.valueOf(readMessage);

                    readMessage="";
                    while(true){
                        bytes = mmInStream.read(buffer);
                         readMessage =readMessage+ new String(buffer, 0, bytes);

                        if(tamanho<=readMessage.length()){
                            break;
                        }

                    }


                }catch (Exception e){

                }



                if(TIPO_CLIENTE.equals(tipoUser)){
                    enviaHandler("Servidor: "+readMessage, STATUS_CONECTADO);
                }else if(TIPO_SERVIDOR.equals(tipoUser)){
                    enviaHandler("Cliente: "+readMessage, STATUS_CONECTADO);
                }




                    tentativas=TENTATIVASMAX;


            } catch (IOException e) {
                tentativas--;
                break;

            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

                    enviaHandler("-> "+activity.getString(R.string.status_conectandobt)+device.getName(),STATUS_CONECTANDO);

                    iniciaCliente(device);

                }

            }


        }
    };

    private  final BroadcastReceiver mReceiverfound = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // Quando um dispositivo for encontrado


           if (BluetoothDevice.ACTION_FOUND.equals(action)&&bluetoothAdapter.isDiscovering()) {
                // Obter o BluetoothDevice vindo pela Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Adicionar o nome e endere�o ao array adapter para mostrar na ListView



                if(device!=null){
                    devices = adicionaDispositivos(device);
                    mostraDisponiveis(devices);

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
                   // enviaHandler(activity.getString(R.string.status_verificandobt)+device.getName(),STATUS_DESCONECTADO);
                    carregaTipoUser(tipoUser, activity);



                }
            }
        }
    };

    public void enviaDado(String dado){

        if(mmOutStream==null)return;

        try {

            mmOutStream.write((String.valueOf(dado.length())).getBytes());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            mmOutStream.write((dado).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Handler getHandler() {
        return handler;
    }
    private void enviaHandler(String str,String status){
        if(handler!=null){
            Message msg = handler.obtainMessage();
            String[] messageString = new String[2];
            messageString[0]=str;
            messageString[1]=status;
            msg.obj=messageString;
            handler.sendMessage(msg);
        }


    }


    @Override
    protected void finalize() throws Throwable {



        cancel=true;

        if(acceptThreadServer!=null)acceptThreadServer.cancel();
        if(connectThreadClient!=null)connectThreadClient.cancel();


        if (mmInStream != null) {
            try {mmInStream.close();} catch (Exception e) {}
            mmInStream = null;
        }

        if (mmOutStream != null) {
            try {mmOutStream.close();} catch (Exception e) {}
            mmOutStream = null;
        }

        if (mmServerSocket != null) {
            try {mmServerSocket.close();} catch (Exception e) {}
            mmServerSocket = null;
        }

        if(handler!=null){
            handler=null;
        }
        if(builder!=null){
            builder=null;
        }
        if(dialog!=null){

            dialog=null;
        }





        super.finalize();
    }


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
    public  BluetoothDevice getDevice() {
        return device;
    }

    private  void setDevice(BluetoothDevice device) {

        BluetoothUtil.device = device;
    }

};





