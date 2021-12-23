package com.example.wipay_iot_shop.wipay_qr.data

data class QRcodeData(
    val data:data
    )
data class data(
    val generateShopQrCode : generateShopQrCode
)
data class generateShopQrCode(
    val qrCode:String
)
