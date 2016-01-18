package com.example.heightofobject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentForWelcomePage4 extends Fragment {
	
	private TextView titleFragment1;
	private TextView messageFragment1;
	
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view =inflater.inflate(R.layout.fragmentinstructionpage1, container,false);
		
		titleFragment1=(TextView) view.findViewById(R.id.titleFragment);
		titleFragment1.setText("STEP 4 :Length Calculation");
		messageFragment1=(TextView) view.findViewById(R.id.messageFragment);
		messageFragment1.setText("a. Point the camera at one end of wall(let say left) and click on button \"Get Lenght\".\nb. Then rotate the camera to other end of wall(let say right) and click on button \"Lock Lenght\". ");
		
		return view;
	}


}
