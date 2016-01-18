package com.example.heightofobject;



import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class CustomAlertDialogFragment extends DialogFragment {

	Context context;
	RelativeLayout layout;
	private SharedPreferences sharedPreferencesNeverShowAgain;

	public CustomAlertDialogFragment(Context context,RelativeLayout relativeLayout){
		this.context= context;
		layout=relativeLayout;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.dialog_layout_for_instruction_message, container);
		layout.setAlpha(0.2f);
		
		sharedPreferencesNeverShowAgain = PreferenceManager.getDefaultSharedPreferences(context);
		
		final LinearLayout indicaterLayout=(LinearLayout)rootView. findViewById(R.id.layoutIndicater);
		ViewPager viewPager=(ViewPager) rootView.findViewById(R.id.pagerInstruction);
		final ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

		adapter.addFrag(new FragmentForWelcomePage1());
		adapter.addFrag(new FragmentForWelcomePage2());
		adapter.addFrag(new FragmentForWelcomePage3());
		viewPager.setAdapter(adapter);

		for (int i = 0; i < adapter.getCount(); i++)
		{
			ImageButton imageDotIndicater = new ImageButton(context);
			imageDotIndicater.setTag(i);
			imageDotIndicater.setImageResource(R.drawable.dot_selector);
			imageDotIndicater.setBackgroundResource(0);
			imageDotIndicater.setPadding(5, 5, 5, 5);
			LayoutParams params = new LayoutParams(20, 20);
			imageDotIndicater.setLayoutParams(params);
			if(i == 0)
				imageDotIndicater.setSelected(true);

			indicaterLayout.addView(imageDotIndicater);
		}

		viewPager.addOnPageChangeListener(new OnPageChangeListener()
		{

			public void onPageSelected(int pos)
			{
				Log.e("", "Page Selected is ===> " + pos);
				for (int i = 0; i < adapter.getCount(); i++)
				{
					if(i != pos)
					{
						((ImageView)indicaterLayout.findViewWithTag(i)).setSelected(false);
					}
				}
				((ImageView)indicaterLayout.findViewWithTag(pos)).setSelected(true);
			}

			public void onPageScrolled(int pos, float arg1, int arg2)
			{

			}

			public void onPageScrollStateChanged(int arg0)
			{

			}
		});
		
		
		TextView txtTitle = (TextView) rootView.findViewById(R.id.dialog_title);
		txtTitle.setText("How to USE");
		TextView txtTitle2 = (TextView) rootView.findViewById(R.id.title1);
		txtTitle2.setText("Instructions");

		ViewFlipper mFlipper = ((ViewFlipper) rootView.findViewById(R.id.flipper));
		mFlipper.startFlipping();
		mFlipper.setInAnimation(AnimationUtils.loadAnimation(context,android.R.anim.fade_in));
		mFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out));




		Button done = (Button) rootView.findViewById(R.id.done);
		done.setText("Got It");
		done.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setAlpha(1.0f);
				dismiss();
			}
		});

		Button neverShow = (Button) rootView.findViewById(R.id.nevershow);
		neverShow.setText("Never Show Again");
		neverShow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editors = sharedPreferencesNeverShowAgain.edit();
				editors.putBoolean("NeverShowAgain",true);
				editors.commit();
				layout.setAlpha(1.0f);
				dismiss();
			}
		});

		getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
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
		getDialog().setCanceledOnTouchOutside(false);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		
		
		return rootView;
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
	}
	
}