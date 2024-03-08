package io.gattopandacorno.onlinetictactoe;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/**
 * This class is used to replace the deprecated ProgressDialog.
 */
public class Progress
{

    private static AlertDialog dialog = null;

    /**
     * It will show the indeterminate progress with the given message.
     *
     * @param ctx This is the application context.
     * @param msg This is the String to display while waiting the progress to finish.
     */
    public static void showDialog(Context ctx, String msg)
    {
        if(dialog == null)
        {
            LinearLayout ll = new LinearLayout(ctx);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(40, 40, 40, 40);
            ll.setGravity(Gravity.CENTER);
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ll.setGravity(Gravity.CENTER);

            ProgressBar progressBar = new ProgressBar(ctx);
            progressBar.setIndeterminate(true);
            progressBar.setPadding(0, 0, 40, 0);
            progressBar.setLayoutParams(ll.getLayoutParams());

            TextView tvText = new TextView(ctx);
            tvText.setText(msg);
            tvText.setTextColor(Color.parseColor("#000000"));
            tvText.setTextSize(20);
            tvText.setLayoutParams(ll.getLayoutParams());

            ll.addView(progressBar);
            ll.addView(tvText);

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setCancelable(false); // The user cannot dismiss it with a touch
            builder.setView(ll);

            dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * It is used to dismiss the indeterminate progress.
     * If it is not used the user cannot dismiss it with touch event.
     */
    public static  void dismissDialog()
    {
        if(dialog != null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }
}