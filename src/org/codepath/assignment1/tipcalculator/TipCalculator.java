package org.codepath.assignment1.tipcalculator;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TipCalculator extends Activity {
	private static final String LOG_TAG = TipCalculator.class.getSimpleName();
	private EditText etBaseAmount;
	private Button bTenPercent;
	private Button bFifteenPercent;
	private Button bTwentyPercent;
	private TextView tvTipAmount;
	private TextView tvBaseAmountHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tip_calculator);
		etBaseAmount = (EditText) findViewById(R.id.etBillAmount);
//		etBaseAmount.setOnEditorActionListener(new OnEditorActionListener() {
//			
//			@Override
//			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//				
//			}
//		})
		tvBaseAmountHelper = (TextView) findViewById(R.id.tvBaseAmountHelper);
		bTenPercent = (Button) findViewById(R.id.bTenPercent);
		bTenPercent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				computeAndDisplayTip(0.1);
			}
		});
		bFifteenPercent = (Button) findViewById(R.id.bFifteenPercent);
		bFifteenPercent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				computeAndDisplayTip(0.15);
			}
		});
		bTwentyPercent = (Button) findViewById(R.id.bTwentyPercent);
		bTwentyPercent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				computeAndDisplayTip(0.2);
			}
		});
		tvTipAmount = (TextView) findViewById(R.id.tvTipAmount);
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
		} catch (NumberFormatException nfe) {
			Log.d(LOG_TAG, "Bill Amount Invalid");
			displayBillAmountError("Bill Amount must be a number");
		}	
	}
	
	public void displayBillAmountError(String errorString) {
	    tvBaseAmountHelper.setText(errorString);	
	    tvBaseAmountHelper.setTextColor(Color.RED);
	}
}
