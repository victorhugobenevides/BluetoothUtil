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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);


        ToggleButton btTipo = (ToggleButton) findViewById(R.id.toggleButton);
         txtmsg=(TextView) findViewById(R.id.textview_msg);
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

        btIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    util = new BluetoothUtil(BluetoothActivity.this,tipo,new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            String[] status = (String[]) msg.obj;

                            if(status[1].equals(BluetoothUtil.STATUS_CONECTANDO)){
                                linearLayout1.setVisibility(View.GONE);
                                linearLayout2.setVisibility(View.VISIBLE);

                            }else  if(status[1].equals(BluetoothUtil.STATUS_CONECTADO)){
                                txtmsg.setText(status[0]);
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
