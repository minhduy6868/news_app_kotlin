package com.loc.newsapp.screen.onboarding_screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gk.news_pro.R
import kotlinx.coroutines.launch

object Dimens {
    val MediumPadding1 = 16.dp // Reduced from 24.dp to minimize white space
    val MediumPadding2 = 16.dp // Reduced from 24.dp to minimize white space
    val IndicatorSize = 14.dp
    val ButtonHeight = 48.dp
    val ButtonMinWidth = 100.dp
    val MaxContentWidth = 800.dp
    val ImageAspectRatio = 16f / 9f
    val ImageHeightFraction = 0.5f // Adjusted to balance image size
}

data class Page(
    val title: String,
    val description: String,
    val img: Int
)

val pages = listOf(
    Page(
        title = "Tin Tức Trực Tiếp",
        description = "Cập nhật tin tức nóng hổi mọi lúc, mọi nơi với các bản tin trực tiếp nhanh chóng và chính xác.",
        img = R.drawable.livenews
    ),
    Page(
        title = "MC AI Thông Minh",
        description = "Trải nghiệm các bản tin được dẫn dắt bởi MC AI hiện đại, mang đến thông tin sinh động và hấp dẫn.",
        img = R.drawable.ai
    ),
    Page(
        title = "Radio & Diễn Đàn",
        description = "Nghe radio tin tức mọi lúc và tham gia diễn đàn để thảo luận các chủ đề nổi bật cùng cộng đồng.",
        img = R.drawable.radio
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(onFinish: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isLargeScreen = screenWidth > 600.dp
    val paddingHorizontal = if (isLargeScreen) Dimens.MediumPadding2 * 1.5f else Dimens.MediumPadding2
    val fontScale = if (isLargeScreen) 1.2f else 1f // Slightly reduced to balance text size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF1F8E9),
                        Color(0xFFA0E7A5),
                        Color(0xFFE0F2F1),
                        Color(0xFF84F08C),
                        Color(0xFFE0F7FA)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = Dimens.MaxContentWidth)
                .align(Alignment.Center)
                .padding(horizontal = paddingHorizontal)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Distribute content evenly
        ) {
            val pagerState = rememberPagerState(pageCount = { pages.size })
            val scope = rememberCoroutineScope()

            val buttons = remember {
                derivedStateOf {
                    when (pagerState.currentPage) {
                        0 -> listOf(null, "Tiếp theo")
                        pages.size - 1 -> listOf("Quay lại", "Bắt đầu")
                        else -> listOf("Quay lại", "Tiếp theo")
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f), // Use weight to fill available space
                pageSpacing = 12.dp, // Reduced from 16.dp to tighten layout
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapAnimationSpec = spring(stiffness = 400f)
                )
            ) { index ->
                val pageOffset = pagerState.currentPageOffsetFraction
                val scale by animateFloatAsState(
                    targetValue = if (index == pagerState.currentPage) 1f else 0.95f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
                    label = "page_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (index == pagerState.currentPage) 1f else 0.7f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
                    label = "page_alpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .alpha(alpha)
                ) {
                    OnBoardingPage(
                        page = pages[index],
                        isLargeScreen = isLargeScreen,
                        fontScale = fontScale
                    )
                }
            }

            PageIndicator(
                pageSize = pages.size,
                selectedPage = pagerState.currentPage,
                selectedColor = Color(0xFF1A3C34),
                unselectedColor = Color(0xFF1A3C34).copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(Dimens.MediumPadding1))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.MediumPadding2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttons.value[0]?.let { text ->
                    NewsTextButton(
                        text = text,
                        isLargeScreen = isLargeScreen,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))

                NewsButton(
                    text = buttons.value[1] ?: "",
                    isLargeScreen = isLargeScreen,
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage == pages.size - 1) onFinish()
                            else pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PageIndicator(
    pageSize: Int,
    selectedPage: Int,
    selectedColor: Color,
    unselectedColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageSize) { index ->
            val isSelected = index == selectedPage
            val size by animateDpAsState(
                targetValue = if (isSelected) Dimens.IndicatorSize else Dimens.IndicatorSize * 0.7f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "indicator_size"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) selectedColor else unselectedColor,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "indicator_color"
            )

            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
                    .shadow(4.dp, CircleShape)
            )
        }
    }
}

@Composable
fun OnBoardingPage(
    page: Page,
    isLargeScreen: Boolean,
    fontScale: Float
) {
    val titleFontSize = (20.sp * fontScale) // Slightly reduced for balance
    val descriptionFontSize = (14.sp * fontScale) // Slightly reduced for balance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = Dimens.MediumPadding1),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        Image(
            painter = painterResource(id = page.img),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Use weight to center and scale image
                .aspectRatio(Dimens.ImageAspectRatio)
                .clip(RoundedCornerShape(16.dp))
                .shadow(8.dp, RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(12.dp)) // Reduced from MediumPadding1

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3C34),
                fontSize = titleFontSize
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp)) // Reduced from 8.dp

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A3C34).copy(alpha = 0.8f),
                fontSize = descriptionFontSize
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(0.5f)) // Add flexible spacer to push content up
    }
}

@Composable
fun NewsButton(
    text: String,
    isLargeScreen: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(Dimens.ButtonHeight)
            .widthIn(min = Dimens.ButtonMinWidth)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1A3C34),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isLargeScreen) 16.sp else 14.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
fun NewsTextButton(
    text: String,
    isLargeScreen: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "text_button_scale"
    )

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .height(Dimens.ButtonHeight)
            .padding(horizontal = 8.dp)
            .scale(scale),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isLargeScreen) 16.sp else 14.sp
            ),
            color = Color(0xFF1A3C34),
            maxLines = 1
        )
    }
}