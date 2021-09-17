package com.example.wipay_iot_shop.emv;

import android.content.Context;
import android.util.Log;

import com.example.wipay_iot_shop.emv.data.DataEmv;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.StringUtil;
import vpos.keypad.EMVCOHelper;


public class EmvThread extends Thread {

    String Tag5A_data = "";
    String Tag57_data = "";
    String PAN = "";
    String Tag95_data = "";
    String strEmvStatus = "";

    public static final int TYPE_TEST_EMV = 1;
    public static final int TYPE_PIN_BLOCK = 2;
    public static final int TYPE_SHOW_PAD = 3;
    public static final int TYPE_TEST_PK = 4;
    boolean isOpen = false;

    String McrData = "";
    byte track1[] = new byte[250];
    byte track2[] = new byte[250];
    byte track3[] = new byte[250];
    String PaypssTag57_data = "";

    EmvEvent event;
    Gson gson = new Gson();

    byte[] capkbuf0_M = {};
    byte[] capkbuf1_V = {};
    String EMV_V_Capkinput1 = "9F0605A000000003" +
            "9F220108" +
            "DF070101" +
            "DF060101" +
            "DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B" +
            "DF040103" +
            "DF05083230323431323331" +
            "DF031420D213126955DE205ADC2FD2822BD22DE21CF9A8";
    byte[] Visaaid0 = {};
    String AID_input0 =  "9F0607A0000000031010" + //(AID)
            "DF010101" + //Apply selection indicators, complete or partially matched
            "DF1105DC4000A800" + //(TAC-Default)
            "DF1205DC4000A800" + //(TAC-Online)
            "DF13050010000000" + //(TAC-Denia)
            "DF14039F3704" + //(Default DDOL)
            "DF150400000000" + //(Threshold Value for Biased Random Selection)
            "DF160100" + //(Maximum target percentage)
            "DF170100" + //(Target percentage)
            "9F1B0400000000" + //Terminal Floor Limit
            "9F09020020" + //(Application Version Number)
            "9F15022701" + //(Merchant Category Code)
            "9F160F303030303030303030303030303030" + //(Merchant Identifier)
            "9F4E085465726D696E616C" + //(Merchant Name and Location)
            "9F1C084261636B39363132" + //Terminal ID
            "9F1D0101" + //Terminal Risk Management Data
            "5F360102" + //Transaction Currency Exponent
            "9F3C020764" + //Transaction Reference Currency Code
            "9F3D0102" + //Transaction Reference Currency Exponent
            "5F2A020764" + //Transaction Currency Code
            "9F0106001234567891" + //(Acquirer Identifie)
            "DF180101";// (AID Online capability)

    byte[] TermParabuf = {};
    String EMV_Term_input =
            "DF18070040F8F0FFF2A0" + //ICS
                    "9F350122" + //terminal type
                    "9F3303E0D8C8" + //terminal performance
                    "9F4005A000F0E001" + //terminal added performance
                    "DF260F9F02065F2A029A039C0195059F3704" + //default TDOL
                    "DF4001FF" + //script length limit
                    "9F1A03015601" + //terminal country code
                    "9F1E081122334455667788" + //interface equipment serialnumber
                    "DF420101" + //whether support compulsory acceptance
                    "DF430101" + //whether support compulsory online
                    "DF440100" + //Whether support account choose
                    "DF450100" + //whether it support skip password"
                    "DF460101"; //whether print all the time

    String AID_input_paywave =  "9F0607A0000000031010" + //(AID)
            "DF010101" + //Apply selection indicators, complete or partially matched
            "DF1105DC4000A800" + //(TAC-Default)
            "DF1205DC4000A800" + //(TAC-Online)
            "DF13050010000000" + //(TAC-Denia)
            "DF14039F3704" + //(Default DDOL)
            "DF150400000000" + //(Threshold Value for Biased Random Selection)
            "DF160100" + //(Maximum target percentage)
            "DF170100" + //(Target percentage)
            "9F1B0400000000" + //Terminal Floor Limit
            "9F09020020" + //(Application Version Number)
            "9F15022701" + //(Merchant Category Code)
            "9F160F303030303030303030303030303030" + //(Merchant Identifier)
            "9F4E085465726D696E616C" + //(Merchant Name and Location)
            "9F1C084261636B39363132" + //Terminal ID
            "9F1D0101" + //Terminal Risk Management Data
            "5F360102" + //Transaction Currency Exponent
            "9F3C020764" + //Transaction Reference Currency Code
            "9F3D0102" + //Transaction Reference Currency Exponent
            "5F2A020764" + //Transaction Currency Code
            "9F0106001234567891" + //(Acquirer Identifie)
            "DF180101" + // (AID Online capability)
            "DF1906000010000000" + //contactless offline transaction limit amount
            "DF2006001000000000" + //contactless transaction limit amount
            "DF2106000000001000" + //contactless transaction CVM limit amount
            "9F7B06999999999999" ; //Electronic cash transaction limit amount
    int TagCardEXD_len;
    private byte[] EXDData;
    private int TagCardEXD;
    private String Tag5F24_data;

