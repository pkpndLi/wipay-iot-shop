package com.example.wipay_iot_shop.emv;

import com.example.wipay_iot_shop.emv.data.DataEmv;

public interface EmvEvent {
    void onGetDataCard(DataEmv dataEmvEvent);
}
