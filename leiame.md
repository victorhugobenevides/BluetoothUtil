# BluetoothUtil



1: Adicione a classe BluetoothUtil em seu projeto

2: Adicione as seguintes permissões no manifest:
  
  ```
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" />
  
  ```
3: Adicione o codigo onde (Activity) quer pegar a informação:
  
  ```
  public static BluetoothUtil util;
  
  ```
  -Escolha se quer cliente ou servidor
  
  String tipo = BluetoothUtil.TIPO_SERVIDOR;
  ou
  String tipo = BluetoothUtil.TIPO_CLIENTE;
  
  -Inicia bluetooth
  
 ```
  util = new BluetoothUtil(BluetoothActivity.this,tipo,new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            String[] status = (String[]) msg.obj;
                              //onde recebe a mensagem
                              
                              //primeiro item do vetor indica o status e o segundo o valor
                              //tipos de status: 
                              //BluetoothUtil.STATUS_CONECTANDO, 
                              //BluetoothUtil.STATUS_PAREANDO, 
                              //BluetoothUtil.STATUS_CONECTADO e 
                              //BluetoothUtil.STATUS_COMUNICANDO 
                              
                              
                        }
                    });
  ```   
  
  -Enviando dados
  -Dica: só envie mensagens se ja estiver recebido o status BluetoothUtil.STATUS_CONECTANDO
  
  ```
  util.enviaDado("Sua mensagem");
 
 ```
               
  
