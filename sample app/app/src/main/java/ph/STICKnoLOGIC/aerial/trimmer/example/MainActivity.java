package ph.STICKnoLOGIC.aerial.trimmer.example;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import org.json.*;
import ph.STICKnoLOGIC.aerial.trimmer.*;

public class MainActivity extends AppCompatActivity {
	
	public final int REQ_CD_VIF = 101;
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	
	private ArrayList<String> files = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	
	private LinearLayout linear1;
	private Switch switch1;
	private TextView textview1;
	private EditText e;
	private Button button1;
	private Button button2;
	private ListView listview1;
	
	private Intent vif = new Intent(Intent.ACTION_GET_CONTENT);
	private Intent xx = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1010) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		linear1 = findViewById(R.id.linear1);
		switch1 = findViewById(R.id.switch1);
		textview1 = findViewById(R.id.textview1);
		e = findViewById(R.id.e);
		button1 = findViewById(R.id.button1);
		button2 = findViewById(R.id.button2);
		listview1 = findViewById(R.id.listview1);
		vif.setType("video/*");
		vif.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
						|| ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
				{
				
								ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1010);
						} else {
								startActivityForResult(vif, REQ_CD_VIF);
						}
			}
		});
		
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_refresh();
			}
		});
	}
	
	private void initializeLogic() {
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			case REQ_CD_VIF:
			if (_resultCode == Activity.RESULT_OK) {
				ArrayList<String> _filePath = new ArrayList<>();
				if (_data != null) {
					if (_data.getClipData() != null) {
						for (int _index = 0; _index < _data.getClipData().getItemCount(); _index++) {
							ClipData.Item _item = _data.getClipData().getItemAt(_index);
							_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _item.getUri()));
						}
					}
					else {
						_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _data.getData()));
					}
				}
				//add here your video checker if its corrupted or not
				YhalTrim.getInstance(_filePath.get((int)(0)))//the video source
				
				.setFileDestination(e.getText().toString())//the video output destination
				
				.setDarkMode(switch1.isChecked())//default is true
				
				.setMaxDuration(0)//default is 20
				
				.setMinDuration(0)//default is 10
				
				.setThemeColor(0xFFAFB42B)//set the color of play, pause and thumb of seekbar
				
				.setTrimListener( new ph.STICKnoLOGIC.aerial.trimmer.customVideoViews.OnVideoTrimListener()
				{//listener start
				
					 @Override
				
					    public void onTrimStarted() {//when trim started
				
					      	SketchwareUtil.showMessage(getApplicationContext(), "trimming procesd Started...");
				
					}
				
					    @Override
					    
					    public void getResult(Uri res) {//when success trimming
					    
						      String __FileLocation = (res.getPath());
					          
						SketchwareUtil.showMessage(getApplicationContext(), __FileLocation);
					                 try
					                 {
					                 
							DialogFragment prev =(DialogFragment) getSupportFragmentManager().findFragmentByTag("YHAL_TRIMMER");
					                  
							prev.dismiss();
					                 }catch(Exception e){e.printStackTrace();}//silently fail because yhal trim is possible dismissed
					           
}
					           
					    @Override
					    
					    public void cancelAction(){//when forcibly cancel
					           
						SketchwareUtil.showMessage(getApplicationContext(), "trimming process canceled...");
					                  try
						
					                  {
					                  
							DialogFragment prev =(DialogFragment) getSupportFragmentManager().findFragmentByTag("YHAL_TRIMMER");
					                   
							prev.dismiss();
					                  }catch(Exception e){e.printStackTrace();}//silently fail because yhal trim is possible dismissed
					       
					}
					       
					    @Override
					    
					    public void onError(String __errors) {//when Error
					            
						final String __error=__errors;
					            
						SketchwareUtil.showMessage(getApplicationContext(), __error);
					            
					}
					       
				}
					       )//listener end
		
				.show(MainActivity.this.getSupportFragmentManager(),"YHAL_TRIMMER");
			}
			else {
				
			}
			break;
			default:
			break;
		}
	}
	
	public void _refresh() {
		files.clear();
		map.clear();
		FileUtil.listDir(e.getText().toString().equals("")?"/storage/emulated/0/TRIM":e.getText().toString(), files);
		for (int x = 0; x < (int)(files.size()); x++) {
			HashMap<String,Object> tmp = new HashMap<>();
			tmp.put("a", files.get((int)(x)));
			map.add(tmp);
		}
		listview1.setAdapter(new Listview1Adapter(map));
		((BaseAdapter)listview1.getAdapter()).notifyDataSetChanged();
	}
	
	public class Listview1Adapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Listview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.list, null);
			}
			
			final LinearLayout main = _view.findViewById(R.id.main);
			final TextView title = _view.findViewById(R.id.title);
			final Button delete = _view.findViewById(R.id.delete);
			
			title.setText(Uri.parse(_data.get((int)_position).get("a").toString()).getLastPathSegment());
			main.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					xx.setDataAndType(Uri.parse("file://".concat(_data.get((int)_position).get("a").toString())),"video/*");
					
					startActivity(xx);
				}
			});
			main.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					FileUtil.deleteFile(_data.get((int)_position).get("a").toString());
					_refresh();
				}
			});
			
			return _view;
		}
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}