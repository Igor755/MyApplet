package com.nuls;

import javacard.framework.*;
import sim.toolkit.*;

/**
 * 
 * # INSTANCE AID апплета a0000005591010ffffffff8900000100 # AID пакета
 * a000000559112233445566 # AID апплета a000000559111222
 * 
 */
public class MyAppl extends Applet implements ToolkitConstants, ToolkitInterface {

	private boolean isPersonalized = false;
	private static final byte[] PASSWORD = { (byte) '1', (byte) '1', (byte) '1', (byte) '1' };

	// Переменные для меню
	private byte menuItem1, menuItem2, menuItem3;

	private static final byte[] MENU_RESET_PERSONALIZE = { (byte) 'R', (byte) 'e', (byte) 's', (byte) 'e', (byte) 't',
			(byte) ' ', (byte) 'P', (byte) 'e', (byte) 'r', (byte) 's', (byte) 'o', (byte) 'n', (byte) 'a', (byte) 'l',
			(byte) 'i', (byte) 'z', (byte) 'e' };

	private static final byte[] MENU_SET_PASSWORD = { (byte) 'E', (byte) 'n', (byte) 't', (byte) 'e', (byte) 'r',
			(byte) ' ', (byte) 'P', (byte) 'a', (byte) 's', (byte) 's', (byte) 'w', (byte) 'o', (byte) 'r',
			(byte) 'd' };

	private static final byte[] MENU_PROFILE_INFO = { (byte) 'P', (byte) 'r', (byte) 'o', (byte) 'f', (byte) 'i',
			(byte) 'l', (byte) 'e', (byte) ' ', (byte) 'I', (byte) 'n', (byte) 'f', (byte) 'o' };

	byte[] writePasswordText = { (byte) 'W', (byte) 'r', (byte) 'i', (byte) 't', (byte) 'e', (byte) ' ', (byte) 'P',
			(byte) 'a', (byte) 's', (byte) 's', (byte) 'w', (byte) 'o', (byte) 'r', (byte) 'd' };

	byte[] successTextPassword = { (byte) 's', (byte) 'u', (byte) 'c', (byte) 'c', (byte) 'e', (byte) 's', (byte) 's' };

	byte[] wrongTextPassword = { (byte) 'W', (byte) 'r', (byte) 'o', (byte) 'n', (byte) 'g', (byte) ' ', (byte) 'P',
			(byte) 'a', (byte) 's', (byte) 's', (byte) 'w', (byte) 'o', (byte) 'r', (byte) 'd' };

	byte[] resetText = { (byte) 'R', (byte) 'e', (byte) 's', (byte) 'e', (byte) 't', (byte) ' ', (byte) 'o',
			(byte) 'k' };

	public MyAppl(byte[] bArray, short sOffset, byte bLength) {
		register(bArray, (short) (sOffset + 1), bArray[sOffset]);
		initMenu();
	}

