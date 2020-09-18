package person.wangchen11.guirunner;

//by wangchen11

import java.io.File;
import person.wangchen11.nativeview.DebugInfo;
import person.wangchen11.nativeview.NativeInterface;
import person.wangchen11.nativeview.NativeView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	final static String TAG="MainActivity";
	NativeView mNativeView;
	private RelativeLayout mRelativeLayout = null;
	private View mFakeContentView = null;
	boolean mInited=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(0,0);
		State.init(this);

		Intent intent = getIntent();
		if(intent!=null){
			Bundle bundle = intent.getExtras();
			if(bundle!=null){
				java.util.Set<String> set = bundle.keySet();
				java.util.Iterator<String> iterator = set.iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					Log.i(TAG, key+":"+bundle.getString(key));
				}
			}
		}
		
		if(DebugInfo.mSoPath!=null && DebugInfo.mAssetsPath!=null && DebugInfo.mRequestVersion!=null)
		{
			try {
				String localSo=getFilesDir().getAbsolutePath()+"/temp.so";
				new File(localSo).delete();
				Log.i(TAG,"localSo:"+new File(localSo).exists());
				FileWork.CopyFile(new File(DebugInfo.mSoPath), new File(localSo), new byte[4096]);
				Log.i(TAG,"mSoPath:"+new File(DebugInfo.mSoPath).length());
				Log.i(TAG,"localSo:"+new File(localSo).length());
				System.load(localSo);
				mInited=true;
			} catch (Throwable e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		if(mInited)
		{
			if(!State.VersionNameNow.equals(DebugInfo.mRequestVersion))
			{
				String msg="request version:"+DebugInfo.mRequestVersion+"and current version:"+State.VersionNameNow+" not match!";
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			try {
				mNativeView=new NativeView(this);
				mRelativeLayout = new RelativeLayout(this);
				mRelativeLayout.addView(mNativeView);
				super.setContentView(mRelativeLayout);
				NativeInterface.initActivity(this);
			} catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			setContentView(R.layout.activity_main);
		}
	}
	
	@Override
	protected void onPause() {
		if(mInited)	
			NativeInterface.onPause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		if(mInited)
			NativeInterface.onResume();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		if(mInited)
			NativeInterface.destroy();
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(mInited)
		{
			if(NativeInterface.backPressed())
			{
				return ;
			}	
		}
		super.onBackPressed();
	}

	@Override
	public void setContentView(View view) {
		if(mFakeContentView!=null){
			mRelativeLayout.removeView(mFakeContentView);
			mFakeContentView = null;
		}
		mFakeContentView = view;
		mRelativeLayout.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		//super.setContentView(view);
	}
}
