package com.example.hiveqm_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;

public class MQTTActivity extends AppCompatActivity {

    MqttAndroidClient mqttClient;
    String topic = "home/school";
    String id = "Smartphone";
    String user, pw, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttactivity);

        user = getString(R.string.user);
        pw = getString(R.string.user);
        url = getString(R.string.serverUrl);

        mqttClient = new MqttAndroidClient(getApplicationContext(), url, id);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Lost Connection!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i("MQTT", "Received Message!");
                ((EditText)findViewById(R.id.txt_received)).append(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i("MQTT","Delivered successfully!");
            }
        });

        MqttConnectOptions option = new MqttConnectOptions();
        option.setCleanSession(false);
        option.setUserName(user);
        option.setPassword(pw.toCharArray());



        try{
            mqttClient.connect(option, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTT", "Connection established!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Failed to connect!");
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }

        ((TextView)findViewById(R.id.txt_topic)).setText(topic);
    }

    public void publish(View v){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(((EditText)findViewById(R.id.txt_sent)).getText().toString().getBytes(StandardCharsets.UTF_8));
            if(fieldsValid()) {
                String temp = ("Channel: " + ((EditText) findViewById(R.id.txt_topic)).getText().toString() + "\nUser: " + ((EditText) findViewById(R.id.txt_client)).getText().toString() + "\n" + ((EditText) findViewById(R.id.txt_sent)).getText().toString() + "\n\n");
                message.setPayload(temp.getBytes(StandardCharsets.UTF_8));
                mqttClient.publish("topic", message);

                Log.i("MQTT", "Message published!");
                if (!mqttClient.isConnected())
                    Log.e("MQTT", "Lost Connection!");
            }else
                Log.e("MQTT", "A field is empty!");

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void subscribe(View v){
        try {
            mqttClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTT", "Subscribe successfully!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Failed to Subscribe!");
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void unsubscribe(View v) {
        try {
            if(((EditText)findViewById(R.id.txt_topic)).getText().toString() != null) {
                mqttClient.unsubscribe(((EditText) findViewById(R.id.txt_topic)).getText().toString());
                Log.i("MQTT", "Successfully unsubscribed");
            } else
                Log.e("MQTT", "You are not subscribed!");

        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    public boolean fieldsValid(){
        return ((EditText)findViewById(R.id.txt_topic)).getText().toString() != null && ((EditText)findViewById(R.id.txt_client)).getText().toString() != null && ((EditText)findViewById(R.id.txt_sent)).getText().toString() != null;
    }
}