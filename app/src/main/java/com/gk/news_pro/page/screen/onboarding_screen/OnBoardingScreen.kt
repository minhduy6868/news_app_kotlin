
package com.loc.newsapp.screen.onboarding_screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gk.news_pro.R
import com.gk.news_pro.onboarding_screen.components.Page
import com.gk.news_pro.onboarding_screen.components.pages
import com.gk.news_pro.ui.theme.BlueGray
import com.gk.news_pro.ui.theme.NewsProTheme
import kotlinx.coroutines.launch

object Dimens {
    val MediumPadding1 = 24.dp
    val MediumPadding2 = 30.dp
    val IndicatorSize = 14.dp
    val PageIndicatorWidth = 52.dp
    const val MaxContentWidth = 800f // Max width for large screens in dp
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(onFinish: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isLargeScreen = screenWidth > 600.dp // Consider screens > 600dp as large (e.g., tablets)

    // Scale padding and sizes for large screens
    val paddingHorizontal = if (isLargeScreen) Dimens.MediumPadding2 * 1.5f else Dimens.MediumPadding2
    val indicatorWidth = if (isLargeScreen) Dimens.PageIndicatorWidth * 1.5f else Dimens.PageIndicatorWidth

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .widthIn(max = Dimens.MaxContentWidth.dp) // Constrain max width
                .align(Alignment.Center) // Center on large screens
        ) {
            val pagerState = rememberPagerState(pageCount = { pages.size })
            val scope = rememberCoroutineScope()

            val buttons = remember {
                derivedStateOf {
                    when (pagerState.currentPage) {
                        0 -> listOf(null, "Next")
                        pages.size - 1 -> listOf("Back", "Let's Start")
                        else -> listOf("Back", "Next")
                    }
                }
            }

            HorizontalPager(state = pagerState) { index ->
                OnBoardingPage(page = pages[index], isLargeScreen = isLargeScreen)
            }

            Spacer(Modifier.weight(1f))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingHorizontal)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicator(
                    modifier = Modifier.width(indicatorWidth),
                    pageSize = pages.size,
                    selectedPage = pagerState.currentPage,
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = BlueGray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttons.value[0]?.let { text ->
                        NewsTextButton(text = text, isLargeScreen = isLargeScreen) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    }

                    NewsButton(text = buttons.value[1] ?: "", isLargeScreen = isLargeScreen) {
                        scope.launch {
                            if (pagerState.currentPage == pages.size - 1) onFinish()
                            else pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageIndicator(
    modifier: Modifier = Modifier,
    pageSize: Int,
    selectedPage: Int,
    selectedColor: Color,
    unselectedColor: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageSize) { index ->
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (index == selectedPage) selectedColor else unselectedColor)
            )
        }
    }
}

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page,
    isLargeScreen: Boolean = false
) {
    // Scale font sizes for large screens
    val titleFontSize = if (isLargeScreen) 28.sp else 22.sp
    val descriptionFontSize = if (isLargeScreen) 16.sp else 14.sp

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = page.img),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Maintain aspect ratio for images
                .fillMaxHeight(0.6f) // Reduce height to balance layout
        )

        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(colorResource(id = R.color.input_background))
                .padding(
                    horizontal = if (isLargeScreen) Dimens.MediumPadding2 * 1.5f else Dimens.MediumPadding2,
                    vertical = Dimens.MediumPadding1
                )
        ) {
            Column {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.display_small),
                        fontSize = titleFontSize
                    )
                )
                Spacer(Modifier.height(Dimens.MediumPadding1))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = colorResource(id = R.color.text_medium),
                        fontSize = descriptionFontSize
                    )
                )
            }
        }
    }
}

@Composable
fun NewsButton(
    text: String,
    isLargeScreen: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .height(if (isLargeScreen) 48.dp else 40.dp) // Larger buttons on big screens
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isLargeScreen) 16.sp else 14.sp
            )
        )
    }
}

@Composable
fun NewsTextButton(
    text: String,
    isLargeScreen: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .height(if (isLargeScreen) 48.dp else 40.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isLargeScreen) 16.sp else 14.sp
            ),
            color = Color.Blue
        )
    }
}

@Preview(showBackground = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, device = "id:pixel_tablet") // Tablet preview
@Composable
fun OnBoardingScreenPreview() {
    NewsProTheme {
        OnBoardingScreen(onFinish = {})
    }
}
