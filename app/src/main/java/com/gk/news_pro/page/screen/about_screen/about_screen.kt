package com.gk.news_pro.page.screen.about_screen

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Về News Pro",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.size(40.dp).padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            AppIntroductionSection()
            PrivacyPolicySection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AppIntroductionSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = "Giới thiệu News Pro",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 32.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "News Pro mang đến tin tức nhanh, cá nhân hóa với AI tiên tiến và giao diện thân thiện.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, textAlign = TextAlign.Justify, lineHeight = 24.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Tính năng nổi bật",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val features = listOf(
            "Tin tức trực tiếp" to "Cập nhật tin từ các nguồn uy tín theo chủ đề.",
            "AI video tóm tắt" to "Video ngắn tóm tắt sự kiện quan trọng.",
            "AI đánh giá tin tức" to "Tóm tắt và đánh giá độ tin cậy bài báo.",
            "Nghe radio" to "Hàng trăm kênh radio trong và ngoài nước.",
            "Phát nền" to "Nghe radio khi dùng ứng dụng khác hoặc khóa màn hình.",
            "Diễn đàn tin tức" to "Lưu bài, bình luận, chia sẻ quan điểm.",
            "Lưu tin tức" to "Lưu bài báo để đọc offline.",
            "Kênh radio yêu thích" to "Cá nhân hóa danh sách kênh radio.",
            "Đọc offline" to "Tải bài viết để đọc không cần Internet."
        )

        features.forEach { (title, desc) ->
            FeatureItem(title, desc)
        }

        Text(
            text = "Tải News Pro để khám phá thông tin theo cách của bạn!",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
    }
}

@Composable
private fun FeatureItem(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, textAlign = TextAlign.Justify, lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyPolicySection() {
    var expandedSections by remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = "Chính sách Bảo mật",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 32.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "News Pro cam kết bảo vệ quyền riêng tư và dữ liệu cá nhân của bạn.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, textAlign = TextAlign.Justify, lineHeight = 24.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val policies = listOf(
            Triple(1, "Thông tin thu thập", """
                - **Thông tin cá nhân**: Tên, email, mật khẩu mã hóa khi đăng ký.
                - **Dữ liệu sử dụng**: Tương tác với tin tức, radio, bài lưu, bình luận.
                - **Dữ liệu thiết bị**: Thiết bị, hệ điều hành, IP, mã định danh.
                - **Dữ liệu vị trí** (tùy chọn): Cung cấp nội dung phù hợp khu vực.
                - **Dữ liệu offline**: Bài viết lưu cục bộ trên thiết bị.
            """.trimIndent()),
            Triple(2, "Sử dụng thông tin", """
                - Cải thiện trải nghiệm: Đề xuất tin tức, radio.
                - Tạo video tóm tắt và đánh giá tin tức bằng AI.
                - Quản lý tài khoản, hỗ trợ kỹ thuật.
                - Phân tích dữ liệu để tối ưu ứng dụng.
                - Gửi thông báo tin tức nóng (nếu bật).
                - Quản lý nội dung diễn đàn.
            """.trimIndent()),
            Triple(3, "Chia sẻ thông tin", """
                Không bán dữ liệu cá nhân, trừ:
                - **Nhà cung cấp dịch vụ**: Hỗ trợ phân tích, lưu trữ, bị ràng buộc bảo mật.
                - **Yêu cầu pháp lý**: Tiết lộ khi luật yêu cầu.
                - **Diễn đàn công khai**: Bình luận, bài đăng hiển thị công khai.
            """.trimIndent()),
            Triple(4, "Lưu trữ và bảo mật", """
                - **Lưu trữ**: Dữ liệu mã hóa AES-256, offline lưu cục bộ.
                - **Bảo mật**: Mã hóa, tường lửa, kiểm tra định kỳ.
                - **Thời gian lưu**: Xóa sau 30 ngày nếu xóa tài khoản.
            """.trimIndent()),
            Triple(5, "Quyền của bạn", """
                - **Truy cập**: Xem dữ liệu cá nhân.
                - **Chỉnh sửa**: Sửa thông tin sai.
                - **Xóa**: Xóa tài khoản, dữ liệu.
                - **Tắt theo dõi**: Ngừng thu thập vị trí, cá nhân hóa.
                - **Tải dữ liệu**: Xuất dữ liệu cá nhân.
                Liên hệ: **support@newspro.app**.
            """.trimIndent()),
            Triple(6, "Dữ liệu trẻ em", """
                Không thu thập dữ liệu từ trẻ dưới 13 tuổi. Dữ liệu phát hiện sẽ xóa ngay.
            """.trimIndent()),
            Triple(7, "Quảng cáo và theo dõi", """
                - Hiển thị quảng cáo cá nhân hóa (có thể tắt).
                - Sử dụng Google Analytics ẩn danh, không liên kết cá nhân trừ khi được phép.
            """.trimIndent()),
            Triple(8, "Thay đổi chính sách", """
                Cập nhật chính sách sẽ thông báo qua email hoặc ứng dụng.
            """.trimIndent()),
            Triple(9, "Liên hệ", """
                - **Email**: support@newspro.app
                - **Địa chỉ**: Tầng 10, Tech Tower, 123 Đường Tin Tức, TP.HCM
                - **Hotline**: (+84) 123 456 789
            """.trimIndent())
        )

        policies.forEach { (index, title, content) ->
            PrivacyPolicyItem(
                index = index,
                title = title,
                content = content,
                isExpanded = index in expandedSections,
                onToggle = {
                    expandedSections = if (index in expandedSections) {
                        expandedSections - index
                    } else {
                        expandedSections + index
                    }
                }
            )
        }
    }
}

@Composable
private fun PrivacyPolicyItem(
    index: Int,
    title: String,
    content: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$index. $title",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(500))
        ) {
            EnhancedMarkdownText(
                markdown = content,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun EnhancedMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        val lines = markdown.split("\n")
        lines.forEach { line ->
            when {
                line.trim().startsWith("- ") -> {
                    append("• ")
                    appendStyledText(line.trim().removePrefix("- ").trim())
                    append("\n")
                }
                else -> {
                    appendStyledText(line.trim())
                    append("\n")
                }
            }
        }
    }
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            textAlign = TextAlign.Justify,
            lineHeight = 20.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun AnnotatedString.Builder.appendStyledText(text: String) {
    val segments = mutableListOf<TextSegment>()
    var currentPos = 0

    // Handle bold (**text**)
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    boldRegex.findAll(text).forEach { match ->
        segments.add(
            TextSegment(
                startPos = match.range.first,
                endPos = match.range.last + 1,
                content = match.groupValues[1],
                type = TextSegmentType.BOLD
            )
        )
    }

    // Sort segments by start position
    segments.sortBy { it.startPos }

    // Process segments and plain text
    segments.forEach { segment ->
        if (currentPos < segment.startPos) {
            append(text.substring(currentPos, segment.startPos))
        }
        when (segment.type) {
            TextSegmentType.BOLD -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(segment.content)
                }
            }
        }
        currentPos = segment.endPos
    }
    if (currentPos < text.length) {
        append(text.substring(currentPos))
    }
}

private enum class TextSegmentType {
    BOLD
}

private data class TextSegment(
    val startPos: Int,
    val endPos: Int,
    val content: String,
    val type: TextSegmentType
)