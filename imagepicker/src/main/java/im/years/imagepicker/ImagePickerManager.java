package im.years.imagepicker;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.soundcloud.android.crop.Crop;

import java.io.File;

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

    private ChosenImage chosenedImage;
    private ImagePickerListener imageChooserListener;
    private ImageChooserManager imageChooserManager;

    private ImagePickerManager(){};

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

        imageChooserListener = l;

        if(activity != null) {
            imageChooserManager = new ImageChooserManager(activity,
                    ChooserType.REQUEST_CAPTURE_PICTURE, true);
        } else if(appFragment != null) {
            imageChooserManager = new ImageChooserManager(appFragment,
                    ChooserType.REQUEST_CAPTURE_PICTURE, true);
        } else {
            imageChooserManager = new ImageChooserManager(fragment,
                    ChooserType.REQUEST_CAPTURE_PICTURE, true);
        }

        imageChooserManager.setImageChooserListener(new ImageChooserListener() {
            @Override
            public void onImageChosen(ChosenImage chosenImage) {
                if (crop) {
                    Uri source = Uri.fromFile(new File(chosenImage
                            .getFilePathOriginal()));
                    Uri destination = Uri.fromFile(new File(chosenImage
                            .getFileThumbnailSmall()));

                    final Crop cropIns = Crop.of(source, destination).asSquare();
                    if(activity != null) {

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
                                if(appFragment != null) {
                                    cropIns.start(getActivity(), appFragment);
                                } else {
                                    cropIns.start(getActivity(), fragment);
                                }
                            }
                        });

                    }

                    chosenedImage = chosenImage;
                } else {
                    l.onImageChosen(chosenImage);
                }
            }

            @Override
            public void onError(String s) {
                l.onError(s);
            }
        });
        try {
            imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseImage(final boolean crop, final ImagePickerListener l) {

        imageChooserListener = l;

        if(activity != null) {
            imageChooserManager = new ImageChooserManager(activity,
                    ChooserType.REQUEST_PICK_PICTURE, true);
        } else if(appFragment != null) {
            imageChooserManager = new ImageChooserManager(appFragment,
                    ChooserType.REQUEST_PICK_PICTURE, true);
        } else {
            imageChooserManager = new ImageChooserManager(fragment,
                    ChooserType.REQUEST_PICK_PICTURE, true);
        }

        imageChooserManager.setImageChooserListener(new ImageChooserListener() {
            @Override
            public void onImageChosen(ChosenImage chosenImage) {
                if (crop) {

                    Uri source = Uri.fromFile(new File(chosenImage
                            .getFilePathOriginal()));
                    Uri destination = Uri.fromFile(new File(chosenImage
                            .getFileThumbnailSmall()));

                    final Crop cropIns = Crop.of(source, destination).asSquare();
                    if(activity != null) {

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
                                if(appFragment != null) {
                                    cropIns.start(getActivity(), appFragment);
                                } else {
                                    cropIns.start(getActivity(), fragment);
                                }
                            }
                        });

                    }
                } else {
                    l.onImageChosen(chosenImage);
                }

                chosenedImage = chosenImage;
            }

            @Override
            public void onError(String s) {
                l.onError(s);
            }
        });
        try {
            imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent result){
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == Crop.REQUEST_CROP) {
                if(imageChooserListener != null) {
                    imageChooserListener.onImageChosen(chosenedImage);
                }
            } else {
                if(imageChooserManager != null) {
                    imageChooserManager.submit(requestCode, result);
                }
            }
        }
    }

    protected Activity getActivity() {
        if(activity != null) {
            return activity;
        }

        if(appFragment != null) {
            return appFragment.getActivity();
        }
        
        if(fragment != null) {
            return fragment.getActivity();
        }

        return  null;
    }
}
