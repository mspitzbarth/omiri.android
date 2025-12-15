package com.example.omiri.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.input.pointer.positionChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlyerViewerScreen(
    pdfUrl: String,
    storeName: String,
    initialPage: Int = 0,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Global Renderer State protected by Mutex
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    val rendererMutex = remember { Mutex() }
    
    var pageCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = initialPage) // State for bottom pagination row
    
    // Zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom when page changes
    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offset = Offset.Zero
        if (pageCount > 0) {
            listState.animateScrollToItem(pagerState.currentPage)
        }
    }

    // Download and Open PDF
    LaunchedEffect(pdfUrl) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "temp_flyer.pdf")
                // Simple download (in production use a proper downloader or cache lib)
                URL(pdfUrl).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                pdfRenderer = renderer
                pageCount = renderer.pageCount
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                error = if (e is java.io.FileNotFoundException) {
                    "Flyer is no longer available (Expired)."
                } else {
                    "Failed to load flyer: ${e.localizedMessage}"
                }
                isLoading = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pdfRenderer?.close()
        }
    }

    Scaffold(
        topBar = {
            Column {
                // 1. Main Header (Custom to match OmiriHeader height)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = com.example.omiri.ui.theme.Spacing.lg)
                    ) {
                        Spacer(Modifier.height(com.example.omiri.ui.theme.Spacing.xxs))
                        
                        // Header Content Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Back + Store Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier.size(40.dp).offset(x = (-8).dp) // Negative offset to align nicely with padding
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.BrandInk)
                                }
                                
                                Spacer(Modifier.width(0.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.BrandOrange),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Storefront, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = storeName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.BrandInk
                                    )
                                }
                            }

                            // Right: Actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { /* Share */ }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Outlined.Share, "Share", tint = AppColors.BrandInk)
                                }
                                IconButton(onClick = { /* Save */ }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Outlined.BookmarkBorder, "Save", tint = AppColors.BrandInk)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(com.example.omiri.ui.theme.Spacing.xxs))
                    }
                }
                
                // 2. Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page ${pagerState.currentPage + 1} of $pageCount",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.BrandInk
                    )
                    
                    IconButton(
                        onClick = { 
                           val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(pdfUrl))
                           try {
                               context.startActivity(intent)
                           } catch (e: Exception) {
                               // ignore
                           }
                        },
                        modifier = Modifier
                            .background(AppColors.BrandOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .size(36.dp)
                    ) {
                        Icon(Icons.Outlined.FileDownload, "Download", tint = AppColors.BrandOrange, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = AppColors.Border)
            }
        },
        bottomBar = {
            // 3. Pagination Bar
            if (pageCount > 0) {
                Column {
                    HorizontalDivider(color = AppColors.Border)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous
                        IconButton(
                            onClick = { 
                                scope.launch {
                                    if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1) 
                                }
                            },
                            enabled = pagerState.currentPage > 0,
                            modifier = Modifier
                                .run { if(pagerState.currentPage > 0) background(AppColors.Bg, RoundedCornerShape(8.dp)) else this }
                                .size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous", modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        // Page Numbers (Simplified 1..N rendering for now, could act like LazyRow)
                        // Implementing a scrolling row like the design
                        androidx.compose.foundation.lazy.LazyRow(
                            state = listState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(pageCount) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (pagerState.currentPage == index) AppColors.BrandOrange else AppColors.Bg)
                                        .clickable { 
                                            scope.launch { pagerState.animateScrollToPage(index) } 
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (pagerState.currentPage == index) Color.White else AppColors.BrandInk,
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(16.dp))
                        
                        // Next
                        IconButton(
                            onClick = { 
                                scope.launch {
                                    if (pagerState.currentPage < pageCount - 1) pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            enabled = pagerState.currentPage < pageCount - 1,
                            modifier = Modifier
                                .run { if (pagerState.currentPage < pageCount - 1) background(AppColors.Bg, RoundedCornerShape(8.dp)) else this }
                                .size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.Bg) // Light grey background like generic views
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColors.BrandOrange
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SentimentDissatisfied,
                        contentDescription = null,
                        tint = AppColors.MutedText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = error ?: "Unknown error", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.BrandInk
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    // Fallback Action
                    Button(
                        onClick = { 
                           // Launch browser with original URL as last resort, 
                           // although likely dead if 404, but maybe just PDF link changed?
                           // Or direct to store page. 
                           // For now just "Close" or "Retry" ?
                           // User asked for "fallback when pdf can not be found" -> implies an alternative view?
                           // "Open website" is safe.
                           val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(pdfUrl))
                           try {
                               context.startActivity(intent)
                           } catch (e: Exception) {
                               // ignore
                           }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandOrange)
                    ) {
                        Text("Open Link in Browser")
                    }
                }
            } else {
                HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = scale == 1f // Enable pager swipe only when not zoomed
            ) { pageIndex ->
                PdfPage(
                    renderer = pdfRenderer,
                    mutex = rendererMutex,
                    pageIndex = pageIndex,
                    scale = if (pageIndex == pagerState.currentPage) scale else 1f,
                    offset = if (pageIndex == pagerState.currentPage) offset else Offset.Zero,
                    onZoomChange = { s, o ->
                        if (pageIndex == pagerState.currentPage) {
                            scale = s
                            offset = o
                        }
                    }
                )
            }
        }
    }
}
}

