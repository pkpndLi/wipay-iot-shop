package com.example.wipay_iot_shop.helper;

import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.example.wipay_iot_shop.helper.data.CertificateObject;
import com.example.wipay_iot_shop.helper.data.RegisterObject;
import com.google.gson.Gson;

import java.security.KeyStore;
import java.util.UUID;

public class AWSIoTHelper implements AWSIotMqttClientStatusCallback , AWSIotMqttNewMessageCallback {
    final String LOG_TAG = "AWSIoTHelper";

    Gson gson = new Gson();
    private AWSIotMqttManager mqtt;

    AWSIoTEvent event;
    private String keyStorePath = "";
    private String keyStoreName = "";
    private String ProvisioningTemplate = "";
    private String certificatePem = "";
    private String privatekeyPem ="";

    final String REQ_CER_TOPIC = "$aws/certificates/create/json";
    final String REQ_CER_ACCEPT_TOPIC = "$aws/certificates/create/json/accepted";
    final String REQ_CER_REJECT_TOPIC = "$aws/certificates/create/json/rejected";

    private String registerTopic = "$aws/provisioning-templates/";
    private String registerTopicAccepted = "$aws/provisioning-templates/";
    private String registerTopicRejected = "$aws/provisioning-templates/";


    public AWSIoTHelper(String host, String keyStorePath, String keyStoreName, String provisioningTemplate) {

        this.keyStoreName = keyStoreName;
        this.keyStorePath = keyStorePath;
        this.ProvisioningTemplate = provisioningTemplate;

        registerTopic += provisioningTemplate + "/provision/json";
        registerTopicAccepted += provisioningTemplate + "/provision/json/accepted";
        registerTopicRejected += provisioningTemplate + "/provision/json/rejected";

        String clientID = UUID.randomUUID().toString();
        // init mqtt instance
        mqtt = new AWSIotMqttManager("device-" + clientID, host);
    }

    public AWSIotMqttManager connect(String aliases, String password) throws Exception {
        KeyStore keyStore = AWSIotKeystoreHelper.getIotKeystore(aliases, this.keyStorePath, this.keyStoreName, password);
        Log.e(LOG_TAG, "connect");
        if (keyStore != null) {
            mqtt.disconnect();
            mqtt.connect(keyStore, this);
            return mqtt;
        } else {
            throw new Exception("keystore is null");
        }
    }

    public boolean disconnect() {
        return this.mqtt.disconnect();
    }

    public void deletecer(String aliases,String password){
        AWSIotKeystoreHelper.deleteKeystoreAlias(aliases, this.keyStorePath, this.keyStoreName, password);
    }

    public void setProvisioningEvent(AWSIoTEvent event) {
        this.event = event;
    }

    public void saveKeyAndCertificate(String privateKey, String certificate, String aliases, String password) {
        if (AWSIotKeystoreHelper.isKeystorePresent(this.keyStorePath,this.keyStoreName)){
            if (!AWSIotKeystoreHelper.keystoreContainsAlias(aliases, this.keyStorePath, this.keyStoreName, password)){
                    AWSIotKeystoreHelper.deleteKeystoreAlias(aliases, this.keyStorePath, this.keyStoreName, password);
                    AWSIotKeystoreHelper.saveCertificateAndPrivateKey(aliases, certificate, privateKey, this.keyStorePath, this.keyStoreName, password);
            }
        }
        else {
            Log.i(LOG_TAG,"keystore is null");
            AWSIotKeystoreHelper.saveCertificateAndPrivateKey(aliases, certificate, privateKey, this.keyStorePath, this.keyStoreName, password);
        }
    }

