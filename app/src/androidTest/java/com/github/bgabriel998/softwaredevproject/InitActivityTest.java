package com.github.bgabriel998.softwaredevproject;

import androidx.test.espresso.intent.Intents;

import org.junit.After;
import org.junit.Before;

public class InitActivityTest {
    @Before
    public void setup(){
        Intents.init();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }


}
