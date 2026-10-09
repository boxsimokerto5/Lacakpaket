package com.example

import com.example.ads.AdManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAdManagerConfiguration() {
        assertEquals("28979fc9d", AdManager.DEFAULT_APP_KEY)
        assertEquals("a49w6q65xfz50ql6", AdManager.BANNER_AD_UNIT_ID)
        assertEquals("3i8ol7cfj1i6831c", AdManager.INTERSTITIAL_AD_UNIT_ID)
        assertEquals("haijoq497a755vwc", AdManager.NATIVE_AD_UNIT_ID)
        assertEquals(5, AdManager.CLICKS_PER_INTERSTITIAL)
        assertNotNull(AdManager.isInitialized)
    }
}

