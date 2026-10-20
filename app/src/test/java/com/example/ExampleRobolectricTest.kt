package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KCET Gen Z", appName)
  }

  @Test
  fun `admin panel screen class and composable load without error`() {
    val outerCls = Class.forName("com.example.ui.screens.AdminPanelScreenKt")
    org.junit.Assert.assertNotNull(outerCls)

    // Verify all inner lambda classes can be loaded and verified by JVM/D8
    val classNames = listOf(
        "com.example.ui.screens.AdminScreenScope",
        "com.example.ui.screens.AdminPanelState",
        "com.example.ui.screens.AdminPanelActions",
        "com.example.ui.screens.AdminPanelScreenKt\$AdminPanelContent\$verifyAdminAccess\$1\$1\$1"
    )
    for (name in classNames) {
        val cls = Class.forName(name)
        org.junit.Assert.assertNotNull(cls)
    }
    
    // Also check methods on AdminPanelScreenKt
    val methods = outerCls.declaredMethods
    org.junit.Assert.assertTrue(methods.isNotEmpty())
    for (m in methods) {
        org.junit.Assert.assertTrue(
            "Method ${m.name} has ${m.parameterCount} params, must be <= 255",
            m.parameterCount <= 255
        )
    }
  }
}
