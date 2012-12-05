package se.tdp025.Rangi.settings;

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import se.tdp025.Rangi.R;


public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private static final String androidns="http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mSplashText,mValueText;
    private Context mContext;

    private String mDialogMessage, mSuffix;
    private int mDefault, mMax, mValue = 0;
    private int mLastValue;
    private int mNewValue;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mDialogMessage = attrs.getAttributeValue(androidns,"dialogMessage");
        mSuffix = attrs.getAttributeValue(androidns,"text");
        mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 4);
        mMax = attrs.getAttributeIntValue(androidns,"max", 10);

    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {

        Log.v("RANGI_SeekBarPreference", "Bool: " + positiveResult);
        Log.v("RANGI_SeekBarPreference", "mLastValue: " + mLastValue);
        Log.v("RANGI_SeekBarPreference", "mValue: " + mNewValue);
        if (positiveResult) {
            callChangeListener(new Integer(mNewValue));
            //String text=MyEditText.getText();
        }
        else {
            callChangeListener(new Integer(mLastValue));
            // cancel hit
        }
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
            mValue = shouldPersist() ? getPersistedInt(mDefault) : 1;
        else
            mValue = (Integer)defaultValue;
        mLastValue = mValue;
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
    {
        if(value == 0)
            value = 1;
        String t = String.valueOf(value);
        mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
        if (shouldPersist())
            persistInt(value);
        mNewValue = value;
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
    public int getProgress() { return mValue; }
}