    @Override
    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
        if (throwable != null)
            Log.e(LOG_TAG, "state " + throwable.getMessage() + " cause : " + throwable.getCause()+" : "+throwable);
//            Log.e(LOG_TAG,"registerTopic : "+registerTopic+"\n"+"registerTopicAccepted : "+registerTopicAccepted+"\n"+"registerTopicRejected : "+registerTopicRejected+"\n"+"REQ_CER_TOPIC : "+REQ_CER_TOPIC);
        switch (status) {
            case Connecting:
                Log.i(LOG_TAG, "Try to connect");
                if (event != null){
                    event.onStatusConnection("Try to connect");
                }
                break;
            case Connected:
                Log.i(LOG_TAG, "connected");
                if (event != null){
//                    mqtt.subscribeToTopic("$aws/things/POS_config/shadow/name/POS_CS10_config/get/accepted",AWSIotMqttQos.QOS0,this);
                    event.onStatusConnection("connected");

                }
                break;
            case ConnectionLost:
                Log.i(LOG_TAG, "connection lost");
                if (event != null){
                    event.onStatusConnection("connection lost");
                }
                break;
            case Reconnecting:
                Log.e(LOG_TAG, "reconnecting");
                if (event != null){
                    event.onStatusConnection("reconnecting");
                }
                break;
        }
    }

    @Override
    public void onMessageArrived(String topic, byte[] data) {

        String dataRecv = "";
        for (int index = 0; index < data.length; index++) {
            dataRecv += (char)data[index];
        }
        // request certificate & private key
//        Log.i(LOG_TAG,"topic Success :"+topic.equals(REQ_CER_ACCEPT_TOPIC));
        if (topic.equals(REQ_CER_ACCEPT_TOPIC)) {

            CertificateObject cer = gson.fromJson(dataRecv, CertificateObject.class);
            Log.i(LOG_TAG,"cer&key :"+dataRecv);
            certificatePem += cer.certificatePem;
            privatekeyPem += cer.privateKey;
            String formatRegister = "{\"certificateOwnershipToken\": \""+cer.certificateOwnershipToken+"\",\"parameters\": {\"SerialNumber\": \""+UUID.randomUUID().toString()+"\"}}";
            mqtt.publishString(formatRegister, registerTopic, AWSIotMqttQos.QOS0);
            if (event != null){
                event.onGetPermanentCertificate(cer);
            }

        } else if (topic.equals(REQ_CER_REJECT_TOPIC)) {
            Log.e(LOG_TAG, "cer reject");

        }

        // register certificate
        else if (topic.equals(registerTopicAccepted)) {
            RegisterObject regis = gson.fromJson(dataRecv, RegisterObject.class);
            if (event != null){
                event.onRegisterComplete(regis);
            }
//            Log.i(LOG_TAG,"register Success :"+dataRecv);


        } else if (topic.equals(registerTopicRejected)) {
            Log.i(LOG_TAG,"register Rejected :"+dataRecv);
        }
        else if(topic.equals("$aws/things/POS_config/shadow/name/POS_CS10_config/get/accepted")){
//            SetTView msg = gson.fromJson(dataRecv, SetTView.class);
            if (event != null){
                event.onSetConfig(dataRecv);
            }
        }
        else{
//            SetTView msg = gson.fromJson(dataRecv, SetTView.class);
            if (event != null){
//                event.onSetTextView(msg);
            }
            Log.i(LOG_TAG,"msg : "+dataRecv);
        }

    }
    public void publish(String msg,String topic){
        try {
            mqtt.publishString(msg, topic, AWSIotMqttQos.QOS0);
        }
        catch (Exception e){
            Log.e(LOG_TAG, "cer reject"+e);
        }
    }

    public void subscribe(String topic){
        try {
            mqtt.subscribeToTopic(topic, AWSIotMqttQos.QOS0, this);
        }
        catch (Exception e){
            Log.e(LOG_TAG, "cer reject"+e);
        }
    }
    public void loadAndRegistecerkey(){
        try {
            mqtt.subscribeToTopic(REQ_CER_ACCEPT_TOPIC, AWSIotMqttQos.QOS0, this);
            mqtt.subscribeToTopic(REQ_CER_REJECT_TOPIC, AWSIotMqttQos.QOS0, this);
            mqtt.subscribeToTopic(registerTopicAccepted, AWSIotMqttQos.QOS0, this);
            mqtt.subscribeToTopic(registerTopicRejected, AWSIotMqttQos.QOS0, this);
            mqtt.publishString("", REQ_CER_TOPIC, AWSIotMqttQos.QOS0);

        }catch (Exception e){

        }
    }
}
