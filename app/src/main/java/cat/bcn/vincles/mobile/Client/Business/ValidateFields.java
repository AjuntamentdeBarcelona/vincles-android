package cat.bcn.vincles.mobile.Client.Business;


import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateFields {

    public boolean isValididName(String name) {
        return name != null && name.length() > 0;
    }

    private boolean isNameLastNameValid (String name) {
        return !name.matches(".*\\d+.*");
    }

    public boolean isValididLastName(String lastName) {
        return lastName != null && lastName.length() > 0;
    }

    public boolean isValididEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public boolean isEmailMatchingPattern(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValididPassword(String password) {
        return password.length() >= 8 && password.length() <= 16 &&
                Pattern.matches("^[a-zA-Z0-9_!&%$@\\\\-]*", password);
        /*Pattern p = Pattern.compile("[<>%$ª!·&/()=?¿]");
        Matcher m = p.matcher(password);

        return password.length() >= 8 && password.length() <= 16 && !m.find();*/
    }

    public boolean isPasswordMaching(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }

    public boolean isValidPhone (String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public boolean isEmptyBirthDate (Long birthdate) {
        if (birthdate==null){
            return false;
        } else{
            return true;
        }
    }

    public boolean isValidBirthDate (long birthdate) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 14);
        long epochTime = c.getTimeInMillis();

        return  birthdate < epochTime;
    }
}
