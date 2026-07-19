package com.nuls;

import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.ISO7816;
import javacard.framework.APDU;

public class MyAppl extends Applet {
	
	private boolean isPersonalized = false;
    private static final byte[] PASSWORD = {(byte)'P', (byte)'A', (byte)'S', (byte)'S'};
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new com.nuls.MyAppl().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) {
        if (selectingApplet()) return;
        
        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];

        switch (ins) {
            case (byte) 0x10: // Персонализация
                if (isPersonalized) ISOException.throwIt((short) 0x6986);
                
                // 1. Получаем все входящие данные
                short bytesReceived = apdu.setIncomingAndReceive();
                
                // 2. ЖЕСТКАЯ ПРОВЕРКА: пароль должен быть ровно 4 байта
                if (bytesReceived != (short) PASSWORD.length) {
                    ISOException.throwIt((short) 0x6700); // Ошибка длины
                }
                
                // 3. Сравниваем (OFFSET_CDATA = 5, там лежат данные после заголовка)
                if (Util.arrayCompare(buffer, ISO7816.OFFSET_CDATA, PASSWORD, (short) 0, (short) PASSWORD.length) == 0) {
                    isPersonalized = true;
                    return; // Успех, вернет 90 00
                } else {
                    ISOException.throwIt((short) 0x6300); // Ошибка пароля
                }
                break;

            case (byte) 0x20: // Статус
                buffer[0] = isPersonalized ? (byte) 0x01 : (byte) 0x00;
                apdu.setOutgoingAndSend((short) 0, (short) 1);
                break;
            case (byte) 0x00: // Hello
                if (!isPersonalized) ISOException.throwIt((short) 0x6985);
                byte[] data = {'H','e','l','l','o',' ','p','e','o','p','l','e'};
                Util.arrayCopyNonAtomic(data, (short) 0, buffer, (short) 0, (short) data.length);
                apdu.setOutgoingAndSend((short) 0, (short) data.length);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
	}
}