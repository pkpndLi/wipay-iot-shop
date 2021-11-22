package com.example.wipay_iot_shop.helper;

import com.example.wipay_iot_shop.helper.data.CertificateObject;
import com.example.wipay_iot_shop.helper.data.RegisterObject;

public interface AWSIoTEvent {
    void onGetPermanentCertificate(CertificateObject certificateObject);
    void onRegisterComplete(RegisterObject registerObject);
    void onSetConfig(String setTView);
    void onStatusConnection(String Showmsg);
}
