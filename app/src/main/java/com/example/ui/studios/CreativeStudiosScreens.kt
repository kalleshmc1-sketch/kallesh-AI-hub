package com.example.ui.studios

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UiState
import com.example.data.model.WorkspaceItem
import com.example.ui.viewmodel.CreativeStudioTab
import com.example.ui.viewmodel.HubSection
import com.example.ui.viewmodel.KalleshHubViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeStudiosHubScreen(
    viewModel: KalleshHubViewModel,
    selectedTab: CreativeStudioTab,
    isGenerating: Boolean,
    latestImagePath: String?,
    latestMusicPath: String?,
    liveVoiceHistory: List<Pair<String, String>>,
    workspaceItemsState: UiState<List<WorkspaceItem>>,
    isPlayingAudio: Boolean
) {
    BackHandler {
        viewModel.navigateToSection(HubSection.HOME)
    }

    val allItems = (workspaceItemsState as? UiState.Success)?.data.orEmpty()
    val quotaStatus by viewModel.quotaStatusState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("creative_studios_screen")
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 12.dp
        ) {
            CreativeStudioTab.entries.forEach { tab ->
                val icon = when (tab) {
                    CreativeStudioTab.IMAGE -> Icons.Default.Image
                    CreativeStudioTab.VIDEO -> Icons.Default.Movie
                    CreativeStudioTab.MUSIC -> Icons.Default.MusicNote
                    CreativeStudioTab.VOICE_LIVE -> Icons.Default.GraphicEq
                }
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.openCreativeStudio(tab) },
                    text = { Text(tab.title) },
                    icon = { Icon(icon, contentDescription = tab.title) },
                    modifier = Modifier.testTag("studio_tab_${tab.name.lowercase()}")
                )
            }
        }

        // Backend Limited AI Tool Usage Strip
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan: ${quotaStatus.planType} (₹${quotaStatus.priceInr}) • Remaining Uses: ${quotaStatus.remainingCount} / ${quotaStatus.allowedLimit}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Failed requests never deduct usage",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        when (selectedTab) {
            CreativeStudioTab.IMAGE -> ImageGenerationStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                latestImagePath = latestImagePath,
                savedImages = allItems.filter { it.type == "IMAGE" }
            )
            CreativeStudioTab.VIDEO -> VideoGenerationStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                savedVideos = allItems.filter { it.type == "VIDEO" }
            )
            CreativeStudioTab.MUSIC -> MusicLabStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                latestMusicPath = latestMusicPath,
                isPlayingAudio = isPlayingAudio,
                savedTracks = allItems.filter { it.type == "MUSIC" }
            )
            CreativeStudioTab.VOICE_LIVE -> LiveVoiceStudioPane(
                viewModel = viewModel,
                isGenerating = isGenerating,
                liveVoiceHistory = liveVoiceHistory,
                isPlayingAudio = isPlayingAudio
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImageGenerationStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    latestImagePath: String?,
    savedImages: List<WorkspaceItem>
) {
    val context = LocalContext.current
    val latestImageResult by viewModel.latestGeneratedImageResult.collectAsStateWithLifecycle()
    val imageError by viewModel.imageGenerationError.collectAsStateWithLifecycle()
    val imageProgress by viewModel.imageGenerationProgress.collectAsStateWithLifecycle()

    var prompt by remember { mutableStateOf("") }
    var selectedAspect by remember { mutableStateOf("1:1") }
    var selectedQuality by remember { mutableStateOf("1K") }
    var numberOfImages by remember { mutableIntStateOf(1) }
    var selectedStyle by remember { mutableStateOf("None") }
    var sourcePhotoBase64 by remember { mutableStateOf<String?>(null) }
    var sourcePhotoMime by remember { mutableStateOf("image/jpeg") }
    var sourcePhotoName by remember { mutableStateOf("") }
    var selectedVariationIndex by remember { mutableIntStateOf(0) }
    var fullScreenPreviewPath by remember { mutableStateOf<String?>(null) }
    var fullScreenPreviewPrompt by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                sourcePhotoMime = loaded.first
                sourcePhotoBase64 = loaded.second
                sourcePhotoName = loaded.third
            }
        }
    }

    if (fullScreenPreviewPath != null) {
        FullScreenImagePreviewDialog(
            filePath = fullScreenPreviewPath!!,
            prompt = fullScreenPreviewPrompt,
            onDownload = {
                viewModel.downloadGeneratedMediaToDevice(
                    sourcePathOrUrl = fullScreenPreviewPath!!,
                    mediaType = "IMAGE",
                    promptTitle = fullScreenPreviewPrompt
                )
            },
            onDismiss = { fullScreenPreviewPath = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Image Studio • Real Image Generation API",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "POST /api/ai/image/generate • Supports aspect ratio, quality (1K–4K), image count, and reference image editing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = {
                            prompt = it
                            if (imageError != null) viewModel.clearImageGenerationError()
                        },
                        label = { Text("Describe the image or photo edit...") },
                        placeholder = { Text("e.g., Futuristic eco-city at golden hour, architectural photography, 8k...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_studio_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isGenerating
                    )

                    Text("Optional Style Enhancement", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Futuristic Neon", "Photorealistic", "3D Glass-Metal", "Digital Art", "Cinematic").forEach { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { if (!isGenerating) selectedStyle = style },
                                label = { Text(style) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1:1", "16:9", "9:16", "4:3", "3:4").forEach { ratio ->
                                    FilterChip(
                                        selected = selectedAspect == ratio,
                                        onClick = { if (!isGenerating) selectedAspect = ratio },
                                        label = { Text(ratio) },
                                        modifier = Modifier.testTag("image_aspect_${ratio.replace(":", "_")}")
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Resolution / Quality", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1K", "2K", "4K").forEach { size ->
                                    FilterChip(
                                        selected = selectedQuality == size,
                                        onClick = { if (!isGenerating) selectedQuality = size },
                                        label = { Text(size) },
                                        modifier = Modifier.testTag("image_quality_$size")
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Number of Images", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 3, 4).forEach { count ->
                                    FilterChip(
                                        selected = numberOfImages == count,
                                        onClick = {
                                            if (!isGenerating) {
                                                numberOfImages = count
                                                selectedVariationIndex = 0
                                            }
                                        },
                                        label = { Text("$count") },
                                        modifier = Modifier.testTag("image_count_$count")
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isGenerating,
                            modifier = Modifier.testTag("pick_reference_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (sourcePhotoName.isBlank()) "Upload Photo to Edit" else "Ref: $sourcePhotoName")
                        }

                        if (sourcePhotoBase64 != null) {
                            AssistChip(
                                onClick = {
                                    sourcePhotoBase64 = null
                                    sourcePhotoName = ""
                                },
                                label = { Text("Clear Photo") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            selectedVariationIndex = 0
                            val effectivePrompt = if (selectedStyle != "None") {
                                "$prompt, style: $selectedStyle"
                            } else {
                                prompt
                            }
                            viewModel.generateOrEditStudioImage(
                                prompt = effectivePrompt,
                                aspectRatio = selectedAspect,
                                qualitySize = selectedQuality,
                                sourceImageBase64 = sourcePhotoBase64,
                                numberOfImages = numberOfImages,
                                sourceMimeType = sourcePhotoMime
                            )
                        },
                        enabled = prompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_image_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Image(s)...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (sourcePhotoBase64 != null) {
                                    "Edit Photo with AI ($selectedQuality)"
                                } else {
                                    "Generate ${if (numberOfImages > 1) "$numberOfImages AI Images" else "AI Image"} ($selectedQuality)"
                                }
                            )
                        }
                    }

                    // Progress State while generating
                    if (isGenerating && !imageProgress.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_generation_progress_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = imageProgress!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Actionable Error Card (Never consumes limited quota on failure)
                    if (!imageError.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_generation_error_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Image Generation Error (No Usage Deducted)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearImageGenerationError() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss error",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = imageError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Latest Generated Result Card with View, Download, Regenerate, Save to History
        val activeFiles = latestImageResult?.allFilePaths?.filter { it.isNotBlank() }
            ?: listOfNotNull(latestImagePath?.takeIf { it.isNotBlank() })
        if (activeFiles.isNotEmpty()) {
            val safeIdx = selectedVariationIndex.coerceIn(0, activeFiles.lastIndex)
            val currentFilePath = activeFiles[safeIdx]
            val currentPrompt = prompt.ifBlank {
                savedImages.firstOrNull { it.mediaData == currentFilePath }?.prompt ?: "Generated AI Image"
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("image_generation_result_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Latest Generated Image",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Model: ${latestImageResult?.modelUsed ?: "gemini-2.5-flash-image"} • ${latestImageResult?.aspectRatio ?: selectedAspect} • ${latestImageResult?.imageSize ?: selectedQuality}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "COMPLETED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LocalFileBitmapPreview(
                            filePath = currentFilePath,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    fullScreenPreviewPath = currentFilePath
                                    fullScreenPreviewPrompt = currentPrompt
                                }
                        )

                        if (activeFiles.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                activeFiles.forEachIndexed { idx, _ ->
                                    FilterChip(
                                        selected = safeIdx == idx,
                                        onClick = { selectedVariationIndex = idx },
                                        label = { Text("Image #${idx + 1}") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    fullScreenPreviewPath = currentFilePath
                                    fullScreenPreviewPrompt = currentPrompt
                                },
                                modifier = Modifier.testTag("view_image_button")
                            ) {
                                Icon(Icons.Default.OpenInFull, contentDescription = "View", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View")
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.downloadGeneratedMediaToDevice(
                                        sourcePathOrUrl = currentFilePath,
                                        mediaType = "IMAGE",
                                        promptTitle = currentPrompt
                                    )
                                },
                                modifier = Modifier.testTag("download_image_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.generateOrEditStudioImage(
                                        prompt = currentPrompt,
                                        aspectRatio = selectedAspect,
                                        qualitySize = selectedQuality,
                                        sourceImageBase64 = sourcePhotoBase64,
                                        numberOfImages = numberOfImages,
                                        sourceMimeType = sourcePhotoMime
                                    )
                                },
                                enabled = !isGenerating,
                                modifier = Modifier.testTag("regenerate_image_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Regenerate")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.saveGeneratedImageToHistory(
                                        prompt = currentPrompt,
                                        filePath = currentFilePath,
                                        aspectRatio = latestImageResult?.aspectRatio ?: selectedAspect,
                                        qualitySize = latestImageResult?.imageSize ?: selectedQuality,
                                        modelUsed = latestImageResult?.modelUsed ?: "gemini-2.5-flash-image"
                                    )
                                },
                                modifier = Modifier.testTag("save_image_history_button")
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to History", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save to History")
                            }
                        }
                    }
                }
            }
        }

        if (savedImages.isNotEmpty()) {
            item {
                Text(
                    text = "YOUR IMAGE STUDIO HISTORY (${savedImages.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(savedImages, key = { it.id }) { item ->
                GeneratedImagePreviewCard(
                    item = item,
                    isGenerating = isGenerating,
                    onView = {
                        fullScreenPreviewPath = item.mediaData
                        fullScreenPreviewPrompt = item.prompt.ifBlank { item.title }
                    },
                    onDownload = {
                        viewModel.downloadGeneratedMediaToDevice(
                            sourcePathOrUrl = item.mediaData,
                            mediaType = "IMAGE",
                            promptTitle = item.prompt.ifBlank { item.title }
                        )
                    },
                    onRegenerate = {
                        prompt = item.prompt.ifBlank { item.title }
                        selectedAspect = item.aspectRatio.ifBlank { "1:1" }
                        viewModel.generateOrEditStudioImage(
                            prompt = prompt,
                            aspectRatio = selectedAspect,
                            qualitySize = selectedQuality,
                            numberOfImages = 1
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeneratedImagePreviewCard(
    item: WorkspaceItem,
    isGenerating: Boolean,
    onView: () -> Unit,
    onDownload: () -> Unit,
    onRegenerate: () -> Unit
) {
    val formattedDate = remember(item.createdAt) {
        val ts = item.createdAt?.toDate() ?: Date()
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(ts)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Image Generator",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LocalFileBitmapPreview(
                filePath = item.mediaData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onView() }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.prompt.ifBlank { item.title },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$formattedDate • ${item.aspectRatio} • ${item.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = onView,
                    label = { Text("Open / View") },
                    leadingIcon = { Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = onDownload,
                    label = { Text("Download") },
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = onRegenerate,
                    enabled = !isGenerating,
                    label = { Text("Regenerate") },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoGenerationStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    savedVideos: List<WorkspaceItem>
) {
    val context = LocalContext.current
    val activeVideoJob by viewModel.activeVideoJobState.collectAsStateWithLifecycle()
    val videoError by viewModel.videoGenerationError.collectAsStateWithLifecycle()
    val isPollingVideo by viewModel.isPollingVideo.collectAsStateWithLifecycle()

    var prompt by remember { mutableStateOf("") }
    var aspectRatio by remember { mutableStateOf("16:9") }
    var startingPhotoBase64 by remember { mutableStateOf<String?>(null) }
    var startingPhotoMime by remember { mutableStateOf("image/jpeg") }
    var startingPhotoName by remember { mutableStateOf("") }
    var modalVideoPath by remember { mutableStateOf<String?>(null) }
    var modalVideoTitle by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = viewModel.readUriAsBase64(context, uri)
            if (loaded != null) {
                startingPhotoMime = loaded.first
                startingPhotoBase64 = loaded.second
                startingPhotoName = loaded.third
            }
        }
    }

    if (modalVideoPath != null) {
        VideoPlayerModalDialog(
            videoPathOrUrl = modalVideoPath!!,
            title = modalVideoTitle,
            onDownload = {
                viewModel.downloadGeneratedMediaToDevice(
                    sourcePathOrUrl = modalVideoPath!!,
                    mediaType = "VIDEO",
                    promptTitle = modalVideoTitle
                )
            },
            onDismiss = { modalVideoPath = null }
        )
    }

    val isBusy = isGenerating || isPollingVideo

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Veo 3 Video Studio • Real Video Generation API",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "POST /api/ai/video/generate • Generate cinematic text-to-video scenes or animate an uploaded reference photo in 16:9 Landscape or 9:16 Vertical.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = {
                            prompt = it
                            if (videoError != null) viewModel.clearVideoGenerationError()
                        },
                        label = { Text("Describe camera motion, subject, and lighting...") },
                        placeholder = { Text("e.g., Cinematic drone tracking shot over bioluminescent ocean waves at night...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_studio_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isBusy
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = aspectRatio == "16:9",
                            onClick = { if (!isBusy) aspectRatio = "16:9" },
                            label = { Text("16:9 Landscape") },
                            modifier = Modifier.testTag("video_aspect_16_9")
                        )
                        FilterChip(
                            selected = aspectRatio == "9:16",
                            onClick = { if (!isBusy) aspectRatio = "9:16" },
                            label = { Text("9:16 Vertical") },
                            modifier = Modifier.testTag("video_aspect_9_16")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isBusy,
                            modifier = Modifier.testTag("upload_video_frame_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (startingPhotoName.isBlank()) "Optional: Animate Reference Image" else "Frame: $startingPhotoName")
                        }

                        if (startingPhotoBase64 != null) {
                            AssistChip(
                                onClick = {
                                    startingPhotoBase64 = null
                                    startingPhotoName = ""
                                },
                                label = { Text("Clear Frame") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.generateVeoVideo(
                                prompt = prompt,
                                aspectRatio = aspectRatio,
                                sourceImageBase64 = startingPhotoBase64,
                                sourceMimeType = startingPhotoMime
                            )
                        },
                        enabled = prompt.isNotBlank() && !isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_video_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPollingVideo) "Rendering Video (Polling Status)..." else "Submitting to Veo 3 Engine...")
                        } else {
                            Icon(Icons.Default.Movie, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (startingPhotoBase64 != null) {
                                    "Generate Image-to-Video ($aspectRatio)"
                                } else {
                                    "Generate Veo 3 Video ($aspectRatio)"
                                }
                            )
                        }
                    }

                    // Asynchronous Job Pipeline Status Card (Queued -> Processing -> Completed / Failed)
                    if (activeVideoJob != null) {
                        VideoJobStatusPipelineCard(
                            job = activeVideoJob!!,
                            isPolling = isPollingVideo,
                            onPlayFull = {
                                if (activeVideoJob!!.videoUrlOrPath.isNotBlank()) {
                                    modalVideoPath = activeVideoJob!!.videoUrlOrPath
                                    modalVideoTitle = prompt.ifBlank { "Generated AI Video" }
                                }
                            },
                            onDownload = {
                                viewModel.downloadGeneratedMediaToDevice(
                                    sourcePathOrUrl = activeVideoJob!!.videoUrlOrPath,
                                    mediaType = "VIDEO",
                                    promptTitle = prompt.ifBlank { "Generated AI Video" }
                                )
                            },
                            onRegenerate = {
                                viewModel.generateVeoVideo(
                                    prompt = prompt,
                                    aspectRatio = aspectRatio,
                                    sourceImageBase64 = startingPhotoBase64,
                                    sourceMimeType = startingPhotoMime
                                )
                            },
                            onSaveToHistory = {
                                viewModel.saveGeneratedVideoToHistory(
                                    prompt = prompt,
                                    videoUrlOrPath = activeVideoJob!!.videoUrlOrPath,
                                    aspectRatio = activeVideoJob!!.aspectRatio,
                                    modelUsed = activeVideoJob!!.modelUsed
                                )
                            }
                        )
                    }

                    // Specific Video Error Banner (No usage deducted on failure)
                    if (!videoError.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("video_generation_error_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Video Generation Error (No Usage Deducted)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearVideoGenerationError() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss error",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = videoError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        if (savedVideos.isNotEmpty()) {
            item {
                Text(
                    text = "VEO 3 VIDEO RENDERING QUEUE & HISTORY (${savedVideos.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(savedVideos, key = { it.id }) { item ->
                val formattedDate = remember(item.createdAt) {
                    val ts = item.createdAt?.toDate() ?: Date()
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(ts)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                VideoFrameThumbnailPreview(
                                    videoPath = item.mediaData,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = item.mediaData.isNotBlank()) {
                                            modalVideoPath = item.mediaData
                                            modalVideoTitle = item.prompt.ifBlank { item.title }
                                        }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AI Video Generator",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = item.prompt.ifBlank { item.title },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$formattedDate • ${item.category} • Aspect ${item.aspectRatio}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (item.status) {
                                    "COMPLETED" -> Color(0xFF16A34A).copy(alpha = 0.16f)
                                    "FAILED" -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.tertiaryContainer
                                }
                            ) {
                                Text(
                                    text = item.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.status) {
                                        "COMPLETED" -> Color(0xFF15803D)
                                        "FAILED" -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (item.status == "COMPLETED" && item.mediaData.isNotBlank()) {
                            InlineAndroidVideoPlayer(
                                videoPathOrUrl = item.mediaData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (item.status == "COMPLETED" && item.mediaData.isNotBlank()) {
                                AssistChip(
                                    onClick = {
                                        modalVideoPath = item.mediaData
                                        modalVideoTitle = item.prompt.ifBlank { item.title }
                                    },
                                    label = { Text("Play Video") },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(14.dp))
                                    }
                                )
                                AssistChip(
                                    onClick = {
                                        viewModel.downloadGeneratedMediaToDevice(
                                            sourcePathOrUrl = item.mediaData,
                                            mediaType = "VIDEO",
                                            promptTitle = item.prompt.ifBlank { item.title }
                                        )
                                    },
                                    label = { Text("Download MP4") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(14.dp))
                                    }
                                )
                            }
                            if (item.status == "PROCESSING" || item.status == "QUEUED") {
                                AssistChip(
                                    onClick = { viewModel.pollVideoJobStatus(item) },
                                    enabled = !isBusy,
                                    label = { Text("Poll Status") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Refresh, contentDescription = "Poll Status", modifier = Modifier.size(14.dp))
                                    }
                                )
                            }
                            AssistChip(
                                onClick = {
                                    prompt = item.prompt.ifBlank { item.title }
                                    aspectRatio = if (item.aspectRatio == "9:16") "9:16" else "16:9"
                                    viewModel.generateVeoVideo(
                                        prompt = prompt,
                                        aspectRatio = aspectRatio
                                    )
                                },
                                enabled = !isBusy,
                                label = { Text("Regenerate") },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoJobStatusPipelineCard(
    job: com.example.data.remote.VideoOperationResult,
    isPolling: Boolean,
    onPlayFull: () -> Unit,
    onDownload: () -> Unit,
    onRegenerate: () -> Unit,
    onSaveToHistory: () -> Unit
) {
    val stages = listOf("QUEUED", "PROCESSING", "COMPLETED")
    val currentStageIndex = when (job.status.uppercase()) {
        "QUEUED" -> 0
        "PROCESSING" -> 1
        "COMPLETED" -> 2
        else -> -1
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_job_status_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Video Job Status: ${job.status}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (job.status) {
                        "COMPLETED" -> Color(0xFF16A34A).copy(alpha = 0.18f)
                        "FAILED" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = job.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (job.status) {
                            "COMPLETED" -> Color(0xFF15803D)
                            "FAILED" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { idx, stageName ->
                    val reached = currentStageIndex >= idx
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (reached) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                            contentDescription = stageName,
                            tint = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stageName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                            color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (job.status == "QUEUED" || job.status == "PROCESSING" || isPolling) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = job.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (job.status == "COMPLETED" && job.videoUrlOrPath.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                InlineAndroidVideoPlayer(
                    videoPathOrUrl = job.videoUrlOrPath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onPlayFull,
                        modifier = Modifier.testTag("play_video_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play video", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Fullscreen")
                    }
                    FilledTonalButton(
                        onClick = onDownload,
                        modifier = Modifier.testTag("download_video_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download video", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download MP4")
                    }
                    OutlinedButton(
                        onClick = onRegenerate,
                        modifier = Modifier.testTag("regenerate_video_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate video", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Regenerate")
                    }
                    OutlinedButton(
                        onClick = onSaveToHistory,
                        modifier = Modifier.testTag("save_video_history_button")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to History", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to History")
                    }
                }
            }
        }
    }
}

@Composable
fun InlineAndroidVideoPlayer(
    videoPathOrUrl: String,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    val mediaController = MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)
                    val uri = if (videoPathOrUrl.startsWith("http", ignoreCase = true)) {
                        Uri.parse(videoPathOrUrl)
                    } else {
                        Uri.fromFile(File(videoPathOrUrl))
                    }
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        seekTo(100)
                    }
                    setOnCompletionListener {
                        isPlaying = false
                    }
                    videoViewRef = this
                }
            },
            update = { view ->
                videoViewRef = view
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    videoViewRef?.let { vv ->
                        if (vv.isPlaying) {
                            vv.pause()
                            isPlaying = false
                        } else {
                            vv.start()
                            isPlaying = true
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
}

@Composable
fun VideoFrameThumbnailPreview(
    videoPath: String,
    modifier: Modifier = Modifier
) {
    val frameBitmap = remember(videoPath) {
        if (videoPath.isBlank()) null
        else runCatching {
            val file = File(videoPath)
            if (file.exists()) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val bmp = retriever.getFrameAtTime(0)
                retriever.release()
                bmp
            } else null
        }.getOrNull()
    }

    if (frameBitmap != null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Image(
                bitmap = frameBitmap.asImageBitmap(),
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = "Video",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun FullScreenImagePreviewDialog(
    filePath: String,
    prompt: String,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Image Viewer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LocalFileBitmapPreview(
                    filePath = filePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PNG")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerModalDialog(
    videoPathOrUrl: String,
    title: String,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Video Player",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                InlineAndroidVideoPlayer(
                    videoPathOrUrl = videoPathOrUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download MP4")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun LocalFileBitmapPreview(
    filePath: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(filePath) {
        if (filePath.isBlank()) null
        else runCatching {
            val file = File(filePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Generated AI image",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Image placeholder",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun MusicLabStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    latestMusicPath: String?,
    isPlayingAudio: Boolean,
    savedTracks: List<WorkspaceItem>
) {
    var prompt by remember { mutableStateOf("") }
    var useProFullSong by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lyria 3 Music Lab",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Compose 30-second studio loops (lyria-3-clip-preview) or full-length songs with lyrics (lyria-3-pro-preview).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("Describe genre, instruments, BPM, mood, or lyrics...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_studio_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !useProFullSong,
                            onClick = { useProFullSong = false },
                            label = { Text("30s Studio Clip (Lyria 3 Clip)") }
                        )
                        FilterChip(
                            selected = useProFullSong,
                            onClick = { useProFullSong = true },
                            label = { Text("Full Track (Lyria 3 Pro)") }
                        )
                    }

                    Button(
                        onClick = { viewModel.generateMusic(prompt, useProFullSong) },
                        enabled = prompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_music_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Composing Track with Lyria 3...")
                        } else {
                            Icon(Icons.Default.MusicNote, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Music")
                        }
                    }
                }
            }
        }

        if (!latestMusicPath.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Latest Generated Track", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Ready for playback", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                if (isPlayingAudio) {
                                    viewModel.audioVoiceManager.togglePauseResume()
                                } else {
                                    viewModel.audioVoiceManager.playWavFile(latestMusicPath)
                                }
                            }
                        ) {
                            Icon(
                                if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlayingAudio) "Pause" else "Play")
                        }
                    }
                }
            }
        }

        items(savedTracks, key = { it.id }) { track ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(track.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (track.mediaData.isNotBlank()) {
                        OutlinedButton(onClick = { viewModel.audioVoiceManager.playWavFile(track.mediaData) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveVoiceStudioPane(
    viewModel: KalleshHubViewModel,
    isGenerating: Boolean,
    liveVoiceHistory: List<Pair<String, String>>,
    isPlayingAudio: Boolean
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var manualPrompt by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecording = true
            viewModel.startVoiceRecording()
        } else {
            viewModel.showStatus("Microphone permission is needed for Live Voice.")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(44.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.GraphicEq,
                            contentDescription = "Live Voice Orb",
                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Text(
                        text = "Kallesh Live Voice • gemini-3.8-live",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRecording) {
                            "Listening... Tap Stop to transcribe with gemini-3.5-transcribe & speak reply."
                        } else if (isPlayingAudio) {
                            "Kallesh AI is speaking..."
                        } else {
                            "Tap the microphone to talk naturally or type below for spoken AI conversation."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (isRecording) {
                                    isRecording = false
                                    viewModel.stopVoiceRecordingAndTranscribe { transcript ->
                                        viewModel.sendLiveVoiceMessage(transcript)
                                    }
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        isRecording = true
                                        viewModel.startVoiceRecording()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("live_voice_mic_button")
                        ) {
                            Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRecording) "Stop & Send Audio" else "Tap to Speak")
                        }

                        if (isPlayingAudio) {
                            OutlinedButton(onClick = { viewModel.audioVoiceManager.stopPlayback() }) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop Audio")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualPrompt,
                            onValueChange = { manualPrompt = it },
                            placeholder = { Text("Or ask Live Voice via text...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("live_voice_text_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.sendLiveVoiceMessage(manualPrompt)
                                manualPrompt = ""
                            },
                            enabled = manualPrompt.isNotBlank() && !isGenerating,
                            modifier = Modifier.testTag("live_voice_send_button")
                        ) {
                            Text("Speak")
                        }
                    }
                }
            }
        }

        items(liveVoiceHistory) { (role, text) ->
            Surface(
                color = if (role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (role == "user") "You (Spoken)" else "Kallesh Live Voice",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
