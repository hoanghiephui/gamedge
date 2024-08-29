package com.paulrybitskyi.gamedge.common.ui.v2.component

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrybitskyi.gamedge.core.R

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    showKeyBoard: () -> Unit,
    showEmoteBoard: () -> Unit,
    setIconClicked: (Boolean) -> Unit,
    iconClicked: Boolean,
    newFilterMethod: (TextFieldValue) -> Unit,
    actualTextFieldValue: TextFieldValue,
    changeActualTextFieldValue: (String, TextRange) -> Unit
) {

    var input by remember { mutableStateOf(actualTextFieldValue) }
    val textEmpty: Boolean by remember { derivedStateOf { input.text.isEmpty() } }
    LaunchedEffect(actualTextFieldValue) {
        if (actualTextFieldValue.text.isEmpty()) {
            input = actualTextFieldValue
        } else if (iconClicked) {
            input = actualTextFieldValue
        }
    }

    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        ChatTextField(
            modifier = modifier.weight(1f),
            input = actualTextFieldValue,
            empty = textEmpty,
            onValueChange = {
                input = it
                newFilterMethod(it)
                changeActualTextFieldValue(it.text, it.selection)
            },
            showKeyBoard, showEmoteBoard, setIconClicked, iconClicked
        )
    }
}

@Composable
private fun ChatTextField(
    modifier: Modifier = Modifier,
    input: TextFieldValue,
    empty: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    showKeyBoard: () -> Unit,
    showEmoteBoard: () -> Unit,
    setIconClicked: (Boolean) -> Unit,
    iconClicked: Boolean,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val source = remember {
        MutableInteractionSource()
    }
    if (source.collectIsPressedAsState().value && iconClicked) {
        Log.d("TextFieldClicked", "clicked and iconclicked")
        setIconClicked(false)
        showKeyBoard()
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CompositionLocalProvider(LocalContentColor provides LocalContentColor.current) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = circleButtonSize),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        interactionSource = source,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.background
                        ),
                        value = input,
                        onValueChange = onValueChange,
                        maxLines = 5,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.background),
                        decorationBox = { innerTextField ->
                            if (empty) {
                                Text(stringResource(R.string.send_a_message), fontSize = 18.sp)
                            }
                            innerTextField()
                        }
                    )
                }

                if (!iconClicked) {
                    IndicatingIconButton(
                        onClick = {
                            setIconClicked(true)
                            showEmoteBoard()
                        },
                        modifier = Modifier.then(Modifier.size(circleButtonSize)),
                        indication = ripple(bounded = false, radius = circleButtonSize / 2)
                    ) {
                        Icon(imageVector = Icons.Default.Mood, contentDescription = "emoji")
                    }
                } else {
                    IndicatingIconButton(
                        onClick = {
                            setIconClicked(false)
                            showKeyBoard()
                            keyboard?.show()
                        },
                        modifier = Modifier.then(Modifier.size(circleButtonSize)),
                        indication = ripple(bounded = false, radius = circleButtonSize / 2)
                    ) {
                        Icon(
                            modifier = Modifier,
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "attach"
                        )
                    }
                }
            }
        }
    }
}

val circleButtonSize = 44.dp

@Preview
@Preview("dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_C)
@Composable
private fun IndicatingIconButtonPreview() {
    IndicatingIconButton(onClick = {}) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "camera"
        )
    }
}

@Preview
@Preview("dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_C)
@Composable
private fun ChatInputPreview() {
    ChatInput(
        showKeyBoard = {},
        showEmoteBoard = {

        },
        setIconClicked = {

        },
        iconClicked = false,
        newFilterMethod = {},
        actualTextFieldValue = TextFieldValue(),
        changeActualTextFieldValue = { _, _ -> }
    )
}
