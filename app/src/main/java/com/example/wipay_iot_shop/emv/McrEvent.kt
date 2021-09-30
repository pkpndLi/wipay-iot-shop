package com.example.wipay_iot_shop.emv

import com.example.wipay_iot_shop.emv.data.DataMcr

interface McrEvent {
    fun onGetDataCardMagnetic(dataMagnetic: DataMcr?)
}