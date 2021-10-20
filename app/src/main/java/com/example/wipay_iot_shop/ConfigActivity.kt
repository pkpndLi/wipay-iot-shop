package com.example.wipay_iot_shop

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wipay_iot_shop.readcard.EmvThread
import com.example.wipay_iot_shop.readcard.EmvThread.TYPE_TEST_EMV
import vpos.apipackage.PosApiHelper
import vpos.apipackage.StringUtil
import vpos.keypad.EMVCOHelper

class ConfigActivity : AppCompatActivity(),View.OnClickListener {
    lateinit var editText_DF18_T : EditText
    lateinit var editText_9F35 : EditText
    lateinit var editText_9F33 : EditText
    lateinit var editText_9F40 : EditText
    lateinit var editText_DF26 : EditText
    lateinit var editText_DF40 : EditText
    lateinit var editText_9F1A : EditText
    lateinit var editText_9F1E : EditText
    lateinit var editText_DF42 : EditText
    lateinit var editText_DF43 : EditText
    lateinit var editText_DF44 : EditText
    lateinit var editText_DF45 : EditText
    lateinit var editText_DF46 : EditText
    lateinit var editText_9F06 : EditText
    lateinit var editText_DF01 : EditText
    lateinit var editText_DF11 : EditText
    lateinit var editText_DF12 : EditText
    lateinit var editText_DF13 : EditText
    lateinit var editText_DF14 : EditText
    lateinit var editText_DF15 : EditText
    lateinit var editText_DF16 : EditText
    lateinit var editText_DF17 : EditText
    lateinit var editText_9F1B : EditText
    lateinit var editText_9F09 : EditText
    lateinit var editText_9F15 : EditText
    lateinit var editText_9F16 : EditText
    lateinit var editText_9F4E : EditText
    lateinit var editText_9F1C : EditText
    lateinit var editText_9F1D : EditText
    lateinit var editText_5F36 : EditText
    lateinit var editText_9F3C : EditText
    lateinit var editText_9F3D : EditText
    lateinit var editText_5F2A : EditText
    lateinit var editText_9F01 : EditText
    lateinit var editText_DF18_A : EditText
    lateinit var editText_DF19 : EditText
    lateinit var editText_DF20 : EditText
    lateinit var editText_DF21 : EditText
    lateinit var editText_9F7B : EditText
    lateinit var editText_GetTag : EditText
    lateinit var btn_Clear : Button
    lateinit var btn_Add : Button
    lateinit var btn_OK: Button
    private var emvThread: EmvThread? = null
    var MY_PERMISSIONS_STORAGE = arrayOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
    )

    val REQUEST_EXTERNAL_STORAGE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        setView()
        requestPermission()
        PosApiHelper.getInstance().SysLogSwitch(1)

    }
    fun setView(){
        editText_DF18_T = findViewById(R.id.editText_DF18_T)
        editText_9F35 = findViewById(R.id.editText_9F35)
        editText_9F33 = findViewById(R.id.editText_9F33)
        editText_9F40 = findViewById(R.id.editText_9F40)
        editText_DF26 = findViewById(R.id.editText_DF26)
        editText_DF40 = findViewById(R.id.editText_DF40)
        editText_9F1A = findViewById(R.id.editText_9F1A)
        editText_9F1E = findViewById(R.id.editText_9F1E)
        editText_DF42 = findViewById(R.id.editText_DF42)
        editText_DF43 = findViewById(R.id.editText_DF43)
        editText_DF44 = findViewById(R.id.editText_DF44)
        editText_DF45 = findViewById(R.id.editText_DF45)
        editText_DF46 = findViewById(R.id.editText_DF46)
        editText_9F06 = findViewById(R.id.editText_9F06)
        editText_DF01 = findViewById(R.id.editText_DF01)
        editText_DF11 = findViewById(R.id.editText_DF11)
        editText_DF12 = findViewById(R.id.editText_DF12)
        editText_DF13 = findViewById(R.id.editText_DF13)
        editText_DF14 = findViewById(R.id.editText_DF14)
        editText_DF15 = findViewById(R.id.editText_DF15)
        editText_DF16 = findViewById(R.id.editText_DF16)
        editText_DF17 = findViewById(R.id.editText_DF17)
        editText_9F1B = findViewById(R.id.editText_9F1B)
        editText_9F09 = findViewById(R.id.editText_9F09)
        editText_9F15 = findViewById(R.id.editText_9F15)
        editText_9F16 = findViewById(R.id.editText_9F16)
        editText_9F4E = findViewById(R.id.editText_9F4E)
        editText_9F1C = findViewById(R.id.editText_9F1C)
        editText_9F1D = findViewById(R.id.editText_9F1D)
        editText_5F36 = findViewById(R.id.editText_5F36)
        editText_9F3C = findViewById(R.id.editText_9F3C)
        editText_9F3D = findViewById(R.id.editText_9F3D)
        editText_5F2A = findViewById(R.id.editText_5F2A)
        editText_9F01 = findViewById(R.id.editText_9F01)
        editText_DF18_A = findViewById(R.id.editText_DF18_A)
        editText_DF19 = findViewById(R.id.editText_DF19)
        editText_DF20 = findViewById(R.id.editText_DF20)
        editText_DF21 = findViewById(R.id.editText_DF21)
        editText_9F7B = findViewById(R.id.editText_9F7B)
        editText_GetTag = findViewById(R.id.editText_GetTag)

        btn_Clear = findViewById(R.id.btn_Clear)
        btn_Add = findViewById(R.id.btn_Add)
        btn_OK = findViewById(R.id.btn_OK)

        btn_Clear.setOnClickListener(this)
        btn_Add.setOnClickListener(this)
        btn_OK.setOnClickListener(this)

    }

    private fun requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        val checkCallPhonePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(
                this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        } else {

        }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn_Clear->{
//                PosApiHelper.getInstance().SysLogSwitch(1)
                EMVCOHelper.EmvEnvParaInit()
                EMVCOHelper.EmvClearAllCapks()
                EMVCOHelper.EmvClearAllAIDS()
            }
            R.id.btn_Add->{
                addAID()
            }
            R.id.btn_OK->{
            }
        }
    }

    fun addAID(){
        var capk: ByteArray? = byteArrayOf()
        var AID = byteArrayOf()
        var Term = byteArrayOf()
        var TagDF18_T: String = editText_DF18_T.text.toString()
        var Tag9F35: String = editText_9F35.text.toString()
        var Tag9F33: String = editText_9F33.text.toString()
        var Tag9F40: String = editText_9F40.text.toString()
        var TagDF26: String = editText_DF26.text.toString()
        var TagDF40: String = editText_DF40.text.toString()
        var Tag9F1A: String = editText_9F1A.text.toString()
        var Tag9F1E: String = editText_9F1E.text.toString()
        var TagDF42: String = editText_DF42.text.toString()
        var TagDF43: String = editText_DF43.text.toString()
        var TagDF44: String = editText_DF44.text.toString()
        var TagDF45: String = editText_DF45.text.toString()
        var TagDF46: String = editText_DF46.text.toString()
        var Tag9F06: String = editText_9F06.text.toString()
        var TagDF01: String = editText_DF01.text.toString()
        var TagDF11: String = editText_DF11.text.toString()
        var TagDF12: String = editText_DF12.text.toString()
        var TagDF13: String = editText_DF13.text.toString()
        var TagDF14: String = editText_DF14.text.toString()
        var TagDF15: String = editText_DF15.text.toString()
        var TagDF16: String = editText_DF16.text.toString()
        var TagDF17: String = editText_DF17.text.toString()
        var Tag9F1B: String = editText_9F1B.text.toString()
        var Tag9F09: String = editText_9F09.text.toString()
        var Tag9F15: String = editText_9F15.text.toString()
        var Tag9F16: String = editText_9F16.text.toString()
        var Tag9F4E: String = editText_9F4E.text.toString()
        var Tag9F1C: String = editText_9F1C.text.toString()
        var Tag9F1D: String = editText_9F1D.text.toString()
        var Tag5F36: String = editText_5F36.text.toString()
        var Tag9F3C: String = editText_9F3C.text.toString()
        var Tag9F3D: String = editText_9F3D.text.toString()
        var Tag5F2A: String = editText_5F2A.text.toString()
        var Tag9F01: String = editText_9F01.text.toString()
        var TagDF18_A: String = editText_DF18_A.text.toString()
        var TagDF19: String = editText_DF19.text.toString()
        var TagDF20: String = editText_DF20.text.toString()
        var TagDF21: String = editText_DF21.text.toString()
        var Tag9F7B: String = editText_9F7B.text.toString()
        var Capkinput = "9F0605A000000003" +
                "9F220108" +
                "DF070101" +
                "DF060101" +
                "DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B" +
                "DF040103" +
                "DF05083230323431323331" +
                "DF031420D213126955DE205ADC2FD2822BD22DE21CF9A8"
        var AIDinput :String = Tag9F06+TagDF01+TagDF11+TagDF12+TagDF13+TagDF14+TagDF15+TagDF16+TagDF17+Tag9F1B+
                Tag9F09+Tag9F15+Tag9F16+Tag9F4E+Tag9F1C+Tag9F1D+Tag5F36+Tag9F3C+Tag9F3D+Tag5F2A+Tag9F01+TagDF18_A+
                TagDF19+TagDF20+TagDF21+Tag9F7B
        var Terminput :String = TagDF18_T+Tag9F35+Tag9F33+Tag9F40+TagDF26+TagDF40+Tag9F1A+Tag9F1E+TagDF42+TagDF43+TagDF44+TagDF45+TagDF46

//        Log.i("test",AIDinput+"\n"+Terminput)

        capk = StringUtil.hexStringToBytes(Capkinput)
        EMVCOHelper.EmvAddOneCAPK(capk, capk.size)
        AID = StringUtil.hexStringToBytes(AIDinput)
        EMVCOHelper.EmvAddOneAIDS(AID, AID.size)
        Term = StringUtil.hexStringToBytes(Terminput)
        EMVCOHelper.EmvSaveTermParas(Term, Term.size, 0)

    }
}