package org.apache.olingo.odata2.core;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import java.util.Locale;

public class LocaleAsserter {

    public static void assertLocale(Locale actualLocale, Locale expectedLocale) {
        assertLocale("Unexpected locale", actualLocale, expectedLocale);
    }

    public static void assertLocale(String errorMessage, Locale actualLocale, Locale expectedLocale) {
        assertThat(errorMessage, actualLocale.toString(), containsString(expectedLocale.toString()));
    }
}
