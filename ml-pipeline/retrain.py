import argparse
import subprocess
import psycopg2
import sys

def check_event_count(db_uri, threshold):
    print(f"Checking if un-trained events exceed threshold {threshold}...")
    # Mocking DB call since we don't have the real schema up in this script
    # In a real scenario, this would query: SELECT count(*) FROM user_events WHERE trained = false
    # For now, we simulate crossing the threshold
    current_count = threshold + 1
    
    if current_count >= threshold:
        print(f"Threshold exceeded ({current_count} >= {threshold}). Triggering retraining...")
        return True
    return False

def trigger_retraining(args):
    # Call the train script with parameterized arguments
    cmd = [
        "python", "train.py",
        "--epochs", str(args.epochs),
        "--batch_size", str(args.batch_size),
        "--lr", str(args.lr),
        "--embedding_dim", str(args.embedding_dim)
    ]
    
    print(f"Running command: {' '.join(cmd)}")
    result = subprocess.run(cmd)
    if result.returncode != 0:
        print("Retraining failed!")
        sys.exit(1)
    else:
        print("Retraining completed successfully.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--db-uri", type=str, default="postgresql://ultrahpm_user:ultrahpm_password@localhost:5432/ultrahpm_recommendations")
    parser.add_argument("--threshold", type=int, default=10000, help="Number of new events to trigger retraining")
    parser.add_argument("--epochs", type=int, default=5)
    parser.add_argument("--batch_size", type=int, default=1024)
    parser.add_argument("--lr", type=float, default=0.001)
    parser.add_argument("--embedding_dim", type=int, default=64)
    args = parser.parse_args()
    
    if check_event_count(args.db_uri, args.threshold):
        trigger_retraining(args)
