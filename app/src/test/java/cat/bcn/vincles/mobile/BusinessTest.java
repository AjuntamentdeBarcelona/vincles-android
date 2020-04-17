package cat.bcn.vincles.mobile;


import org.junit.Test;

import java.util.Calendar;

import cat.bcn.vincles.mobile.Client.Business.ValidateFields;

import static org.junit.Assert.assertEquals;

public class BusinessTest {
    @Test
    public void givenSomeStringShouldIdentifiyAreValidNames() throws Exception {
        String name0 = "Lorem Ipsum";
        String name1 = "Lorem9";
        String name2 = "";

        ValidateFields validateFields = new ValidateFields();

        assertEquals(true,validateFields.isValididName(name0));
        assertEquals(false,validateFields.isValididName(name1));
        assertEquals(false,validateFields.isValididName(name2));
    }

    @Test
    public void givenSomeStringShouldIdentifiyAreValidLastNames() throws Exception {
        String lastName0 = "Lorem Ipsum";
        String lastName1 = "Lorem9";
        String lastName2 = "";

        ValidateFields validateFields = new ValidateFields();

        assertEquals(true,validateFields.isValididLastName(lastName0));
        assertEquals(false,validateFields.isValididLastName(lastName1));
        assertEquals(false,validateFields.isValididLastName(lastName2));
    }



    @Test
    public void givenSomeStringShouldIdentifiyIfAreValidEmail() throws Exception {

        String email0 = "asdf";
        String email1 = "asdf@.asd";
        String email2 = "a@a@a.com";
        String email3 = "a@a.com";
        String email4 = "";

        ValidateFields validateFields = new ValidateFields();

        assertEquals(false,validateFields.isValididEmail(email0));
        assertEquals(false,validateFields.isValididEmail(email1));
        assertEquals(false,validateFields.isValididEmail(email2));
        assertEquals(true,validateFields.isValididEmail(email3));
        assertEquals(false,validateFields.isValididEmail(email4));
    }

    @Test
    public void givenSomeStringShouldIdentifiyIfAreValidisValididPasswords() throws Exception {
        String password0 = "";
        String password1 = "01234567891234567";
        String password2 = "0123456";
        String password3 = "12345678";
        String password4 = "12345$678";

        ValidateFields validateFields = new ValidateFields();

        assertEquals(false,validateFields.isValididPassword(password0));
        assertEquals(false,validateFields.isValididPassword(password1));
        assertEquals(false,validateFields.isValididPassword(password2));
        assertEquals(true,validateFields.isValididPassword(password3));
        assertEquals(false,validateFields.isValididPassword(password4));
    }

    @Test
    public void givenSomeStringShouldIdentifiyIfAreTheSamePassword() throws Exception {
        String password0 = "";
        String password1 = "1";
        String password3 = "12345678";
        String password4 = "12345678";

        ValidateFields validateFields = new ValidateFields();

        assertEquals(false,validateFields.isPasswordMaching(password0,password1));
        assertEquals(true,validateFields.isPasswordMaching(password3,password4));
    }

    @Test
    public void givenSomeStringShouldIdentifiyIfAreValidPhoneNumber() throws Exception {
        String phone0 = "";
        String phone1 = "123456";
        String phone2 = "1234567";

        ValidateFields validateFields = new ValidateFields();

        assertEquals(false,validateFields.isValidPhone(phone0));
        assertEquals(true,validateFields.isValidPhone(phone1));
        assertEquals(false,validateFields.isValidPhone(phone2));
    }

    @Test
    public void givenSomeEpochTimeShouldIdentifyIfIsMoreThan14YearsFromNow() throws Exception {

        long epochTime0 = Calendar.getInstance().getTimeInMillis();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 14);
        c.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH-1));
        long epochTime1 = c.getTimeInMillis();

        ValidateFields validateFields = new ValidateFields();

        assertEquals(false,validateFields.isValidBirthDate(epochTime0));
        assertEquals(true,validateFields.isValidBirthDate(epochTime1));

    }


}

