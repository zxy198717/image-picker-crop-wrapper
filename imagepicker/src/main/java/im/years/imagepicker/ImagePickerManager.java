package im.years.imagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.gun0912.tedpermission.TedPermission;
import com.kbeanie.multipicker.api.CacheLocation;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alvinzeng on 11/6/15.
 */
public class ImagePickerManager {

    public interface ImagePickerListener {
        /**
         * When the processing is complete, you will receive this callback with
         * {@link ChosenImage}
         *
         * @param image
         */
        public void onImageChosen(ChosenImage image);

        /**
         * Handle any error conditions if at all, when you receieve this callback
         *
         * @param reason
         */
        public void onError(String reason);
    }

    protected Activity activity;
    protected android.app.Fragment appFragment;
    protected Fragment fragment;

    private com.kbeanie.multipicker.api.entity.ChosenImage chosenedImage;
    private ImagePickerListener imageChooserListener;

    private CameraImagePicker cameraPicker;
    private ImagePicker imagePicker;
    private boolean crop;
    private boolean cropSquare;

    private ImagePickerManager() {
    }

    public ImagePickerManager(Activity activity) {
        this.activity = activity;
    }

    public ImagePickerManager(android.app.Fragment fragment) {
        this.appFragment = fragment;
    }

    public ImagePickerManager(Fragment fragment) {
        this.fragment = fragment;
    }

    public ImagePickerManager(Fragment fragment, boolean cropSquare) {
        this.fragment = fragment;
        this.cropSquare = cropSquare;
    }

    public void setCropSquare(boolean cropSquare) {
        this.cropSquare = cropSquare;
    }

    public void pickImage(ImagePickerListener l) {
        this.pickImage(false, l);
    }

