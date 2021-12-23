package com.example.wipay_iot_shop.wipay_qr.data

import java.util.*

data class GenQRcode (
    val mutation : mutation
)
data class mutation(
    val generateShopQrCode : generateShopQrCode,
    val qrCode : qrCode?
)
data class generateShopQrCode(
    val input : input
)
data class input(
    val terminalRefId:String,
    val reference1:String,
    val reference2:String,
    val amount:String,
    val currency:String
)
data class qrCode(
    val qrCode:String?
)