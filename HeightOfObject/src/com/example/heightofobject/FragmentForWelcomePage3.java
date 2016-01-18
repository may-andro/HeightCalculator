package com.example.heightofobject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentForWelcomePage3 extends Fragment {
	
	private TextView titleFragment1;
	private TextView messageFragment1;
	
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view =inflater.inflate(R.layout.fragmentinstructionpage1, container,false);
		
		titleFragment1=(TextView) view.findViewById(R.id.titleFragment);
		titleFragment1.setText("STEP 3 :Height Calculation");
		messageFragment1=(TextView) view.findViewById(R.id.messageFragment);
		messageFragment1.setText(" To calculate height point the camrea at the top of wall ");
		
		return view;
	}


}