	private void initMenu() {
		ToolkitRegistry reg = ToolkitRegistry.getEntry();

		
		 menuItem1 = reg.initMenuEntry( MENU_RESET_PERSONALIZE, (short) 0, (short)
		 MENU_RESET_PERSONALIZE.length, (byte) 0, false, (byte) 0, (short) 0);
		 

		menuItem2 = reg.initMenuEntry(MENU_SET_PASSWORD, (short) 0, (short) MENU_SET_PASSWORD.length, (byte) 0, false,
				(byte) 0, (short) 0);

		menuItem3 = reg.initMenuEntry(MENU_PROFILE_INFO, (short) 0, (short) MENU_PROFILE_INFO.length, (byte) 0, false,
				(byte) 0, (short) 0);
	}

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new MyAppl(bArray, bOffset, bLength);

	}

	@Override
	public void processToolkit(byte event) throws ToolkitException {
		if (event == EVENT_MENU_SELECTION) {
			try {
				EnvelopeHandler envHdlr = EnvelopeHandler.getTheHandler();
				byte selected = envHdlr.getItemIdentifier();
				ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();

				if (selected == menuItem2) { // Пункт ввода пароля
					if (isPersonalized) {
						return;
					}

					// GET INPUT
					proHdlr.init(PRO_CMD_GET_INPUT, (byte) 0x00, sim.toolkit.ToolkitConstants.DEV_ID_ME);
					proHdlr.appendTLV(TAG_TEXT_STRING, sim.toolkit.ToolkitConstants.DCS_8_BIT_DATA, writePasswordText,
							(short) 0, (short) writePasswordText.length);
					// Указываем минимальную (1) и максимальную (4) длину ввода
					proHdlr.appendTLV(TAG_RESPONSE_LENGTH, (byte) 1, (byte) 4);

					byte result = proHdlr.send();

					if (result == RES_CMD_PERF) {
						ProactiveResponseHandler respHdlr = ProactiveResponseHandler.getTheHandler();

						// Получаем реальную длину введенных данных
						short length = respHdlr.getTextStringLength();
						byte[] inputBuffer = new byte[length];
						respHdlr.copyTextString(inputBuffer, (short) 0);

						// Проверяем совпадение длины и содержимого
						if (length == PASSWORD.length && Util.arrayCompare(inputBuffer, (short) 0, PASSWORD, (short) 0,
								(short) PASSWORD.length) == 0) {
							isPersonalized = true;

							// Показываем сообщение об успехе (DISPLAY TEXT = 0x21)
							proHdlr.initDisplayText((byte) 0x81, DCS_8_BIT_DATA, successTextPassword, (short) 0,
									(short) successTextPassword.length);
							proHdlr.send();
						} else {
							// Ошибка (неверный пароль)
							proHdlr.initDisplayText((byte) 0x81, DCS_8_BIT_DATA, wrongTextPassword, (short) 0,
									(short) wrongTextPassword.length);
							proHdlr.send();
						}
					}
				} else if (selected == menuItem1) { // Пункт сброса
					isPersonalized = false;
					// Показываем сообщение об успешном сбросе
					// proHdlr.initDisplayText((byte) 0x81, DCS_8_BIT_DATA, resetText, (short) 0,
					// (short) resetText.length);
					// proHdlr.send();
					proHdlr.initDisplayText((byte) 0x00, DCS_8_BIT_DATA, resetText, (short) 0,
							(short) resetText.length);
					proHdlr.send();
				} else if (selected == menuItem3) {
					if (!isPersonalized) {
						return;
					}

					getProfileInfo(proHdlr);
				}

			} catch (Exception e) {
				ISOException.throwIt((short) 0x6A03);
			}
		}
	}

	private void getProfileInfo(ProactiveHandler proHdlr) {
		byte[] imei = getImei(proHdlr);
		byte[] tech = getAccessTechnology(proHdlr);
		//byte[] locInfo = getLocationInfo(proHdlr);
		
		byte[] titleImei = { 'I', 'M', 'E', 'I', ':', ' ' };

		byte[] titleTech = { '\n', 'T', 'E', 'C', 'H', ':', ' ' };
		

		short length = (short) (titleImei.length + imei.length + titleTech.length + tech.length /* + locInfo.length */);

		byte[] data = new byte[length];

		short offset = 0;

		Util.arrayCopy(titleImei, (short) 0, data, offset, (short) titleImei.length);

		offset += titleImei.length;

		Util.arrayCopy(imei, (short) 0, data, offset, (short) imei.length);

		offset += imei.length;

		Util.arrayCopy(titleTech, (short) 0, data, offset, (short) titleTech.length);

		offset += titleTech.length;

		Util.arrayCopy(tech, (short) 0, data, offset, (short) tech.length);

		offset += tech.length;

		//Util.arrayCopy(locInfo, (short) 0, data, offset, (short) locInfo.length);

		proHdlr.initDisplayText((byte) 0x00, DCS_8_BIT_DATA, data, (short) 0, (short) data.length);

		proHdlr.send();
	}

	private byte[] getImei(ProactiveHandler proHdlr) {
		byte[] imeiDisplay = new byte[15]; // Обычный ASCII массив для экрана
		byte[] imei = new byte[16];

		proHdlr.init(PRO_CMD_PROVIDE_LOCAL_INFORMATION, (byte) 0x01, // IMEI
				DEV_ID_ME);
		byte result = proHdlr.send();

		if (result == RES_CMD_PERF) {
			ProactiveResponseHandler resp = ProactiveResponseHandler.getTheHandler();
			if (resp.findTLV(TAG_IMEI, (byte) 1) != TLV_NOT_FOUND) {
				short len = resp.getValueLength();
				// Читаем сырые байты ответа в буфер imei
				short readLen = resp.copyValue((short) 0, imei, (short) 0, len);

				// Распаковываем BCD/упакованные цифры IMEI в нормальный ASCII текст.
				// Обычно IMEI в ответе идет начиная с определенного смещения (пропускаем байт
				// типа, если он есть)
				// или декодируем побайтово:
				short outIdx = 0;
				for (short i = 0; i < readLen && outIdx < 15; i++) {
					byte b = imei[i];
					byte low = (byte) (b & 0x0F); // Сначала младший ниббл
					byte high = (byte) ((b >> 4) & 0x0F); // Потом старший ниббл

					if (low <= 9) {
						imeiDisplay[outIdx++] = (byte) ('0' + low);
					}
					if (high <= 9 && outIdx < 15) {
						imeiDisplay[outIdx++] = (byte) ('0' + high);
					}
				}

				return imeiDisplay;
				/*
				 * proHdlr.initDisplayText( (byte)0x00, DCS_8_BIT_DATA, imeiDisplay, (short)0,
				 * outIdx); proHdlr.send();
				 */
			} else {
				byte[] text = { (byte) 'N', (byte) 'o', (byte) ' ', (byte) 'I', (byte) 'M', (byte) 'E', (byte) 'I' };
				/*
				 * proHdlr.initDisplayText((byte)0x00, DCS_8_BIT_DATA, text, (short)0,
				 * (short)text.length); proHdlr.send();
				 */
				return text;
			}
		} else {
			byte[] text = { (byte) 'E', (byte) 'r', (byte) 'r', (byte) 'o', (byte) 'r' };
			/*
			 * proHdlr.initDisplayText((byte)0x00, DCS_8_BIT_DATA, text, (short)0,
			 * (short)text.length); proHdlr.send();
			 */
			return text;
		}
	}

	private byte[] getAccessTechnology(ProactiveHandler proHdlr) {
		proHdlr.init(PRO_CMD_PROVIDE_LOCAL_INFORMATION, (byte) 0x06, DEV_ID_ME);

		byte result = proHdlr.send();

		if (result == RES_CMD_PERF) {
			ProactiveResponseHandler resp = ProactiveResponseHandler.getTheHandler();

			// Вторым аргументом передаем byte вместо short: (byte) 1 вместо (short) 1
			if (resp.findTLV((byte) 0x3F, (byte) 1) != TLV_NOT_FOUND) {
				byte[] techBuffer = new byte[1];
				resp.copyValue((short) 0, techBuffer, (short) 0, (short) 1);
				byte techByte = techBuffer[0];

				byte[] text;
				switch (techByte) {
				case 0x00:
					text = new byte[] { (byte) 'G', (byte) 'S', (byte) 'M' };
					break;
				case 0x01:
					text = new byte[] { (byte) 'U', (byte) 'T', (byte) 'R', (byte) 'A', (byte) 'N' };
					break;
				case 0x02:
					text = new byte[] { (byte) 'E', (byte) '-', (byte) 'U', (byte) 'T', (byte) 'R', (byte) 'A',
							(byte) 'N' };
					break;
				case 0x03:
					text = new byte[] { (byte) '5', (byte) 'G' };
					break;
				default:
					text = new byte[] { (byte) 'U', (byte) 'n', (byte) 'k', (byte) 'n', (byte) 'o', (byte) 'w',
							(byte) 'n' };
					break;
				}

				return text;
				/*
				 * proHdlr.initDisplayText( (byte)0x00, DCS_8_BIT_DATA, text, (short)0,
				 * (short)text.length); proHdlr.send();
				 */
			} else {
				byte[] text = { (byte) 'N', (byte) 'o', (byte) ' ', (byte) 'T', (byte) 'e', (byte) 'c', (byte) 'h' };
				/*
				 * proHdlr.initDisplayText((byte)0x00, DCS_8_BIT_DATA, text, (short)0,
				 * (short)text.length); proHdlr.send();
				 */
				return text;
			}
		} else {
			byte[] text = { (byte) 'E', (byte) 'r', (byte) 'r', (byte) 'o', (byte) 'r' };
			/*
			 * proHdlr.initDisplayText( (byte)0x00, DCS_8_BIT_DATA, text, (short)0,
			 * (short)text.length); proHdlr.send();
			 */
			return text;
		}
	}

	private byte[] getLocationInfo(ProactiveHandler proHdlr) {
		 proHdlr.init(
		            PRO_CMD_PROVIDE_LOCAL_INFORMATION,
		            (byte)0x00,
		            DEV_ID_ME);

		    if (proHdlr.send() != 0) {
		        return new byte[]{'E','r','r'};
		    }


		    ProactiveResponseHandler resp =
		            ProactiveResponseHandler.getTheHandler();


		    short len = resp.getValueLength();


		    byte[] debug = new byte[len];

		    resp.copyValue(
		            (short)0,
		            debug,
		            (short)0,
		            len);


		    return debug;
	}
	
	private byte[] decodeLocation(byte[] location) {

	    if (location.length < 7) {
	        return new byte[]{
	                'N','o',' ','L','o','c'
	        };
	    }


	    // MCC
	    byte mcc1 = (byte)(location[0] & 0x0F);
	    byte mcc2 = (byte)((location[0] >> 4) & 0x0F);
	    byte mcc3 = (byte)(location[1] & 0x0F);


	    // MNC
	    byte mnc1 = (byte)((location[1] >> 4) & 0x0F);
	    byte mnc2 = (byte)(location[2] & 0x0F);
	    byte mnc3 = (byte)((location[2] >> 4) & 0x0F);


	    short lac = (short)(
	            ((location[3] & 0xFF) << 8) |
	            (location[4] & 0xFF)
	    );


	    short cell = (short)(
	            ((location[5] & 0xFF) << 8) |
	            (location[6] & 0xFF)
	    );


	    byte[] result = new byte[60];

	    short pos = 0;


	    result[pos++]='M';
	    result[pos++]='C';
	    result[pos++]='C';
	    result[pos++]=':';
	    result[pos++]=' ';

	    result[pos++]=(byte)('0'+mcc1);
	    result[pos++]=(byte)('0'+mcc2);
	    result[pos++]=(byte)('0'+mcc3);


	    result[pos++]='\n';

	    result[pos++]='M';
	    result[pos++]='N';
	    result[pos++]='C';
	    result[pos++]=':';
	    result[pos++]=' ';

	    result[pos++]=(byte)('0'+mnc1);
	    result[pos++]=(byte)('0'+mnc2);

	    if(mnc3 != 0x0F) {
	        result[pos++]=(byte)('0'+mnc3);
	    }


	    result[pos++]='\n';

	    result[pos++]='L';
	    result[pos++]='A';
	    result[pos++]='C';
	    result[pos++]=':';
	    result[pos++]=' ';

	    pos = appendNumber(result, pos, lac);


	    result[pos++]='\n';

	    result[pos++]='C';
	    result[pos++]='E';
	    result[pos++]='L';
	    result[pos++]='L';
	    result[pos++]=':';
	    result[pos++]=' ';

	    pos = appendNumber(result, pos, cell);



	    byte[] out = new byte[pos];

	    Util.arrayCopy(
	            result,
	            (short)0,
	            out,
	            (short)0,
	            pos);

	    return out;
	}

	private short appendNumber(byte[] buffer, short pos, short value) {

		byte[] digits = new byte[5];
		short index = 0;

		if (value == 0) {
			buffer[pos++] = '0';
			return pos;
		}

		while (value > 0) {
			digits[index++] = (byte) ('0' + (value % 10));
			value /= 10;
		}

		while (index > 0) {
			buffer[pos++] = digits[--index];
		}

		return pos;
	}

	public void process(APDU apdu) {
		if (selectingApplet())
			return;

		byte[] buffer = apdu.getBuffer();
		byte ins = buffer[ISO7816.OFFSET_INS];

		switch (ins) {
		case (byte) 0x10: // Персонализация
			if (isPersonalized)
				ISOException.throwIt((short) 0x6986);

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
			if (!isPersonalized)
				ISOException.throwIt((short) 0x6985);
			byte[] data = { 'H', 'e', 'l', 'l', 'o', ' ', 'p', 'e', 'o', 'p', 'l', 'e' };
			Util.arrayCopyNonAtomic(data, (short) 0, buffer, (short) 0, (short) data.length);
			apdu.setOutgoingAndSend((short) 0, (short) data.length);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
		if (null == clientAID) // It's the system invoking
			return (Shareable) this;
		return null;
	}
}