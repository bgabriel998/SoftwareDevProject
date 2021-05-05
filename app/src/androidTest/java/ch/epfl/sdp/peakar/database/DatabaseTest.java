package ch.epfl.sdp.peakar.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static ch.epfl.sdp.peakar.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.TestingConstants.SHORT_SLEEP_TIME;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private static final int USER_OFFSET = new Random().nextInt();
    private static final String user1 = BASIC_USERNAME + USER_OFFSET;

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child(Database.CHILD_USERS).child(user1).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /* Test that isPresent method works */
    @Test
    public void isPresentTest() throws InterruptedException {
        Database.setChild(Database.CHILD_USERS + user1, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);

        Database.isPresent(Database.CHILD_USERS, Database.CHILD_USERNAME, "", Assert::fail, () -> assertTrue("Correct behavior", true));
        Database.isPresent(Database.CHILD_USERS, Database.CHILD_USERNAME, user1, () -> assertTrue("Correct behavior", true),  Assert::fail);
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /* Test that setChild method works */
    @Test
    public void setChildTest() throws InterruptedException {
        Database.isPresent(Database.CHILD_USERS, Database.CHILD_USERNAME, user1, Assert::fail, () -> assertTrue("Correct behavior", true));
        Thread.sleep(SHORT_SLEEP_TIME);
        Database.setChild(Database.CHILD_USERS + user1, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);
        Database.isPresent(Database.CHILD_USERS, Database.CHILD_USERNAME, user1, () -> assertTrue("Correct behavior", true), Assert::fail);
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /* Test that exception is thrown if different list sizes are provided */
    @Test(expected = RuntimeException.class)
    public void differentSizes_SetChildTest() throws InterruptedException {
        Database.setChild(Database.CHILD_USERS + user1, Arrays.asList("string1", "string2"), Collections.singletonList(""));
    }
}
