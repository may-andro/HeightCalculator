package com.example.heightofobject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class FragmentForWelcomePage1 extends Fragment{
	
	TextView titleFragment1,messageFragment1;
	
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view =inflater.inflate(R.layout.fragmentinstructionpage1, container,false);
		
		titleFragment1=(TextView) view.findViewById(R.id.titleFragment);
		titleFragment1.setText("STEP 1 :Enter Height Of Camera");
		messageFragment1=(TextView) view.findViewById(R.id.messageFragment);
		messageFragment1.setText("Camera Height = Your Height - 0.3m(1 ft.).\nMake sure the object and the camera are referencing to same floor.");
		
		return view;
	}

				
	}

