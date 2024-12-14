package wethinkcode.loadshed.common;

public class Helpers {
    public static boolean isDigit(String string){
        try {
            Integer.parseInt(string);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static String capitalizeString(String string){
        StringBuilder capString = new StringBuilder();
        String prevChar = "";
        String[] strings = string.split("");
        int charCount = 0;
        for (String cha : strings){

            if (charCount == 0){
                capString.append(cha.toUpperCase());
                prevChar = cha;
                charCount ++;
                continue;
            }

            if(prevChar.equals("_") || prevChar.equals("-")){
                String capChar = cha.toUpperCase();
                capString.append(capChar);
                prevChar = cha;
                charCount ++;
                continue;
            }

            capString.append(cha);
            prevChar= cha;
            charCount ++;
        }

        return capString.toString();
    }
}
