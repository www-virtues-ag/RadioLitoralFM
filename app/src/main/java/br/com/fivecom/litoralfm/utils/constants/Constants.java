package br.com.fivecom.litoralfm.utils.constants;

import br.com.fivecom.litoralfm.models.Data;

public class Constants {
    public static int ID = 0, ID1=0;
    public static Data data = new Data.Builder().build();
    public enum Extra {
        ID, TITLE, BODY, URL, NOTIFICATION, IMAGE, LAT, LNG, VERSION, CAP;
    }
}

