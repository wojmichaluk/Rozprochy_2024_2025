import ray
from random import random
from time import sleep
from consts import *

# data node
@ray.remote(max_task_retries=1)
class DataNode(object):
    def __init__(self, node_id, idx):
        self.id = node_id
        self.idx = idx
        self.state = READY
        self.chunks = dict()

    
    @ray.method(enable_task_events=False)
    def get_id(self):
        return self.id


    @ray.method(enable_task_events=False)
    def get_idx(self):
        return self.idx

    
    @ray.method(enable_task_events=False)
    def get_state(self):
        return self.state

    
    @ray.method(enable_task_events=False)
    def comp(self):
        return len(self.chunks)

    
    def add_chunk(self, artifact_id, chunk_no, chunk):
        if self.state == READY:
            self.chunks[(artifact_id, chunk_no)] = chunk
    
            if random() < FAILURE_PROB:
                self.state = FAULTY

    
    def update_chunk(self, artifact_id, chunk_no, chunk):
        if self.state == READY:
            self.chunks[(artifact_id, chunk_no)] = chunk
    
            if random() < FAILURE_PROB:
                self.state = FAULTY

    
    def get_chunk(self, artifact_id, chunk_no):
        if self.state == READY:
            chunk = self.chunks[(artifact_id, chunk_no)]
    
            if random() < FAILURE_PROB:
                self.state = FAULTY

            return chunk

    
    def delete_chunk(self, artifact_id, chunk_no):
        if self.state == READY:
            del self.chunks[(artifact_id, chunk_no)]

            if random() < FAILURE_PROB:
                self.state = FAULTY

    
    @ray.method(enable_task_events=False)
    def fix(self):
        if self.state == FAULTY:
            sleep(3.0)
            self.state = READY

    
    @ray.method(enable_task_events=False)
    def list_chunks(self):
        ret = f"--- Data chunks that are stored in data node '{self.id}', state: {self.state} ---\n"
        
        for (artifact_id, chunk_no) in self.chunks:
            ret += f"Artifact {artifact_id}, chunk {chunk_no} out of {CHUNKS}\n"

        ret += "\n"
        return ret
