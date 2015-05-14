package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QntWidgetLand extends LinearLayout
{
	Button decrease;
	Button increase;
	TextView qnty;

	public QntWidgetLand(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.qntywidgetland, this);
		decrease = (Button) findViewById(R.id.decrease);
		increase = (Button) findViewById(R.id.increase);
		qnty = (TextView) findViewById(R.id.qnty);
		decrease.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				int quantity = Integer.parseInt(qnty.getText().toString());
				if (quantity == 0)
				{
				}
				else
				{
					quantity--;
					qnty.setText("" + quantity);
				}
			}
		});
		increase.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				int quantity = Integer.parseInt(qnty.getText().toString());
				quantity++;
				qnty.setText("" + quantity);
			}
		});
	}
}
