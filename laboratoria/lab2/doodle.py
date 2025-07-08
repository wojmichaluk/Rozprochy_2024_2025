from fastapi import Body, FastAPI, status
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Union
from typing import Dict

class Poll(BaseModel):
    name: str
    description: Union[str, None] = None
    votes: Dict[str, int]

app = FastAPI()
polls = {}
current_id = 1

@app.get("/poll")
async def display_all_polls():
    return {"polls" : polls}

@app.post("/poll")
async def add_new_poll(poll : Poll):
    global current_id
    polls[str(current_id)] = poll
    current_id += 1
    return poll

@app.get("/poll/{id}")
async def display_poll(id : str):
    if id in polls:
        return {"poll" : polls[id]}
    else:
        return_content = f"failed to display poll id {id}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)

@app.put("/poll/{id}")
async def upsert_poll(id : str, poll : Poll):
    existed = id in polls
    polls[id] = poll

    if existed:
        return polls[id]
    else:
        return JSONResponse(status_code=status.HTTP_201_CREATED, content=polls[id].dict())

@app.delete("/poll/{id}")
async def delete_poll(id : str):
    if id in polls:
        del polls[id]
        return_content = f"deleted poll id {id}"
        return JSONResponse(status_code=status.HTTP_202_ACCEPTED, content=return_content)
    else:
        return_content = f"failed to delete poll id {id}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)

@app.get("/poll/{id}/vote")
async def display_vote_count(id : str):
    if id in polls:    
        return {"votes" : polls[id].votes}
    else:
        return_content = f"failed to display votes for poll id {id}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)

@app.post("/poll/{id}/vote")
async def add_new_option(id : str, option : str):
    if id in polls:
        poll = polls[id]
        if option not in poll.votes:
            poll.votes[option] = 0
        return poll
    else:
        return_content = f"failed to add option to poll id {id}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)

@app.get("/poll/{id1}/vote/{id2}")
async def display_vote_count_for_option(id1 : str, id2 : str):
    if id1 in polls:
        votes = polls[id1].votes
        if id2 in votes:
            return {"vote_count" : votes[id2]}
        
        return_content = f"failed to display vote count for option {id2} in poll id {id1}: no such option"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)
    else:
        return_content = f"failed to display vote count for option {id2} in poll id {id1}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)

@app.put("/poll/{id1}/vote/{id2}")
async def cast_vote(id1 : str, id2 : str):
    if id1 in polls:
        votes = polls[id1].votes
        if id2 in votes:
            votes[id2] += 1
            return {"vote_count_after_casting_vote" : votes[id2]}
        
        return_content = f"failed to cast vote for option {id2} in poll id {id1}: no such option"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)
    else:
        return_content = f"failed to cast vote for option {id2} in poll id {id1}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)

@app.delete("/poll/{id1}/vote/{id2}")
async def delete_vote(id1 : str, id2 : str):
    if id1 in polls:
        votes = polls[id1].votes
        if id2 in votes:
            if votes[id2] > 0: votes[id2] -= 1
            return {"vote_count_after_deleting_vote" : votes[id2]}
        
        return_content = f"failed to delete vote for option {id2} in poll id {id1}: no such option"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)
    else:
        return_content = f"failed to delete vote for option {id2} in poll id {id1}: no such poll"
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND, content=return_content)
