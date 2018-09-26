package in.org.business.calllogapp;

/*
 *    Created by Rohan on 26/09/18 at 12:22 PM
 */
public class Recording {

    String Uri, fileName;
    boolean isPlaying = false;


    public Recording(String uri, String fileName, boolean isPlaying) {
        Uri = uri;
        this.fileName = fileName;
        this.isPlaying = isPlaying;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing){
        this.isPlaying = playing;
    }
}
