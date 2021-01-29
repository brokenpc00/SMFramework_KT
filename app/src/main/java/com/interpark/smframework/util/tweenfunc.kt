package com.brokenpc.smframework.util

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class tweenfunc {

    enum class TweenType {
        CUSTOM_EASING, Linear, Sine_EaseIn, Sine_EaseOut, Sine_EaseInOut, Quad_EaseIn, Quad_EaseOut, Quad_EaseInOut, Cubic_EaseIn, Cubic_EaseOut, Cubic_EaseInOut, Quart_EaseIn, Quart_EaseOut, Quart_EaseInOut, Quint_EaseIn, Quint_EaseOut, Quint_EaseInOut, Expo_EaseIn, Expo_EaseOut, Expo_EaseInOut, Circ_EaseIn, Circ_EaseOut, Circ_EaseInOut, Elastic_EaseIn, Elastic_EaseOut, Elastic_EaseInOut, Back_EaseIn, Back_EaseOut, Back_EaseInOut, Bounce_EaseIn, Bounce_EaseOut, Bounce_EaseInOut
    }

    companion object {
        const val M_PI = Math.PI
        const val M_PI_2 = Math.PI / 2
        const val M_PI_4 = Math.PI / 4
        const val M_1_PI = 1 / Math.PI
        const val M_2_PI = 2 / Math.PI
        const val M_PI_X_2 = M_PI * 2.0f

        @JvmStatic
        fun tweenTo(time: Float, type: TweenType, easingParam: FloatArray?): Float {

            return when (type) {
                TweenType.CUSTOM_EASING -> customEase(time, easingParam)
                TweenType.Linear -> linear(time)
                TweenType.Sine_EaseIn -> sineEaseIn(time)
                TweenType.Sine_EaseOut -> sineEaseOut(time)
                TweenType.Sine_EaseInOut -> sineEaseInOut(time)
                TweenType.Quad_EaseIn -> quadEaseIn(time)
                TweenType.Quad_EaseOut -> quadEaseOut(time)
                TweenType.Quad_EaseInOut -> quadEaseInOut(time)
                TweenType.Cubic_EaseIn -> cubicEaseIn(time)
                TweenType.Cubic_EaseOut -> cubicEaseOut(time)
                TweenType.Cubic_EaseInOut -> cubicEaseInOut(time)
                TweenType.Quart_EaseIn -> quartEaseIn(time)
                TweenType.Quart_EaseOut -> quartEaseOut(time)
                TweenType.Quart_EaseInOut -> quartEaseInOut(time)
                TweenType.Quint_EaseIn -> quintEaseIn(time)
                TweenType.Quint_EaseOut -> quintEaseOut(time)
                TweenType.Quint_EaseInOut -> quintEaseInOut(time)
                TweenType.Expo_EaseIn -> expoEaseIn(time)
                TweenType.Expo_EaseOut -> expoEaseOut(time)
                TweenType.Expo_EaseInOut -> expoEaseInOut(time)
                TweenType.Circ_EaseIn -> circEaseIn(time)
                TweenType.Circ_EaseOut -> circEaseOut(time)
                TweenType.Circ_EaseInOut -> circEaseInOut(time)
                TweenType.Elastic_EaseIn -> {
                    var period = 0.3f
                    if (null != easingParam) {
                        period = easingParam[0]
                    }
                    elasticEaseIn(time, period)
                }
                TweenType.Elastic_EaseOut -> {
                    var period = 0.3f
                    if (null != easingParam) {
                        period = easingParam[0]
                    }
                    elasticEaseOut(time, period)
                }
                TweenType.Elastic_EaseInOut -> {
                    var period = 0.3f
                    if (null != easingParam) {
                        period = easingParam[0]
                    }
                    elasticEaseInOut(time, period)
                }
                TweenType.Back_EaseIn -> backEaseIn(time)
                TweenType.Back_EaseOut -> backEaseOut(time)
                TweenType.Back_EaseInOut -> backEaseInOut(time)
                TweenType.Bounce_EaseIn -> bounceEaseIn(time)
                TweenType.Bounce_EaseOut -> bounceEaseOut(time)
                TweenType.Bounce_EaseInOut -> bounceEaseInOut(time)
                else -> sineEaseInOut(time)
            }
        }

        @JvmStatic
        fun linear(time: Float): Float {
            return time
        }

        @JvmStatic
        fun sineEaseIn(time: Float): Float {
            return -1 * cos(time * M_PI_2).toFloat() + 1
        }

        @JvmStatic
        fun sineEaseOut(time: Float): Float {
            return sin(time * M_PI_2).toFloat()
        }

        @JvmStatic
        fun sineEaseInOut(time: Float): Float {
            return -0.5f * (Math.cos(M_PI.toFloat() * time.toDouble()).toFloat() - 1)
        }

        @JvmStatic
        fun quadEaseIn(time: Float): Float {
            return time * time
        }

        @JvmStatic
        fun quadEaseOut(time: Float): Float {
            return -1 * time * (time - 2)
        }

        @JvmStatic
        fun quadEaseInOut(time: Float): Float {
            var time = time
            time = time * 2
            if (time < 1) return 0.5f * time * time
            --time
            return -0.5f * (time * (time - 2) - 1)
        }

        @JvmStatic
        fun cubicEaseIn(time: Float): Float {
            return time * time * time
        }

        @JvmStatic
        fun cubicEaseOut(time: Float): Float {
            var time = time
            time -= 1f
            return time * time * time + 1
        }

        @JvmStatic
        fun cubicEaseInOut(time: Float): Float {
            var time = time
            time = time * 2
            if (time < 1) return 0.5f * time * time * time
            time -= 2f
            return 0.5f * (time * time * time + 2)
        }

        @JvmStatic
        fun quartEaseIn(time: Float): Float {
            return time * time * time * time
        }

        @JvmStatic
        fun quartEaseOut(time: Float): Float {
            var time = time
            time -= 1f
            return -(time * time * time * time - 1)
        }

        @JvmStatic
        fun quartEaseInOut(time: Float): Float {
            var time = time
            time = time * 2
            if (time < 1) return 0.5f * time * time * time * time
            time -= 2f
            return -0.5f * (time * time * time * time - 2)
        }

        @JvmStatic
        fun quintEaseIn(time: Float): Float {
            return time * time * time * time * time
        }

        @JvmStatic
        fun quintEaseOut(time: Float): Float {
            var time = time
            time -= 1f
            return time * time * time * time * time + 1
        }

        @JvmStatic
        fun quintEaseInOut(time: Float): Float {
            var time = time
            time = time * 2
            if (time < 1) return 0.5f * time * time * time * time * time
            time -= 2f
            return 0.5f * (time * time * time * time * time + 2)
        }

        @JvmStatic
        fun expoEaseIn(time: Float): Float {
            return if (time == 0f) 0f else Math.pow(
                2.0,
                10 * (time / 1 - 1).toDouble()
            ).toFloat() - 1 * 0.001f
        }

        @JvmStatic
        fun expoEaseOut(time: Float): Float {
            return if (time == 1f) 1f else ((-2.0).pow(-10 * time / 1.toDouble())).toFloat() + 1f
        }

        @JvmStatic
        fun expoEaseInOut(time: Float): Float {
            if (time == 0f || time == 1f) return time
            return if (time < 0.5f) 0.5f * Math.pow(
                2.0,
                10 * (time * 2 - 1).toDouble()
            ).toFloat() else 0.5f * ((-Math.pow(
                2.0,
                -10 * (time * 2 - 1).toDouble()
            )).toFloat() + 2)
        }

        @JvmStatic
        fun circEaseIn(time: Float): Float {
            return -1 * (Math.sqrt(1 - time * time.toDouble()).toFloat() - 1)
        }

        @JvmStatic
        fun circEaseOut(time: Float): Float {
            var time = time
            time = time - 1
            return Math.sqrt(1 - time * time.toDouble()).toFloat()
        }

        @JvmStatic
        fun circEaseInOut(time: Float): Float {
            var time = time
            time = time * 2
            if (time < 1) return -0.5f * (Math.sqrt(1 - time * time.toDouble()).toFloat() - 1)
            time -= 2f
            return 0.5f * (Math.sqrt(1 - time * time.toDouble()).toFloat() + 1)
        }

        @JvmStatic
        fun elasticEaseIn(time: Float, period: Float): Float {
            var time = time
            var newT = 0f
            if (time == 0f || time == 1f) {
                newT = time
            } else {
                val s = period / 4
                time = time - 1
                newT =
                    (-Math.pow(2.0, 10 * time.toDouble())).toFloat() * Math.sin(
                        (time - s) * M_PI_X_2.toFloat() / period.toDouble()
                    ).toFloat()
            }
            return newT
        }

        @JvmStatic
        fun elasticEaseOut(time: Float, period: Float): Float {
            var newT = 0f
            newT = if (time == 0f || time == 1f) {
                time
            } else {
                val s = period / 4
                Math.pow(2.0, -10 * time.toDouble()).toFloat() * Math.sin(
                    (time - s) * M_PI_X_2 / period
                ).toFloat() + 1
            }
            return newT
        }

        @JvmStatic
        fun elasticEaseInOut(time: Float, period: Float): Float {
            var time = time
            var period = period
            var newT = 0f
            if (time == 0f || time == 1f) {
                newT = time
            } else {
                time = time * 2
                if (period == 0f) {
                    period = 0.3f * 1.5f
                }
                val s = period / 4
                time = time - 1
                newT = if (time < 0) {
                    -0.5f * Math.pow(
                        2.0,
                        10 * time.toDouble()
                    ).toFloat() * Math.sin((time - s) * M_PI_X_2 / period).toFloat()
                } else {
                    Math.pow(
                        2.0,
                        -10 * time.toDouble()
                    ).toFloat() * Math.sin((time - s) * M_PI_X_2 / period).toFloat() * 0.5f + 1
                }
            }
            return newT
        }

        @JvmStatic
        fun backEaseIn(time: Float): Float {
            val overshoot = 1.70158f
            return time * time * ((overshoot + 1) * time - overshoot)
        }

        @JvmStatic
        fun backEaseOut(time: Float): Float {
            var time = time
            val overshoot = 1.70158f
            time = time - 1
            return time * time * ((overshoot + 1) * time + overshoot) + 1
        }

        @JvmStatic
        fun backEaseInOut(time: Float): Float {
            var time = time
            val overshoot = 1.70158f * 1.525f
            time = time * 2
            return if (time < 1) {
                time * time * ((overshoot + 1) * time - overshoot) / 2
            } else {
                time = time - 2
                time * time * ((overshoot + 1) * time + overshoot) / 2 + 1
            }
        }

        @JvmStatic
        fun bounceTime(time: Float): Float {
            var time = time
            if (time < 1 / 2.75f) {
                return 7.5625f * time * time
            } else if (time < 2 / 2.75f) {
                time -= 1.5f / 2.75f
                return 7.5625f * time * time + 0.75f
            } else if (time < 2.5f / 2.75f) {
                time -= 2.25f / 2.75f
                return 7.5625f * time * time + 0.9375f
            }
            time -= 2.625f / 2.75f
            return 7.5625f * time * time + 0.984375f
        }

        @JvmStatic
        fun bounceEaseIn(time: Float): Float {
            return 1 - bounceTime(1 - time)
        }

        @JvmStatic
        fun bounceEaseOut(time: Float): Float {
            return bounceTime(time)
        }

        @JvmStatic
        fun bounceEaseInOut(time: Float): Float {
            var time = time
            var newT = 0f
            if (time < 0.5f) {
                time = time * 2
                newT = (1 - bounceTime(1 - time)) * 0.5f
            } else {
                newT = bounceTime(time * 2 - 1) * 0.5f + 0.5f
            }
            return newT
        }

        @JvmStatic
        fun customEase(time: Float, easingParam: FloatArray?): Float {
            if (easingParam != null && easingParam.size > 7) {
                val tt = 1 - time
                return easingParam[1] * tt * tt * tt + 3 * easingParam[3] * time * tt * tt + 3 * easingParam[5] * time * time * tt + easingParam[7] * time * time * time
            }
            return time
        }

        @JvmStatic
        fun easeIn(time: Float, rate: Float): Float {
            return Math.pow(time.toDouble(), rate.toDouble()).toFloat()
        }

        @JvmStatic
        fun easeOut(time: Float, rate: Float): Float {
            return Math.pow(time.toDouble(), 1 / rate.toDouble()).toFloat()
        }

        @JvmStatic
        fun easeInOut(time: Float, rate: Float): Float {
            var time = time
            time *= 2f
            return if (time < 1) {
                0.5f * Math.pow(time.toDouble(), rate.toDouble()).toFloat()
            } else {
                1.0f - 0.5f * Math.pow(
                    2 - time.toDouble(),
                    rate.toDouble()
                ).toFloat()
            }
        }

        @JvmStatic
        fun quadraticIn(time: Float): Float {
            return Math.pow(time.toDouble(), 2.0).toFloat()
        }

        @JvmStatic
        fun quadraticOut(time: Float): Float {
            return -time * (time - 2)
        }

        @JvmStatic
        fun quadraticInOut(time: Float): Float {
            var time = time
            var resultTime = time
            time = time * 2
            resultTime = if (time < 1) {
                time * time * 0.5f
            } else {
                --time
                -0.5f * (time * (time - 2) - 1)
            }
            return resultTime
        }

        @JvmStatic
        fun bezieratFunction(
            a: Float,
            b: Float,
            c: Float,
            d: Float,
            t: Float
        ): Float {
            return Math.pow(
                1 - t.toDouble(),
                3.0
            ).toFloat() * a + 3 * t * Math.pow(
                1 - t.toDouble(),
                2.0
            ).toFloat() * b + 3 * Math.pow(
                t.toDouble(),
                2.0
            ).toFloat() * (1 - t) * c + Math.pow(
                t.toDouble(),
                3.0
            ).toFloat() * d
        }
    }


}