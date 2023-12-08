package com.xperiencelabs.arapp

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.ar.node.PlacementMode
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var setButton: FloatingActionButton
    private val modelFiles = listOf("models/desktop_computer.glb", "models/gaming_chair.glb")
    private var modelIndex = 0
    private var placedModelNodes = mutableListOf<ArModelNode>() // List to keep track of placed models
    private var currentModelNode: ArModelNode? = null
    private var lastRotation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView) ?: throw IllegalStateException("SceneView not found")
        placeButton = findViewById(R.id.place) ?: throw IllegalStateException("Place button not found")
        setButton = findViewById(R.id.set) ?: throw IllegalStateException("Set button not found")

        placeButton.setOnClickListener {
            if (modelIndex < modelFiles.size) {
                currentModelNode = loadAndPlaceModel(modelFiles[modelIndex])
                // Do not increment modelIndex here. It will be incremented after "Set" is clicked
            } else {
                // Handle the case where there are no more models to place
                showToast("All models have been placed.")
            }
        }

        setButton.setOnClickListener {
            currentModelNode?.let {
                placedModelNodes.add(it)
                currentModelNode = null // Clear the reference to allow a new model to be placed
                modelIndex++ // Ready to place the next model
            }
        }

        sceneView.setOnTouchListener { _, event ->
            handleRotation(event)
            true
        }
    }

    private fun handleRotation(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastRotation = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                if (!placedModelNodes.isEmpty()) {
                    val rotationDelta = (event.x - lastRotation) / 10
                    val currentRotation = placedModelNodes.last().rotation
                    placedModelNodes.last().rotation = Rotation(x = currentRotation.x, y = currentRotation.y + rotationDelta, z = currentRotation.z)
                    lastRotation = event.x
                }
            }
        }
    }

    private fun loadAndPlaceModel(modelFileLocation: String): ArModelNode {
        val offset = 1.5f * placedModelNodes.size // Adjust this offset based on how many models have been placed
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

    // Override onPause and onDestroy if needed...
}
