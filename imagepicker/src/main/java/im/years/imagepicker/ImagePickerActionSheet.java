package im.years.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;

/**
 * Created by alvinzeng on 11/6/15.
 */
public class ImagePickerActionSheet extends PopupWindow {

    public interface ImagePickerActionSheetListener {

        public void onItemSelected(int index);
    }

    public ImagePickerActionSheet(Activity activity, final ImagePickerActionSheetListener l) {
        super(activity);

        View view = View.inflate(activity, R.layout.im_pop_image_picker, null);
        view.startAnimation(AnimationUtils.loadAnimation(activity,
                R.anim.im_fade_ins));

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        setBackgroundDrawable(dw);
        setFocusable(true);
        setOutsideTouchable(true);
        setContentView(view);

        View parent = ((ViewGroup)activity
                .findViewById(android.R.id.content)).getChildAt(0);

        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        update();

        Button bt1 = (Button) view
                .findViewById(R.id.item_popupwindows_camera);
        Button bt2 = (Button) view
                .findViewById(R.id.item_popupwindows_Photo);
        Button bt3 = (Button) view
                .findViewById(R.id.item_popupwindows_cancel);

        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                l.onItemSelected(0);
                dismiss();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                l.onItemSelected(1);
                dismiss();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                l.onItemSelected(2);
                dismiss();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


}
