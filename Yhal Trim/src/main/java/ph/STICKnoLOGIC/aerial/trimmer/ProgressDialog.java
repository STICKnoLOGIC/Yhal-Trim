package ph.STICKnoLOGIC.aerial.trimmer;
import androidx.annotation.*;
import android.view.*;

import android.os.*;
import android.content.*;
import android.app.*;
import android.widget.Button;
import android.graphics.*;
import android.graphics.drawable.*;
import ph.STICKnoLOGIC.aerial.trimmer.R;
import androidx.fragment.app.DialogFragment;

public class ProgressDialog extends DialogFragment {
  private Button cancel;
  private boolean isDark = true;

  public ProgressDialog(boolean b) {
    isDark = b;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    if (savedInstanceState != null)
      isDark = savedInstanceState.getBoolean("dark");
    View v = inflater.inflate(isDark ? R.layout.yhal_prog_dark : R.layout.yhal_prog, container);
    cancel = v.findViewById(R.id.cancel);
    cancel.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        dismiss();
      }
    });
    return v;
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    try {
      savedInstanceState.putBoolean("dark", isDark);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Utility.cancel();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    Dialog dialog = new Dialog(getActivity());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(false);
    dialog.setCanceledOnTouchOutside(false);

    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
    return dialog;

  }

}