package org.codepath.assignment1.tipcalculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class TipCalculator extends Activity {
	private static final String LOG_TAG = TipCalculator.class.getSimpleName();
	private static final int DEFAULT_NUM_PEOPLE = 1;
	private static final String AC_CUSTOM_TIP_PERCENT_FILE = "acCustomTipPercent";
	private static final long REPEAT_DELAY = 50;
	private static final String OTHER_STRING = "Other";
	private static final String TEN_PERCENT_STRING = "10%";
	private static final String FIFTEEN_PERCENT_STRING = "15%";
	private static final String TEN_STRING = "10";
	private static final String FIFTEEN_STRING = "15";
	
	private EditText mEtBaseAmount;
	private RadioGroup rgTipPercent;
	private TextView tvTotalAmount;
	private TextView tvTipAmount;
	private AutoCompleteTextView acCustomTipPercent;
	private EditText etNumPeople;
	private TextView tvTipPerPerson;
	private List<String> acCustomTipPercentItems;
	private Set<String> acCustomTipPercentSet;
	private ArrayAdapter<String> acCustomTipPercentAdapter;
	private Button bNumPersonsMore;
	private Button bNumPersonsLess;
	
	private int mNumPeople = DEFAULT_NUM_PEOPLE;
    private Handler mRepeatUpdateHandler = new Handler();
	
	private boolean mAutoIncrement = false;
	private boolean mAutoDecrement = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tip_calculator);
		rgTipPercent = (RadioGroup) (findViewById(R.id.rbTipPercent));
		rgTipPercent.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() 
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            getTipPercentAndDisplayTip();
	        }
	    });
		
		acCustomTipPercent = (AutoCompleteTextView) findViewById(R.id.autoCustomPercent);
		acCustomTipPercent.setTextColor(Color.BLACK);
		acCustomTipPercentItems = new ArrayList<String>();
		acCustomTipPercentSet = new HashSet<String>();
		loadACItemsFromFile();
		acCustomTipPercentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, acCustomTipPercentItems);
		acCustomTipPercent.setAdapter(acCustomTipPercentAdapter);
		acCustomTipPercent.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
				    RadioButton customPercentRb = (RadioButton) findViewById(R.id.rbCustomPercent);
				    customPercentRb.setChecked(true);
				}
			}
		});
		acCustomTipPercent.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Log.d(LOG_TAG, "onEditorAction. Custom Percent");
				hideSoftKeyboard(acCustomTipPercent);
				getTipPercentAndDisplayTip();
				return true;
			}
		});
		
		mEtBaseAmount = (EditText) findViewById(R.id.etBillAmount);
		mEtBaseAmount.setTextColor(Color.BLACK);
		mEtBaseAmount.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				hideSoftKeyboard(mEtBaseAmount);
				Log.d(LOG_TAG, "onEditorAction. actionId:" + actionId + " , IME_ACTION_DONE=" + EditorInfo.IME_ACTION_DONE);
				try {
					double billAmount = Double.parseDouble(mEtBaseAmount.getText().toString());
					if (billAmount < 0) {
						displayBillAmountError("Bill Amount cannot be less than zero");	
						return true;
					} 
				} catch (NumberFormatException nfe) {
					Log.d(LOG_TAG, "Bill Amount Invalid");
					displayBillAmountError("Bill Amount must be a number");
				}
				getTipPercentAndDisplayTip();
				return true;
			}
		});
		
		etNumPeople = (EditText) findViewById(R.id.etNumPeople);
		etNumPeople.setTextColor(Color.BLACK);
		etNumPeople.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				hideSoftKeyboard(etNumPeople);
				int numPeople = getNumPeopleSelected();
				if (numPeople <= 0) {
					displayBillAmountError("Number of people cannot be zero");
					etNumPeople.setText(String.valueOf(mNumPeople));
				} else {
					mNumPeople = numPeople;
					getTipPercentAndDisplayTip();	
				}
				return true;
			}
	    });
		
		tvTipAmount = (TextView) findViewById(R.id.tvTipAmount);
		
		tvTipPerPerson = (TextView) findViewById(R.id.tvTipAmountPerPerson);
		
		tvTotalAmount = (TextView) findViewById(R.id.tvTotalBillAmount);
		
		bNumPersonsLess = (Button) findViewById(R.id.buttonMinus);
		bNumPersonsLess.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				decrementNumPersons();
			}
		});
		
		// Auto Decrement for a long click
		bNumPersonsLess.setOnLongClickListener( 
				new View.OnLongClickListener(){
					public boolean onLongClick(View arg0) {
						mAutoDecrement = true;
						mRepeatUpdateHandler.post( new RepetetiveUpdater() );
						return false;
					}
	    });

		// When the button is released, if we're auto decrementing, stop
		bNumPersonsLess.setOnTouchListener( new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_UP && mAutoDecrement ){
					mAutoDecrement = false;
				}
				return false;
			}
		});

		bNumPersonsMore = (Button) findViewById(R.id.buttonPlus);
		bNumPersonsMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				incrementNumPersons();
			}
		});
		// Auto increment for a long click
		bNumPersonsMore.setOnLongClickListener( 
						new View.OnLongClickListener(){
							public boolean onLongClick(View arg0) {
								mAutoIncrement = true;
								mRepeatUpdateHandler.post( new RepetetiveUpdater() );
								return false;
							}
						}
				);
				
	    // When the button is released, if we're auto incrementing, stop
		bNumPersonsMore.setOnTouchListener( new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if( event.getAction() == MotionEvent.ACTION_UP && mAutoIncrement ){
							mAutoIncrement = false;
						}
						return false;
					}
	    });
	}
	
    public void getTipPercentAndDisplayTip() {
    	int selectedTipPercentId = rgTipPercent.getCheckedRadioButtonId();
		RadioButton selectedRb = (RadioButton) findViewById(selectedTipPercentId);
		double tipPercent = 0;
		if (selectedRb != null) {
			String rbText = selectedRb.getText().toString();
			String tipPercentString = "0";
			if (OTHER_STRING.equals(rbText)) {
				tipPercentString = acCustomTipPercent.getText().toString();
				if (tipPercentString != null && !tipPercentString.isEmpty()) {
					if (!acCustomTipPercentSet.contains(tipPercentString)) {
						acCustomTipPercentItems.add(tipPercentString);
						acCustomTipPercentSet.add(tipPercentString);
						acCustomTipPercentAdapter.notifyDataSetChanged();
					}
					try {
						tipPercent = Double.parseDouble(tipPercentString)/100;
						if (tipPercent <= 0) {
			            	displayBillAmountError("Custom tip percent cannot be negative");
			            	return;
			            }
						Log.d(LOG_TAG, "Tip Percent:" + tipPercent);
					} catch (NumberFormatException nfe) {
						Log.e(LOG_TAG, "TipPercent is not a number");
						displayBillAmountError("Tip Percent is not a valid number");
					}
					computeAndDisplayTip(tipPercent);	
				}
			} else {
				if (TEN_PERCENT_STRING.equals(rbText)) {
				    tipPercentString = TEN_STRING;
				} else if (FIFTEEN_PERCENT_STRING.equals(rbText)) {
				    tipPercentString = FIFTEEN_STRING;
				} else {
				    // not valid	
				}
				try {
					tipPercent = Double.parseDouble(tipPercentString)/100;
					Log.d(LOG_TAG, "Tip Percent:" + tipPercent);
				} catch (NumberFormatException nfe) {
					Log.e(LOG_TAG, "TipPercent is not a number");
					displayBillAmountError("Tip Percent is not a valid number");
				}
				computeAndDisplayTip(tipPercent);
			}
		}
    }
	
	public void computeAndDisplayTip(double tipPercent) {
		try {
		    double billAmount = Math.round(Double.parseDouble(mEtBaseAmount.getText().toString()) * 100)/100;
		    if (billAmount < 0) {
		        displayBillAmountError("Bill Amount cannot be less than zero");	
		        return;
		    } 
		    double tipAmount = (double)Math.round((billAmount * tipPercent) * 100) / 100;
		    tvTipAmount.setText("$ " + String.valueOf(tipAmount));
		    int numPeople = getNumPeopleSelected();
		    double totalAmount = billAmount + tipAmount;
		    double billPerPerson = Math.round((totalAmount/numPeople) * 100)/100;
		    tvTipPerPerson.setText("$ " + String.valueOf(billPerPerson));
		    tvTotalAmount.setText("$ " + String.valueOf(totalAmount));
		} catch (NumberFormatException nfe) {
			Log.d(LOG_TAG, "Bill Amount Invalid");
			displayBillAmountError("Bill Amount must be a number");
		}	
	}
	
	public void displayBillAmountError(String errorString) {
		Log.e(LOG_TAG, errorString);
		Toast toast = Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
	
	public int getNumPeopleSelected() {
		String numPeopleString = etNumPeople.getText().toString();
	    int numPeople = DEFAULT_NUM_PEOPLE;
	    if (numPeopleString != null && !numPeopleString.isEmpty()) {
	    	try {
	            numPeople = Integer.parseInt(etNumPeople.getText().toString());
	    	} catch(NumberFormatException nfe) {
	    		Log.e(LOG_TAG, "Invalid number of people");
	    		displayBillAmountError("Number of people entered should be a number");
	    	}
	    }
	    return numPeople;
	}
	
	public void hideSoftKeyboard(View view){
		  InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		  imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
	
	public void saveACItemsToFile() {
		if (acCustomTipPercentSet != null && !acCustomTipPercentSet.isEmpty()) {
			BufferedWriter writer = null;
		    try {
				writer = new BufferedWriter(new FileWriter(new File(getFilesDir(), AC_CUSTOM_TIP_PERCENT_FILE)));
				Log.d(LOG_TAG, "Saving tip percent to file");
				for (String item : acCustomTipPercentSet) {
					Log.d(LOG_TAG, "Item:" + item);
					writer.write(item);
					writer.newLine();
				}
			} catch (FileNotFoundException e) {
				Log.e(LOG_TAG, "File does not exist:" + AC_CUSTOM_TIP_PERCENT_FILE + " in parent path " + getFilesDir());
				return;
			} catch (IOException e) {
				Log.e(LOG_TAG, "IOException while writing to file", e);
				return;
			} finally {
			    if (writer != null) {
			    	try {
			    		writer.close();
					} catch (IOException e) {
						Log.e(LOG_TAG, "IOException while closing file", e);
					}
			    }
			}    	
		}
	}
	
    public void loadACItemsFromFile() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(getFilesDir(), AC_CUSTOM_TIP_PERCENT_FILE)));
			String item;
			Log.d(LOG_TAG, "Reading tip percent from file");
			while ((item = reader.readLine()) != null) {
			    if (!item.isEmpty()) {
			    	Log.d(LOG_TAG, "Item:" + item);
			    	acCustomTipPercentSet.add(item);
			    }
			}
			acCustomTipPercentItems = new ArrayList<String>(acCustomTipPercentSet);
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, "File does not exist:" + AC_CUSTOM_TIP_PERCENT_FILE + " in parent path " + getFilesDir());
			return;
		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException while reading file", e);
			return;
		} finally {
		    if (reader != null) {
		    	try {
					reader.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, "IOException while closing file", e);
				}
		    }
		}
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	saveACItemsToFile();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	loadACItemsFromFile();
    	acCustomTipPercentAdapter.notifyDataSetChanged();
    }
    
    public void incrementNumPersons(){
    	mNumPeople = mNumPeople + 1;
    	etNumPeople.setText( String.valueOf(mNumPeople));
    	getTipPercentAndDisplayTip();
    	
    }

	public void decrementNumPersons(){
		if (mNumPeople > 1) {
    	    mNumPeople = mNumPeople - 1;
    	    etNumPeople.setText( String.valueOf(mNumPeople));
    	    getTipPercentAndDisplayTip();
		}
	}
	
	class RepetetiveUpdater implements Runnable {
		public void run() {
			if(mAutoIncrement ){
				incrementNumPersons();
				mRepeatUpdateHandler.postDelayed(new RepetetiveUpdater(), REPEAT_DELAY);
			} else if(mAutoDecrement ){
				decrementNumPersons();
				mRepeatUpdateHandler.postDelayed(new RepetetiveUpdater(), REPEAT_DELAY);
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e(LOG_TAG, "Configuration Changed");
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	TableLayout result = (TableLayout) findViewById(R.id.tlTipResult);
	    	result.setVisibility(ViewGroup.INVISIBLE);
	    }
	}
}
