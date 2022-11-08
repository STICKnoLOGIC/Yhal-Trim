package ph.STICKnoLOGIC.aerial.trimmer;
import android.content.Context;
import android.view.*;
import android.media.*;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;
import androidx.annotation.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.app.*;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.graphics.drawable.ColorDrawable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import ph.STICKnoLOGIC.aerial.trimmer.Utility;
import ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.BackgroundTask;
import ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.BarThumb;
import ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.CustomRangeSeekBar;
import ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.OnRangeSeekBarChangeListener;
import ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.OnVideoTrimListener;
import ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.TileView;
import ph.STICKnoLOGIC.aerial.trimmer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class YhalTrim extends BottomSheetDialogFragment implements View.OnClickListener {

  private final String TAG = "Yhal";

  private TextView cancel, trim, time1, timer;
  private View prog;
  private TileView tile;
  private CustomRangeSeekBar range;
  private VideoView video;
  private LinearLayout play;
  private SeekBar seekbar;
  private boolean isDragging = false;
  private boolean isDark = false;

  private ProgressDialog progress;

  private Handler handler = new Handler();

  private int min = 10, max = 20, start = 0, end = 0, duration = 0, TimeVideo = 0, mTimeVideo = 0, primCol = Color.parseColor("#ffc107");;
  private String path = "", save_to = "", error = "", fileLoc = "";
  
  private OnVideoTrimListener listener;
  
  private OnVideoTrimListener trimmer = new OnVideoTrimListener() {
    @Override
    public void onTrimStarted() {
      handler.postDelayed(onTrim, 100);
    }

    @Override
    public void getResult(Uri res) {
      if(progress!=null)
        progress.dismiss();
      fileLoc = (res.getPath());
      handler.postDelayed(exit, 300);

    }

    @Override
    public void cancelAction() {
      handler.postDelayed(onCancel, 100);
    }

    @Override
    public void onError(String msg) {
      //if(progress!=null)
      error = msg;
      handler.postDelayed(errors, 100);
    }
  };

  public static YhalTrim getInstance(String path) {
    YhalTrim yt = new YhalTrim();
    yt.path = path;
    return yt;
  }

  public YhalTrim setMaxDuration(int max) {
    if (max > 0)
      this.max = max;
    return this;
  }

  public YhalTrim setMinDuration(int min) {
    if (min > 0)
      this.min = min;
    return this;
  }

  public YhalTrim setFileDestination(String dst) {
    if (!dst.equals(""))
      save_to = dst;
    return this;
  }

  public YhalTrim setDarkMode(boolean b) {
    isDark = b;
    return this;
  }

  public YhalTrim setThemeColor(int theme) {
    primCol = theme;
    return this;
  }
  
  public YhalTrim setTrimListener(OnVideoTrimListener listener)
  {
    this.listener=listener;
    return this;
  }

  @Override
  public void onViewCreated(View v, @Nullable final Bundle s) {
    super.onViewCreated(v, s);
    cancel = v.findViewById(R.id.cancel);
    trim = v.findViewById(R.id.Trim);
    time1 = v.findViewById(R.id.time1);
    timer = v.findViewById(R.id.timer);
    tile = v.findViewById(R.id.timeLineView);
    range = v.findViewById(R.id.timeLineBar);
    video = v.findViewById(R.id.video);
    play = v.findViewById(R.id.play);
    seekbar = v.findViewById(R.id.seekbar);

    initialize(s);
    //Set content
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle save) {
    if (save != null)
      isDark = save.getBoolean("dark");

    View v = inflater.inflate(isDark ? R.layout.yhal_trim_dark : R.layout.yhal_trim, container);

    return v;
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    BottomSheetDialog dialog =
      new BottomSheetDialog(getActivity());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
    dialog.getBehavior().setHideable(false);
    return dialog;
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    try {
      savedInstanceState.putInt("min", min);
      savedInstanceState.putInt("max", max);
      savedInstanceState.putInt("start", start);
      savedInstanceState.putInt("end", end);
      savedInstanceState.putInt("dur", duration);
      savedInstanceState.putInt("time", TimeVideo);
      savedInstanceState.putInt("mtime", mTimeVideo);
      savedInstanceState.putInt("theme", primCol);
      savedInstanceState.putBoolean("dark", isDark);
      savedInstanceState.putString("save_path", save_to);
      savedInstanceState.putString("path", path);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void showText(String msg) {
    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.cancel:
      dismiss();
      break;
    case R.id.Trim:
      int dif = end - start;
      if (dif < min) {
        showText("the minimum duration is " + min + " seconds...");
        return;
      }
      if (dif > max) {
        showText("the maximum duration is " + max + " seconds...");
        return;
      }

      MediaMetadataRetriever metadata = new MediaMetadataRetriever();
      metadata.setDataSource(getActivity(), Uri.parse(path));
      final File file = new File(path);
      if (trimmer != null)
        trimmer.onTrimStarted();
      BackgroundTask.execute(new BackgroundTask.Task("", 0L, "") {
        @Override
        public void execute() {
          try {
            Utility.startTrim(getActivity(), file, save_to, start * 1000, end * 1000, trimmer);
          } catch (Exception e) {
            if (trimmer != null)
              trimmer.onError(e.getMessage());
          }
        }
      });

      break;
    case R.id.play:
      if (video != null) {

        if (video.isPlaying()) {
          video.pause();
        } else {
          video.start();
          if (seekbar.getProgress() == 0) {
            time1.setText("00 : 00");
          }
          updateProgressBar();
        }
        setPlay(video.isPlaying());

      }

      break;
    }
  }
  private Runnable onTrim = new Runnable() {
    public void run() {
      if(listener!=null){
          listener.onTrimStarted();
          return;
      }
      if (progress == null)
        progress = new ProgressDialog(isDark);
      progress.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), TAG);

    }
  };

  private Runnable exit = new Runnable() {
    public void run() {

      if(listener!=null){
          listener.getResult(Uri.parse(fileLoc));
          return;
      }
      showText("Successfully trimmed: "+fileLoc);
      dismiss();
    }
  };

  private Runnable errors = new Runnable() {
    public void run() {
      if(listener!=null){
          listener.onError(error);
          return;
      }
      if(progress!=null)
        progress.dismiss();
      showText(error);
      dismiss();
    }
  };

  private Runnable onCancel = new Runnable() {
    public void run() {
        if(listener!=null){
            listener.cancelAction();
            return;
        }
      //canceled
      showText("canceled");

    }
  };

  private void initTheme() {
    seekbar.getThumb().setColorFilter(primCol, PorterDuff.Mode.SRC_ATOP);
    setPlay(video.isPlaying());
  }

  private void updateProgressBar() {
    handler.postDelayed(updateTimeTask, 100);
  }

  private Runnable updateTimeTask = new Runnable() {
    public void run() {
      if (seekbar.getProgress() >= seekbar.getMax()) {
        seekbar.setProgress(video.getCurrentPosition() - start * 1000);
        time1.setText(ms2tmr(seekbar.getProgress()));
        video.seekTo(start * 1000);
        video.pause();
        seekbar.setProgress(0);
        time1.setText("00 : 00");
        play.setBackgroundResource(R.drawable.ic_white_play);
      } else if (video.isPlaying()) {
        seekbar.setProgress(seekbar.getProgress() + 100);
        time1.setText(ms2tmr(seekbar.getProgress()));
        handler.postDelayed(this, 100);
      }
    }
  };

  private String ms2tmr(int ms) {
    String f = "", s, m;

    int hrs = (int)(ms / (1000 * 60 * 60));
    int mnts = (int)(ms % (1000 * 60 * 60) / (1000 * 60));
    int scnds = (int)(ms % (1000 * 60 * 60) % (1000 * 60) / 1000);

    if (hrs > 0)
      f = hrs + " : ";
    if (scnds < 10)
      s = "0" + scnds;
    else
      s = scnds + "";
    if (mnts < 10)
      m = "0" + mnts;
    else
      m = mnts + "";
    f += m + " : " + s;
    return f;
  }

  private void setBitmap(Uri uri) {
    tile.setVideo(uri);
  }

  private void onVideoPrepared(MediaPlayer mp) {
    duration = video.getDuration() / 1000;

    if (duration < min) {
      trimmer.onError("this video doesn't meet the minimum duration...");
      return;
    }

    setSeekBarPosition();
  }

  private void setSeekBarPosition() {

    if (end > 0) {
      range.setThumbValue(0, (start * 100) / duration);
      range.setThumbValue(1, (end * 100) / duration);
      mTimeVideo = end - start;
    } else {
      if (duration >= max) {
        start = 0;
        end = max;
        range.setThumbValue(0, (start * 100) / duration);
        range.setThumbValue(1, (end * 100) / duration);
        mTimeVideo = max;
      } else {
        start = 0;
        end = duration;
        mTimeVideo = duration;
      }
    }
    range.initWidths(min, max, duration);
    seekbar.setMax(end * 1000);
    video.seekTo(start * 1000);
    setRange();

  }
  private void setRange() {
    String mStart = start + "";
    if (start < 10)
      mStart = "0" + start;

    int startMin = Integer.parseInt(mStart) / 60;
    int startSec = Integer.parseInt(mStart) % 60;

    String mEnd = end + "";
    if (end < 10)
      mEnd = "0" + end;

    int endMin = Integer.parseInt(mEnd) / 60;
    int endSec = Integer.parseInt(mEnd) % 60;

    timer.setText(String.format(Locale.US, "%02d : %02d - %02d : %02d", startMin, startSec, endMin, endSec));
  }

  private void onVideoCompleted() {
    handler.removeCallbacks(updateTimeTask);
    seekbar.setProgress(0);
    video.seekTo(start * 1000);
    video.pause();
    setPlay(false);
  }

  private void onSeekThumbs(int i, float v) {
    try {
      switch (i) {
      case BarThumb.LEFT: {
        start = (int)((duration * v) / 100L);
        if (end - start < min)
          start = end - min;
        else if (end - start > max)
          start = end - max;

        video.seekTo(start * 1000);
        break;
      }
      case BarThumb.RIGHT: {
        end = (int)((duration * v) / 100L);
        if (end - start < min)
          end = start + min;
        else if (end - start > max)
          end = start + max;

        video.seekTo(start * 1000);
        break;
      }
      }
      time1.setText("00 : 00");
      mTimeVideo = end - start;
      seekbar.setMax(mTimeVideo * 1000);
      seekbar.setProgress(0);
      video.seekTo(start * 1000);
      setRange();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void onStopSeekThumb() {
    //nothing todo here rn
  }

  private void setPlay(boolean b) {
    Drawable icon = getResources().getDrawable(b ? R.drawable.ic_white_pause : R.drawable.ic_white_play);
    icon.setColorFilter(primCol, PorterDuff.Mode.SRC_IN);
    play.setBackgroundDrawable(icon);
  }

  private void initialize(Bundle s) {
    cancel.setOnClickListener(this);
    trim.setOnClickListener(this);
    play.setOnClickListener(this);

    if (s != null) {
      save_to = s.getString("save_path");
      path = s.getString("path");
      min = s.getInt("min");
      max = s.getInt("max");
      start = s.getInt("start");
      end = s.getInt("end");
      duration = s.getInt("dur");
      TimeVideo = s.getInt("time");
      mTimeVideo = s.getInt("mtime");
      primCol = s.getInt("theme");
    }

    save_to = save_to.equals("") ? Environment.getExternalStorageDirectory() + "/TRIM/" : save_to;

    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
      ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      showText("Reading and Writing media files denied...");
      dismiss();
      return;
    }
    if (min >= max) {
      showText("Minimum duration should be less than to the maximum duration");
      dismiss();
      return;
    }
    File f = new File(path);
    if (!f.exists()) {

      showText("file doesnt exist...");
      dismiss();
      return;
    }

    tile.post(new Runnable() {
        @Override
        public void run()
      {
        setBitmap(Uri.parse(path));
        video.setVideoURI(Uri.parse(path));
        duration=video.getDuration() / 1000;
          
      }
    
    });
    video.requestFocus();
    
    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
        @Override
        public void onPrepared(MediaPlayer mp){
            onVideoPrepared(mp);
        }
    });
    video.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
    {
        @Override
        public void onCompletion(MediaPlayer mp){
            onVideoCompleted();
        }
    });

    range.addOnRangeSeekBarListener(new OnRangeSeekBarChangeListener() {
      @Override
      public void onCreate(CustomRangeSeekBar cr, int i, float f) {}

      @Override
      public void onSeek(CustomRangeSeekBar cr, int i, float v) {
        onSeekThumbs(i, v);
      }

      @Override
      public void onSeekStart(CustomRangeSeekBar cr, int i, float v) {
        if (video != null) {
          handler.removeCallbacks(updateTimeTask);
          seekbar.setProgress(0);
          video.seekTo(0);
          video.pause();
          setPlay(false);
        }
      }

      @Override
      public void onSeekStop(CustomRangeSeekBar cr, int i, float v) {
        onStopSeekThumb();
      }

    });

    seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (isDragging) {
          video.pause();
          time1.setText(ms2tmr(seekbar.getProgress()));
          video.seekTo(start * 1000 + seekbar.getProgress());

        }

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        if (video != null) {
          isDragging = true;
          handler.removeCallbacks(updateTimeTask);
          seekbar.setMax(mTimeVideo * 1000);
          video.seekTo(start * 1000 + seekbar.getProgress());
          video.pause();

          setPlay(false);
        }
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        isDragging = false;
        handler.removeCallbacks(updateTimeTask);
        video.seekTo((start * 1000) + seekBar.getProgress());
        video.start();
        setPlay(true);
        seekBar.setMax(mTimeVideo * 1000);
        updateProgressBar();
      }
    });
    initTheme();
  }

}