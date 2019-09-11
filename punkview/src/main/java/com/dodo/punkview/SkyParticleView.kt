package com.dodo.punkview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class SkyParticleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var mHeight: Int = 0
    private var mWidth: Int = 0

    private var mPaint = Paint()

    private var mPointCenter = PointF()

    private var mPointList = mutableListOf<PointTrackInfo>()
    //控制星星的速度（0 - 1）0：最慢  1：最快
    private var mSpeed = 0f

    //控制是否有星星出现
    private var mFlagRunning = true

    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.WHITE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)

        mPointCenter.x = mWidth * 1f / 2
        mPointCenter.y = mHeight * 1f / 2

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mFlagRunning) {
            addPoint()
            addPoint()
            //速度越大，粒子消耗的越快，需要补充
            if (mSpeed > 0.4) {
                addPoint()
            }
            if (mSpeed > 0.7) {
                addPoint()
            }
        }
        for (point: PointTrackInfo in mPointList) {
            mPaint.alpha = point.getPointAlpha()
            var pointF = point.getPointInfo()
            canvas?.drawCircle(pointF.x, pointF.y, point.pointSize, mPaint)
        }
        if (mPointList.size > 0) {
            invalidate()
        }
    }

    fun start() {
        if (mFlagRunning) {
            return
        }
        mFlagRunning = true
        postInvalidate()
    }

    fun stop() {
        mFlagRunning = false
    }

    /**
     * speed 取值范围（0 - 1）值越大，速度越快
     */
    fun setSpeed(speed: Float) {
        if (mSpeed > 1f || mSpeed < 0) {
            return
        }
        mSpeed = speed
    }

    private fun addPoint() {
        var point = PointTrackInfo(mWidth, mHeight, mSpeed)
        var animator = ValueAnimator.ofFloat(1f)
        animator.duration = point.animationTime
        animator.addUpdateListener {
            point.animatedFraction = it.animatedFraction
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                mPointList.remove(point)
            }
        })
        animator.start()
        mPointList.add(point)
    }

    class PointTrackInfo(width: Int, height: Int, speed: Float) {

        val MAX_TRACK_LENGTH = Math.max(width, height) / 2
        val MIN_TRACK_LENGTH = Math.min(width, height) / 4

        var viewWidth = width
        var viewHeight = height

        //轨迹的方向（0 - 360）
        var trackAngle = 0.0
        //轨迹的长度(最大值为：长宽中较长一边的一半，最小值：长宽中较短一边的1/4)
        var trackLength = 0f
        //最大亮度（201 - 255）
        var maxLight = 0
        //动画时长
        var animationTime = 0L
        //动画进度
        var animatedFraction = 0f
        //点的大小尺寸
        var pointSize = 1f

        init {
            trackLength = MIN_TRACK_LENGTH + (MAX_TRACK_LENGTH - MIN_TRACK_LENGTH) * Random().nextFloat()
            trackAngle = (360 * Random().nextFloat()).toDouble()
            maxLight = Random().nextInt(55) + 200
            animationTime = ((Random().nextInt(1000) + 2000) - (2000 * speed)).toLong()
            pointSize = Random().nextInt(3).toFloat()
        }

        /**
         * 获取当前运动轨迹的点坐标
         */
        fun getPointInfo(): PointF {
            var tempWidth = trackLength * cos(Math.toRadians(trackAngle)) * animatedFraction + viewWidth / 2
            var tempHeight = trackLength * sin(Math.toRadians(trackAngle)) * animatedFraction + viewHeight / 2
            return PointF(tempWidth.toFloat(), tempHeight.toFloat())
        }

        /**
         * 获取当前运动轨迹的点的透明度
         * @return 透明度（0 或者 201 - 255）
         */
        fun getPointAlpha(): Int {
            if (animatedFraction < 0.2) {
                return 0
            }
            return (maxLight * animatedFraction).toInt()
        }
    }

}