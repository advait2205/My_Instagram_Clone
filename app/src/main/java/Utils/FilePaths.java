package Utils;

import android.os.Environment;

public class FilePaths extends Environment{
    //"storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/" + Environment.DIRECTORY_PICTURES;
    public String CAMERA = ROOT_DIR + "/" + Environment.DIRECTORY_DCIM + "/Camera";
    public String FIREBASE_IMAGE_STORAGE = "photos/users";
}
