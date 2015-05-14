package com.kodakalaris.kodakmomentslib.activity.contactdetails;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.util.StringUtils;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;

public class MContactDetailsActivity extends BaseContactDetailsActivity {
	private static final String TAG = "MContactDetailsActivity";
	
	private MActionBar vActionBar;
	private EditText vEtxtFirstname;
	private EditText vEtxtLastname;
	private EditText vEtxtEmail;
	private EditText vEtxtPhone;
	private LinearLayout vLinelyErrorFirstname;
	private LinearLayout vLinelyErrorLastname;
	private LinearLayout vLinelyErrorEmail;
	private LinearLayout vLinelyErrorPhone;
	
	private Runnable mCheckFirstname;
	private Runnable mCheckLastname;
	private Runnable mCheckEmail;
	private Runnable mCheckPhone;
	
	private boolean mIsFirstnameValid;
	private boolean mIsLastnameValid;
	private boolean mIsEmailValid;
	private boolean mIsPhoneValid;
	
	private LocalCustomerInfo mCustomerInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_contact_details);
		
		initViews();
		initData();
		initEvents();
	}
	
	private void initViews() {
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		vEtxtFirstname = (EditText) findViewById(R.id.etxt_firstname);
		vEtxtLastname = (EditText) findViewById(R.id.etxt_lastname);
		vEtxtEmail = (EditText) findViewById(R.id.etxt_email);
		vEtxtPhone = (EditText) findViewById(R.id.etxt_phone);
		vLinelyErrorFirstname = (LinearLayout) findViewById(R.id.linely_error_firstname);
		vLinelyErrorLastname = (LinearLayout) findViewById(R.id.linely_error_lastname);
		vLinelyErrorEmail = (LinearLayout) findViewById(R.id.linely_error_email);
		vLinelyErrorPhone = (LinearLayout) findViewById(R.id.linely_error_phone);
	}
	
	private void initData() {
		mCustomerInfo = new LocalCustomerInfo(this);
		
		vEtxtFirstname.setText(mCustomerInfo.getCusFirstName());
		vEtxtLastname.setText(mCustomerInfo.getCusLastName());
		vEtxtEmail.setText(mCustomerInfo.getCusEmail());
		vEtxtPhone.setText(mCustomerInfo.getCusPhone());
		
		mCheckFirstname = new Runnable() {
			
			@Override
			public void run() {
				vLinelyErrorFirstname.setVisibility(isFirstNameValid() ? View.GONE : View.VISIBLE);
				updateSaveBtn();
			}
		};
		
		mCheckLastname = new Runnable() {
			
			@Override
			public void run() {
				vLinelyErrorLastname.setVisibility(isLastNameValid() ? View.GONE : View.VISIBLE);
				updateSaveBtn();
			}
		};
		
		mCheckEmail = new Runnable() {
			
			@Override
			public void run() {
				vLinelyErrorEmail.setVisibility(isEmailValid() ? View.GONE : View.VISIBLE);
				updateSaveBtn();
			}
		};
		
		mCheckPhone = new Runnable() {
			
			@Override
			public void run() {
				vLinelyErrorPhone.setVisibility(isPhoneValid() ? View.GONE : View.VISIBLE);
				updateSaveBtn();
			}
		};
	}
	
	private void saveData() {
		mCustomerInfo.setCusFirstName(vEtxtFirstname.getText().toString());
		mCustomerInfo.setCusLastName(vEtxtLastname.getText().toString());
		mCustomerInfo.setCusEmail(vEtxtEmail.getText().toString());
		mCustomerInfo.setCusPhone(vEtxtPhone.getText().toString());
		
		mCustomerInfo.save(this);
		SharedPreferrenceUtil.setString(this, SharedPreferrenceUtil.BACK_DOOR_NAME, mCustomerInfo.getCusFirstName());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		checkAndUpdateInfo();
	}
	
	private void initEvents() {
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		vActionBar.setOnRightButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean allValid = checkAndUpdateInfo();
				if (!allValid) {
					Log.i(TAG, "some info is wrong");
				} else {
					saveData();
					finish();
				}
				
			}
		});
		
		vEtxtFirstname.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				vEtxtFirstname.removeCallbacks(mCheckFirstname);//remove previous runnable to improve performance
				vEtxtLastname.postDelayed(mCheckFirstname, 300);
			}
		});
		
		vEtxtLastname.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				vEtxtLastname.removeCallbacks(mCheckLastname);
				vEtxtLastname.postDelayed(mCheckLastname, 300);
			}
		});
		
		vEtxtEmail.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				vEtxtEmail.removeCallbacks(mCheckEmail);
				vEtxtEmail.postDelayed(mCheckEmail, 300);
			}
		});
		
		vEtxtPhone.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				vEtxtPhone.removeCallbacks(mCheckPhone);
				vEtxtPhone.postDelayed(mCheckPhone, 300);
			}
		});
		
		vEtxtFirstname.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mCheckFirstname.run();
			}
		});
		vEtxtLastname.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mCheckLastname.run();
			}
		});
		vEtxtEmail.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mCheckEmail.run();
			}
		});
		vEtxtPhone.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mCheckPhone.run();
			}
		});
		
	}
	
	private boolean checkAndUpdateInfo() {
		boolean result = true;
		
		if (isFirstNameValid()) {
			vLinelyErrorFirstname.setVisibility(View.GONE);
		} else {
			vLinelyErrorFirstname.setVisibility(View.VISIBLE);
			result = false;
		}
		
		if (isLastNameValid()) {
			vLinelyErrorLastname.setVisibility(View.GONE);
		} else {
			vLinelyErrorLastname.setVisibility(View.VISIBLE);
			result = false;
		}
		
		if (isEmailValid()) {
			vLinelyErrorEmail.setVisibility(View.GONE);
		} else {
			vLinelyErrorEmail.setVisibility(View.VISIBLE);
			result = false;
		}
		
		if (isPhoneValid()) {
			vLinelyErrorPhone.setVisibility(View.GONE);
		} else {
			vLinelyErrorPhone.setVisibility(View.VISIBLE);
			result = false;
		}
		
		vActionBar.setRightBtnEnabled(result);
		
		return result;
	}
	
	private void updateSaveBtn() {
		vActionBar.setRightBtnEnabled(mIsFirstnameValid && mIsLastnameValid && mIsEmailValid && mIsPhoneValid);
	}
	
	private boolean isFirstNameValid() {
		mIsFirstnameValid = !StringUtils.isBlank(vEtxtFirstname.getText().toString());
		return mIsFirstnameValid;
	}
	
	private boolean isLastNameValid() {
		mIsLastnameValid = !StringUtils.isBlank(vEtxtLastname.getText().toString());
		return mIsLastnameValid;
	}
	
	private boolean isEmailValid() {
		String email = vEtxtEmail.getText().toString();
		mIsEmailValid = !StringUtils.isBlank(email);
		if (mIsEmailValid) {
			String str = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
			mIsEmailValid = email.matches(str);
		}
		return mIsEmailValid;
	}
	
	private boolean isPhoneValid() {
		mIsPhoneValid = !StringUtils.isBlank(vEtxtPhone.getText().toString());
		return mIsPhoneValid;
	}
}
