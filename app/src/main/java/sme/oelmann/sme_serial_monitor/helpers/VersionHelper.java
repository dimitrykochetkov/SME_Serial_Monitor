package sme.oelmann.sme_serial_monitor.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionHelper {

    private Context context;

    private String version;
    private String date;

    public VersionHelper(Context context, int DATE){
        this.context = context;
        this.version = proceedVersion();
        this.date = getDateFromInt(DATE);
    }

    public String getVersion(){
        return this.version;
    }

    public String getDate(){
        return this.date;
    }

    private String proceedVersion(){
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException nnfe){ nnfe.printStackTrace(); }
        return "";
    }

    private String getDateFromInt(int date){
        String s = String.valueOf(date);
        return s.substring(0,2) + "." + s.substring(2,4) + "." + s.substring(4);
    }
}
