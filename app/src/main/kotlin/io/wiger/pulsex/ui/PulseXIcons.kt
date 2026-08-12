package io.wiger.pulsex.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object PulseXIcons {
    val BluetoothSearching: ImageVector
        get() = ImageVector.Builder(
            name = "BluetoothSearching",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(360f, 880f)
            lineTo(360f, 576f)
            lineTo(176f, 760f)
            lineTo(120f, 704f)
            lineTo(344f, 480f)
            lineTo(120f, 256f)
            lineTo(176f, 200f)
            lineTo(360f, 384f)
            lineTo(360f, 80f)
            lineTo(400f, 80f)
            lineTo(628f, 308f)
            lineTo(456f, 480f)
            lineTo(628f, 652f)
            lineTo(400f, 880f)
            lineTo(360f, 880f)
            close()
            moveTo(440f, 384f)
            lineTo(516f, 308f)
            lineTo(440f, 234f)
            lineTo(440f, 384f)
            close()
            moveTo(440f, 726f)
            lineTo(516f, 652f)
            lineTo(440f, 576f)
            lineTo(440f, 726f)
            close()
            moveTo(662f, 574f)
            lineTo(570f, 480f)
            lineTo(662f, 388f)
            quadTo(671f, 410f, 676.5f, 433f)
            quadTo(682f, 456f, 682f, 480f)
            quadTo(682f, 504f, 676.5f, 527.5f)
            quadTo(671f, 551f, 662f, 574f)
            close()
            moveTo(780f, 688f)
            lineTo(730f, 640f)
            quadTo(750f, 603f, 761f, 562.5f)
            quadTo(772f, 522f, 772f, 480f)
            quadTo(772f, 438f, 761f, 397.5f)
            quadTo(750f, 357f, 730f, 320f)
            lineTo(780f, 270f)
            quadTo(809f, 318f, 824.5f, 371f)
            quadTo(840f, 424f, 840f, 480f)
            quadTo(840f, 536f, 824.5f, 588.5f)
            quadTo(809f, 641f, 780f, 688f)
            close()
        }.build()

    val BluetoothDisabled: ImageVector
        get() = ImageVector.Builder(
            name = "BluetoothDisabled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(792f, 904f)
            lineTo(624f, 736f)
            lineTo(480f, 880f)
            lineTo(440f, 880f)
            lineTo(440f, 576f)
            lineTo(256f, 760f)
            lineTo(200f, 704f)
            lineTo(396f, 508f)
            lineTo(56f, 168f)
            lineTo(112f, 112f)
            lineTo(848f, 848f)
            lineTo(792f, 904f)
            close()
            moveTo(520f, 726f)
            lineTo(566f, 680f)
            lineTo(520f, 634f)
            lineTo(520f, 726f)
            close()
            moveTo(564f, 452f)
            lineTo(508f, 396f)
            lineTo(596f, 308f)
            lineTo(520f, 234f)
            lineTo(520f, 408f)
            lineTo(440f, 328f)
            lineTo(440f, 80f)
            lineTo(480f, 80f)
            lineTo(708f, 308f)
            lineTo(564f, 452f)
            close()
        }.build()

    val ArrowForward: ImageVector
        get() = ImageVector.Builder(
            name = "ArrowForward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(12f, 4f)
            lineToRelative(-1.41f, 1.41f)
            lineTo(16.17f, 11f)
            lineTo(4f, 11f)
            verticalLineToRelative(2f)
            lineToRelative(12.17f, 0f)
            lineToRelative(-5.58f, 5.59f)
            lineTo(12f, 20f)
            lineToRelative(8f, -8f)
            lineTo(12f, 4f)
            close()
        }.build()

    val Heart: ImageVector
        get() = ImageVector.Builder(
            name = "Heart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(480f, 840f)
            quadToRelative(-18f, 0f, -34.5f, -6.5f)
            quadToRelative(-16.5f, -6.5f, -29.5f, -19.5f)
            lineTo(148f, 545f)
            quadToRelative(-35f, -35f, -51.5f, -80f)
            quadToRelative(-16.5f, -45f, -16.5f, -94f)
            quadToRelative(0f, -103f, 67f, -177f)
            quadToRelative(67f, -74f, 167f, -74f)
            quadToRelative(48f, 0f, 90.5f, 19f)
            quadToRelative(42.5f, 19f, 75.5f, 53f)
            quadToRelative(32f, -34f, 74.5f, -53f)
            quadToRelative(42.5f, -19f, 90.5f, -19f)
            quadToRelative(100f, 0f, 167.5f, 74f)
            quadToRelative(67.5f, 74f, 67.5f, 176f)
            quadToRelative(0f, 49f, -17f, 94f)
            quadToRelative(-17f, 45f, -51f, 80f)
            lineTo(543f, 814f)
            quadToRelative(-13f, 13f, -29f, 19.5f)
            quadToRelative(-16f, 6.5f, -34f, 6.5f)
            close()
            moveTo(520f, 320f)
            quadToRelative(10f, 0f, 19f, 5f)
            quadToRelative(9f, 5f, 14f, 13f)
            lineToRelative(68f, 102f)
            lineTo(787f, 440f)
            quadToRelative(7f, -17f, 10.5f, -34.5f)
            quadToRelative(3.5f, -17.5f, 3.5f, -35.5f)
            quadToRelative(-2f, -69f, -46f, -118.5f)
            quadToRelative(-44f, -49.5f, -110f, -49.5f)
            quadToRelative(-31f, 0f, -59.5f, 12f)
            quadToRelative(-28.5f, 12f, -49.5f, 35f)
            lineToRelative(-27f, 29f)
            quadToRelative(-5f, 6f, -13f, 9.5f)
            quadToRelative(-8f, 3.5f, -16f, 3.5f)
            quadToRelative(-8f, 0f, -16f, -3.5f)
            quadToRelative(-8f, -3.5f, -14f, -9.5f)
            lineToRelative(-27f, -29f)
            quadToRelative(-21f, -23f, -49f, -36f)
            quadToRelative(-28f, -13f, -60f, -13f)
            quadToRelative(-66f, 0f, -110f, 50.5f)
            quadToRelative(-44f, 50.5f, -44f, 119.5f)
            quadToRelative(0f, 18f, 3f, 35.5f)
            quadToRelative(3f, 17.5f, 10f, 34.5f)
            lineTo(360f, 440f)
            quadToRelative(10f, 0f, 19f, 5f)
            quadToRelative(9f, 5f, 14f, 13f)
            lineToRelative(35f, 52f)
            lineToRelative(54f, -162f)
            quadToRelative(4f, -12f, 14.5f, -20f)
            quadToRelative(10.5f, -8f, 23.5f, -8f)
            close()
            moveTo(532f, 450f)
            lineToRelative(-54f, 162f)
            quadToRelative(-4f, 12f, -15f, 20f)
            quadToRelative(-11f, 8f, -24f, 8f)
            quadToRelative(-10f, 0f, -19f, -5f)
            quadToRelative(-9f, -5f, -14f, -13f)
            lineToRelative(-68f, -102f)
            lineToRelative(-102f, 0f)
            lineTo(473f, 757f)
            quadToRelative(2f, 2f, 3.5f, 2.5f)
            quadToRelative(1.5f, 0.5f, 3.5f, 0.5f)
            quadToRelative(2f, 0f, 3.5f, -0.5f)
            quadToRelative(1.5f, -0.5f, 3.5f, -2.5f)
            lineToRelative(236f, -237f)
            lineToRelative(-123f, 0f)
            quadToRelative(-10f, 0f, -19f, -5f)
            quadToRelative(-9f, -5f, -15f, -13f)
            lineToRelative(-34f, -52f)
            close()
        }.build()

    val Stop: ImageVector
        get() = ImageVector.Builder(
            name = "Stop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(240f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(480f)
            verticalLineToRelative(480f)
            horizontalLineTo(240f)
            close()
        }.build()

    val Delete: ImageVector
        get() = ImageVector.Builder(
            name = "Delete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(280f, 840f)
            quadToRelative(-33f, 0f, -56.5f, -23.5f)
            reflectiveQuadTo(200f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(-40f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(-40f)
            horizontalLineToRelative(240f)
            verticalLineToRelative(40f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(80f)
            horizontalLineToRelative(-40f)
            verticalLineToRelative(560f)
            quadToRelative(0f, 33f, -23.5f, 56.5f)
            reflectiveQuadTo(680f, 840f)
            horizontalLineTo(280f)
            close()
            moveTo(680f, 200f)
            horizontalLineTo(280f)
            verticalLineToRelative(560f)
            horizontalLineToRelative(400f)
            verticalLineToRelative(-560f)
            close()
            moveTo(360f, 680f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(-360f)
            horizontalLineToRelative(-80f)
            verticalLineToRelative(360f)
            close()
            moveTo(520f, 680f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(-360f)
            horizontalLineToRelative(-80f)
            verticalLineToRelative(360f)
            close()
        }.build()

    val History: ImageVector
        get() = ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(480f, 880f)
            quadToRelative(-83f, 0f, -156f, -31.5f)
            reflectiveQuadTo(197f, 763f)
            reflectiveQuadTo(111.5f, 636f)
            reflectiveQuadTo(80f, 480f)
            quadToRelative(0f, -83f, 31.5f, -156f)
            reflectiveQuadTo(197f, 197f)
            reflectiveQuadTo(324f, 111.5f)
            reflectiveQuadTo(480f, 80f)
            quadToRelative(83f, 0f, 156f, 31.5f)
            reflectiveQuadTo(763f, 197f)
            reflectiveQuadTo(848.5f, 324f)
            reflectiveQuadTo(880f, 480f)
            quadToRelative(0f, 83f, -31.5f, 156f)
            reflectiveQuadTo(763f, 763f)
            reflectiveQuadTo(636f, 848.5f)
            reflectiveQuadTo(480f, 880f)
            close()
            moveTo(480f, 800f)
            quadToRelative(133f, 0f, 226.5f, -93.5f)
            reflectiveQuadTo(800f, 480f)
            reflectiveQuadTo(706.5f, 253.5f)
            reflectiveQuadTo(480f, 160f)
            reflectiveQuadTo(253.5f, 253.5f)
            reflectiveQuadTo(160f, 480f)
            reflectiveQuadTo(253.5f, 706.5f)
            reflectiveQuadTo(480f, 800f)
            close()
            moveTo(440f, 516f)
            lineTo(610f, 686f)
            lineTo(666f, 630f)
            lineTo(520f, 480f)
            verticalLineToRelative(-240f)
            horizontalLineToRelative(-80f)
            verticalLineToRelative(276f)
            close()
        }.build()

    val Dashboard: ImageVector
        get() = ImageVector.Builder(
            name = "Dashboard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(80f, 440f)
            verticalLineToRelative(-360f)
            horizontalLineToRelative(360f)
            verticalLineToRelative(360f)
            horizontalLineTo(80f)
            close()
            moveTo(520f, 440f)
            verticalLineToRelative(-360f)
            horizontalLineToRelative(360f)
            verticalLineToRelative(360f)
            horizontalLineTo(520f)
            close()
            moveTo(80f, 880f)
            verticalLineToRelative(-360f)
            horizontalLineToRelative(360f)
            verticalLineToRelative(360f)
            horizontalLineTo(80f)
            close()
            moveTo(520f, 880f)
            verticalLineToRelative(-360f)
            horizontalLineToRelative(360f)
            verticalLineToRelative(360f)
            horizontalLineTo(520f)
            close()
            moveTo(160f, 360f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(-200f)
            horizontalLineTo(160f)
            verticalLineToRelative(200f)
            close()
            moveTo(600f, 360f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(-200f)
            horizontalLineTo(600f)
            verticalLineToRelative(200f)
            close()
            moveTo(160f, 800f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(-200f)
            horizontalLineTo(160f)
            verticalLineToRelative(200f)
            close()
            moveTo(600f, 800f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(-200f)
            horizontalLineTo(600f)
            verticalLineToRelative(200f)
            close()
        }.build()

    val ArrowBack: ImageVector
        get() = ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(20f, 11f)
            lineTo(7.83f, 11f)
            lineToRelative(5.59f, -5.59f)
            lineTo(12f, 4f)
            lineToRelative(-8f, 8f)
            lineToRelative(8f, 8f)
            lineToRelative(1.41f, -1.41f)
            lineTo(7.83f, 13f)
            lineTo(20f, 13f)
            verticalLineToRelative(-2f)
            close()
        }.build()

    val Bluetooth: ImageVector
        get() = ImageVector.Builder(
            name = "Bluetooth",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(440f, 880f)
            verticalLineToRelative(-304f)
            lineTo(256f, 760f)
            lineTo(200f, 704f)
            lineTo(424f, 480f)
            lineTo(200f, 256f)
            lineTo(256f, 200f)
            lineTo(440f, 384f)
            verticalLineTo(80f)
            horizontalLineToRelative(40f)
            lineToRelative(228f, 228f)
            lineTo(536f, 480f)
            lineToRelative(172f, 172f)
            lineTo(480f, 880f)
            horizontalLineToRelative(-40f)
            close()
            moveTo(520f, 384f)
            lineTo(596f, 308f)
            lineTo(520f, 232f)
            verticalLineToRelative(152f)
            close()
            moveTo(520f, 728f)
            lineTo(596f, 652f)
            lineTo(520f, 576f)
            verticalLineToRelative(152f)
            close()
        }.build()

    val Settings: ImageVector
        get() = ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).path(fill = SolidColor(Color.White)) {
            moveTo(370f, 880f)
            quadToRelative(-16f, 0f, -29.5f, -10f)
            reflectiveQuadTo(322f, 844f)
            lineToRelative(-18f, -110f)
            quadToRelative(-20f, -8f, -38.5f, -18.5f)
            reflectiveQuadTo(230f, 692f)
            lineToRelative(-102f, 42f)
            quadToRelative(-14f, 7f, -29f, 0.5f)
            reflectiveQuadTo(81f, 712f)
            lineToRelative(-70f, -120f)
            quadToRelative(-8f, -14f, -4f, -29f)
            reflectiveQuadTo(21f, 538f)
            lineToRelative(88f, -68f)
            quadToRelative(-2f, -10f, -2.5f, -20f)
            reflectiveQuadTo(106f, 430f)
            quadToRelative(0f, -10f, 0.5f, -20f)
            reflectiveQuadTo(109f, 390f)
            lineToRelative(-88f, -68f)
            quadToRelative(-14f, -11f, -18f, -26f)
            reflectiveQuadToRelative(4f, -29f)
            lineToRelative(70f, -120f)
            quadToRelative(8f, -14f, 22.5f, -20.5f)
            reflectiveQuadTo(128f, 126f)
            lineToRelative(102f, 42f)
            quadToRelative(17f, -13f, 35.5f, -23.5f)
            reflectiveQuadTo(304f, 126f)
            lineToRelative(18f, -110f)
            quadToRelative(3f, -16f, 16.5f, -26f)
            reflectiveQuadTo(368f, -20f)
            horizontalLineToRelative(140f)
            quadToRelative(16f, 0f, 29.5f, 10f)
            reflectiveQuadTo(554f, 16f)
            lineToRelative(18f, 110f)
            quadToRelative(20f, 8f, 38.5f, 18.5f)
            reflectiveQuadTo(646f, 168f)
            lineToRelative(102f, -42f)
            quadToRelative(14f, -7f, 29f, -0.5f)
            reflectiveQuadTo(795f, 148f)
            lineToRelative(70f, 120f)
            quadToRelative(8f, 14f, 4f, 29f)
            reflectiveQuadTo(855f, 322f)
            lineToRelative(-88f, 68f)
            quadToRelative(2f, 10f, 2.5f, 20f)
            reflectiveQuadTo(770f, 430f)
            quadToRelative(0f, 10f, -0.5f, 20f)
            reflectiveQuadTo(767f, 470f)
            lineToRelative(88f, 68f)
            quadToRelative(14f, 11f, 18f, 26f)
            reflectiveQuadToRelative(-4f, 29f)
            lineToRelative(-70f, 120f)
            quadToRelative(-8f, 14f, -22.5f, 20.5f)
            reflectiveQuadTo(748f, 734f)
            lineToRelative(-102f, -42f)
            quadToRelative(-17f, 13f, -35.5f, 23.5f)
            reflectiveQuadTo(572f, 734f)
            lineToRelative(-18f, 110f)
            quadToRelative(-3f, 16f, -16.5f, 26f)
            reflectiveQuadTo(408f, 880f)
            horizontalLineTo(370f)
            close()
            moveTo(438f, 620f)
            quadToRelative(75f, 0f, 127.5f, -52.5f)
            reflectiveQuadTo(618f, 440f)
            reflectiveQuadTo(565.5f, 312.5f)
            reflectiveQuadTo(438f, 260f)
            reflectiveQuadTo(310.5f, 312.5f)
            reflectiveQuadTo(258f, 440f)
            reflectiveQuadTo(310.5f, 567.5f)
            reflectiveQuadTo(438f, 620f)
            close()
            moveTo(438f, 540f)
            quadToRelative(-42f, 0f, -71f, -29f)
            reflectiveQuadTo(338f, 440f)
            reflectiveQuadTo(367f, 369f)
            reflectiveQuadTo(438f, 340f)
            reflectiveQuadTo(509f, 369f)
            reflectiveQuadTo(538f, 440f)
            reflectiveQuadTo(509f, 511f)
            reflectiveQuadTo(438f, 540f)
            close()
            moveTo(438f, 440f)
            close()
        }.build()
}
