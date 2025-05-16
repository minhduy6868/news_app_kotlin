package com.gk.news_pro.page.screen.detail_screen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.gk.news_pro.data.local.AppDatabase
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    navController: NavController,
    news: News,
    geminiRepository: GeminiRepository
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val viewModel = remember { NewsDetailViewModel(geminiRepository, database, context) }
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var expandedAiAnalysis by remember { mutableStateOf(true) }

    // Automatically analyze news from URL when screen is created
    LaunchedEffect(news.link) {
        if (news.link.isNotEmpty()) {
            scope.launch {
                viewModel.analyzeNewsFromUrl(news)
                snackbarHostState.showSnackbar(
                    message = "Đang phân tích bài báo...",
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        topBar = {
            NewsDetailAppBar(
                navController = navController,
                news = news,
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, "Bài báo: ${news.title}")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            """
                            ${news.title}
                            
                            PHÂN TÍCH AI:
                            ${aiAnalysis ?: "Chưa có phân tích"}
                            
                            Đọc bài viết gốc: ${news.link}
                            """.trimIndent()
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ bài báo"))
                },
                onReadLater = {
                    scope.launch {
                        viewModel.saveForReadLater(news, aiAnalysis)
                        snackbarHostState.showSnackbar(
                            message = "Đã lưu bài báo để đọc sau",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp),
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        action = {
                            data.visuals.actionLabel?.let { actionLabel ->
                                TextButton(onClick = { data.performAction() }) {
                                    Text(
                                        text = actionLabel,
                                        color = MaterialTheme.colorScheme.inversePrimary
                                    )
                                }
                            }
                        }
                    ) {
                        Text(text = data.visuals.message)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Image with gradient overlay
            if (!news.image_url.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = news.image_url,
                            error = rememberAsyncImagePainter(model = "https://via.placeholder.com/800x400?text=Image+not+available")
                        ),
                        contentDescription = news.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )

                    // Source name chip on top right
                    if (news.source_name.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = news.source_name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Content area with padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (news.image_url.isNullOrEmpty()) 16.dp else 8.dp)
            ) {
                // Title with animated appearance
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Meta Information with icons
                NewsMetaInfo(news)

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                // Description with styled text
                if (!news.description.isNullOrEmpty()) {
                    Text(
                        text = news.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Justify,
                            lineHeight = 24.sp
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Read Original Article Button
                if (news.link.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { uriHandler.openUri(news.link) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.5.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Đọc bài viết gốc",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // AI Analysis Section with full-width styling and matching background
            AnimatedVisibility(
                visible = aiAnalysis != null || isLoading || error != null,
                enter = fadeIn(animationSpec = tween(300)) +
                        expandVertically(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300)) +
                        shrinkVertically(animationSpec = tween(500))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // AI Analysis Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "AI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Phân tích AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            IconButton(
                                onClick = { expandedAiAnalysis = !expandedAiAnalysis },
                                enabled = aiAnalysis != null && !isLoading && error == null
                            ) {
                                Icon(
                                    imageVector = if (expandedAiAnalysis)
                                        Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expandedAiAnalysis)
                                        "Thu gọn" else "Mở rộng",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Content based on state
                        when {
                            isLoading -> LoadingIndicator()
                            error != null -> ErrorDisplay(error ?: "Lỗi không xác định")
                            aiAnalysis != null -> {
                                EnhancedMarkdownText(
                                    markdown = aiAnalysis!!,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Spacer at the bottom
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "loading_animation")
            val animationValue by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "loading_animation"
            )

            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Đang phân tích...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorDisplay(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun NewsMetaInfo(news: News) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetaInfoItem(
            icon = Icons.Default.Info,
            text = news.source_name.takeIf { it.isNotEmpty() } ?: "Không xác định",
            modifier = Modifier.weight(1f)
        )

        if (!news.pubDate.isNullOrEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))
            MetaInfoItem(
                icon = Icons.Default.DateRange,
                text = formatDate(news.pubDate),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetaInfoItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsDetailAppBar(
    navController: NavController,
    news: News,
    onShare: () -> Unit,
    onReadLater: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = news.source_name.takeIf { it.isNotEmpty() } ?: "Bài báo",
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Chia sẻ",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onReadLater,
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Đọc sau",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun EnhancedMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        val paragraphs = markdown.split("\n\n") // Split by paragraphs

        paragraphs.forEach { paragraph ->
            when {
                paragraph.startsWith("# ") -> {
                    // Main heading
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.25.sp
                        )
                    ) {
                        append(paragraph.removePrefix("# ").trim())
                        append("\n\n")
                    }
                }
                paragraph.startsWith("## ") -> {
                    // Subheading
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            letterSpacing = 0.15.sp
                        )
                    ) {
                        append(paragraph.removePrefix("## ").trim())
                        append("\n\n")
                    }
                }
                paragraph.startsWith("### ") -> {
                    // Sub-subheading
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    ) {
                        append(paragraph.removePrefix("### ").trim())
                        append("\n\n")
                    }
                }
                paragraph.startsWith("> ") -> {
                    // Blockquote
                    withStyle(
                        style = ParagraphStyle(
                            textIndent = TextIndent(firstLine = 16.sp)
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            val lines = paragraph.split("\n")
                            lines.forEach { line ->
                                append("❝ ${line.removePrefix("> ").trim()}")
                                append("\n")
                            }
                        }
                    }
                    append("\n")
                }
                paragraph.trim().startsWith("- ") || paragraph.trim().startsWith("* ") -> {
                    // List items
                    val items = paragraph.split("\n")
                    items.forEach { item ->
                        if (item.trim().startsWith("- ") || item.trim().startsWith("* ")) {
                            val cleanItem = item.trim().removePrefix("- ").removePrefix("* ").trim()
                            append("• ")
                            appendStyledText(cleanItem)
                            append("\n")
                        }
                    }
                    append("\n")
                }
                paragraph.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                    // Numbered list
                    val items = paragraph.split("\n")
                    items.forEachIndexed { index, item ->
                        if (item.trim().matches(Regex("^\\d+\\.\\s.*"))) {
                            val cleanItem = item.replace(Regex("^\\d+\\.\\s"), "").trim()
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                append("${index + 1}. ")
                            }
                            appendStyledText(cleanItem)
                            append("\n")
                        }
                    }
                    append("\n")
                }
                paragraph.startsWith("```") && paragraph.endsWith("```") -> {
                    // Code block
                    val codeContent = paragraph
                        .removePrefix("```")
                        .removePrefix("kotlin")
                        .removePrefix("java")
                        .removePrefix("swift")
                        .removePrefix("js")
                        .removePrefix("python")
                        .removePrefix("html")
                        .removePrefix("css")
                        .removePrefix("json")
                        .removePrefix("\n")
                        .removeSuffix("```")
                        .trim()

                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        append(codeContent)
                    }
                    append("\n\n")
                }
                else -> {
                    // Regular paragraph with complex formatting
                    appendStyledText(paragraph)
                    append("\n\n")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 16.sp,
            textAlign = TextAlign.Justify,
            lineHeight = 24.sp
        ),
        modifier = modifier
    )
}

@Composable
private fun AnnotatedString.Builder.appendStyledText(text: String) {
    val segments = mutableListOf<TextSegment>()
    var currentPos = 0

    // Handle bold and italic
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    extractSegments(text, boldRegex, segments, TextSegmentType.BOLD)

    val italicRegex = Regex("\\*(.*?)\\*|_(.*?)_")
    extractSegments(text, italicRegex, segments, TextSegmentType.ITALIC)

    // Handle code blocks
    val codeRegex = Regex("`(.*?)`")
    extractSegments(text, codeRegex, segments, TextSegmentType.CODE)

    // Handle links
    val linkRegex = Regex("\\[(.*?)\\]\\((.*?)\\)")
    extractLinkSegments(text, linkRegex, segments)

    // Handle strikethrough
    val strikethroughRegex = Regex("~~(.*?)~~")
    extractSegments(text, strikethroughRegex, segments, TextSegmentType.STRIKETHROUGH)

    // Sort all segments by start position
    segments.sortBy { it.startPos }

    // Process segments and fill gaps with plain text
    currentPos = 0
    segments.forEach { segment ->
        // Add plain text before this segment if needed
        if (currentPos < segment.startPos) {
            append(text.substring(currentPos, segment.startPos))
        }

        // Add the segment with appropriate styling
        when (segment.type) {
            TextSegmentType.BOLD -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(segment.content)
                }
            }
            TextSegmentType.ITALIC -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(segment.content)
                }
            }
            TextSegmentType.CODE -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        letterSpacing = 0.sp
                    )
                ) {
                    append(" ${segment.content} ")
                }
            }
            TextSegmentType.LINK -> {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(segment.content)
                }
            }
            TextSegmentType.STRIKETHROUGH -> {
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.LineThrough
                    )
                ) {
                    append(segment.content)
                }
            }
        }

        currentPos = segment.endPos
    }

    // Add any remaining plain text
    if (currentPos < text.length) {
        append(text.substring(currentPos))
    }
}

private enum class TextSegmentType {
    BOLD, ITALIC, CODE, LINK, STRIKETHROUGH
}

private data class TextSegment(
    val startPos: Int,
    val endPos: Int,
    val content: String,
    val type: TextSegmentType,
    val url: String? = null
)

private fun extractSegments(
    text: String,
    regex: Regex,
    segments: MutableList<TextSegment>,
    type: TextSegmentType
) {
    regex.findAll(text).forEach { match ->
        val content = when (type) {
            TextSegmentType.ITALIC -> match.groupValues[1].ifEmpty { match.groupValues[2] }
            else -> match.groupValues[1]
        }

        segments.add(
            TextSegment(
                startPos = match.range.first,
                endPos = match.range.last + 1,
                content = content,
                type = type
            )
        )
    }
}

private fun extractLinkSegments(
    text: String,
    regex: Regex,
    segments: MutableList<TextSegment>
) {
    regex.findAll(text).forEach { match ->
        val linkText = match.groupValues[1]
        val url = match.groupValues[2]

        segments.add(
            TextSegment(
                startPos = match.range.first,
                endPos = match.range.last + 1,
                content = linkText,
                type = TextSegmentType.LINK,
                url = url
            )
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}