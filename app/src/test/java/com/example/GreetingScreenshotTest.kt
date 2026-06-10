package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.MemberEntity
import com.example.ui.screens.FamilyNodeCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleMember = MemberEntity(
      treeId = 1,
      firstName = "Arthur",
      lastName = "Pendragon",
      gender = "Male",
      birthDate = "1910-04-12",
      isDeceased = true,
      deathDate = "1988-11-20",
      occupation = "Ship Captain"
    )
    composeTestRule.setContent { 
      MyApplicationTheme { 
        FamilyNodeCard(member = sampleMember, spouseName = "Guinevere", onSelected = {}) 
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
