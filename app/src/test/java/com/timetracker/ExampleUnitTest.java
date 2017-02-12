package com.timetracker;

import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.internal.builders.JUnit3Builder;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Instant instant = new Instant();
        LocalDateTime now = new LocalDateTime();
        System.out.println(now);
        System.out.println(now.toDate().getTime());
        System.out.println(instant.getMillis());
    }
}