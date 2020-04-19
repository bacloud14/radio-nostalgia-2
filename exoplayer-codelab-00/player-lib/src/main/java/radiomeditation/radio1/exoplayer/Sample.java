package radiomeditation.radio1.exoplayer;

import android.net.Uri;

public class Sample {
    public String title;
    public Uri url;
    public String description;
    public static String[] categories = {"EN", "FR", "AR", "ENDT"};
    public static boolean[] mixing = {true, true, true, false};
    public static String[] descriptions = {"EN description", "FR description", "AR description", "ENDT description"};
    public static String[] titles = {"EN title", "FR title", "AR title", "ENDT title"};
    public boolean toBeMixed = true;

    public Sample(String title, Uri url, String description, boolean toBeMixed) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.toBeMixed = toBeMixed;
    }

}
