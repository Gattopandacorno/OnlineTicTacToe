package io.gattopandacorno.onlinetictactoe;

import android.content.Context;
import android.widget.ProgressBar;

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
     */
    public static void showDialog(Context ctx)
    {
        if(dialog == null)
        {

            ProgressBar progressBar = new ProgressBar(ctx);
            progressBar.setIndeterminate(true);
            progressBar.setPadding(0, 0, 40, 0);

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("Waiting opponent...");
            builder.setView(progressBar);
            builder.setCancelable(false); // The user cannot dismiss it with a touch

            dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * It is used to dismiss the indeterminate progress.
     * If it is not used the user cannot dismiss it with touch event.
     */
    public static void dismissDialog()
    {
        if(dialog != null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }

    public static boolean isVisible()
    {
        return dialog.isShowing();
    }
}