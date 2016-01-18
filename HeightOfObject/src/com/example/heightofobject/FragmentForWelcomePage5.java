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

public class FragmentForWelcomePage5 extends Fragment {
	
	private TextView titleFragment1;
	private TextView messageFragment1;
	
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view =inflater.inflate(R.layout.fragmentinstructionpage1, container,false);
		
		titleFragment1=(TextView) view.findViewById(R.id.titleFragment);
		titleFragment1.setText("STEP 5 :Area and Screenshot");
		messageFragment1=(TextView) view.findViewById(R.id.messageFragment);
		messageFragment1.setText("a. After length is calculated ,Area is dispalyed.\nb. To reset the reading click on button \"Reset\".\nc. To change the metric system use the drop down in top-rightside of screen.\nd. To capture the image click on  \"Camera Icon\". ");
		
		return view;
	}


}