    public void pickImage(final boolean crop, final ImagePickerListener l) {

        String[] items = {"拍照", "相册"};

        new AlertDialog.Builder(getActivity())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            takePicture(crop, l);
                        } else if (which == 1) {
                            chooseImage(crop, l);
                        }
                    }
                })
                .show();
    }

    public void takePicture(final boolean crop, final ImagePickerListener l) {

        new TedPermission(getActivity()).setPermissionListener(new com.gun0912.tedpermission.PermissionListener() {
            @Override
            public void onPermissionGranted() {
                doTakePicture(crop, l);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                Toast.makeText(getActivity(), "在设置-应用-"+getApplicationName()+"-权限中开启相机与储存空间权限，以正常使用拍照", Toast.LENGTH_SHORT).show();
            }
        }).setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).check();
    }

    private void doTakePicture(final boolean crop, final ImagePickerListener l) {
        this.crop = crop;
        imageChooserListener = l;

        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("CROP", crop);

        String path = getCameraImagePicker().pickImage();

        editor.putString("CAMERA_IMAGE_PATH", path);
        editor.commit();
    }

    public void chooseImage(final boolean crop, final ImagePickerListener l) {

        new TedPermission(getActivity()).setPermissionListener(new com.gun0912.tedpermission.PermissionListener() {
            @Override
            public void onPermissionGranted() {
                doChooseImage(crop, l);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                Toast.makeText(getActivity(), "在设置-应用-"+getApplicationName()+"-权限中开启储存空间权限，以正常使用相册", Toast.LENGTH_SHORT).show();
            }
        }).setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).check();
    }

    private void doChooseImage(final boolean crop, final ImagePickerListener l) {
        this.crop = crop;
        imageChooserListener = l;
        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("CROP", crop);
        editor.commit();
        getImagePicker().pickImage();
    }

    ImagePicker getImagePicker() {
        if (imagePicker == null) {
            if (activity != null) {
                imagePicker = new ImagePicker(activity);
            } else if (appFragment != null) {
                imagePicker = new ImagePicker(appFragment);
            } else {
                imagePicker = new ImagePicker(fragment);
            }
            imagePicker.shouldGenerateMetadata(true);
            imagePicker.shouldGenerateThumbnails(false);
            imagePicker.setCacheLocation(CacheLocation.EXTERNAL_CACHE_DIR);
            DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
            int w = metrics.widthPixels;
            int h = metrics.heightPixels;
            imagePicker.ensureMaxSize(w, h);
            imagePicker.setImagePickerCallback(new ImagePickerCallback() {
                @Override
                public void onImagesChosen(List<com.kbeanie.multipicker.api.entity.ChosenImage> list) {
                    if (list != null && list.size() > 0) {
                        onImageChosen(list.get(0));
                    }
                }

                @Override
                public void onError(String s) {
                    imageChooserListener.onError(s);
                }
            });
        }

        return imagePicker;
    }

    CameraImagePicker getCameraImagePicker() {
        if (cameraPicker == null) {
            if (activity != null) {
                cameraPicker = new CameraImagePicker(activity);
            } else if (appFragment != null) {
                cameraPicker = new CameraImagePicker(appFragment);
            } else {
                cameraPicker = new CameraImagePicker(fragment);
            }

            cameraPicker.shouldGenerateMetadata(true);
            cameraPicker.shouldGenerateThumbnails(false);
            cameraPicker.setCacheLocation(CacheLocation.EXTERNAL_CACHE_DIR);

            DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
            int w = metrics.widthPixels;
            int h = metrics.heightPixels;
            cameraPicker.ensureMaxSize(w, h);
            cameraPicker.setImagePickerCallback(new ImagePickerCallback() {
                @Override
                public void onImagesChosen(List<com.kbeanie.multipicker.api.entity.ChosenImage> list) {
                    if (list != null && list.size() > 0) {
                        onImageChosen(list.get(0));
                    }
                }

                @Override
                public void onError(String s) {
                    imageChooserListener.onError(s);
                }
            });
        }

        return cameraPicker;
    }

    void onImageChosen(com.kbeanie.multipicker.api.entity.ChosenImage chosenImage) {
        if (imageChooserListener == null) {
            return;
        }
        if (crop) {
            File original = new File(chosenImage.getOriginalPath());
            Uri source = Uri.fromFile(original);
            String cropPath = original.getParent() + File.separator + original.getName()
                    .replace(".", "-crop.");
            File file = new File(cropPath);
            Uri destination = Uri.fromFile(file);
            chosenImage.setThumbnailSmallPath(cropPath);

            final UCrop ucrop = UCrop.of(source, destination).withAspectRatio(1, 1);

            UCrop.Options options = new UCrop.Options();
            if (!this.cropSquare) {
                options.setFreeStyleCropEnabled(true);
            }
            options.setHideBottomControls(true);
            ucrop.withOptions(options);

            if (activity != null) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ucrop.start(activity);
                    }
                });


            } else {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (appFragment != null) {
                            ucrop.start(getActivity(), appFragment);
                        } else {
                            ucrop.start(getActivity(), fragment);
                        }
                    }
                });

            }
        } else {
            imageChooserListener.onImageChosen(chosenImage);
        }

        chosenedImage = chosenImage;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                if (imagePicker == null) {
                    this.crop = getSharedPreferences().getBoolean("CROP", false);
                }
                getImagePicker().submit(data);
            } else if (requestCode == Picker.PICK_IMAGE_CAMERA) {
                if (cameraPicker == null) {
                    SharedPreferences sp = getSharedPreferences();
                    getCameraImagePicker().reinitialize(sp.getString("CAMERA_IMAGE_PATH", ""));
                    this.crop = sp.getBoolean("CROP", false);
                }
                getCameraImagePicker().submit(data);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                if (imageChooserListener != null) {
                    imageChooserListener.onImageChosen(chosenedImage);
                }
            }
        }
    }

    SharedPreferences getSharedPreferences() {
        return getActivity().getSharedPreferences("ImagePickerCache", Activity.MODE_PRIVATE);
    }

    private String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getActivity().getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    @SuppressLint("NewApi")
    protected Activity getActivity() {
        if (activity != null) {
            return activity;
        }

        if (appFragment != null) {
            return appFragment.getActivity();
        }

        if (fragment != null) {
            return fragment.getActivity();
        }

        return null;
    }
}
