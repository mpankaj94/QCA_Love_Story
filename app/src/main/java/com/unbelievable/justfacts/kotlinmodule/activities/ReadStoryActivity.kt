package com.unbelievable.justfacts.kotlinmodule.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.unbelievable.justfacts.BuildConfig
import com.unbelievable.justfacts.R
import com.unbelievable.justfacts.kotlinmodule.model.ThemePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReadStoryActivity : AppCompatActivity() {

    private var titleText: String = ""
    private var titleDesc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        titleText = intent.getStringExtra("story_title").toString()
        titleDesc = intent.getStringExtra("story_desc").toString()


        setContent {
            ReadStoryScreenUI(titleText, titleDesc)
        }

    }
}

@Composable
fun ReadStoryScreenUI(title: String, desc: String) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val themeStore = remember { ThemePreferences(context) }
    val isNightMode by themeStore.darkModeFlow.collectAsState(initial = false)

    var fontSize by remember { mutableStateOf(16.sp) }
    var isAutoScroll by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(isAutoScroll) {
        while (isAutoScroll) {
            delay(40)
            scrollState.scrollBy(2f)
        }
    }

    val bgColor = if (isNightMode) Color.Black else Color.White
    val txtColor = if (isNightMode) Color.White else Color.Black


    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .background(bgColor)

    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            color = txtColor,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            fontFamily = androidx.compose.ui.text.font.FontFamily(Font(R.font.mukta_regular)),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(8.dp)
        ) {
            Text(
                text = desc,
                fontFamily = androidx.compose.ui.text.font.FontFamily(Font(R.font.mukta_regular)),
                fontSize = fontSize,
                color = txtColor
            )
        }
        ControlBar(
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "$title\n\n$desc")
                }
                context.startActivity(Intent.createChooser(intent, "Share"))
            },
            onFont = {
                fontSize = when (fontSize.value.toInt()) {
                    16 -> 18.sp
                    18 -> 22.sp
                    else -> 16.sp
                }
            },
            onNightMode = {    val newValue = !isNightMode
                scope.launch {
                    themeStore.saveDarkMode(newValue)
                } },
            onAutoScroll = { isAutoScroll = !isAutoScroll }
        )
        AdaptiveAdMobBanner(

            adUnitId = BuildConfig.ADMOB_BANNER_ID
        )
    }
}


@Composable
fun ControlBar(
    onShare: () -> Unit,
    onFont: () -> Unit,
    onNightMode: () -> Unit,
    onAutoScroll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onShare) {
                Icon(
                    painter = painterResource(id = R.drawable.share),
                    contentDescription = null
                )
            }
            IconButton(onClick = onFont) {
                Icon(
                    painter = painterResource(id = R.drawable.font_size),
                    contentDescription = null
                )
            }
            IconButton(onClick = onNightMode) {
                Icon(
                    painter = painterResource(id = R.drawable.brightness),
                    contentDescription = null
                )
            }
            IconButton(onClick = onAutoScroll) {
                Icon(
                    painter = painterResource(id = R.drawable.scroll),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun AdaptiveAdMobBanner(adUnitId: String) {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

    val adView = remember {
        AdView(context).apply {
            setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    context, adWidth
                )
            )
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            adView.destroy()   // ✅ AUTO CLEANUP
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { adView }
    )
}

//@Preview(showBackground = true)
@Composable
fun PrevStory() {
    ReadStoryScreenUI("title", "desc")
}