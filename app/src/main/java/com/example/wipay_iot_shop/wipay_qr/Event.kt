package com.example.wipay_iot_shop.wipay_qr

interface Event {
    fun onGenQRcode(QRcode: String?)
}