package org.codepath.assignment1.tipcalculator;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TipCalculator extends Activity {
	private static final String LOG_TAG = TipCalculator.class.getSimpleName();
	private static final int DEFAULT_NUM_PEOPLE = 1;
	
	private EditText etBaseAmount;
	private RadioGroup rgTipPercent;
	private TextView tvTipAmount;
	private TextView tvBaseAmountHelper;
	private EditText etCustomTipPercent;
	private EditText etNumPeople;
	private TextView tvTipPerPerson;

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
		etCustomTipPercent = (EditText) findViewById(R.id.etCustomPercent);
		etCustomTipPercent.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Log.d(LOG_TAG, "onEditorAction. Custom Percent");
				hideSoftKeyboard(etCustomTipPercent);
				int selectedTipPercentId = rgTipPercent.getCheckedRadioButtonId();
				RadioButton selectedRb = (RadioButton) findViewById(selectedTipPercentId);
				if (selectedRb != null && "other".equals(selectedRb.getText())) {
					String tipPercentString = etCustomTipPercent.getText().toString();
					try {
					    double tipPercent = Double.parseDouble(tipPercentString)/100;
					    if (tipPercent <= 0) {
			            	displayBillAmountError("Custom tip percent cannot be negative");
			            	return true;
			            }
					    computeAndDisplayTip(tipPercent);
					} catch (NumberFormatException nfe) {
						displayBillAmountError("Custom Percent Entered is not a number");
					}
				} 
				return true;
			}
		});
		etBaseAmount = (EditText) findViewById(R.id.etBillAmount);
		etBaseAmount.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				hideSoftKeyboard(etBaseAmount);
				Log.d(LOG_TAG, "onEditorAction. actionId:" + actionId + " , IME_ACTION_DONE=" + EditorInfo.IME_ACTION_DONE);
				try {
					double billAmount = Double.parseDouble(etBaseAmount.getText().toString());
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
		etNumPeople.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				hideSoftKeyboard(etNumPeople);
				getNumPeopleSelected();
				getTipPercentAndDisplayTip();
				return true;
			}
	    });
		tvBaseAmountHelper = (TextView) findViewById(R.id.tvBaseAmountHelper);
		tvTipAmount = (TextView) findViewById(R.id.tvTipAmount);
		tvTipPerPerson = (TextView) findViewById(R.id.tvTipAmountPerPerson);
	}
	
    public void getTipPercentAndDisplayTip() {
    	int selectedTipPercentId = rgTipPercent.getCheckedRadioButtonId();
		RadioButton selectedRb = (RadioButton) findViewById(selectedTipPercentId);
		double tipPercent = 0;
		if (selectedRb != null) {
			String rbText = selectedRb.getText().toString();
			String tipPercentString = "0";
			if ("other".equals(rbText)) {
				tipPercentString = etCustomTipPercent.getText().toString();
				if (tipPercentString != null && !tipPercentString.isEmpty()) {
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
				if ("10%".equals(rbText)) {
				    tipPercentString = "10";
				} else if ("20%".equals(rbText)) {
				    tipPercentString = "20";
				} else if ("15%".equals(rbText)) {
				    tipPercentString = "15";
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
		    double billAmount = Double.parseDouble(etBaseAmount.getText().toString());
		    if (billAmount < 0) {
		        displayBillAmountError("Bill Amount cannot be less than zero");	
		        return;
		    } else {
		    	tvBaseAmountHelper.setText("");
		    }
		    double tipAmount = (double)Math.round((billAmount * tipPercent) * 100) / 100;
		    tvTipAmount.setText("$" + String.valueOf(tipAmount));
		    int numPeople = getNumPeopleSelected();
		    double tipPerPerson = (double)Math.round((tipAmount/numPeople) *100) / 100;
		    tvTipPerPerson.setText(String.valueOf(tipPerPerson));
		} catch (NumberFormatException nfe) {
			Log.d(LOG_TAG, "Bill Amount Invalid");
			displayBillAmountError("Bill Amount must be a number");
		}	
	}
	
	public void displayBillAmountError(String errorString) {
		Log.e(LOG_TAG, errorString);
	    tvBaseAmountHelper.setText(errorString);	
	    tvBaseAmountHelper.setTextColor(Color.RED);
	}
	
	public int getNumPeopleSelected() {
		String numPeopleString = etNumPeople.getText().toString();
	    int numPeople = DEFAULT_NUM_PEOPLE;
	    if (numPeopleString != null && !numPeopleString.isEmpty()) {
	    	try {
	            numPeople = Integer.parseInt(etNumPeople.getText().toString());
	            if (numPeople <= 0) {
	            	displayBillAmountError("Number of People cannot be less than 1");
	            }
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
}
