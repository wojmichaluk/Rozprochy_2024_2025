from kazoo.client import KazooClient
from tkinter import ttk, Canvas
from random import choice
import tkinter as tk
import logging


logging.basicConfig()
PATH = "/a"
BITMAPS = [
    "error", "gray75", "gray50", "gray25", "gray12", 
    "hourglass", "info", "questhead", "question", "warning"
]

    
class GraphicalZookeeper:
    def __init__(self, hosts):
        self.zk = KazooClient(hosts=hosts)


    def run(self):
        self.zk.start()
        self.prepare_gui()

        # setting watchers for '/a' znode and recursively for its descendants
        if self.zk.exists(PATH, watch=self.znode_watcher):
            self.root.deiconify()
            self.zk.get_children(PATH, watch=self.children_watcher)
            
            # build node tree compliant with existing nodes
            self.update_tree()

        # mainloop for tkinter window
        self.root.mainloop()


    def prepare_gui(self):
        self.root = tk.Tk()
        self.root.geometry("480x480")
        self.root.withdraw()
        self.root.title("Graphical Overview for Zookeeper")

        try:
            # check if the node already has descendants
            self.descendants_count = self.get_descendants_count(PATH)
        except Exception:
            # if node does not exist (NoNodeError)
            self.descendants_count = 0

        # textual information about descendants
        self.descendant_count_label = tk.Label(
            self.root, 
            text=f"Descendant count = {self.descendants_count}"
        )
        self.descendant_count_label.config(width=450, font=("Courier", 20))

        # graphical information about descendants
        self.descendant_canvas = Canvas(self.root, width=450, height=60)

        for i in range(self.descendants_count):
            x = (i + 1) * 450 / (self.descendants_count + 1)
            self.descendant_canvas.create_bitmap(x, 30, bitmap=choice(BITMAPS))

        # subtree of znodes from znode '/a'
        self.treeview = ttk.Treeview(self.root, height=17)
        self.treeview.column("#0", width=450)
        self.treeview.heading(column="#0", text="Node tree structure", anchor="w")

        # setting style for Treeview widget
        ttk.Style().configure("Treeview.Heading", font=("None", 16))
        self.treeview.tag_configure("tree_item", font=("None", 12))

        # packing prepared widgets
        self.descendant_count_label.pack()
        self.descendant_canvas.pack()
        self.treeview.pack()


    def get_descendants_count(self, path):
        children = self.zk.get_children(path, watch=self.children_watcher)
        count = len(children)

        for child in children:
            count += self.get_descendants_count(f"{path}/{child}")

        return count


    def znode_watcher(self, event):
        self.zk.exists(PATH, watch=self.znode_watcher)

        if event.type == "CREATED":
            print(f"Znode {PATH} has been created!")

            # restore root and clear widgets after node has been freshly created
            self.root.deiconify()
            self.zk.get_children(PATH, watch=self.children_watcher) 
            self.descendants_count = 0
            self.descendant_count_label.config(text="Descendant count = 0")
            self.descendant_canvas.delete('all')
            self.update_tree()
        elif event.type == "DELETED":
            print(f"Znode {PATH} has been deleted!")

            # withdraw root after node has been deleted so that tkinter window closes
            self.root.withdraw()

    
    def children_watcher(self, event):
        if self.zk.exists(PATH) and event.type == "CHILD":
            # get information about number of descendants
            descendants_count = self.get_descendants_count(PATH)
            op = "deleted" if descendants_count < self.descendants_count else "created"
            self.descendants_count = descendants_count
            print(f"Child of znode {event.path} has been {op}!")

            # updating textual information
            self.descendant_count_label.config(
                text=f"Descendant count = {descendants_count}"
            )

            # updating graphical information
            self.descendant_canvas.delete('all')

            for i in range(descendants_count):
                x = (i + 1) * 450 / (descendants_count + 1)
                self.descendant_canvas.create_bitmap(x, 30, bitmap=choice(BITMAPS))

            # update node tree structure as well
            self.update_tree()


    def update_tree(self):
        # clear current tree and build new one from scratch
        self.treeview.delete(*self.treeview.get_children())
        self.build_tree(PATH, "")


    def build_tree(self, path, parent_id):
        node_id = self.treeview.insert(
            parent_id, 
            "end", 
            text=path, 
            open=True, 
            tag="tree_item"
        )
        children = self.zk.get_children(path)

        # recursively build subtree(s)
        for child in children:
            self.build_tree(f"{path}/{child}", node_id)


if __name__ == "__main__":
    # single host (default)
    # hosts = "127.0.0.1:2181"

    # many hosts for Replicated Zookeeper
    hosts = "127.0.0.1:2281, 127.0.0.1:2381, 127.0.0.1:2481"

    gz = GraphicalZookeeper(hosts)
    print("Graphic app is ready!")
    gz.run()
