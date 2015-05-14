package com.kodak.kodak_kioskconnect_n2r;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.AppManager;
import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCard;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardProvider;
import com.kodak.utils.DownloadPicture;
import com.kodak.utils.RSSLocalytics;

public class GreetingCardSelectionActivity extends BaseActivity {

	private Button btBack;
	private Button btNext;
	// private Button btSettings;
	// private Button btInfo;
	private TextView tvTitle;
	private TextView tvFindMore;

	private Gallery galleryCards;

	private WaitingDialog waitingDialog;

	private GreetingCardProvider provider;
	private List<GreetingCard> cards;
	private GreetingCardManager manager;
	private GreetingCardAdapter adapter;

	// added by Robin.Qian
	private int maxCardWidth;
	private int maxCardHeight;

	private DownloadPicture downloadPic;

	private final int spacing = 30;

	private ArrayList<String> waitingForDownload;

	private Display display;

	private static final int REQUEST_MAKING = 1;
	private static final int RESULT_ERROR = 2;
	private final String SCREEN_NAME = "GC Category";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_MAKING:
			if (resultCode == RESULT_ERROR) {
				// show the error dialog
				showNoCardsFoundDialog();
			} else if (resultCode == RESULT_OK) {
				if (data != null) {

					if (!Connection.isConnected(GreetingCardSelectionActivity.this)) {
						showNoConnectionDialog();
						return;
					}
					GreetingCard selectedGreetingCard = (GreetingCard) data.getSerializableExtra("selectedGreetingcard");
					String productIdentifier = data.getStringExtra("productIdentifier");
					waitingDialog = new WaitingDialog(GreetingCardSelectionActivity.this, R.string.animation_quickbook_wait);
					waitingDialog.setCancelable(false);
					waitingDialog.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							if (manager.getGreetingCardProduct() != null) {
								Intent intent = new Intent(GreetingCardSelectionActivity.this, GreetingCardProductActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								AppManager.getAppManager().finishActivity(GreetingCardSelectionActivity.class);
								AppManager.getAppManager().finishActivity(GreetingCardThemeSelectionActivity.class);	
							} else {
								showNorespondDialog(null);
							}

						}
					});

