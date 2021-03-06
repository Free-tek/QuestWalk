<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorAccent">

    <TextView
        android:id="@+id/title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="QUESTION OF THE DAY"
        android:textSize="18sp"
        android:textColor="@color/colorPrimaryDark"
        android:layout_marginTop="30dp"
        android:layout_marginLeft="20dp"
        />

    <TextView
        android:id="@+id/question"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text=""
        android:textColor="@color/colorPrimaryDark"
        android:textSize="14sp"
        android:layout_below="@+id/title"
        android:layout_marginTop="25dp"
        android:layout_marginLeft="20dp"/>

    <RadioGroup
        android:id="@+id/radioGroup"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginLeft="20dp"
        android:layout_marginRight="10dp"
        android:layout_marginTop="120dp">

        <RadioButton
            android:textSize="14sp"
            android:id="@+id/optionA"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="false"
            android:text=""
            android:textColor="@color/colorPrimaryDark"
            android:backgroundTint="@color/colorPrimaryDark"
            android:buttonTint="@color/colorPrimaryDark"
            />

        <RadioButton
            android:textSize="14sp"
            android:id="@+id/optionB"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="false"
            android:text=""
            android:textColor="@color/colorPrimaryDark"
            android:backgroundTint="@color/colorPrimaryDark"
            android:buttonTint="@color/colorPrimaryDark"
            android:layout_marginTop="5dp"
            />

        <RadioButton
            android:textSize="14sp"
            android:id="@+id/optionC"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="false"
            android:text=""
            android:textColor="@color/colorPrimaryDark"
            android:backgroundTint="@color/colorPrimaryDark"
            android:buttonTint="@color/colorPrimaryDark"
            android:layout_marginTop="5dp"

            />

        <RadioButton
            android:textSize="14sp"
            android:id="@+id/optionD"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="false"
            android:text=""
            android:textColor="@color/colorPrimaryDark"
            android:backgroundTint="@color/colorPrimaryDark"
            android:buttonTint="@color/colorPrimaryDark"
            android:layout_marginTop="5dp"

            />
    </RadioGroup>


    <Button
        android:id="@+id/submit"
        android:text="Submit"
        android:textColor="@android:color/white"
        android:background="@drawable/button"
        android:layout_height="wrap_content"
        android:layout_width="wrap_content"
        android:layout_centerHorizontal="true"
        android:layout_below="@+id/radioGroup"
        android:layout_marginTop="20dp"
        android:layout_marginBottom="40dp"/>




</RelativeLayout>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    