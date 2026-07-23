# UltraHPM - MLOps & AI Pipeline

UltraHPM takes a hybrid approach to AI integration: Python for training, Java for serving.

## Why this approach?
Python is the undisputed king of ML training (PyTorch, TensorFlow, Pandas). However, serving ML models in a Python Flask/FastAPI app introduces a network hop and HTTP overhead that slows down a high-performance Java microservice architecture. 

To solve this, we train in Python, export the model as a static execution graph (**ONNX**), and run it embedded directly inside our Java Spring Boot process using the ONNX Runtime engine. This provides single-digit millisecond inference latency.

## Architecture

1. **Data Source**: User interaction events (clicks, add_to_cart, purchases) are streamed via Kafka and persisted in the `ultrahpm_recommendations` PostgreSQL database.
2. **Model**: Neural Collaborative Filtering (NCF). It learns latent embeddings for both Users and Products based on historical interactions.
3. **Training Script**: `ml-pipeline/train.py`.
4. **Experiment Tracking**: MLflow.
5. **Automation**: `ml-pipeline/retrain.py` monitors the database and triggers retraining automatically when a threshold of new data is reached.

## MLflow Integration
We use MLflow to track every training run. Because of local Docker constraints on some developer machines, we currently default to the **Local File Backend (SQLite)** for MLflow.
- Tracking URI: `sqlite:///mlflow.db`
- Artifacts: Saved to `./mlruns` directory.

To view the MLflow UI locally, run:
```bash
cd ml-pipeline
mlflow ui --backend-store-uri sqlite:///mlflow.db
```
Navigate to `http://localhost:5000` in your browser.

## The Retraining Lifecycle
1. User events accumulate in PostgreSQL.
2. A cron job or background process executes `retrain.py`.
3. If new events > threshold (e.g., 10,000), `train.py` is invoked.
4. `train.py` reads data, trains the NCF PyTorch model, and logs metrics to MLflow.
5. The model is converted to `recommendation_model.onnx`.
6. The ONNX file is saved to the shared `/models` volume (or object storage in production).
7. The Java Recommendation Service hot-reloads the new `.onnx` file without dropping traffic.

## Online Inference Flow
When a request hits the Recommendation Service:
1. **A/B Testing**: The API Gateway uses weighted routing to direct traffic (e.g., 80% to Model v1, 20% to Model v2) for live experimentation.
2. **ONNX Execution**: The Java service extracts user features and runs the embedded ONNX model to generate latent embeddings and a set of recommended product IDs.
3. **Availability Filtering (Elasticsearch)**: The service queries Elasticsearch to immediately filter out any out-of-stock items from the recommended set.
4. **Hydration (gRPC)**: The remaining valid product IDs are sent to the `product-service` via a high-speed gRPC call to hydrate the full product details (name, price, images).

## Expanding the Pipeline
If you are an ML Engineer looking to improve the models:
- **Feature Engineering**: You can add item metadata (category, price) to the embedding layers to upgrade the model from standard NCF to a hybrid deep learning model (e.g., Two-Tower model).
- **Metrics**: Current evaluations rely on Loss. You can implement offline metrics like NDCG@10 or Hit Rate@10 before exporting the ONNX model to ensure the new model is actually better than the current production model.
