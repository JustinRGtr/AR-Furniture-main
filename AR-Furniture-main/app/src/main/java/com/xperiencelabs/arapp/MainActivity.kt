package com.xperiencelabs.arapp

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.ar.node.PlacementMode
import android.widget.Toast
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var setButton: FloatingActionButton
    private val modelFiles = listOf("models/desktop_computer.glb", "models/gaming_chair.glb")
    private var modelIndex = 0
    private var placedModelNodes = mutableListOf<ArModelNode>()
    private var currentModelNode: ArModelNode? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastRotation = 0f
    private var isDragging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView) ?: throw IllegalStateException("SceneView not found")
        placeButton = findViewById(R.id.place) ?: throw IllegalStateException("Place button not found")
        setButton = findViewById(R.id.set) ?: throw IllegalStateException("Set button not found")

        placeButton.setOnClickListener {
            if (modelIndex < modelFiles.size) {
                currentModelNode = loadAndPlaceModel(modelFiles[modelIndex])
            } else {
                showToast("All models have been placed.")
            }
        }

        setButton.setOnClickListener {
            currentModelNode?.let {
                placedModelNodes.add(it)
                currentModelNode = null
                modelIndex++
            }
        }

        sceneView.setOnTouchListener { _, event ->
            if (currentModelNode != null && event.pointerCount == 1) {
                handleTouch(event)
            } else if (event.pointerCount == 2) {
                handleRotation(event)
            }
            true
        }
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                lastTouchX = event.x
                lastTouchY = event.y
                Log.d("ARApp", "Touch down at: x=$lastTouchX, y=$lastTouchY")
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = (event.x - lastTouchX) * 0.005f
                    val deltaZ = (event.y - lastTouchY) * 0.005f

                    val modelNode = currentModelNode ?: placedModelNodes.lastOrNull()
                    if (modelNode != null) {
                        val currentPosition = modelNode.position
                        val newPosition = Position(currentPosition.x + deltaX, currentPosition.y, currentPosition.z - deltaZ)

                        if (newPosition != currentPosition) {
                            modelNode.position = newPosition
                            Log.d("ARApp", "Moved to position: $newPosition")
                        } else {
                            Log.d("ARApp", "Position unchanged")
                        }
                    } else {
                        Log.d("ARApp", "No modelNode found for movement")
                    }

                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
                Log.d("ARApp", "Touch up, drag ended")
            }
        }
    }




    private fun handleRotation(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastRotation = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val rotationDelta = (event.x - lastRotation) / 10
                val currentRotation = placedModelNodes.lastOrNull()?.rotation ?: return
                placedModelNodes.lastOrNull()?.rotation = Rotation(x = currentRotation.x, y = currentRotation.y + rotationDelta, z = currentRotation.z)
                lastRotation = event.x
            }
        }
    }

    private fun loadAndPlaceModel(modelFileLocation: String): ArModelNode {
        val offset = 1.0f * placedModelNodes.size
        val modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = modelFileLocation,
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f + offset, 0f, -offset)
            ) {
                sceneView.planeRenderer.isVisible = true
            }
        }
        sceneView.addChild(modelNode)
        return modelNode
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
