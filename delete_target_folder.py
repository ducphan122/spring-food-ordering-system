import os
import shutil

def delete_target_folders(root_dir):
    for dirpath, dirnames, filenames in os.walk(root_dir, topdown=False):
        if 'target' in dirnames:
            target_path = os.path.join(dirpath, 'target')
            try:
                shutil.rmtree(target_path)
                print(f"Deleted: {target_path}")
            except Exception as e:
                print(f"Error deleting {target_path}: {e}")
        
        # Remove 'target' from dirnames to prevent os.walk from trying to enter it
        dirnames[:] = [d for d in dirnames if d != 'target']

if __name__ == "__main__":
    root_directory = "."  # Current directory, change this if needed
    delete_target_folders(root_directory)
    print("Finished deleting target folders.")