package com.interpark.smframework.util

import android.graphics.Paint
import android.graphics.Rect
import androidx.core.util.rangeTo
import kotlin.math.max
import kotlin.math.min

class TextTextureUtil {
    companion object {
        private const val MAX_LINES:Int = 256
        private val starts:IntArray = IntArray(MAX_LINES)
        private val stops:IntArray = IntArray(MAX_LINES)
        private val bounds:Rect = Rect()

        @JvmStatic
        // those members are stored per instance to minimize
        // the number of allocations to avoid triggering the
        // GC too much
        open fun getDivideString(paint: Paint, text: String, maxWidth: Int, maxLinest: Int, lineCount: IntArray): String? {
            var maxLines = maxLinest
            maxLines = min(max(0, maxLines), MAX_LINES)
            val metrics = paint.fontMetricsInt
            var lines = 0
            var textHeight = 0
            var wasCut = false

            // get maximum number of characters in one line
            paint.getTextBounds("i", 0, 1, bounds)
            val maximumInLine = maxWidth / bounds.width()
            val length = text.length
            if (length > 0) {
                val lineHeight = -metrics.ascent + metrics.descent
                var start = 0
                var stop = if (maximumInLine > length) length else maximumInLine
                while (true) {

                    // skip LF and spaces
                    while (start < length) {
                        val ch = text[start]
                        if (ch != '\n' && ch != '\r' && ch != '\t' && ch != ' '
                        ) break
                        ++start
                    }
                    var o = stop + 1
                    while (stop < o && stop > start) {
                        o = stop
                        var lowest = text.indexOf("\n", start)
                        paint.getTextBounds(text, start, stop, bounds)
                        if (lowest in start until stop || bounds.width() > maxWidth) {
                            --stop
                            if (lowest < start || lowest > stop) {
                                val blank = text.lastIndexOf(" ", stop)
                                val hyphen = text.lastIndexOf("-", stop)
                                if (blank > start && (hyphen < start || blank > hyphen)) lowest = blank else if (hyphen > start) lowest = hyphen
                            }
                            if (lowest in lowest .. stop) {
                                val ch = text[stop]
                                if (ch != '\n' &&
                                    ch != ' '
                                ) ++lowest
                                stop = lowest
                            }
                            continue
                        }
                        break
                    }
                    if (start >= stop) break
                    var minus = 0

                    // cut off lf or space
                    if (stop < length) {
                        val ch = text[stop - 1]
                        if (ch == '\n' ||
                            ch == ' '
                        ) minus = 1
                    }

                    starts[lines] = start
                    stops[lines] = stop - minus
                    if (++lines > maxLines) {
                        wasCut = true
                        break
                    }
                    if (textHeight > 0) textHeight += metrics.leading
                    textHeight += lineHeight
                    if (stop >= length) break
                    start = stop
                    stop = length
                }
            }

            /// 자르기 시작
            var result = ""
            --lines
            for (n in 0..lines) {
                var t: String
                if (result.isNotEmpty()) {
                    if (!result.endsWith("\n")) {
                        result += "\n"
                    }
                }
                if (wasCut && n == lines - 1 && stops[n] - starts[n] > 3
                ) {
                    t = text.substring(
                        starts[n],
                        stops[n] - 3
                    ) + "..."
                    result += t
                    break
                } else {
                    t = text.substring(starts[n], stops[n])
                }
                result += t
            }
            lineCount[0] = lines + 1
            return result
        }
    }


}