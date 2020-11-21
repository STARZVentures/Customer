package com.shree.app.Utils;

import android.app.Activity;

import com.shree.app.Objects.TypeObject;
import com.shree.app.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {


    /**
     * Round a float value to a specific decimal place
     * @param amount - the value to round
     * @param decimalPlace - to what decimal place to round the amount to
     * @return rounded number
     */
    public BigDecimal round(float amount, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(amount));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }


    /**
     * Returns array list with all of the driver rides available for this
     * application.
     * @param activity - activity that called this function
     * @return typeArrayList - array list with all the driver types
     */
    public static ArrayList<TypeObject> getTypeList(Activity activity){
        ArrayList<TypeObject> typeArrayList = new ArrayList<>();
        typeArrayList.add(new TypeObject("type_1", activity.getResources().getString(R.string.type_1), activity.getResources().getDrawable(R.drawable.ic_type_1), 4));
        typeArrayList.add(new TypeObject("type_2", activity.getResources().getString(R.string.type_2), activity.getResources().getDrawable(R.drawable.ic_type_2), 0));
        typeArrayList.add(new TypeObject("type_3", activity.getResources().getString(R.string.type_3), activity.getResources().getDrawable(R.drawable.ic_type_3), 0));
        typeArrayList.add(new TypeObject("type_4", activity.getResources().getString(R.string.type_4), activity.getResources().getDrawable(R.drawable.ic_type_5), 3));
        typeArrayList.add(new TypeObject("type_5", activity.getResources().getString(R.string.type_5), activity.getResources().getDrawable(R.drawable.ic_erickshaw), 5));
        typeArrayList.add(new TypeObject("type_6", activity.getResources().getString(R.string.type_6), activity.getResources().getDrawable(R.drawable.ic_type_6), 1));
        return  typeArrayList;
    }


    /**
     * get type object that is in the arrayList with a certain id
     * @param activity - activity that called this function
     * @param id - id of the object to find
     * @return - type object
     */
    public static TypeObject getTypeById(Activity activity, String id){
        ArrayList<TypeObject> typeArrayList = getTypeList(activity);
        for(TypeObject mType : typeArrayList){
            if(mType.getId().equals(id)){
                return mType;
            }
        }
        return null;
    }

    /*       ------------     created by Sadaf     -----------         */
    public static boolean isValidLicenseNo(String str) {
        // Regex to check valid driving license number
        String regex
                = "^(([A-Z]{2}[0-9]{2})"
                + "( )|([A-Z]{2}-[0-9]"
                + "{2}))((19|20)[0-9]"
                + "[0-9])[0-9]{7}$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        // Find match between given string
        // and regular expression
        // uSing Pattern.matcher()
        Matcher m = p.matcher(str);
        return m.matches();
    }
    public static boolean isValidPhoneNo(String str){
        String regex = "^[0-9\\\\-]*$";
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        Matcher m = p.matcher(str);
        return m.matches();
    }
    public static boolean isValidEmail(String str){
        String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$";
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        Matcher m = p.matcher(str);
        return m.matches();
    }
    public static boolean isNameValid(String str){
        String regex = "[A-Z][a-z]*";
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
