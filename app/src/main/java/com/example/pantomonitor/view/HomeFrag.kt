package com.example.pantomonitor.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pantomonitor.databinding.FragmentHomeBinding
import com.example.pantomonitor.viewmodel.BdMainViewModel
import com.example.pantomonitor.viewmodel.BdViewModelFactoy
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar


class HomeFrag : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: BdMainViewModel
    private lateinit var pieChart: PieChart
    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)  // Inflate using ViewBinding
        viewModel = ViewModelProvider(this, BdViewModelFactoy()).get(BdMainViewModel::class.java)
        pieChart = binding.pieChart

        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false
        val legend = pieChart.legend
        legend.isEnabled = false

        pieChart.isRotationEnabled = false
        pieChart.minAngleForSlices = 10f
        pieChart.holeRadius = 60f
        pieChart.setTransparentCircleAlpha(255)


        // GOOD
        viewModel.getGoodData().observe(viewLifecycleOwner, Observer { data ->
            binding.tvgoodcounter.text = data.toString()
        })
        // DEFECT
        viewModel.getDefectData().observe(viewLifecycleOwner, Observer { data ->
            binding.tvdefectcounter.text = data.toString()
        })
        //TOTAL
        viewModel.gettotalcounter().observe(viewLifecycleOwner, Observer { data ->
            binding.totalnumEntries.text = data.toString()
        })

        // Pie chart settings
        viewModel.pieChartData.observe(viewLifecycleOwner) { entries ->
            setupPieChart(entries)
        }

        //TRAIN NO
        viewModel.gettrainno().observe(viewLifecycleOwner) { data ->
            binding.Trainno.text = data.toString()
        }
        //CART NO
        viewModel.getcartno().observe(viewLifecycleOwner) { data ->
            binding.cartno.text = data.toString()
        }
        //STATUS
        viewModel.getlatestStatus().observe(viewLifecycleOwner) { data ->
            binding.statusview.text = data.toString()
        }
        //DATE
        viewModel.getlatestDate().observe(viewLifecycleOwner) { data ->

            val date = data.toLong() * 1000L
            val dateFormat = SimpleDateFormat("MM-dd-yyyy")
            binding.dateview.text = dateFormat.format(date)
        }
        viewModel.getlatestTime().observe(viewLifecycleOwner) { data ->
            binding.timeview.text = data.toString()
        }
        viewModel.getlatestImg().observe(viewLifecycleOwner) { data ->
            var imageRef = viewModel.getlatestpic(data)
            imageRef.downloadUrl.addOnSuccessListener { uri: Uri? ->
                Picasso.get().load(uri).into(binding.imageView)

                binding.imageView.setOnClickListener{
                    zoomImageFromThumb(binding.imageView, uri)
                }
                shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
















            }.addOnFailureListener { exception: Exception? -> }

        }









        //DAILY ANALYTICS
        viewModel.getdgood().observe(viewLifecycleOwner, Observer { data1 ->
            binding.Dgood.text = data1.toString()

            viewModel.getdbad().observe(viewLifecycleOwner, Observer { data2 ->
                var currentDated = Calendar.getInstance()
                var dateFormat = SimpleDateFormat("MMMM dd, yyyy")
                var current = dateFormat.format(currentDated.time).toString()


                var total = data1 + data2
                var good = data1.toInt()
                var bad = data2.toInt()
                binding.Dbad.text = data2.toString()

                binding.tvgoodcounterdaily.text = good.toString()
                binding.tvdefectcounterdaily.text = bad.toString()
                binding.totacounterdaily.text = total.toString()
                binding.datedaily.text = current



                if ( total== 0 ){
                    binding.dailyconclu.text = "The data reveals that,there are zero records for both operational and replacement carbon strips. Please provide additional data for further analysis."

                }
                else if (good > bad){
                    binding.dailyconclu.text = "The records indicate that the ratio between functional and replacement-worthy carbon strips is within normal boundaries."
                }
                else if (good < bad) {
                    binding.dailyconclu.text =  "The records suggest an abnormal ratio between functional and replacement carbon strips, with a notably high amount of inoperative carbon strips."
                }

                else if (bad == 0){
                    binding.dailyconclu.text = "The records indicate an abnormal ratio between operational and replacement carbon strips, as there are currently no records marked for replacement"
                }
            })
        })
        return binding.root
    }


    private fun setupPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "Daily Assessment")
        dataSet.colors = mutableListOf(Color.rgb(241, 201, 59), Color.rgb(159, 187, 115))
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 5f
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }



    private fun zoomImageFromThumb(thumbView: View, imageResId: Uri?) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        Picasso.get().load(imageResId).into(binding.imageView9)

        // Calculate the starting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the
        // container view. Set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        binding.container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Using the "center crop" technique, adjust the start bounds to be the
        // same aspect ratio as the final bounds. This prevents unwanted
        // stretching during the animation. Calculate the start scaling factor.
        // The end scaling factor is always 1.0.
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally.
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically.
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it positions the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f

        animateZoomToLargeImage(startBounds, finalBounds, startScale)

        setDismissLargeImageAnimation(thumbView, startBounds, startScale)
    }

    private fun animateZoomToLargeImage(startBounds: RectF, finalBounds: RectF, startScale: Float) {
        binding.imageView9.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of the zoomed-in view. The default is the center of
        // the view.
        binding.imageView9.pivotX = 0f
        binding.imageView9.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties: X, Y, SCALE_X, and SCALE_Y.
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    binding.imageView9,
                    View.X,
                    startBounds.left,
                    finalBounds.left)
            ).apply {
                with(ObjectAnimator.ofFloat(binding.imageView9, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(binding.imageView9, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(binding.imageView9, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }
    }

    private fun setDismissLargeImageAnimation(thumbView: View, startBounds: RectF, startScale: Float) {
        // When the zoomed-in image is tapped, it zooms down to the original
        // bounds and shows the thumbnail instead of the expanded image.
        binding.imageView9.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning and sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(binding.imageView9, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(binding.imageView9, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(binding.imageView9, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(binding.imageView9, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.imageView9.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.imageView9.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }





}



