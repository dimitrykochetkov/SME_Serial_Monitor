package sme.oelmann.sme_serial_monitor.helpers;

public class Converter {

    public static String convertHEXStringToASCII(String hexString){
        hexString = hexString.replace("00", "").replace(" ", "").replace("0d0a0d0a","0d0a");
        if (hexString.endsWith("0d0a")){
            hexString = hexString.substring(0, hexString.lastIndexOf("0d0a"));
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String str = hexString.substring(i, i + 2);
            try {
                output.append((char) Integer.parseInt(str, 16));
            } catch (NumberFormatException nfe) { nfe.printStackTrace(); }
        }
        return output.toString();
    }
}