					CreateProductRunnable runnable = new CreateProductRunnable(selectedGreetingCard, productIdentifier);
					Thread makingProductThread = new Thread(runnable);
					makingProductThread.start();
				}
			}

			break;

		default:
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		// setContentView(R.layout.greetingcard_selection);
		setContentLayout(R.layout.greetingcard_selectionfield);
		getViews();
		initData();
		setEvents();
		new PostNotifyDataSetChanged().start();
	}

	class PostNotifyDataSetChanged extends Thread {

		@Override
		public void run() {
			try {
				sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(INIT_CARDS);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(GreetingCardSelectionActivity.this, GreetingCardThemeSelectionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class CreateProductRunnable implements Runnable {
		private GreetingCard card;
		private String productIdentifier;

		public CreateProductRunnable(GreetingCard card, String productIdentifier) {
			this.card = card;
			this.productIdentifier = productIdentifier;
		}

		@Override
		public void run() {
			handler.sendEmptyMessage(START_CREATE_PRODUCT);
			manager.createGreetingCard(card.language, card.id, productIdentifier);
			handler.sendEmptyMessage(FINISH_CREATE_PRODUCT);

		}

	}

	private final int REFRESH_VIEW = 0;
	private final int START_CREATE_PRODUCT = 1;
	private final int FINISH_CREATE_PRODUCT = 2;
	private final int INIT_CARDS = 4;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case REFRESH_VIEW:
				adapter.notifyDataSetChanged();
				break;
			case START_CREATE_PRODUCT:
				waitingDialog.show();
				break;
			case FINISH_CREATE_PRODUCT:
				waitingDialog.dismiss();
				break;
			case INIT_CARDS:
				adapter = new GreetingCardAdapter(GreetingCardSelectionActivity.this);
				galleryCards.setAdapter(adapter);
				Bundle b = getIntent().getExtras();
				if (null != b && b.getBoolean("isFromGreetingCard")) {
					galleryCards.setSelection(PrintHelper.carSelectedPosition);
				}
				break;
			}
		}

	};

	private class Holder {
		ImageView imageView;
		TextView textView;
		LinearLayout linearLayout;
	}

	private class GreetingCardAdapter extends BaseAdapter {
		LayoutInflater layoutInflater;
		private boolean hide = false;
		private Bitmap waitingBimtap = null;

		public GreetingCardAdapter(Context context) {
			if (layoutInflater == null) {
				layoutInflater = LayoutInflater.from(context);
			}
			waitingForDownload = new ArrayList<String>();
			waitingBimtap = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait_4x6);
			maxCardHeight = galleryCards.getHeight() - 40;
		}

		@Override
		public int getCount() {
			if (cards != null) {
				return cards.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				holder = new Holder();
				// TODO: use a formal layout to replace itemtest
				convertView = layoutInflater.inflate(R.layout.item_with_textview_imageview, null);
				holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.llTest);
				holder.imageView = (ImageView) convertView.findViewById(R.id.ivTest);
				holder.textView = (TextView) convertView.findViewById(R.id.tvTest);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.textView.setVisibility(View.GONE);
			Bitmap bitmap = provider.getPreview(cards.get(position).id);
			if (bitmap == null) {
				bitmap = waitingBimtap;
				if (!waitingForDownload.contains(cards.get(position).id) && waitingForDownload.size() < 5) {
					waitingForDownload.add(cards.get(position).id);
					new DownloadPreviewTask().execute(cards.get(position));
				}

			}
			Gallery.LayoutParams gParams = new Gallery.LayoutParams(android.widget.Gallery.LayoutParams.WRAP_CONTENT,
					android.widget.Gallery.LayoutParams.MATCH_PARENT);
			holder.linearLayout.setLayoutParams(gParams);

			int w = getCardWidthAfterScale(bitmap.getWidth(), bitmap.getHeight(), maxCardWidth, maxCardHeight);
			LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.MATCH_PARENT);
			lParams.bottomMargin = lParams.topMargin = 15;
			holder.imageView.setLayoutParams(lParams);

			holder.imageView.setImageBitmap(bitmap);
			holder.imageView.setScaleType(ScaleType.FIT_CENTER);
			if (hide) {
				holder.imageView.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

	}

	private void showNorespondDialog(final View v) {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		if (v != null) {
			builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					v.performClick();
				}
			});
		}

		builder.setCancelable(false);
		builder.create().show();
	}



	private void showNoCardsFoundDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.no_cards_were_found));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
			adapter = null;
			galleryCards.setAdapter(adapter);
			galleryCards = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * note this method is only applied for scale type fit_center
	 * 
	 * @param width
	 * @param height
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	private int getCardWidthAfterScale(int width, int height, int maxWidth, int maxHeight) {
		double r1 = (double) width / height;
		double r2 = (double) maxWidth / maxHeight;
		if (r1 > r2) {
			return maxWidth;
		} else {
			int w = (int) ((double) width * maxHeight / height);
			return w > maxWidth ? maxWidth : w;
		}
	}

	@SuppressLint("NewApi")
	private class DownloadPreviewTask extends AsyncTask<GreetingCard, Void, byte[]> {
		private String id;
		private String url;

		public DownloadPreviewTask() {
			downloadPic = new DownloadPicture();
		}

		@Override
		protected byte[] doInBackground(GreetingCard... params) {
			byte[] bytes = null;
			try {
				id = params[0].id;
				url = params[0].glyphURL;
				bytes = downloadPic.downloadImageFromURL(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (provider != null) {
				waitingForDownload.remove(id);
				provider.cachPreview(id, bytes);
			}
			return bytes;
		}

		@Override
		protected void onPostExecute(byte[] result) {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
			// if(cards.get(selectedPosition).id.equals(id) &&
			// animationIsShowing && inAnimView!=null && tempBitmap!=null){
			// tempBitmap = provider.getPreview(id);
			// ((ImageView)inAnimView).setImageBitmap(tempBitmap);
			// }
		}

	}

	@Override
	public void getViews() {
		btBack = (Button) findViewById(R.id.back_btn);
		btNext = (Button) findViewById(R.id.next_btn);
		tvTitle = (TextView) findViewById(R.id.headerBar_tex);
		tvFindMore = (TextView) findViewById(R.id.versionCopyright_tex);
		galleryCards = (Gallery) findViewById(R.id.gallery_cards);
	}

	@Override
	public void initData() {
		display = getWindowManager().getDefaultDisplay();
		maxCardWidth = display.getWidth() / 3;// robin
		final float scale = getBaseContext().getResources().getDisplayMetrics().density;
		RelativeLayout.LayoutParams preLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		preLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		preLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		tvFindMore.setLayoutParams(preLayoutParams);
		tvFindMore.setTextColor(Color.rgb(0, 174, 239));
		galleryCards.setSpacing((int) (spacing * scale + 0.5f));

		tvTitle.setText(getString(R.string.product_cards));
		tvTitle.setTypeface(PrintHelper.tf);
		tvFindMore.setText(getString(R.string.greetingcard_selection_findmorecards));
		tvFindMore.setTypeface(PrintHelper.tf);
		btNext.setVisibility(View.INVISIBLE);
		tvFindMore.setVisibility(View.VISIBLE);
		btBack.setVisibility(View.VISIBLE);
		provider = GreetingCardProvider.getGreetingCardProvoider(this);
		manager = GreetingCardManager.getGreetingCardManager(this);
		cards = manager.getGreetingCardCategory();
	}

	@Override
	public void setEvents() {

		btBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GreetingCardSelectionActivity.this, GreetingCardThemeSelectionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		tvFindMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GreetingCardSelectionActivity.this, GreetingCardThemeSelectionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		galleryCards.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> viewGroup, View view, int position, long id) {

				Intent intent = new Intent(GreetingCardSelectionActivity.this, GreetingCardMakingActivity.class);
				GreetingCard greetingCard = cards.get(position);
				intent.putExtra("selectedcard", greetingCard);

				startActivityForResult(intent, REQUEST_MAKING);

				PrintHelper.carSelectedPosition = position;
			}
		});

	}
}
