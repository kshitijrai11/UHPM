import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import pandas as pd
import numpy as np
import mlflow
import mlflow.pytorch
import argparse
import os

class NCFModel(nn.Module):
    def __init__(self, num_users, num_items, embedding_dim=64):
        super(NCFModel, self).__init__()
        self.user_embedding = nn.Embedding(num_embeddings=num_users, embedding_dim=embedding_dim)
        self.item_embedding = nn.Embedding(num_embeddings=num_items, embedding_dim=embedding_dim)
        
        self.fc_layers = nn.Sequential(
            nn.Linear(embedding_dim * 2, 128),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(128, 64),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(64, 32),
            nn.ReLU(),
            nn.Linear(32, 1),
            nn.Sigmoid()
        )
        
    def forward(self, user_indices, item_indices):
        user_embedding = self.user_embedding(user_indices)
        item_embedding = self.item_embedding(item_indices)
        
        vector = torch.cat([user_embedding, item_embedding], dim=-1)
        return self.fc_layers(vector).squeeze()

class InteractionDataset(Dataset):
    def __init__(self, user_tensor, item_tensor, label_tensor):
        self.user_tensor = user_tensor
        self.item_tensor = item_tensor
        self.label_tensor = label_tensor
        
    def __len__(self):
        return len(self.user_tensor)
    
    def __getitem__(self, idx):
        return self.user_tensor[idx], self.item_tensor[idx], self.label_tensor[idx]

def generate_synthetic_data(num_users=1000, num_items=5000, num_interactions=100000):
    # This is a placeholder for actual PostgreSQL data loading
    print(f"Generating synthetic data: {num_users} users, {num_items} items, {num_interactions} interactions")
    users = np.random.randint(0, num_users, size=num_interactions)
    items = np.random.randint(0, num_items, size=num_interactions)
    labels = np.random.randint(0, 2, size=num_interactions).astype(np.float32)
    
    return torch.tensor(users, dtype=torch.long), torch.tensor(items, dtype=torch.long), torch.tensor(labels, dtype=torch.float32)

def train(args):
    mlflow.set_tracking_uri("sqlite:///mlflow.db")
    mlflow.set_experiment("ultrahpm-recommendations")
    
    # Generate data
    num_users = 1000
    num_items = 5000
    users, items, labels = generate_synthetic_data(num_users, num_items, args.num_samples)
    
    dataset = InteractionDataset(users, items, labels)
    train_size = int(0.8 * len(dataset))
    val_size = len(dataset) - train_size
    train_dataset, val_dataset = torch.utils.data.random_split(dataset, [train_size, val_size])
    
    train_loader = DataLoader(train_dataset, batch_size=args.batch_size, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=args.batch_size, shuffle=False)
    
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    model = NCFModel(num_users=num_users, num_items=num_items, embedding_dim=args.embedding_dim).to(device)
    criterion = nn.BCELoss()
    optimizer = optim.Adam(model.parameters(), lr=args.lr)
    
    with mlflow.start_run():
        mlflow.log_param("embedding_dim", args.embedding_dim)
        mlflow.log_param("batch_size", args.batch_size)
        mlflow.log_param("epochs", args.epochs)
        mlflow.log_param("lr", args.lr)
        
        for epoch in range(args.epochs):
            model.train()
            total_loss = 0
            for u, i, l in train_loader:
                u, i, l = u.to(device), i.to(device), l.to(device)
                optimizer.zero_grad()
                output = model(u, i)
                loss = criterion(output, l)
                loss.backward()
                optimizer.step()
                total_loss += loss.item()
                
            avg_train_loss = total_loss / len(train_loader)
            
            # Validation
            model.eval()
            val_loss = 0
            with torch.no_grad():
                for u, i, l in val_loader:
                    u, i, l = u.to(device), i.to(device), l.to(device)
                    output = model(u, i)
                    loss = criterion(output, l)
                    val_loss += loss.item()
            
            avg_val_loss = val_loss / len(val_loader)
            print(f"Epoch {epoch+1}/{args.epochs} - Train Loss: {avg_train_loss:.4f} - Val Loss: {avg_val_loss:.4f}")
            
            mlflow.log_metric("train_loss", avg_train_loss, step=epoch)
            mlflow.log_metric("val_loss", avg_val_loss, step=epoch)
        
        # Export to ONNX
        dummy_u = torch.zeros(1, dtype=torch.long).to(device)
        dummy_i = torch.zeros(1, dtype=torch.long).to(device)
        
        os.makedirs("models", exist_ok=True)
        onnx_path = "models/recommendation_model.onnx"
        
        torch.onnx.export(
            model, 
            (dummy_u, dummy_i), 
            onnx_path, 
            export_params=True, 
            opset_version=14, 
            do_constant_folding=True, 
            input_names=['user_id', 'item_id'], 
            output_names=['score'], 
            dynamic_axes={'user_id': {0: 'batch_size'}, 'item_id': {0: 'batch_size'}, 'score': {0: 'batch_size'}}
        )
        
        print(f"Model exported to {onnx_path}")
        mlflow.log_artifact(onnx_path)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--epochs", type=int, default=5)
    parser.add_argument("--batch_size", type=int, default=1024)
    parser.add_argument("--lr", type=float, default=0.001)
    parser.add_argument("--embedding_dim", type=int, default=64)
    parser.add_argument("--num_samples", type=int, default=100000)
    args = parser.parse_args()
    
    train(args)
