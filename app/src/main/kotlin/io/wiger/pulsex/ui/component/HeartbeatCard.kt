package io.wiger.pulsex.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframesWithSpline
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.wiger.pulsex.R
import io.wiger.pulsex.data.local.pref.PulseXState
import io.wiger.pulsex.ui.PulseXIcons
import io.wiger.pulsex.ui.theme.PulseXTheme

@Composable
fun HeartbeatCard(
    sensorName: String,
    macAddress: String,
    state: PulseXState,
    modifier: Modifier = Modifier,
    status: String = stringResource(R.string.heartbeat_normal),
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {}
) {
    val cardBackground = MaterialTheme.colorScheme.surfaceContainerHigh
    val cardGlow = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val accentColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(cardGlow, Color.Transparent),
                        center = Offset(800f, 200f),
                        radius = 600f
                    )
                )
                .padding(24.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge(state is PulseXState.Connected)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = sensorName,
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = macAddress,
                color = onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isConnected = state is PulseXState.Connected
                if (isConnected) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        AnimatedContent(
                            targetState = state.heartbeat,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(
                                    animationSpec = tween(
                                        600
                                    )
                                )
                            },
                            label = "HeartbeatPulsing"
                        ) { targetHeartbeat ->
                            val scale = remember { Animatable(1f) }
                            LaunchedEffect(targetHeartbeat) {
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = keyframesWithSpline {
                                        durationMillis = 2000
                                        1.1f at 500
                                        1f at 1000
                                        1.1f at 1500
                                        1f at 1000
                                    }
                                )
                            }
                            Text(
                                text = targetHeartbeat.toString(),
                                color = accentColor,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                },
                                style = TextStyle(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Medium,
                                    textMotion = TextMotion.Animated
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(
                                text = stringResource(R.string.heartbeat_bpm),
                                color = onSurface.copy(alpha = 0.7f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(400)) + slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            ) {
                                StatusBadge(status = status, accentColor = accentColor)
                            }
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.heartbeat_disconnected),
                        style = MaterialTheme.typography.headlineSmall,
                        color = onSurface.copy(alpha = 0.5f)
                    )
                }

                ConnectionActionButtons(
                    state = state,
                    onConnectClick = onConnectClick,
                    onDisconnectClick = onDisconnectClick
                )
            }
        }
    }
}

@Composable
private fun ConnectionActionButtons(
    state: PulseXState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    if (state is PulseXState.Connected) {
        Button(
            onClick = onDisconnectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.common_disconnect))
        }
    } else if (state is PulseXState.Disconnected) {
        Button(
            onClick = onConnectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = PulseXIcons.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.common_connect))
        }
    }
}

@Composable
private fun LiveBadge(
    isLive: Boolean
) {
    val accentColor =
        if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    val infiniteTransition = rememberInfiniteTransition(label = "LiveBadgePulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LiveBadgeAlpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LiveBadgeScale"
    )

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(accentColor.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.5f))
                )
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        AnimatedContent(
            targetState = isLive,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "LiveBadgeText"
        ) { live ->
            Text(
                text = if (live) stringResource(R.string.heartbeat_live) else stringResource(R.string.heartbeat_disconnected),
                color = accentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LiveBadgePreview() {
    PulseXTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveBadge(true)
            LiveBadge(false)
        }
    }
}

@Composable
private fun StatusBadge(status: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            color = accentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun HeartbeatChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val path = Path()
        val width = size.width
        val height = size.height
        val xStep = width / (data.size - 1)

        data.forEachIndexed { index, value ->
            val x = index * xStep
            val y = height - (value * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw background gradient under path
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw last point glow
        val lastX = width
        val lastY = height - (data.last() * height)
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = 8.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}

@PreviewLightDark
@Composable
private fun HeartbeatCardPreview() {
    PulseXTheme {
        HeartbeatCard(
            stringResource(R.string.unknown_name),
            "12:34:56:78:90",
            PulseXState.Connected(90)
        )
    }
}
