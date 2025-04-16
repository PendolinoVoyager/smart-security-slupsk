# Object Detection Stream Processor

This Python service performs real-time object detection using OpenCV and a YOLOv8 model. It dynamically discovers and connects to available video streams provided by a streaming server, detects objects in the video feed, and reports significant detections to a central Spring Boot backend.

---

## 🧠 Features

- 🎥 Connects to a streaming server to request live video streams from available devices.
- 📦 Uses a shared Redis database to discover connected and actively streaming devices.
- 🕵️ Performs real-time object detection using YOLOv8 and OpenCV.
- ✅ Filters detections by size and confidence threshold.
- 🚨 Sends structured alerts to a Spring Boot backend via HTTP when notable detections occur.

---


---

## ⚙️ Requirements

- Python 3.8+
- OpenCV
- Ultralytics YOLOv8
- Redis (for device discovery)
- `requests` (for HTTP API calls)

Install dependencies with:

```bash
pip install -r requirements.txt
```

## Usage
```python
python main.py
```
