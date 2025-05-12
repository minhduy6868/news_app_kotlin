package com.loc.newsapp.screen.onboarding_screen.components

import Dimens.MediumPadding1
import Dimens.MediumPadding2
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gk.news_pro.R
import com.gk.news_pro.onboarding_screen.components.Page
import com.gk.news_pro.onboarding_screen.components.pages
import com.gk.news_pro.ui.theme.NewsProTheme


@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page
) {
    Box(modifier = modifier) {
        Column {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f), // Chiếm 60% chiều cao màn hình
                painter = painterResource(id = page.img),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(colorResource(id = R.color.input_background))
                .padding(horizontal = MediumPadding2, vertical = MediumPadding1)
        ) {
            Column {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.display_small)
                    )
                )
                Spacer(modifier = Modifier.height(MediumPadding1))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = colorResource(id = R.color.text_medium)
                    )
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun OnBaordingpagePreview() {
    NewsProTheme {
        OnBoardingPage(
            page = pages[0])
    }
}