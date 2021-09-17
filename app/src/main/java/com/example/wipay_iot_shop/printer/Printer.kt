package com.example.wipay_iot_shop.printer

import vpos.apipackage.PosApiHelper

class Printer(menuName:String, totalAmount: Int?){
    var posApiHelper = PosApiHelper.getInstance()
    val menuName = menuName
    val totalAmount = totalAmount
    fun printSlip(){
        var ret = posApiHelper!!.PrintInit()
        posApiHelper!!.PrintSetGray(ret)
        posApiHelper.PrintSetFont(24.toByte(), 24.toByte(), 0x00.toByte())
        posApiHelper.PrintStr("\ttest print slip\n")
        posApiHelper.PrintStr("\tLIST\t\t\t\t\t\t\t\t\t\t\t\t\tTOTAL\n")
        posApiHelper.PrintStr("\t$menuName\t\t\t\t\t\t\t$totalAmount")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStart()
    }
}