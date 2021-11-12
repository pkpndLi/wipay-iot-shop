package com.example.wipay_iot_shop.awsiot

import android.provider.Settings
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback
import java.lang.Exception

class AwsIoT (keyStorePath:String): AWSIotMqttClientStatusCallback, AWSIotMqttNewMessageCallback{
    val keyStoreName = "Wipay_keystore"
    val aliases_M = "master"
    val aliases_C = "Clam"
    val LOG_TAG = ""
    val password = "12345678"
    val keyStorePath = keyStorePath
    val clientID = ""
    val host = ""
    // init mqtt instance

    private val mqtt: AWSIotMqttManager? = AWSIotMqttManager("device-" + clientID, host)



    @Throws(Exception::class)
    fun connect(): AWSIotMqttManager {
        val keyStore = AWSIotKeystoreHelper.getIotKeystore(
            aliases_M,
            keyStorePath,
            keyStoreName,
            password
        )
        Log.e(LOG_TAG, "connect")
        return if (keyStore != null) {
            mqtt!!.disconnect()
            mqtt.connect(keyStore, this)
            mqtt
        } else {
            throw Exception("keystore is null")
        }
    }

    fun saveKeyAndCertificate(privateKey: String?, certificate: String?, aliases: String?, password: String?) {
        if (AWSIotKeystoreHelper.isKeystorePresent(keyStorePath, keyStoreName)) {
            if (AWSIotKeystoreHelper.keystoreContainsAlias(
                    aliases_M,
                    keyStorePath, keyStoreName, password
                )
            ) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(
                        aliases_C,
                        keyStorePath, keyStoreName, password
                    )
                ){

                }
            } else {
                AWSIotKeystoreHelper.deleteKeystoreAlias(
                    aliases,
                    keyStorePath, keyStoreName, password
                )
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                    aliases, certificate, privateKey,
                    keyStorePath, keyStoreName, password
                )
            }
        } else {

            Log.i(LOG_TAG, "keystore is null")
            AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                aliases, certificate, privateKey,
                keyStorePath, keyStoreName, password
            )
        }
    }

    fun deletecer(aliases: String?, password: String?) {
        AWSIotKeystoreHelper.deleteKeystoreAlias(aliases, keyStorePath, keyStoreName, password)
    }

    override fun onStatusChanged(status: AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus?,throwable: Throwable?) {
        if (throwable != null)
            Log.e(LOG_TAG, "state " + throwable.message + " cause : " + throwable.cause + " : " + throwable)
//            Log.e(LOG_TAG,"registerTopic : "+registerTopic+"\n"+"registerTopicAccepted : "+registerTopicAccepted+"\n"+"registerTopicRejected : "+registerTopicRejected+"\n"+"REQ_CER_TOPIC : "+REQ_CER_TOPIC);
        when (status) {
            AWSIotMqttClientStatus.Connecting -> {
                Log.i(LOG_TAG, "Try to connect")
            }
            AWSIotMqttClientStatus.Connected -> {
                Log.i(LOG_TAG, "connected")
            }
            AWSIotMqttClientStatus.ConnectionLost -> {
                Log.i(LOG_TAG, "connection lost")
            }
            AWSIotMqttClientStatus.Reconnecting -> {
                Log.e(LOG_TAG, "reconnecting")
            }

        }
    }

    override fun onMessageArrived(topic: String?, msg: ByteArray?) {
        var dataRecv = ""
        if (msg != null) {
            for (i in msg.indices) {
                dataRecv += msg.get(i) as Char
            }
        }
        when(topic){

        }



    }


}