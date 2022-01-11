package com.example.wipay_iot_shop.printer

import vpos.apipackage.PosApiHelper

class Printer{
    var posApiHelper = PosApiHelper.getInstance()
    fun printSlip(menuName:String, totalAmount: Int?){
        val menuName = menuName
        val totalAmount = totalAmount
        var ret = posApiHelper!!.PrintInit()
        posApiHelper!!.PrintSetGray(ret)
        posApiHelper.PrintSetFont(20.toByte(), 20.toByte(), 0x00.toByte())
        posApiHelper.PrintStr("\tREPEIPE\n")
        posApiHelper.PrintStr("\tLIST\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tTOTAL\n")
        posApiHelper.PrintStr("\t$menuName\t\t\t$totalAmount")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStart()
    }
    fun printSlipSettlement(menuName:String, totalAmount: Int?){
        val menuName = menuName
        val totalAmount = totalAmount
        var ret = posApiHelper!!.PrintInit()
        posApiHelper!!.PrintSetGray(ret)
        posApiHelper.PrintSetFont(20.toByte(), 20.toByte(), 0x00.toByte())
        posApiHelper.PrintStr("\tREPEIPE\n")
        posApiHelper.PrintStr("\tsalecount\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tTOTAL\n")
        posApiHelper.PrintStr("\t$menuName\t\t\t$totalAmount")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStr("\n")
        posApiHelper.PrintStart()
    }
}
