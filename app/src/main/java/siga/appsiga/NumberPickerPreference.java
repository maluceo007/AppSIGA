package siga.appsiga;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Sam on 29/06/2017.
 */

public class NumberPickerPreference extends DialogPreference{
    public static final int DEFAULT_MAX_VALUE = 100;
    public static final int DEFAULT_MIN_VALUE = 0;
    public static final boolean DEFAULT_WRAP_SELECTOR_WHEEL = true;

    private final int minValue;
    private final int maxValue;
    private final boolean wrapSelectorWheel;

    private NumberPicker picker;
    private int value;
    private int limit; //value from xml file
    private String pickerId; // id of picker, either max or min picker
    private int temperature;
    private String currentTemp;
    private SharedPreferences sharedPreferences;
    private int defaultValue;
    //private MainActivity mainActivity = new MainActivity();
    private SwitchPreference tempAlarm;
    private Context context;
    //private Context applicationContext;
    //PreferenceHelper preferenceHelper = null;
    private TextView textDspMax, textDspMin;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);

        //call MainActivity context
        //applicationContext = MainActivity.getContextOfApplication();

    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE);
        maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE);
        limit = a.getInteger(R.styleable.NumberPickerPreference_limit, DEFAULT_MAX_VALUE);
        pickerId = a.getString(R.styleable.NumberPickerPreference_pickedId);

        currentTemp = LoadVariable("currentTemp");
        // work around if currentTemp is null
        if (currentTemp == null){
            currentTemp = String.valueOf(0);
        }
        temperature = (int) Math.round(Float.valueOf(currentTemp));

        wrapSelectorWheel = a.getBoolean(R.styleable.NumberPickerPreference_wrapSelectorWheel, DEFAULT_WRAP_SELECTOR_WHEEL);
        a.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        //String tCurrent = getSharedPreferences().getString(currentTemp, DEFAULT_MAX_VALUE);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        //if picker is mininum value alarm
        if (pickerId.equals("min")) {
            picker.setMinValue(limit);
            picker.setMaxValue(temperature-1);
            //setValue(defaultValue = temperature -2);

        }
        //if picker is maximum value alarm
        if (pickerId.equals("max")) {
            picker.setMinValue(temperature +1 );
            picker.setMaxValue(limit);
            //setValue(defaultValue = temperature + 2);

        }
        picker.setWrapSelectorWheel(wrapSelectorWheel);
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);

                if ( pickerId.equals("min")){
                    //TextView txtView = (TextView) ((Activity)context).findViewById(R.id.textDspMin);
                    //txtView.setText(newValue);
                    //mainActivity.setReference(Integer.toString(newValue), "minNewValue");
                    //Toast.makeText(getContext(), "Picker id: " + pickerId + " onDialogClosed : "  + newValue , Toast.LENGTH_SHORT).show();
                }
                if ( pickerId.equals("max")){
                    //mainActivity.setReference(Integer.toString(newValue), "maxNewValue");
                    //Toast.makeText(getContext(), "Picker id: " + pickerId + " onDialogClosed : "  + newValue , Toast.LENGTH_SHORT).show();
                    //this.preferenceHelper.upDateAlarm(textDspMax, newValue);
                }


            }
        }
    }
/*
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Toast.makeText(getContext(), "onGetDefaultValue: " + a.getInt(index,minValue), Toast.LENGTH_SHORT).show();
        return a.getInt(index, minValue);
    }
*/
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
       //Toast.makeText(getContext(), "onSetInitialValue: 1 " + restorePersistedValue + " 2:" + getPersistedInt(minValue)  + " 3: " + (Integer) defaultValue, Toast.LENGTH_SHORT).show();
        setValue(restorePersistedValue ? getPersistedInt(minValue) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        //Toast.makeText(getContext(), "setValue: " + value , Toast.LENGTH_SHORT).show();
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        Toast.makeText(getContext(), "getValue: " + this.value , Toast.LENGTH_SHORT).show();
        return this.value;
    }

    //function to get temperature from sharedPreferences
    public String LoadVariable(String key){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(key, "string not found" );

    }

    // save value to PreferenceManager
    public void setVariables(String key, Boolean value){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    // use interface to update alarm textview in main activity


}