@Composable
fun PdfPage(
    renderer: PdfRenderer?,
    mutex: Mutex,
    pageIndex: Int,
    scale: Float,
    offset: Offset,
    onZoomChange: (Float, Offset) -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(renderer, pageIndex) {
        if (renderer == null) return@LaunchedEffect
        
        withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    val page = renderer.openPage(pageIndex)
                    val w = (page.width * 2).coerceAtLeast(1000)
                    val h = (page.height * w / page.width)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmap = bmp
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (bitmap != null) {
        val currentScale by rememberUpdatedState(scale)
        val currentOffset by rememberUpdatedState(offset)
        
        // Only attach gesture detector if we are zoomed in.
        // This allows the HorizontalPager to receive touches when scale is 1f.
        val isZoomed = scale > 1f 

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val pointerCount = event.changes.size
                            
                            // Condition to ignore gesture and let Parent (Pager) handle it:
                            // We are at min scale (1f) AND it is a single pointer (simple drag/swipe).
                            val isSwipeToNav = currentScale <= 1f && pointerCount < 2
                            
                            if (!isSwipeToNav) {
                                // Handle Zoom and Pan
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                val centroid = event.calculateCentroid()
                                
                                if (zoomChange != 1f || panChange != Offset.Zero) {
                                    // Calculate new scale
                                    val newScale = (currentScale * zoomChange).coerceIn(1f, 5f)
                                    
                                    // Calculate new offset
                                    // Logic to keep zoom centered on centroid:
                                    // This simple version just adds pan. 
                                    // Correct pivot logic is complex, sticking to simple pan + center zoom or just pan.
                                    // The user previously accepted "offset + pan".
                                    
                                    // Improved logic: 
                                    // If we are Zooming, we usually want to pivot around centroid.
                                    // But previous code was just `offset + pan`.
                                    // Let's check previous implementation:
                                    // `val newOffset = currentOffset + pan`
                                    // `detectTransformGestures` provides centroid, pan, zoom.
                                    // The previous simple one didn't use centroid.
                                    // `detectTransformGestures` implementation:
                                    //      onGesture(centroid, pan, zoom)
                                    // The user was happy with `offset + pan`.
                                    // Let's stick to that to avoid "fix this, it's buggy" regressions.
                                    
                                    val finalOffset = if (newScale <= 1f) Offset.Zero else currentOffset + panChange
                                    onZoomChange(newScale, finalOffset)
                                    
                                    // Consume events so Pager doesn't see them
                                    event.changes.forEach { 
                                        if (it.positionChanged()) it.consume() 
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale = ContentScale.Fit, // Fits the whole page in the screen
                modifier = Modifier
                    .fillMaxSize() // Use MaxSize so Fit works properly
                    .background(Color.White)
            )
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.BrandOrange)
        }
    }
}

// Helper for shadow since elevation is removed globally but PDFs look better with a card look?
// User said REMOVE ALL UI SHADOWS earlier. I will respect that and use Border or just flat.
// Let's stick to flat or subtle border.
fun Modifier.shadowShim(elevation: androidx.compose.ui.unit.Dp): Modifier {
    // No-op or border, adhering to "Remove All UI Shadows" rule from context
    // The design image shows a slight card effect (rounded corners) for the page ON TOP of the bg.
    // I already have .background(White). I'll add rounded corners to the page image?
    return this 
}
