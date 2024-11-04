import os
import subprocess

def create_directories():
    # Start from current directory and move to volumes
    volumes_path = os.path.join(os.getcwd(), 'volumes')
    os.makedirs(volumes_path, exist_ok=True)
    os.chdir(volumes_path)

    # First remove existing kafka and zookeeper directories using nmuidi command
    subprocess.run(['nmuidi', 'kafka'], shell=True)
    subprocess.run(['nmuidi', 'zookeeper'], shell=True)

    # Create kafka directory and its broker subdirectories
    kafka_path = os.path.join(volumes_path, 'kafka')
    os.makedirs(kafka_path, exist_ok=True)
    
    # Create broker directories
    for i in range(1, 4):
        broker_path = os.path.join(kafka_path, f'broker-{i}')
        os.makedirs(broker_path, exist_ok=True)

    # Create zookeeper directory and its subdirectories
    zookeeper_path = os.path.join(volumes_path, 'zookeeper')
    os.makedirs(zookeeper_path, exist_ok=True)
    
    # Create zookeeper subdirectories
    os.makedirs(os.path.join(zookeeper_path, 'data'), exist_ok=True)
    os.makedirs(os.path.join(zookeeper_path, 'transactions'), exist_ok=True)

if __name__ == "__main__":
    create_directories()