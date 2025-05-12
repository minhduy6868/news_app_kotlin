import Dimens.MediumPadding2
import Dimens.PageIndicatorWidth
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gk.news_pro.onboarding_screen.components.pages
import com.gk.news_pro.page.screen.onboarding_screen.components.NewsButton
import com.gk.news_pro.page.screen.onboarding_screen.components.NewsTextButton
import com.loc.newsapp.screen.onboarding_screen.components.OnBoardingPage
import com.loc.newsapp.screen.onboarding_screen.components.PageIndicator


import kotlinx.coroutines.launch
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(initialPage = 0) {
            pages.size
        }

        val scope = rememberCoroutineScope()

        val buttonState = remember {
            derivedStateOf {
                when (pagerState.currentPage) {
                    0 -> listOf(null, "Next")  // Không hiển thị nút "Back", chỉ có "Next"
                    pages.size - 1 -> listOf("Back", "Let's Start") // Trang cuối
                    else -> listOf("Back", "Next") // Các trang giữa
                }
            }
        }

        HorizontalPager(state = pagerState) { index ->
            OnBoardingPage(page = pages[index])
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MediumPadding2)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(
                modifier = Modifier.width(PageIndicatorWidth),
                pageSize = pages.size,
                selectPage = pagerState.currentPage
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hiển thị nút "Back" nếu không ở trang đầu tiên
                buttonState.value[0]?.let {
                    NewsTextButton(
                        text = it,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                            }
                        })
                }

                // Luôn hiển thị nút "Next" hoặc "Let's Start"
                NewsButton(text = buttonState.value[1] ?: "",
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage == pages.size - 1) {
                                // Chuyển đến màn hình Home
                            } else {
                                pagerState.animateScrollToPage(
                                    page = pagerState.currentPage + 1
                                )
                            }
                        }
                    })
            }
        }
    }
}
