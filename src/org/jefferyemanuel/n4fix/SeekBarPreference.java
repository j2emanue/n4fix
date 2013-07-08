package org.jefferyemanuel.n4fix;

/* The following code is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * I have modified the seekbar preference and added graphics and get set methods.
 * 
 */




import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;


public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private static final String androidns="http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mSplashText,mValueText;
    private Context mContext;

    private String mDialogMessage, mSuffix;
    private int mDefault, mMax, mValue = 0;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
        mContext = context;

        mDialogMessage = attrs.getAttributeValue(androidns,"dialogMessage");
        mSuffix = attrs.getAttributeValue(androidns,"text");
        mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", Integer.parseInt(context
				.getString(R.string.default_force)));
        mMax = attrs.getAttributeIntValue(androidns,"max", 100);

    }
    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6,6,6,6);

        mSplashText = new TextView(mContext);
        if (mDialogMessage != null)
            mSplashText.setText(mDialogMessage);
        layout.addView(mSplashText);

        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(32);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));



        if (shouldPersist())
            mValue = getPersistedInt(mDefault);

        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);


        if(this.getTitle()!=null &&
                this.getTitle().toString().equalsIgnoreCase(mContext.getString(R.string.confidence_dialog_title))){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View view = inflater.inflate(R.xml.force_dialog_layout, null, false);
            layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            layout.addView(view);
        }


        return layout;
    }
    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);



    }
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        super.onSetInitialValue(restore, defaultValue);
        if (restore)
            mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
        else
            mValue = (Integer)defaultValue;

        this.setDialogIcon(getContext().getResources().getDrawable( R.drawable.ic_launcher));

    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
    {
        String t = String.valueOf(value);
        mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
        if (shouldPersist())
            persistInt(value);
        callChangeListener(new Integer(value));
    }
    public void onStartTrackingTouch(SeekBar seek) {}
    public void onStopTrackingTouch(SeekBar seek) {}

    public void setMax(int max) { mMax = max; }
    public int getMax() { return mMax; }

    public void setProgress(int progress) {
        mValue = progress;
        if (mSeekBar != null)
            mSeekBar.setProgress(progress);
    }
    public int getProgress() { return mValue;

    }


    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);

    }


    
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {

       
    	

        if(positiveResult){
        	this.setProgress(mSeekBar.getProgress());

            callChangeListener(mSeekBar.getProgress());

        }
        
        else//revert the update since user cancelled
        	mSeekBar.setProgress(this.getProgress());
        
        super.onDialogClosed(positiveResult);
    }



}