import ray
import numpy as np
from consts import *

# name node
@ray.remote(max_task_retries=1)
class NameNode(object):
    def __init__(self, node_id, data_nodes):
        self.id = node_id
        self.data_nodes = data_nodes
        self.data_nodes_usage = [0 for _ in range(NODES)]
        self.artifact_distribution = dict()

    
    def distribute_new_artifact_to_data_nodes(self, artifact_id):
        # return value: list of (data_node_idx, chunk_idx) tuples
        data_nodes_chunks = []
        
        for i in range(CHUNKS):            
            for j in range(COPIES):
                node_idx = np.argmin(self.data_nodes_usage)
                node = self.data_nodes[node_idx]
                data_nodes_chunks.append((node_idx, i))
                self.data_nodes_usage[node_idx] += 1
                
                if artifact_id not in self.artifact_distribution:
                    self.artifact_distribution[artifact_id] = [[] for _ in range(CHUNKS)]

                self.artifact_distribution[artifact_id][i].append(node)

        return data_nodes_chunks

    
    def get_data_nodes_for_chunk(self, artifact_id, chunk_idx):
        # control assert
        assert artifact_id in self.artifact_distribution, "Artifact not found, thus cannot be updated!"

        affected_nodes = self.artifact_distribution[artifact_id][chunk_idx]
        nodes_idxs = [node.get_idx.remote() for node in affected_nodes]
        return nodes_idxs

    
    def delete_distributed_artifact(self, artifact_id):
        # control assert
        assert artifact_id in self.artifact_distribution, "Artifact not found, thus cannot be deleted!"

        # return value: list of (data_node_idx, chunk_idx) tuples
        data_nodes_chunks = []

        artifact_info = self.artifact_distribution[artifact_id]
        del self.artifact_distribution[artifact_id]

        for i in range(CHUNKS):
            affected_nodes = artifact_info[i]

            for node in affected_nodes:
                node_idx = ray.get(node.get_idx.remote())
                data_nodes_chunks.append((node_idx, i))
                self.data_nodes_usage[node_idx] -= 1

        return data_nodes_chunks

    
    @ray.method(enable_task_events=False)
    def list_chunks(self):
        ret = f"--- Data chunks that are tracked in name node '{self.id}' ---\n"
        
        for artifact_id in self.artifact_distribution:
            ret += f"\nArtifact {artifact_id}:\n"

            for i, chunk_nodes in enumerate(self.artifact_distribution[artifact_id]):
                ret += f"Chunk {i+1}: "
                
                for node in chunk_nodes:
                    ret += f" {ray.get(node.get_id.remote())}"

                ret += "\n"

        ret += "\n"
        return ret
