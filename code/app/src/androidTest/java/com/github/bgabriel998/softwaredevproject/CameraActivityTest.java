package com.github.bgabriel998.softwaredevproject;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {
    @Rule
    public ActivityScenarioRule<Button1Activity> testRule = new ActivityScenarioRule<>(Button1Activity.class);

}
