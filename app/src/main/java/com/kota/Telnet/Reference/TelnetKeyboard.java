package com.kota.Telnet.Reference;

public class TelnetKeyboard {
    public static final int DELETE = 265;
    public static final int DOWN_ARROW = 259;
    public static final int END = 263;
    public static final int HOME = 262;
    public static final int INSERT = 264;
    public static final int LEFT_ARROW = 256;
    public static final int PAGE_DOWN = 261;
    public static final int PAGE_UP = 260;
    public static final int RIGHT_ARROW = 257;
    public static final int SPACE = 32;
    public static final int TAB = 9;
    public static final int UP_ARROW = 258;
    public static final int CTRL_G = 7;
    public static final int CTRL_P = 16;
    public static final int CTRL_Q = 17;
    public static final int CTRL_R = 18;
    public static final int CTRL_S = 19;
    public static final int CTRL_U = 21;
    public static final int CTRL_X = 24;
    public static final int CTRL_Y = 25;
    public static final int BACK_ONE_CHAR = 83;
    public static final int SHIFT_M = 115;

    public static byte[] getKeyDataWithTimes(int keyCode, int times) {
        byte[] keydata = getKeyData(keyCode);
        byte[] data = new byte[(keydata.length * times)];
        for (int index = 0; index < data.length; index++) {
            data[index] = keydata[index % keydata.length];
        }
        return data;
    }

    /**
     * 輸入鍵盤指令的代號，回傳 telnet command
     * @param keyCode 鍵盤指令
     * @return byte[] telnet command
     * */
    public static byte[] getKeyData(int keyCode) {
        switch (keyCode) {
            case TAB: /* tab */
                return new byte[]{9};
            case SPACE: /* space */
                return new byte[]{32};
            case LEFT_ARROW: /* Left Arrow */
                return new byte[]{27, 91, 68};
            case RIGHT_ARROW: /* Right Arrow */
                return new byte[]{27, 91, 67};
            case UP_ARROW /*258*/:
                return new byte[]{27, 91, 65};
            case DOWN_ARROW /*259*/:
                return new byte[]{27, 91, 66};
            case PAGE_UP /*260*/:
                return new byte[]{27, 91, 53, 126};
            case PAGE_DOWN /*261*/:
                return new byte[]{27, 91, 54, 126};
            case HOME /*262*/:
                return new byte[]{27, 91, 49, 126};
            case END /*263*/:
                return new byte[]{27, 91, 52, 126};
            case INSERT /*264*/:
                return new byte[]{27, 91, 50, 126};
            case DELETE /*265*/:
                return new byte[]{27, 91, 51, 126};
            default:
                return new byte[]{(byte) keyCode};
        }
    }
}
