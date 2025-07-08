import ray
from consts import *

# class representing artifact
@ray.remote
class Artifact(object):
    def __init__(self, artifact_id, artifact_data):
        self.id = artifact_id
        self.data = artifact_data

    
    @ray.method(enable_task_events=False)
    def get_id(self):
        return self.id

    
    @ray.method(enable_task_events=False)
    def split_into_chunks(self):
        chunk_len = len(self.data) // CHUNKS
        chunks = [self.data[i*chunk_len : (i+1)*chunk_len] for i in range(CHUNKS-1)]
        chunks.append(self.data[(CHUNKS-1)*chunk_len : ])
        return chunks
