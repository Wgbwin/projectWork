package com.kodakalaris.kodakmomentslib.fragment.mobile.menu;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.OptionsDialogModel;
import com.kodakalaris.kodakmomentslib.bean.OptionsModel;
import com.kodakalaris.kodakmomentslib.interfaces.menu.OnHeadlineSelectedListener;
import com.kodakalaris.kodakmomentslib.util.CumulusDataUtil;

public class MMentRadioFragment extends Fragment {
	
	public static final String TYPE = "type";
	public static final int TYPE_COUNTRY = 1;
	public static final int TYPE_PRINT_SIZE = 2;
	public static final String OptionsDialogModel = "mOptionsDialogModel";

	private ListView vLisvSize;
	private OnHeadlineSelectedListener mCallback ;
	private Activity mContext;
	private LayoutInflater mInflater;
	private List<OptionsModel> optionModelArrayList;
	private int selecterNum=0;
	private RadioAdapter mAdapter;
	
	private int mType;

	public static MMentRadioFragment getDefaultSizeFragmentInstance(Bundle bundle){
		MMentRadioFragment fileViewFragment = new MMentRadioFragment();
		fileViewFragment.setArguments(bundle);
		return fileViewFragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback  = (OnHeadlineSelectedListener) activity;
		mContext = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_m_radio, null);
		initView(view);
		initData();
		setEvent();
		return view;
	}

	private void initView(View view) {
		vLisvSize = (ListView) view.findViewById(R.id.lisv_menu_radio);

	}

	private void setEvent() {
		vLisvSize.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//save  photo size sharedpreferences
				optionModelArrayList.get(selecterNum).setSelected(false);
				optionModelArrayList.get(position).setSelected(true);
				selecterNum=position;
				mAdapter.notifyDataSetChanged();
				if(mType == TYPE_COUNTRY){
					KM2Application app = KM2Application.getInstance();
					HashMap<String, String> countries = app.getCountries();
					String countryName = optionModelArrayList.get(position).getTextValue();
					String countryCode = CumulusDataUtil.findCountryCodeByName(countries, countryName);
					app.setCountryCodeUsed(countryCode);
				}
			}
		});

	}

	private void initData() {
		mInflater = LayoutInflater.from(mContext);
		Bundle bundle = getArguments();
		OptionsDialogModel mOptionsDialogModel = (OptionsDialogModel) bundle.getSerializable(OptionsDialogModel);
		optionModelArrayList = mOptionsDialogModel.getObjectsArraylist();
		mType = bundle.getInt(TYPE);
		selecterNum = mOptionsDialogModel.getSelecterNum();
		mAdapter  = new RadioAdapter();
		vLisvSize.setAdapter(mAdapter);
	}

	class RadioAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return optionModelArrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return optionModelArrayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_menu_radio, null);
				holder = new ViewHolder();
				holder.radioButton = (RadioButton) convertView.findViewById(R.id.radio_button);
				holder.textView = (TextView) convertView.findViewById(R.id.txt_radio);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(optionModelArrayList.get(position).getTextValue());
			holder.radioButton.setChecked(optionModelArrayList.get(position).isSelected());
			return convertView;
		}
	}
	private class ViewHolder {
		TextView textView;
		RadioButton radioButton;
	}

}
