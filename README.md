# SnappyRulerSet

A precision drawing application for Android with ruler, set square, protractor, and compass tools featuring intelligent snapping and real-time measurements.

## Architecture Overview

### State Management
- **CanvasState**: Central state holder managing shapes, undo/redo stacks, and view transformations (scale, translation)
- **ToolState**: Individual tool states (ruler, set squares, protractor, compass) with position, rotation, and active status
- **Shape Hierarchy**: Sealed class system supporting `Line`, `PolyStroke`, and `Circle` with PointF coordinates

### Interaction Model
- **Gesture Handling**: Multi-touch gestures for drawing (one finger), pan/zoom (two fingers), and tool manipulation
- **Snapping System**: Dynamic radius-based snapping to grid intersections, shape endpoints/midpoints, and line intersections
- **Tool Integration**: Overlay-based tools that modify drawing behavior when active

### Rendering Pipeline
- **Compose Canvas**: Hardware-accelerated rendering with cached grid background
- **Path-based Drawing**: Efficient stroke rendering using Android Path objects
- **Real-time Updates**: Live measurement display and visual feedback during interactions

## Snapping Strategy

### Data Structures
- **SnappingManager**: Centralized snap candidate selection with provider-based architecture
- **Spatial Index**: Grid-based spatial partitioning for efficient proximity queries
- **Dynamic Radius**: Zoom-dependent snap tolerance (larger at low zoom, smaller at high zoom)

### Snap Candidates
1. **Grid Intersections**: 5mm spacing converted to device pixels via DPI
2. **Shape Endpoints**: Start/end points of lines and polyline strokes
3. **Shape Midpoints**: Center points of line segments and sampled stroke segments
4. **Line Intersections**: Computed intersections between existing line segments

### Selection Algorithm
```kotlin
fun findSnapCandidate(point: PointF): PointF? {
    val candidates = mutableListOf<PointF>()
    val snapRadius = (24f / zoom).coerceIn(8f, 48f)
    
    // Add grid intersection
    candidates.add(nearestGridPoint(point))
    
    // Add shape endpoints/midpoints
    candidates.addAll(extractShapePoints())
    
    // Add line intersections
    candidates.addAll(computeLineIntersections())
    
    // Return closest candidate within radius
    return candidates.minByOrNull { distance(point, it) }
        ?.takeIf { distance(point, it) <= snapRadius }
}
```

## Performance Optimizations

### Caching Strategy
- **Grid Bitmap**: Pre-rendered 5mm grid cached as offscreen bitmap, regenerated only on size changes
- **Shape Paths**: Converted to Android Path objects for efficient rendering
- **Spatial Index**: Grid-based spatial partitioning for O(1) proximity queries

### Rendering Optimizations
- **Batched Redraws**: Canvas operations grouped to minimize draw calls
- **Hardware Acceleration**: Leverages Compose Canvas hardware acceleration
- **Efficient Geometry**: Fast math operations using optimized algorithms

### Memory Management
- **PointF Usage**: Native Android PointF for coordinate storage
- **Lazy Evaluation**: Snap candidates computed only when needed
- **State Cleanup**: Undo/redo stacks with configurable depth limits

## Calibration Approach

### DPI Conversion
```kotlin
// Convert 5mm to pixels using device DPI
val densityDpi = canvas.density
val spacingPx = (densityDpi * 0.19685f) // 5mm = 0.19685 inches
```

### Measurement Precision
- **Length Display**: 1mm precision with real-time DPI-based conversion
- **Angle Display**: ±0.5° precision with common angle snapping (30°, 45°, 60°, 90°)
- **Grid Alignment**: Automatic alignment to 5mm grid intersections

### Tool Calibration
- **Ruler**: Configurable length with angle snapping to common values
- **Set Squares**: Precise 45° and 30-60-90° triangle templates
- **Protractor**: 10° tick marks with 1° readout precision
- **Compass**: Radius snapping to grid/intersection points

## File Structure

```
/app/src/main/java/com/example/snappyrulerset/
├── ui/
│   ├── CanvasScreen.kt      # Main Compose screen with drawing canvas
│   ├── ToolOverlays.kt      # Ruler, set-square, protractor, compass
│   └── theme/
│       └── HUD.kt           # Precision HUD overlay
├── model/
│   ├── CanvasState.kt       # Shapes, undo/redo stack
│   └── ToolState.kt         # Active tool state
├── snap/
│   ├── SnappingManager.kt   # Snap logic, candidate selection
│   └── SpatialIndex.kt      # Grid-based spatial index
├── geom/
│   └── geometry.kt          # Geometry helpers
└── util/
    └── exporter.kt          # Export to PNG/JPEG
```

## Usage

### Basic Drawing
1. **Freehand**: Single finger drag to draw continuous strokes
2. **Pan/Zoom**: Two finger gestures for navigation
3. **Snapping**: Automatic snapping to grid and existing geometry

### Tool Usage
1. **Ruler**: Drag to move, two-finger rotate, draw along edge
2. **Set Squares**: 45° or 30-60-90° triangles with edge highlighting
3. **Protractor**: Place over vertex, measure angles between rays
4. **Compass**: Tap center, drag radius, draw circles

### Export
- **Share**: Export drawing as PNG with Android share sheet
- **Cache**: Files saved to app cache directory
- **Quality**: High-resolution bitmap export with background

## Performance Targets

- **60 FPS**: Smooth drawing and tool interactions
- **Low Latency**: <16ms frame time for real-time feedback
- **Memory Efficient**: <100MB typical usage for complex drawings
- **Battery Optimized**: Efficient rendering with minimal CPU usage

## Future Enhancements

- **Layer Support**: Multiple drawing layers with visibility controls
- **Advanced Snapping**: Perpendicular, parallel, and tangent snapping
- **Measurement Tools**: Area calculation and dimension annotations
- **Cloud Sync**: Drawing synchronization across devices
- **Custom Tools**: User-defined templates and measurement units
