package br.com.fivecom.litoralfm.utils.core;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

public class Masks {

    public static String remove(String s) {
        return s.replaceAll("[.]", "").replaceAll("[-]", "").replaceAll("[-]", "")
                .replaceAll("[/]", "").replaceAll("[(]", "")
                .replaceAll("[)]", "").replaceAll("[:]", "").replaceAll("[ ]", "");
    }

/**
     * Convert to Money (Real)
     *
     * @param editText (EditText)
     * @return (TextWatcher)
     */
    public static TextWatcher money(final EditText editText) {
        return new TextWatcher() {
            private String current = "";
            private Locale locale = new Locale("pt", "BR");

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);
                    current = s.toString();
                    if ((current.lastIndexOf(".") + 2) == current.length()) current += "0";
                    current = current.replaceAll("[R$,.]", "");
                    current = NumberFormat.getCurrencyInstance(locale).format(Double.parseDouble(current) / 100);
                    editText.setText(current);
                    editText.setSelection(current.length());
                    editText.addTextChangedListener(this);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

/**
     * Add Mask in EditText
     *
     * @param editText    (EditText)
     * @param defaultMask (String)
     * @param mask        (String)
     * @return (TextWatcher)
     */
    public static TextWatcher add(@NonNull EditText editText, @NonNull final String defaultMask, @Nullable final String mask) {
        return new TextWatcher() {
            boolean isUpdating;
            String old_text = null;
            String sec_mask;

            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                String new_text = remove(sequence.toString());
                sec_mask = defaultMask;
                if (new_text.length() > remove(sec_mask).length() && mask != null) sec_mask = mask;
                if (old_text == null) old_text = new_text;
                if (isUpdating) {
                    old_text = new_text;
                    isUpdating = false;
                    return;
                }
                String custom_text = "";
                int i = 0;
                for (char m : sec_mask.toCharArray()) {
                    if ((m != '#' && new_text.length() >= old_text.length() && new_text.length() > 2)
                            || (m != '#' && new_text.length() < old_text.length() && new_text.length() != i)) {
                        custom_text += m;
                        continue;
                    }
                    try {
                        custom_text += new_text.charAt(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                editText.setText(custom_text.toUpperCase());
                editText.setSelection(custom_text.length());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static String add(@NonNull String doc) {
        String new_text = remove(doc);
        String sec_mask = "###.###.###-##";
        if (new_text.length() > 11) sec_mask = "##.###.###/####-##";
        String custom_text = "";
        int i = 0;
        for (char m : sec_mask.toCharArray()) {
            if ((m != '#' && new_text.length() != i)) {
                custom_text += m;
                continue;
            }
            try {
                custom_text += new_text.charAt(i);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            i++;
        }
        return custom_text;
    }
}
