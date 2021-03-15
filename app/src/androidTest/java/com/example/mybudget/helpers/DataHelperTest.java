package com.example.mybudget.helpers;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DataHelperTest {
    private Context context;
    private DataHelper dataHelper;

    @Before
    public void before()
    {
        this.context = InstrumentationRegistry.getInstrumentation().getContext();
        this.dataHelper = DataHelper.getDataHelper(context);
    }

    @Test
    public void parseInput() {
        List<String> matches = ImmutableList.of("food", "10");
        dataHelper.addItemFromVoice(matches);

        assertEquals("", "");
    }

}