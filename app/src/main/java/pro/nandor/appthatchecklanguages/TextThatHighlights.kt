package pro.nandor.appthatchecklanguages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle


@Composable
fun TextThatHighlights(text: String, modifier: Modifier = Modifier){

    // find bold tags
    val startIndex = text.indexOf("<b>")
    val endIndex = text.indexOf("</b>")

    // if no bold tags, we use a regular text instead
    if (startIndex == -1 || endIndex == -1){
        Text(text, modifier = modifier)
        return
    }

    // apply a style to the tags' content
    val annotatedString = buildAnnotatedString {
        append(text.substring(0, startIndex))

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
            append(text.substring(startIndex + 3, endIndex))
        }

        append(text.substring(endIndex + 4))
    }

    Text(annotatedString, modifier = modifier)


}