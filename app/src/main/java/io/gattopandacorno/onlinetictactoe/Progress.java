package io.gattopandacorno.onlinetictactoe;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class Progress
{

    private static AlertDialog dialog = null;

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
            builder.setCancelable(true);
            builder.setView(ll);

            dialog = builder.create();
            dialog.show();
        }
    }

    public static  boolean isVisible()
    {
        if(dialog != null)
            return dialog.isShowing();
        else
            return false;
    }

    public static  void dismissDialog()
    {
        if(dialog != null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }
}