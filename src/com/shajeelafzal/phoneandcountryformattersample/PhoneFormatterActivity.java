package com.shajeelafzal.phoneandcountryformattersample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class PhoneFormatterActivity extends ActionBarActivity {

	protected EditText mCountryET;
	protected EditText mNumberTextEdit;
	protected String text = "";

	private ArrayList<String> countriesArray = new ArrayList<String>();
	private HashMap<String, String> countriesMap = new HashMap<String, String>();
	private HashMap<String, String> codesMap = new HashMap<String, String>();
	private HashMap<String, String> languageMap = new HashMap<String, String>();
	private int countryState = 0;
	protected int igoreOnTextChange = 0;

	// countryState = 1 -> Choose Country
	// countryState = 0 -> Country Selected
	// countryState = 2 -> Wrong Country

	// protected void onCreate(Bundle savedInstanceState) {
	// mMPMetrics = MPMetrics.getInstance(this,
	// "eda5bad7ed0c151ea01ac28940c6142c");
	// mMPMetrics.identify("some user's distinct_id to enable push for"); //
	// user
	// // identified
	// // above
	// // will
	// // receive
	// // Mixpanel
	// // GCM
	// // notification
	// mMPMetrics.getPeople().initPushHandling("direct-beacon-712");
	// }

	public static void setInputFilter(EditText editText, String value) {
		setInputFilter(editText, value, -1);
	}

	public static void setInputFilter(EditText editText, String value,
			int toastIdZeroLength) {
		editText.setText(value);
		editText.setSelection(editText.length());
		editText.setFilters(new InputFilter[] { new PhoneNumberFilter(editText
				.getContext(), value, toastIdZeroLength) });

	}

	public static class PhoneNumberFilter implements InputFilter {
		private final String mValue;
		private final int mLength;
		private Context mContext;
		private int mToastIdZeroLength;

		public PhoneNumberFilter(Context context, String value,
				int toastIdZeroLength) {
			mContext = context;
			mValue = value;
			mLength = mValue.length();
			mToastIdZeroLength = toastIdZeroLength;
		}

		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			// Binh: format the text [start]
			if (source.length() > 1 && !source.toString().startsWith(mValue)) {
				source = mValue + source;
			}
			// Binh: format the text [end]
			if (mLength == 0 && mToastIdZeroLength > 0) {
				return "";
			}
			// delete existing or ?add to empty field?
			if (dstart == 0 && dend == 0) {
				// try to set empty string
				if (start == 0 && end == 0)
					return mValue;
				return source.toString().startsWith(mValue) ? null : "";
			}
			if (dstart < mLength)
				return dest.subSequence(dstart, dend);
			return null;
		}
	}

	protected void enablePhoneNumberFormatting() {
		setInputFilter(mNumberTextEdit, "+");
		mNumberTextEdit.addTextChangedListener(phoneTextWatcher2);

		if (mCountryET != null) {

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(getResources().getAssets().open(
								"countries.txt")));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] args = line.split(";");
					countriesArray.add(0, args[2]);
					countriesMap.put(args[2], args[0]);
					codesMap.put(args[0], args[2]);
					languageMap.put(args[1], args[2]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Collections.sort(countriesArray, new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					return lhs.compareTo(rhs);
				}
			});

			String country = null;

			try {
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (telephonyManager != null) {
					country = telephonyManager.getSimCountryIso().toUpperCase();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (country != null) {
				String countryName = languageMap.get(country);
				if (countryName != null) {
					int index = countriesArray.indexOf(countryName);
					if (index != -1) {
						mNumberTextEdit.setText("+"
								+ countriesMap.get(countryName) + " ");
						countryState = 0;
					}

					mCountryET.setText(countriesArray.get(index));
				}

			}

			if (mCountryET.length() == 0) {
				mCountryET.setText("Select Country");
				countryState = 1;
			}
		}
	}

	private TextWatcher phoneTextWatcher2 = new TextWatcher() {

		private String mBeforeTextChanged;
		private int mBeforeCursorPosition;

		@Override
		public void onTextChanged(CharSequence s2, int start, int before,
				int count) {
			Log.e("cursor",
					"ONTEXTCHANGED Start:" + start + ", Before:" + before
							+ ", Count:" + count + ", onTextChanged: "
							+ s2.toString());
			if (igoreOnTextChange == 0) {

				String s = s2.toString();

				int length = s.length();

				if (s.length() > 3) {
					String temp = s.substring(1, s.length());
					if (temp.contains("+")) {
						s = temp.substring(temp.indexOf("+"), temp.length());
					}

					if (s.startsWith("+011")) {
						s = "+" + s.substring(4, s.length());
					}

					if (s.startsWith("+00")) {
						s = "+" + s.substring(3, s.length());
					}
				}

				String validChars = "()";
				if (!s.toString().contains(validChars)) {
					if (!s.toString().contains(")")
							&& s.toString().contains("(") && length > 1) {

						String text2 = PhoneFormat.stripExceptNumbers(
								s.toString(), true);

						String subString = removeLastChar(text2);
						Log.w("shajeel", "subString: " + subString
								+ ", onTextChanged: " + s.toString());

						text = PhoneFormat.getInstance().format(subString);

						Log.i("shajeel", text);
					} else {
						text = PhoneFormat.getInstance().format(
								s.toString().trim());

						Log.i("shajeel", text);
					}
				}
			}

			if (mCountryET != null) {
				String countryCode;
				if (text.contains(" ")) {
					// Get the country code and ignore + on the start
					countryCode = text.substring(1, text.indexOf(" "));
					if (countryCode.length() == 0) {
						mCountryET.setText("Select Country");
						countryState = 1;
					} else {
						String country = codesMap.get(countryCode);
						if (country != null) {
							int index = countriesArray.indexOf(country);
							if (index != -1) {
								mCountryET.setText(countriesArray.get(index));
								countryState = 0;
							} else {
								mCountryET.setText("Select Country");
								countryState = 2;
							}
						} else {
							mCountryET.setText("Select Country");
							countryState = 2;
						}
					}
				} else {
					String country;
					if (text.startsWith("+")) {
						String t = text.substring(1, text.length());
						country = codesMap.get(t);
					} else {
						country = codesMap.get(text);
					}

					if (country != null) {
						int index = countriesArray.indexOf(country);
						if (index != -1) {
							mCountryET.setText(countriesArray.get(index));
							countryState = 0;
						} else {
							mCountryET.setText("Select Country");
							countryState = 2;
						}
					} else {
						mCountryET.setText("Select Country");
						countryState = 2;
					}
				}

			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			Log.e("cursor",
					"\n\nBEFORETEXTCHANGED Start:" + start + ", After:" + after
							+ ", Count:" + count + ", BeforeTextChanged: "
							+ s.toString());

			mBeforeTextChanged = s.toString();
			mBeforeCursorPosition = start;

			if (count == 1 && after == 0 && s.length() > 1) {
				String phoneChars = "0123456789";
				String str = s.toString();
				String substr = str.substring(start, start + 1);
				if (!phoneChars.contains(substr)) {
					mNumberTextEdit
							.removeTextChangedListener(phoneTextWatcher2);
					StringBuilder builder = new StringBuilder(str);
					int toDelete = 0;
					for (int a = start; a >= 0; a--) {
						substr = str.substring(a, a + 1);
						if (phoneChars.contains(substr)) {
							break;
						}
						toDelete++;
					}

					mBeforeCursorPosition = mBeforeCursorPosition - toDelete;

					builder.delete(Math.max(0, start - toDelete), start + 1);
					str = builder.toString();
					if (PhoneFormat.strip(str).length() == 0) {
						text = "";
					} else {
						text = PhoneFormat.getInstance().format(str);
					}
					igoreOnTextChange = 1;
					mNumberTextEdit.addTextChangedListener(phoneTextWatcher2);
				} else {
					igoreOnTextChange = 0;
				}
			} else {
				igoreOnTextChange = 0;
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			Log.e("cursor", "AFTER TEXT CHANGED S: " + s.toString() + " text: "
					+ text);

			mNumberTextEdit.removeTextChangedListener(phoneTextWatcher2);
			mNumberTextEdit.setText(text);

			if ((mBeforeTextChanged.length() - text.length()) == 1) {
				// A digit has been delete from the String.
				mNumberTextEdit.setSelection(mBeforeCursorPosition);
			} else if (mBeforeTextChanged.length() - text.length() == -1) {
				// A digit has been added to the String.
				mNumberTextEdit.setSelection(mBeforeCursorPosition + 1);
			} else {
				mNumberTextEdit.setSelection(text.length());
			}

			// TODO do not change cursor position if delete is pressed
			mNumberTextEdit.addTextChangedListener(phoneTextWatcher2);
		}
	};

	public String removeLastChar(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return s.substring(0, s.length() - 1);
	}

}
