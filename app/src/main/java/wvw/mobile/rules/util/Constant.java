package wvw.mobile.rules.util;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class Constant {

    public static final String SHAR_PREF_NAME = "PREFELECTION";
    public static final String HTTP_LINK_NAME_SERVER ="urlServer";
    public static final String HTTP_LINK_URL="http://10.0.2.2:3000/config/";
    public static final String SMS_CONDITION = "Some condition";
    public static String SMS_NUMBER_CONDITION = "+22666867276";//3420 22677198675
    public static final String TABLE_NAME_RUBRIQUES = "rubriques";
    public static final String SHAR_SMS_SERVER_ADRESSE = "serversmsadresse";
    public static final String SHAR_BULLETIN_NUMERO_BUREAU_VOTE= "numerobureauvote";
    public static final String SHAR_CODE_USER= "codeuser";
    public static final int MAX_SMS_MESSAGE_LENGTH = 160;

    public static class HTTP {

        public static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

        public static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

        public static final long SYNC_FREQUENCY = 60 * 60L;  // Fréquence de la synchronisation 1 heure (in seconds)


        public static final String BASE_URL = "http://10.0.2.2:3000/";
        public static final String API_BASE_URL = HTTP.BASE_URL;

        public static final String API_KEY = "5fd87010";

        public static final String API_BASE_URL_APPEND = "";
        //public static final String API_BASE_URL_APPEND = "api/";
        //public static final String API_BASE_URL_APPEND = "m2i.gateway.mobcrofin/";

        public static final String URL_LIST_RUBRIQUES = "rubriques" ;
        public static final String URL_LIST_RUBRIQUES_NULL = " " ;
        public static final String URL_CONFIG_NULL = " " ;
        public static final String URL_LIST_UTILS = "utils";
    }

    public static class REFERENCE {
        public static final String KEY_HOST = PACKAGE_NAME + ".host_ip";
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }


}
