package br.com.fivecom.litoralfm.models.email;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DDIInfo {
    public String code;
    public String country;
    public String placeholder;

    public DDIInfo(String code, String country, String placeholder) {
        this.code = code;
        this.country = country;
        this.placeholder = placeholder;
    }

    @NonNull
    @Override
    public String toString() {
        return code + " (" + country + ")";
    }

    public static List<DDIInfo> getDDIList() {
        List<DDIInfo> ddiList = new ArrayList<>();

        ddiList.add(new DDIInfo("+52", "Mexico", "(##) ####-####"));
        ddiList.add(new DDIInfo("+54", "Argentina", "(##) ####-####"));
        ddiList.add(new DDIInfo("+55", "Brasil", "(##) #####-####"));
        ddiList.add(new DDIInfo("+56", "Chile", "# #### ####"));
        ddiList.add(new DDIInfo("+57", "Colombia", "### ### ####"));
        ddiList.add(new DDIInfo("+58", "Venezuela", "####-#######"));
        ddiList.add(new DDIInfo("+51", "Peru", "### ### ###"));
        ddiList.add(new DDIInfo("+593", "Ecuador", "### ### ####"));
        ddiList.add(new DDIInfo("+591", "Bolivia", "########"));
        ddiList.add(new DDIInfo("+595", "Paraguay", "#### ### ###"));
        ddiList.add(new DDIInfo("+598", "Uruguai", "### ### ###"));
        ddiList.add(new DDIInfo("+505", "Nicaragua", "# ####-####"));
        ddiList.add(new DDIInfo("+504", "Honduras", "####-####"));
        ddiList.add(new DDIInfo("+503", "El Salvador", "####-####"));
        ddiList.add(new DDIInfo("+506", "Costa Rica", "####-####"));
        ddiList.add(new DDIInfo("+507", "Panama", "####-####"));
        ddiList.add(new DDIInfo("+502", "Guatemala", "####-####"));
        ddiList.add(new DDIInfo("+501", "Belize", "#####"));
        ddiList.add(new DDIInfo("+592", "Guyana", "#######"));
        ddiList.add(new DDIInfo("+597", "Suriname", "#######"));

        ddiList.add(new DDIInfo("+1", "Estados Unidos", "(###) ###-####"));
        ddiList.add(new DDIInfo("+1", "Canadá", "(###) ###-####"));

        ddiList.add(new DDIInfo("+44", "Reino Unido", "##### ######"));
        ddiList.add(new DDIInfo("+49", "Alemanha", "#### #######"));
        ddiList.add(new DDIInfo("+33", "França", "## ## ## ## ##"));
        ddiList.add(new DDIInfo("+39", "Itália", "### #######"));
        ddiList.add(new DDIInfo("+34", "Espanha", "### ## ## ##"));

        return ddiList;
    }

}
