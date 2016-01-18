package com.example.heightofobject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
SensorEventListener {
	private Camera camera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private boolean isPreviewRunning = false;

	SensorManager mSensorManager;
	Sensor mAccelerometer;
	Sensor mMagnetometer;

	private float[] mGravity;
	private float[] mMagnetic;
	float[] value = new float[3];
	float pressure;
	private float pitch,azimut;
	
	private double h=0.0;
	private double D;
	private double H;
	private double L;

	DecimalFormat ThreeDForm = new DecimalFormat("#.###");
	private double AngleA = 0.0;
	private double AngleB = 0.0;
	
	TextView textViewAdjustedHeight, textViewGetDistanceContinious;
	TextView textViewGetDistance, textViewGetHeightContinious;
	TextView textViewGetHeight;
	Button buttonAdjustHeight, buttonLockDistance, buttonLockHeight, buttonReset;
	private Spinner spinner;
	RelativeLayout layout, heightContainer,distanceContainer;
	private boolean istextViewGetDistanceContiniousVisible = true;

	private int unit;
	private String[] units = { "m", "cm", "ft.", "in." };

	Button  buttonScreenShot;

	// Sound ID can't be 0 (zero)
	private SoundPool soundPool;
	private SparseIntArray soundPoolMap;

	boolean isReset=false;

	SharedPreferences sharedPreferencesNeverShowAgain ;
	boolean neverShowAgain;
	
	private CompassArrowView mCompassArrowView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);

		File phnMem = Environment.getExternalStorageDirectory();
		File dir= new File(phnMem + "/HeightOfObject");
		dir.mkdirs();

		surfaceView = (SurfaceView) findViewById(R.id.surface);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		initilaizeControlAndFields();

		sharedPreferencesNeverShowAgain = PreferenceManager.getDefaultSharedPreferences(this);
		neverShowAgain = sharedPreferencesNeverShowAgain.getBoolean("NeverShowAgain", false);
		if(!neverShowAgain){
			CustomAlertDialogFragment alertdFragment = new CustomAlertDialogFragment(this,layout);
			// Show Alert DialogFragment
			alertdFragment.show( getSupportFragmentManager(), "");
		}
		
		mCompassArrowView = new CompassArrowView(getApplicationContext());
		layout.addView(mCompassArrowView);
		

		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap =  new SparseIntArray();
		soundPoolMap.put(1, soundPool.load(MainActivity.this, R.raw.camera, 1));


		setAndAdjustHeight();

		lockDistance();

		lockHeight();

		resetData();

		addListenerOnSpinnerItemSelection();

		screenShot();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		float[] distances = new float[3];
		parameters.getFocusDistances(distances);
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (isPreviewRunning) {
			camera.stopPreview();
		}
		camera.setDisplayOrientation(90);
		camera.setParameters(camera.getParameters());
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
		isPreviewRunning = true;
	}


	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		isPreviewRunning = false;
		camera.release();
	}

	public void onSensorChanged(SensorEvent event)
	{
		switch (event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			mGravity = lowPass(event.values.clone(), mGravity);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mMagnetic = lowPass(event.values.clone(), mMagnetic);
			break;
		default:
			return;
		}

		if (mGravity != null && mMagnetic != null) 
		{
			getDirection();
			mCompassArrowView.invalidate();
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}


	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mMagnetometer,SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	private float getDirection() {
		float[] temp = new float[9];
		float[] R = new float[9];
		// Load rotation matrix into R
		SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);
		// Remap to camera's point-of-view
		SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X,	SensorManager.AXIS_Z, R);
		// Return the orientation values
		SensorManager.getOrientation(R, value);

		pitch = value[1];
		azimut = value[0];
		
		textViewGetDistanceContinious.setText(""+ Double.valueOf(ThreeDForm.format(Math.abs(h* (Math.tan((Math.toRadians(90) - pitch))))))	+ units[unit]);

		if (!istextViewGetDistanceContiniousVisible)
		{
			textViewGetHeightContinious.setText(""+ Double.valueOf(ThreeDForm.format(h+ Math.abs(D * Math.tan((pitch))))) + units[unit]);
		}

		return value[1];
	}

	protected float[] lowPass(float[] input, float[] output) {
		if (output == null)
			return input;

		for (int i = 0; i < input.length; i++) {
			output[i] = output[i] + 0.8f * (input[i] - output[i]);
		}
		return output;
	}

	private void initilaizeControlAndFields() {
		textViewAdjustedHeight = (TextView) findViewById(R.id.textViewAdjustedHeight);
		textViewGetDistance = (TextView) findViewById(R.id.textViewDistance);
		textViewGetHeight = (TextView) findViewById(R.id.textViewHeight);
		textViewGetDistanceContinious = (TextView) findViewById(R.id.textViewDistanceContinious);
		textViewGetHeightContinious = (TextView) findViewById(R.id.textViewHeightContinious);

		buttonAdjustHeight = (Button) findViewById(R.id.buttonSetHieght);
		buttonLockDistance = (Button) findViewById(R.id.buttonDistance);
		buttonLockHeight = (Button) findViewById(R.id.buttonHeight);
		buttonReset = (Button) findViewById(R.id.buttonReset);
		buttonScreenShot = (Button) findViewById(R.id.buttonScreenShot);

		buttonLockDistance.setBackgroundResource(R.color.buttonGreen);
		buttonLockHeight.setBackgroundResource(R.color.buttonGreen);

		buttonLockDistance.setVisibility(View.GONE);
		buttonLockHeight.setVisibility(View.GONE);
		buttonScreenShot.setVisibility(View.GONE);

		spinner = (Spinner) findViewById(R.id.spinner1);

		layout = (RelativeLayout) findViewById(R.id.mainContainer);
		heightContainer = (RelativeLayout) findViewById(R.id.heightContainer);
		distanceContainer = (RelativeLayout) findViewById(R.id.lockDistanceContainer);

		heightContainer.setVisibility(View.GONE);
		distanceContainer.setVisibility(View.GONE);
		buttonReset.setVisibility(View.GONE);

	}

	private void setAndAdjustHeight() 
	{
		buttonAdjustHeight.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v) {
				showCustomDialogForAdjustHeight("Input Height",	"Set the height of camera from ground");
			}

		});

	}

	private void showCustomDialogForAdjustHeight(String title, String Msg) {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogLayout = inflater.inflate(R.layout.dialog_layout_for_adjust_height, null, false);
		layout.setAlpha(0.2f);

		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(dialogLayout);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

		// Retrieve views from the inflated dialog layout and update their
		// values
		TextView txtTitle = (TextView) dialog.findViewById(R.id.dialog_title);
		txtTitle.setText(title);
		TextView txtTitle2 = (TextView) dialog.findViewById(R.id.title1);
		txtTitle2.setText(title);

		ViewFlipper mFlipper = ((ViewFlipper) dialog.findViewById(R.id.flipper));
		mFlipper.startFlipping();
		mFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,
				android.R.anim.fade_in));
		mFlipper.setOutAnimation(AnimationUtils.loadAnimation(
				MainActivity.this, android.R.anim.fade_out));

		TextView txtMessage = (TextView) dialog.findViewById(R.id.txt_dialog_message);
		txtMessage.setText(Msg);

		TextInputLayout flaotAdjustHeight = (TextInputLayout) dialog.findViewById(R.id.floatingdialog);
		flaotAdjustHeight.setErrorEnabled(true);

		final EditText editTextAdjustHeight = (EditText) dialog.findViewById(R.id.edit_dialog_data);
		editTextAdjustHeight.setText(String.valueOf(h));
		editTextAdjustHeight.setSelection(editTextAdjustHeight.getText().length());

		Button close = (Button) dialog.findViewById(R.id.cameraButton);
		close.setText("Close");
		close.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				layout.setAlpha(1.0f);
				dialog.dismiss();
			}

		});

		Button done = (Button) dialog.findViewById(R.id.galleryButton);
		done.setText("Done");
		done.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				h = Double.parseDouble(editTextAdjustHeight.getText()
						.toString());
				if (h == 0) {
					Toast toast = Toast.makeText(getApplicationContext(),"Height must be more than 0!", Toast.LENGTH_SHORT);
					toast.show();
				} else { 
					textViewAdjustedHeight.setText("" + h + units[unit]);
					distanceContainer.setVisibility(View.VISIBLE);
					buttonLockDistance.setVisibility(View.VISIBLE);
				}
				layout.setAlpha(1.0f);
				dialog.dismiss();
			}

		});

		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dialog.dismiss();
					layout.setAlpha(1.0f);
					return true;
				}
				return false;
			}

		});
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.show();
	}

	private void lockDistance() {
		buttonLockDistance.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AngleA = getDirection();
				AngleA = Math.toRadians(90) - AngleA;
				D = Double.valueOf(ThreeDForm.format(Math.abs(h* (Math.tan((AngleA))))));

				textViewGetDistanceContinious.setVisibility(View.GONE);
				textViewGetDistance.setVisibility(View.VISIBLE);
				textViewGetDistance.setText("" + D + units[unit]);

				heightContainer.setVisibility(View.VISIBLE);
				buttonLockHeight.setVisibility(View.VISIBLE);

				istextViewGetDistanceContiniousVisible = false;

				buttonLockDistance.setBackgroundResource(R.color.buttonRed);
				buttonLockDistance.setText("Distance Locked");
				buttonLockDistance.setEnabled(false);
				
				buttonReset.setVisibility(View.VISIBLE);

				if(isReset){
					textViewGetHeightContinious.setVisibility(View.VISIBLE);
					textViewGetHeight.setVisibility(View.GONE);
					buttonLockHeight.setText("Lock Height");
					buttonLockHeight.setEnabled(true);
					buttonLockHeight.setBackgroundResource(R.color.buttonGreen);
				}
			}

		});
	}

	private void lockHeight() {
		buttonLockHeight.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AngleB = 0;
				while (AngleB == 0) {
					AngleB = getDirection();
				}
				H = Double.valueOf(ThreeDForm.format(h+ Math.abs(D * Math.tan((AngleB)))));
				textViewGetHeight.setVisibility(View.VISIBLE);
				textViewGetHeightContinious.setVisibility(View.GONE);
				textViewGetHeight.setText("" + H +  units[unit] );

			}

		});
	}

	private void resetData() 
	{
		buttonReset.setOnClickListener(new OnClickListener() 
		{

			public void onClick(View v) {
				AngleA = 0.0;
				AngleB = 0.0;
				D = 0.0;
				H = 0.0;
				L = 0.0;

				distanceContainer.setVisibility(View.VISIBLE);
				heightContainer.setVisibility(View.GONE);
				buttonScreenShot.setVisibility(View.GONE);
				buttonLockHeight.setVisibility(View.GONE);
				buttonLockDistance.setVisibility(View.VISIBLE);
				buttonLockDistance.setEnabled(true);
				buttonLockDistance.setText("Lock Distance");
				buttonLockDistance.setBackgroundResource(R.color.buttonGreen);
				textViewGetDistanceContinious.setVisibility(View.VISIBLE);
				textViewGetDistance.setVisibility(View.GONE);

				istextViewGetDistanceContiniousVisible=true;


				Toast toast = Toast.makeText(getApplicationContext(),"Values reset!", Toast.LENGTH_SHORT);
				toast.show();

				isReset=true;
			}

		});
	}

	public void addListenerOnSpinnerItemSelection()
	{
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.units_array,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3)
			{
				Toast.makeText(arg0.getContext(),"Unit selected : "+ arg0.getItemAtPosition(arg2).toString(),Toast.LENGTH_SHORT).show();

				if (arg2 == 0) {
					// convert to mts
					if (unit == 1) {// cm-m
						h = Double.valueOf(ThreeDForm.format(h * 0.01));
						H = Double.valueOf(ThreeDForm.format(H * 0.01));
						D = Double.valueOf(ThreeDForm.format(D * 0.01));
						L = Double.valueOf(ThreeDForm.format(L * 0.01));
					} else if (unit == 2) {// ft-m
						h = Double.valueOf(ThreeDForm.format(h * .3048));
						H = Double.valueOf(ThreeDForm.format(H * .3048));
						D = Double.valueOf(ThreeDForm.format(D * .3048));
						L = Double.valueOf(ThreeDForm.format(L * .3048));
					} else if (unit == 3) {// in-m
						h = Double.valueOf(ThreeDForm.format(h * .0254));
						H = Double.valueOf(ThreeDForm.format(H * .0254));
						D = Double.valueOf(ThreeDForm.format(D * .0254));
						L = Double.valueOf(ThreeDForm.format(L * .0254));
					}
					unit = 0;
					setUnitInFields();

				} else if (arg2 == 1) {// convert to cms
					if (unit == 0) {// m-cm
						h = Double.valueOf(ThreeDForm.format(h * 100));
						H = Double.valueOf(ThreeDForm.format(H * 100));
						D = Double.valueOf(ThreeDForm.format(D * 100));
						L = Double.valueOf(ThreeDForm.format(L * 100));
					} else if (unit == 2) {// ft-cm
						h = Double.valueOf(ThreeDForm.format(h * 30.48));
						H = Double.valueOf(ThreeDForm.format(H * 30.48));
						D = Double.valueOf(ThreeDForm.format(D * 30.48));
						L = Double.valueOf(ThreeDForm.format(L * 30.48));
					} else if (unit == 3) {// in-cm
						h = Double.valueOf(ThreeDForm.format(h * 2.54));
						H = Double.valueOf(ThreeDForm.format(H * 2.54));
						D = Double.valueOf(ThreeDForm.format(D * 2.54));
						L = Double.valueOf(ThreeDForm.format(L * 2.54));
					}
					unit = 1;
					setUnitInFields();
				} else if (arg2 == 2) {// convert to feet
					if (unit == 0) {// m-ft
						h = Double.valueOf(ThreeDForm.format(h * 3.28084));
						H = Double.valueOf(ThreeDForm.format(H * 3.28084));
						D = Double.valueOf(ThreeDForm.format(D * 3.28084));
						L = Double.valueOf(ThreeDForm.format(L * 3.28084));
					} else if (unit == 1) {// cm-ft
						h = Double.valueOf(ThreeDForm.format(h * 0.0328084));
						H = Double.valueOf(ThreeDForm.format(H * 0.0328084));
						D = Double.valueOf(ThreeDForm.format(D * 0.0328084));
						L = Double.valueOf(ThreeDForm.format(L * 0.0328084));
					} else if (unit == 3) {// in-ft
						h = Double.valueOf(ThreeDForm.format(h * 0.0833333));
						H = Double.valueOf(ThreeDForm.format(H * 0.0833333));
						D = Double.valueOf(ThreeDForm.format(D * 0.0833333));
						L = Double.valueOf(ThreeDForm.format(L * 0.0833333));
					}
					unit = 2;
					setUnitInFields();
				} else {// convert to in
					if (unit == 0) {// m-in
						h = Double.valueOf(ThreeDForm.format(h * 39.3701));
						H = Double.valueOf(ThreeDForm.format(H * 39.3701));
						D = Double.valueOf(ThreeDForm.format(D * 39.3701));
						L = Double.valueOf(ThreeDForm.format(L * 39.3701));
					} else if (unit == 1) {// cm-in
						h = Double.valueOf(ThreeDForm.format(h * 0.393701));
						H = Double.valueOf(ThreeDForm.format(H * 0.393701));
						D = Double.valueOf(ThreeDForm.format(D * 0.393701));
						L = Double.valueOf(ThreeDForm.format(L * 0.393701));
					} else if (unit == 2) {// ft-in
						h = Double.valueOf(ThreeDForm.format(h * 12));
						H = Double.valueOf(ThreeDForm.format(H * 12));
						D = Double.valueOf(ThreeDForm.format(D * 12));
						L = Double.valueOf(ThreeDForm.format(L * 12));
					}
					unit = 3;
					setUnitInFields();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	private void setUnitInFields() {
		textViewAdjustedHeight.setText("" + ThreeDForm.format(h) + units[unit]);
		textViewGetDistance.setText("" + ThreeDForm.format(D )+ units[unit]);
		textViewGetHeight.setText("" + ThreeDForm.format(H) + units[unit]);
	}

	private void screenShot() {
		buttonScreenShot.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				soundPool.play(1, 1, 1, 5, 0, 1);
				Bitmap bitmap = takeScreenshot();
				saveBitmap(bitmap);
			}
		});
	}

	public Bitmap takeScreenshot() {
		View rootView = findViewById(android.R.id.content).getRootView();
		rootView.setDrawingCacheEnabled(true);
		return rootView.getDrawingCache();
	}

	public void saveBitmap(Bitmap bitmap) {
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/HeightOfObject"+ "/screenshot.png");
		FileOutputStream fos;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();

			Toast.makeText(MainActivity.this, "image saved at:"+file.getAbsolutePath(), Toast.LENGTH_LONG).show();

		} 
		catch (FileNotFoundException e) 
		{
		} 
		catch (IOException e) 
		{
		}
	}

	public class CompassArrowView extends View 
	{
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.mark);
		int mBitmapWidth = bitmap.getWidth();
		Paint paint = new Paint();
		int mParentWidth, mParentHeight, mParentCenterX, mParentCenterY,
				mViewTopX, mViewLeftY;

		public CompassArrowView(Context context) {
			super(context);
			paint.setColor(0xff00ff00);
			// style
			paint.setStyle(Style.STROKE);
			// stroke width
			paint.setStrokeWidth(10);
			// antiAlias
			paint.setAntiAlias(true);
			// text size
			paint.setTextSize(30);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mParentWidth = layout.getWidth();
			mParentHeight = layout.getHeight();
			mParentCenterX = mParentWidth / 2;
			mParentCenterY = mParentHeight / 2;
			mViewLeftY = mParentCenterX - mBitmapWidth / 2;
			mViewTopX = mParentCenterY - mBitmapWidth / 2;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.save();
			canvas.rotate((float) Math.toDegrees(azimut), mParentCenterX,mParentCenterY);
			canvas.drawBitmap(bitmap, mViewLeftY, mViewTopX, null);
			paint.setColor(Color.GREEN);
			paint.setStrokeWidth(2);
			// draw two lines
			canvas.drawCircle(mParentWidth / 2, mParentHeight / 2,mParentWidth / 4, paint);
			canvas.drawLine(mParentWidth/4, mParentCenterY, mParentWidth - mParentWidth/4, mParentCenterY,paint);
			canvas.drawLine(mParentCenterX, mParentHeight/2-mParentWidth / 4, mParentCenterX,mParentHeight/2 + mParentWidth/4,paint);
			
			canvas.restore();

			
		}
	}
}

