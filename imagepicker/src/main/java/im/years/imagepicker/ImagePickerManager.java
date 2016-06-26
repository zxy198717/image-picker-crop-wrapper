package im.years.imagepicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.kbeanie.multipicker.api.CacheLocation;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.soundcloud.android.crop.Crop;

import java.io.File;
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

    public void pickImage(ImagePickerListener l) {
        this.pickImage(false, l);
    }

    public void pickImage(final boolean crop, final ImagePickerListener l) {
        new ImagePickerActionSheet(getActivity(), new ImagePickerActionSheet.ImagePickerActionSheetListener() {
            @Override
            public void onItemSelected(int index) {
                switch (index) {
                    case 0:
                        takePicture(crop, l);
                        break;
                    case 1:
                        chooseImage(crop, l);
                        break;
                }
            }
        });
    }

    public void takePicture(final boolean crop, final ImagePickerListener l) {
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
            // CANNOT
            //cameraPicker.setCacheLocation(CacheLocation.EXTERNAL_CACHE_DIR);
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
        if(imageChooserListener == null) {
            return ;
        }
        if (crop) {
            File original = new File(chosenImage.getOriginalPath());
            Uri source = Uri.fromFile(original);
            String cropPath = original.getParent() + File.separator + original.getName()
                    .replace(".", "-crop.");
            File file = new File(cropPath);
            Uri destination = Uri.fromFile(file);
            chosenImage.setThumbnailSmallPath(cropPath);

            final Crop cropIns = Crop.of(source, destination).asSquare();
            if (activity != null) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cropIns.start(activity);
                    }
                });


            } else {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (appFragment != null) {
                            cropIns.start(getActivity(), appFragment);
                        } else {
                            cropIns.start(getActivity(), fragment);
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
            } else if (requestCode == Crop.REQUEST_CROP) {
                if (imageChooserListener != null) {
                    imageChooserListener.onImageChosen(chosenedImage);
                }
            }
        }
    }

    SharedPreferences getSharedPreferences() {
        return getActivity().getSharedPreferences("ImagePickerCache", Activity.MODE_PRIVATE);
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