    public void setEmvEvent(EmvEvent event) {
        this.event = event;

    }

    private EMVCOHelper emvcoHelper = EMVCOHelper.getInstance();
    boolean m_bThreadFinished = true;
    private boolean bIsBack = false;
    int mCardType = -1;

    interface IBackFinish {
        void isBack();
    }

    IBackFinish mIBackFinish;

    public void setIBackFinish(IBackFinish mIBackFinish) {
        this.mIBackFinish = mIBackFinish;
    }

    int type = 0;
    Context context;
    int amount = 0;

    public EmvThread(int type, Context context, int amount) {
        this.type = type;
        this.context = context;
        this.amount = amount;
    }

    public boolean isThreadFinished() {
        return m_bThreadFinished;
    }

    public void run() {
        synchronized (this) {

            m_bThreadFinished = false;
            int ret = 0;


            switch (type) {

                case TYPE_TEST_EMV:

                    byte picc_mode = 'M';
                    byte cardtype[] = new byte[3];
                    byte serialNo[] = new byte[50];
                    byte ATR[] = new byte[40];
                    byte PaypassTagBuff[] = new byte[1024];

                    final long time = System.currentTimeMillis();
                    PosApiHelper.getInstance().EntryPoint_Open();
                    while (System.currentTimeMillis() < time + 30 * 1000) {
                        if (bIsBack) {
                            Log.e("VPOS", "*****************loop detecting bIsBack 11");
                            m_bThreadFinished = true;
                            return;
                        }

                        setIBackFinish(new IBackFinish() {
                            public void isBack() {
                                Log.e("VPOS", "*************setIBackFinish loop");
                                m_bThreadFinished = true;
                                return;
                            }
                        });

                        int index = 0;
                        String CheckCard_data = "";
                        mCardType = PosApiHelper.getInstance().EntryPoint_Detect();
                        Log.e("VPOS", "EntryPoint_Detect mCardType== " + mCardType);
                        if (mCardType >= 0) {
                            break;
                        } else {
                            Log.e("VPOS", "*************loop detecting return ");

                            PosApiHelper.getInstance().EntryPoint_Close();

                            m_bThreadFinished = true;
                            return;
                        }
                    }

                    Log.e("VPOS", "*************loop detecting return 00");
                    PosApiHelper.getInstance().EntryPoint_Close();
                    Log.e("VPOS", "*************loop detecting return11 ");


                    if (mCardType == -1) {
                        Log.e("VPOS", "detect card time out.");
                        m_bThreadFinished = true;
                        return;
                    } else if (mCardType == 0) {
                        McrData = "";
                        Log.e("Mcrtest", "Mcrtest start00");

                        PosApiHelper posApiHelper = PosApiHelper.getInstance();
                        Log.e("Mcrtest", "Mcrtest start11");
                        Arrays.fill(track1, (byte) 0x00);
                        Arrays.fill(track2, (byte) 0x00);
                        Arrays.fill(track3, (byte) 0x00);
                        Log.e("Mcrtest", "Mcrtest start22");
                        ret = posApiHelper.McrRead((byte) 0, (byte) 0, track1, track2, track3);
                        Log.e("Mcrtest", "Mcrtest start44=" + ret);
                        if (ret >= 0) {
                            if ((ret & 0x01) == 0x01) {
                                McrData = "track1:" + new String(track1).trim();
                            }
                            if ((ret & 0x02) == 0x02) {
                                McrData = McrData + "\n\ntrack2:" + new String(track2).trim();
                            }
                            if ((ret & 0x04) == 0x04) {
                                McrData = McrData + "\n\ntrack3:" + new String(track3).trim();
                            }
                        } else {
                            McrData = "Lib_MsrRead check data error";
                        }
                        posApiHelper.McrClose();
                    } else
                        ///*******************-----EMV contact---******************************///
                        if (mCardType == 1) {
                            Tag5A_data = "";
                            Tag5F24_data = "";

                            PosApiHelper.getInstance().SysLogSwitch(1);
                            emvcoHelper.EmvEnvParaInit();  // 1
                            emvcoHelper.EmvClearAllCapks();// 2
                            emvcoHelper.EmvClearAllAIDS(); // 2
                            capkbuf1_V = StringUtil.hexStringToBytes(EMV_V_Capkinput1);
                            emvcoHelper.EmvAddOneCAPK(capkbuf1_V, capkbuf1_V.length);
                            Visaaid0 = StringUtil.hexStringToBytes(AID_input0);
                            emvcoHelper.EmvAddOneAIDS(Visaaid0, Visaaid0.length);
                            TermParabuf = StringUtil.hexStringToBytes(EMV_Term_input);
                            emvcoHelper.EmvSaveTermParas(TermParabuf, TermParabuf.length, 0);
                            Log.e("VPOS", "ERROR    : EmvGetErrCode = " + ret);

                            ret = emvcoHelper.EmvKeyPadInit(context);
                            emvcoHelper.SetPinPadTime(20);  //set pinpad timeout is 20 seconds
                            if (ret != 0) {
                                m_bThreadFinished = true;
                                return;
                            }
                            short TagCardNo = 0x5A;
                            int TagCardNo_len;
                            byte CardNoData[] = new byte[56];

                            int TagCardEXD_len;
                            byte EXDData[] = new byte[56];
                            int TagCardEXD = 0x5F24;

                            int TagPIN = 0xBD;
                            int TagKSN = 0xDF7F;
                            int PinData_len, KsnData_len;
                            byte PinData[] = new byte[56];
                            byte KsnData[] = new byte[56];
                            //        emvcoHelper.SetPinPadType(0);
                            emvcoHelper.SetPinPadType(1);
                            emvcoHelper.EmvKernelInit();
                            emvcoHelper.EmvSetTransType(1);
                            emvcoHelper.EmvSetTransAmount(amount*100);
                            emvcoHelper.EmvSetCardType(1);
                            emvcoHelper.SetAutoAddKSNPIN(1);
                            ret = emvcoHelper.EmvProcess(1, 0);  //The FLOWTYPE value is 1- simplifies the process
                            Log.e("VPOS", "EmvProcess ret = " + ret);
//*/
//                            ret = emvcoHelper.EmvGetErrCode();
                            Log.e("VPOS", "EmvGetErrCode = " + ret);

                            if (ret < 0) {
                                Log.e("VPOS", "EMV Termination");
                                m_bThreadFinished = true;
                            } else if (ret == 3) {
                                Log.e("VPOS", "EMV  GOONLINE");
                            }
                            TagCardNo_len = emvcoHelper.EmvGetTagData(CardNoData, 56, TagCardNo);
                            TagCardEXD_len = EMVCOHelper.EmvGetTagData(EXDData, 56, TagCardEXD);
                            for (int i = 0; i < TagCardNo_len; i++) {
                                Log.e("CardNoData", "i = " + i + "  " + CardNoData[i]);
                                Tag5A_data += ByteUtil.byteToHexString(CardNoData[i] /*& 0xFF*/);
                            }
                            for (int i = 0; i < TagCardEXD_len; i++) {
                                Log.e("EXDData", "i = " + i + "  " + EXDData[i]);
                                Tag5F24_data += ByteUtil.byteToHexString(EXDData[i] /*& 0xFF*/);
                            }
                            PinData_len = emvcoHelper.EmvGetTagData(PinData, 56, TagPIN);
                            final String TagPin_data = ByteUtil.bytearrayToHexString(PinData, PinData_len);
                            KsnData_len = emvcoHelper.EmvGetTagData(KsnData, 56, TagKSN);
                            final String Ksn_data = ByteUtil.bytearrayToHexString(KsnData, KsnData_len);
                            final int bypass = emvcoHelper.EmvPinbyPass();
                            String newTag5F24 = "";
                            char[] EXD = Tag5F24_data.toCharArray();
                            for (int i = 0; i < 4; i++) {
                                ;
//                                newTag5F24 += EXD[i];
                            }
                            Log.e("VPOS", strEmvStatus + "\nCardNO:" + Tag5A_data + "\n" + "PIN0:" + TagPin_data + "\n" + "KSN0:" + Ksn_data);
                            Log.e("EMV PinData", "-TagPin_data=----" + TagPin_data);
                            String Object = "{\"cardNO\": \"" + Tag5A_data + "\",\"cardEXD\": \"" + newTag5F24 + "\"}";
                            DataEmv emvTag = gson.fromJson(Object, DataEmv.class);
//                                Log.i("test",emvTag.cardNO+"\t"+emvTag.cardEXD);
                            if (event != null) {
                                event.onGetDataCard(emvTag);
                            }

                            emvcoHelper.EmvFinal();


                        }
                        ///*******************----Contactless-Quics and PayWave---******************************///
                        else if (mCardType == 3) {

                            PosApiHelper.getInstance().SysLogSwitch(1);

                            //paywave
                            emvcoHelper.PayWaveKernelInit();
                            emvcoHelper.PayWaveClearAllTerm();
                            emvcoHelper.PayWaveClearAllCapk();
                            emvcoHelper.PayWaveClearAllAIDS();
                            emvcoHelper.PayWaveAddAids(AID_input_paywave);
                            emvcoHelper.PayWaveAddCapks(EMV_V_Capkinput1);
                            emvcoHelper.PayWaveAddTerms(EMV_Term_input);

                            Log.e("paywaveunipay", "paywaveunipay0000");
                            Tag5A_data = "";
                            ret = emvcoHelper.EmvKeyPadInit(context);
                            emvcoHelper.SetPinPadTime(20);  //set pinpad timeout is 20 seconds
                            if (ret != 0) {
                                m_bThreadFinished = true;
                                return;
                            }
                            short TagCardNo = 0x57;
                            short TagTvr = 0x95;
                            int TagCardNo_len;
                            int TagTVR_len;
                            byte CardNoData[] = new byte[56];
                            byte TVRData[] = new byte[56];
                            short TagPIN = 0xBD;
                            int PinData_len;
                            byte PinData[] = new byte[56];
                            byte result[] = new byte[2];


                            emvcoHelper.PayWaveSetTransType(0x00);
                            emvcoHelper.PayWaveSetTransAmount(11000);
//                            emvcoHelper.PayWaveSetOtherTransAmount(11000);

                            Log.e("liuhaoPayWave", "PayWave TEST");

                            ret = emvcoHelper.PayWaveTransProcess();

                            Log.e("liuhaoPayWave", "ret = " + ret);
                            if (ret < 0) {
                                Log.e("VPOS", "ret = " + ret);
                                emvcoHelper.PayWaveFinal();
                                m_bThreadFinished = true;
//                                    return;

                            } else if (ret == 22) {
                                strEmvStatus = "EMV  GOONLINE";
                            } else if (ret == 101) {
                                strEmvStatus = "EMV_ACCEPTED_OFFLINE";
                            } else if (ret == 102) {
                                strEmvStatus = "EMV_DENIALED_OFFLINE";

                            }

                            Log.e("VPOS", "strEmvStatus = " + strEmvStatus);


                            int pinkey_n0 = 1;
                            int timeout_s0 = 10;
                            byte[] card_no0 = PAN.getBytes(); //123456789012345678
                            byte[] mode0 = new byte[]{1};
                            byte[] pin_block0 = new byte[8];
                            //    emvcoHelper.EmvSetPtcCounter(2);

                            //     ret = emvcoHelper.EmvGetPinBlock(EmvTestActivity.this, 1, pinkey_n0, card_no0, mode0, pin_block0, timeout_s0);

                            Log.e("Robert EmvGetPinBlock", "EmvGetPinBlock ret=  " + ret);

                            TagCardNo_len = emvcoHelper.PayWaveGetTagData(CardNoData, 56, TagCardNo);
                            for (int i = 0; i < TagCardNo_len; i++) {
                                Log.e("Robert CardNoData", "i = " + i + "  " + CardNoData[i]);
                                Tag57_data += ByteUtil.byteToHexString(CardNoData[i] /*& 0xFF*/);
                            }

                            if (TagCardNo_len % 2 != 0) {
                                Tag57_data = Tag57_data.substring(0, TagCardNo_len * 2 - 1);
                            }
                            Log.e("Robert Tag57", "-Tag57_data=----" + Tag57_data);

                            PAN = getContactlessPan(Tag57_data);

                            TagTVR_len = emvcoHelper.PayWaveGetTagData(TVRData, 56, TagTvr);
                            for (int i = 0; i < TagTVR_len; i++) {
                                Log.e("TagTvr", "i = " + i + "  " + TVRData[i]);
                                Tag95_data += ByteUtil.byteToHexString(TVRData[i] /*& 0xFF*/);
                            }
                            if (TagTVR_len % 2 != 0) {
                                Tag95_data = Tag95_data.substring(0, TagTVR_len * 2);
                            }
                            Log.e("Tag95", "-Tag95_data=----" + Tag95_data);

//
                            PinData_len = emvcoHelper.PayWaveGetTagData(PinData, 56, TagPIN);

                            String TagPin_data = "";
                            for (int i = 0; i < PinData_len; i++) {
                                Log.e("EMV PinData", "i = " + i + "  " + PinData[i]);
                                TagPin_data += ByteUtil.byteToHexString(PinData[i] /*& 0xFF*/);
                            }
//                        final String TagPin_data = new String(PinData, 0, PinData_len);
                            final String finalTagPin_data = ByteUtil.hexStr2Str(TagPin_data);
                            Log.e("VPOS", strEmvStatus + "\n\nCardNO:" + PAN + "\n\n" + "TVR:" + Tag95_data);
                            Log.e("EMV PinData", "-TagPin_data=----" + TagPin_data);


                            emvcoHelper.PayWaveFinal();

                        }
                        ///*******************---if (mCardType == 2) {---******************************///

                        else if (mCardType == 2) {

//                            PosApiHelper.getInstance().SysLogSwitch(1);
//
//                            //paypass
//                            emvcoHelper.PaypassKernelInit();//---step 1
//                            //   emvcoHelper.PaypassAidSet(Aid_input0);//---step 2
//                            emvcoHelper.PaypassAidSet(PaypassAid_input);//
//                            emvcoHelper.PaypassAidSet(Aid_inputT);//
//                            emvcoHelper.PaypassCapkSet(Capk_input); //---step 3
//                            emvcoHelper.PaypassCapkSet(Capk_input1); //---step 3
//                            emvcoHelper.PaypassCapkSet(Capk_input2); //---step 3
//                            emvcoHelper.PaypassReaderSet(Reader_input); //---step 4
//                            emvcoHelper.PaypassKernelSet(Kernel_input); //---step 5
//                            emvcoHelper.PaypassTransSet(Trans_input); //---step 6
//
//                            Log.e("Paypass", "Paypass Kernel Test");
//                            int TagName = 0xDF8129;
//
//                            int result = emvcoHelper.PaypassProcess();
//                            Log.e("Paypass", "Paypass PaypassProcess ret->" + result);
//
//                            int Data_len = emvcoHelper.PaypassGetTagValue(PaypassTagBuff, 1024, TagName);
//                            Log.e("Paypass", "Paypass PaypassGetTagValue" + Data_len);
//                            for (int i = 0; i < Data_len; i++) {
//                                Log.e("CardNoData", "i = " + i + "  " + PaypassTagBuff[i]);
//                                PaypssTag57_data += ByteUtil.byteToHexString(PaypassTagBuff[i] /*& 0xFF*/);
//                            }
//
//                            if (Data_len / 2 != 0) {
//                                PaypssTag57_data = PaypssTag57_data.substring(0, Data_len * 2);
//                            }
//
//
//                            emvcoHelper.PaypassFinal();
//                            Log.e("VPOS", PaypssTag57_data);
//                            emvcoHelper.EmvFinal();
                        }
                    break;


            }

            m_bThreadFinished = true;

        }
    }

    private static final Pattern track2Pattern = Pattern.compile("^([0-9]{1,19})(?:[=Dd])([0-9]{4}|=)([0-9]{3}|=).*$");
    private static final Pattern track1Pattern = Pattern.compile("^.*?([0-9]{10,19})\\^([^^]{2,26})\\^([0-9]{4}|\\^)([0-9]{3}|\\^)[^;]*(;([^?]*)\\?.*)?$");

    public static String getContactlessPan(String track2) {
        Matcher matcher = track2Pattern.matcher(track2);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

}