package server.events;

import com.sun.jna.platform.win32.WinDef;

import server.win32.user32;
import server.win32.WinUser.INPUT;
import server.win32.WinUser.KEYBDINPUT;


public class KeyBoardEvent {
	
	private static WinDef.HKL dwhkl = user32.INSTANCE.LoadKeyboardLayoutA(user32.LANG_SYSTEM_DEFAULT,user32.KLF_ACTIVATE);
	//Remember to add a KEYUP event to release KEYDOWN event
	public static int sendScanKey(int key, int... event) {
		INPUT ip = new INPUT();
		if(event.length == 0) event = new int[] {KEYBDINPUT.KEYEVENTF_KEYDOWN};
		INPUT[] ips = (INPUT[])ip.toArray(event.length); 
		for(int i = 0 ; i < event.length; i++) {
			ips[i].type = new WinDef.DWORD(INPUT.INPUT_KEYBOARD);
			ips[i].dummy.setType("ki");
			ips[i].dummy.ki.dwFlags = new WinDef.DWORD(event[i] | KEYBDINPUT.KEYEVENTF_SCANCODE);
			WinDef.WORD wScan = VKtoSC(key);
			ips[i].dummy.ki.wScan = wScan;
			System.out.println("Scan"+wScan);
		}
		return user32.INSTANCE.SendInput(new WinDef.DWORD(event.length), ips , ip.size());
	}
	
	//Send single key by events
	public static int sendVKey(int key, int... event) {
		INPUT ip = new INPUT();
		if(event.length == 0) event = new int[] {KEYBDINPUT.KEYEVENTF_KEYDOWN};
		INPUT[] ips = (INPUT[])ip.toArray(event.length); 
		for(int i = 0 ; i < event.length; i++) {
			ips[i].type = new WinDef.DWORD(INPUT.INPUT_KEYBOARD);
			ips[i].dummy.setType("ki");
			ips[i].dummy.ki.dwFlags = new WinDef.DWORD(event[i]);
			ips[i].dummy.ki.wVk = new WinDef.WORD(key);
		}
		return user32.INSTANCE.SendInput(new WinDef.DWORD(event.length), ips , ip.size());
	}
	
	public static int sendVKeyEx(int...keys) {
		int len = keys.length;
		INPUT ip = new INPUT();
		INPUT[] ips = (INPUT[])ip.toArray(len * 2);
		for(int i = 0 ; i < len ; i++) {
			ips[i].type = new WinDef.DWORD(INPUT.INPUT_KEYBOARD);
			ips[i].dummy.setType("ki");
			ips[i].dummy.ki.dwFlags = new WinDef.DWORD(KEYBDINPUT.KEYEVENTF_EXTENDEDKEY);
			ips[i].dummy.ki.wVk = new WinDef.WORD(keys[i]);
			
			ips[ips.length -1 -i].type = new WinDef.DWORD(INPUT.INPUT_KEYBOARD);
			ips[ips.length -1 -i].dummy.setType("ki");
			ips[ips.length -1 -i].dummy.ki.dwFlags = new WinDef.DWORD(KEYBDINPUT.KEYEVENTF_EXTENDEDKEY | KEYBDINPUT.KEYEVENTF_KEYUP);
			ips[ips.length -1 -i].dummy.ki.wVk = new WinDef.WORD(keys[i]);
		}
		ips[0].dummy.ki.dwFlags.setValue(KEYBDINPUT.KEYEVENTF_KEYDOWN);
		ips[ips.length - 1].dummy.ki.dwFlags.setValue(KEYBDINPUT.KEYEVENTF_KEYUP);
		return user32.INSTANCE.SendInput(new WinDef.DWORD(ips.length), ips , ip.size());
	}
	
	//Extanded key
	public static int sendScanKeyEx(int... keys) {
		INPUT ip = new INPUT();
		//double the keys to send KEYUP event
		INPUT[] ips = (INPUT[])ip.toArray(keys.length*2);
		int len = keys.length;
		for(int i = 0 ; i < len; i++) {
			ips[i].type = new WinDef.DWORD(INPUT.INPUT_KEYBOARD);
			ips[i].dummy.setType("ki");
			WinDef.WORD wScan = VKtoSC(keys[i]);
			ips[i].dummy.ki.dwFlags = new WinDef.DWORD(KEYBDINPUT.KEYEVENTF_EXTENDEDKEY | KEYBDINPUT.KEYEVENTF_SCANCODE);
			ips[i].dummy.ki.wScan = wScan;
			//Relaese keys by reverse order
			ips[ips.length -1 -i].type = new WinDef.DWORD(INPUT.INPUT_KEYBOARD);
			ips[ips.length -1 -i].dummy.setType("ki");
			WinDef.WORD wScan_r = VKtoSC(keys[i]);	
			ips[ips.length -1 -i].dummy.ki.dwFlags = new WinDef.DWORD(KEYBDINPUT.KEYEVENTF_EXTENDEDKEY | KEYBDINPUT.KEYEVENTF_KEYUP | KEYBDINPUT.KEYEVENTF_SCANCODE);
			ips[ips.length -1 -i].dummy.ki.wScan = wScan_r;
		}
		//reset first input key with no extended flag
		ips[0].dummy.ki.dwFlags.setValue(KEYBDINPUT.KEYEVENTF_SCANCODE);
		ips[ips.length - 1].dummy.ki.dwFlags.setValue(KEYBDINPUT.KEYEVENTF_SCANCODE + KEYBDINPUT.KEYEVENTF_KEYUP);
		return user32.INSTANCE.SendInput(new WinDef.DWORD(ips.length), ips , ip.size());
	}
	
	public static int VkKeyScan(char key) {
		int vk = user32.INSTANCE.VkKeyScanExA(key,dwhkl);
		//the low-order byte of the return value contains the virtual-key code and the high-order byte contains the shift state
		//pick out vk from low order byte
		return vk & 0b0000000011111111;
	}
	
	public static WinDef.WORD VKtoSC(int key){
		long sc = user32.INSTANCE.MapVirtualKeyExA(key, user32.MAPVK_VK_TO_VSC,dwhkl);
		return new WinDef.WORD(sc);
	}
	
}

