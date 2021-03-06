package itbenevides.com.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class BluetoothActivity extends ActionBarActivity {
    String tipo = BluetoothUtil.TIPO_SERVIDOR;
    public static BluetoothUtil util=null;

    LinearLayout linearLayout1;
    LinearLayout linearLayout2;
    TextView txtmsg;
    EditText etmsg;
    Button btenviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);




        ToggleButton btTipo = (ToggleButton) findViewById(R.id.toggleButton);
         txtmsg=(TextView) findViewById(R.id.textview_msg);
        etmsg=(EditText) findViewById(R.id.editText);
         linearLayout1 = (LinearLayout)findViewById(R.id.linearlayout1);
         linearLayout2 = (LinearLayout)findViewById(R.id.linearLayout2);

        btTipo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    tipo = BluetoothUtil.TIPO_CLIENTE;
                }else{
                    tipo = BluetoothUtil.TIPO_SERVIDOR;
                }

            }
        });


        Button btIniciar = (Button) findViewById(R.id.button);
         btenviar = (Button) findViewById(R.id.button2);
        btenviar.setEnabled(false);


        btenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(etmsg.getText().equals("")){
                        Toast.makeText(getApplicationContext(),"Digite uma msg.",Toast.LENGTH_SHORT).show();
                    }else{
                        util.enviaDado(etmsg.getText().toString());
                        txtmsg.setText( txtmsg.getText() + "\n" + "Você: " +etmsg.getText().toString());
                        etmsg.setText("");
                    }
                }catch (Exception e){


                }



            }
        });

        btIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    util = new BluetoothUtil(BluetoothActivity.this,tipo,new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            String[] status = (String[]) msg.obj;

                            if(status[1].equals(BluetoothUtil.STATUS_CONECTANDO)||status[1].equals(BluetoothUtil.STATUS_PAREANDO)){
                                linearLayout1.setVisibility(View.GONE);
                                linearLayout2.setVisibility(View.VISIBLE);

                                txtmsg.setText(txtmsg.getText() + "\n" + status[0]);
                                btenviar.setEnabled(false);


                            }else if(status[1].equals(BluetoothUtil.STATUS_CONECTADO)||status[1].equals(BluetoothUtil.STATUS_COMUNICANDO)){
                                linearLayout1.setVisibility(View.GONE);
                                linearLayout2.setVisibility(View.VISIBLE);
                                txtmsg.setText(txtmsg.getText() + "\n " + status[0]);
                                btenviar.setEnabled(true);
                            }


                        }
                    });

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                }
            }
        });





    }

    @Override
    public void onBackPressed() {


        if(linearLayout1!=null){
            if(linearLayout1.getVisibility()==View.GONE){
                linearLayout1.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
                txtmsg.setText("");
                btenviar.setEnabled(false);
                try {
                    util.finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }else{
                finish();
            }
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
