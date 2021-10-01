package com.example.wipay_iot_shop.readcard;

import com.example.wipay_iot_shop.readcard.data.DataEmv;

public interface EmvEvent {
    void onGetDataCard(DataEmv dataEmvEvent);
}
