package radiomeditation.radio1.exoplayer;

import android.net.Uri;

public class Sample {
    public String title;
    public Uri url;
    public String description;
    public static String[] categories = {"EN", "FR", "AR"};
    public static String[] descriptions = {"EN description", "FR description", "AR description"};
    public static String[] titles = {"EN title", "FR title", "AR title"};

    public Sample(String title, Uri url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }

}
