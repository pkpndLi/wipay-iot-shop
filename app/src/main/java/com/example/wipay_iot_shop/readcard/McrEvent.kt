package com.example.wipay_iot_shop.readcard

import com.example.wipay_iot_shop.readcard.data.DataMcr

interface McrEvent {
    fun onGetDataCardMagnetic(dataMagnetic: DataMcr?)
}