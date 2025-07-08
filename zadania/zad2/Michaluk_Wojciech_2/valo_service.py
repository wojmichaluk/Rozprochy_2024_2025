from fastapi import FastAPI, Form, status
from fastapi.responses import HTMLResponse
from typing import Annotated
import requests
import statistics

app = FastAPI()
URL = "https://valorant-api.com/v1/"

def html_error_template(error_code: int, reason: str):
    return f'''<html>
    <head>
        <title>Error {error_code}</title>
    </head>
    <body>
        <h1>An error {error_code} occurred!</h1>
        <h2>Reason: {reason}</h2>
    </body>
</html>'''

@app.post("/rank-form", response_class=HTMLResponse)
async def get_images_for_rank(
    episode: Annotated[int, Form(ge=1, le=5)],
    rank: Annotated[str, Form()]
):
    # to prevent nonsensical swagger ui requests
    if episode != 5 and rank == "ascendant":
        return HTMLResponse(
            status_code=status.HTTP_406_NOT_ACCEPTABLE,
            content=html_error_template(406, f"no ascendant rank in episode {episode}")
        )

    if rank not in [
        "unranked", 
        "iron",
        "bronze",
        "silver", 
        "gold", 
        "platinum", 
        "diamond", 
        "ascendant", 
        "immortal", 
        "radiant"
    ]:
        return HTMLResponse(
            status_code=status.HTTP_406_NOT_ACCEPTABLE,
            content=html_error_template(406, f"unknown rank: {rank}")
        )

    req = requests.get(f"{URL}competitivetiers")

    # if empty response from the server
    if len(req.content) == 0:
        return HTMLResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content=html_error_template(404, f"no API response!")
        )
    
    data = req.json()["data"]
    division = rank.upper()
    saved = {}

    for episode_ranks in data:
        if episode_ranks["assetObjectName"] == f"Episode{episode}_CompetitiveTierDataTable":
            for tier in episode_ranks["tiers"]:
                if division == tier["divisionName"]:
                    saved[tier["tierName"]] = tier["largeIcon"]

    html_response = f'''<html>
    <head>
        <title>Response</title>
        <style>''' + '''
            .column {
                float: left;
                width: 33.33%;
            }
            
            .row:after {
                content: "";
                display: table;
                clear: both;
            }''' + f'''
        </style>
    </head>
    <body>
        <div class="row">
            <h1>Image(s) for {rank} rank in episode {episode}:</h1>'''

    for tier in saved:
        html_response += f'''
            <div class="column">
                <h2>{tier}:</h2>
                <img src="{saved[tier]}"/>
            </div>'''

    html_response += f'''
        </div>
    </body>
</html>'''
    
    return html_response

@app.post("/weapons-form", response_class=HTMLResponse)
async def specific_weapons_stat(
    min_cost: Annotated[int, Form(ge=300, le=4700)],
    max_cost: Annotated[int, Form(ge=300, le=4700)],
    stat: Annotated[str, Form()]
):
    # to prevent nonsensical swagger ui requests
    if min_cost > max_cost:
        return HTMLResponse(
            status_code=status.HTTP_418_IM_A_TEAPOT,
            content=html_error_template(418, f"min price(={min_cost}) > max price(={max_cost}) - no way!")
        )

    if stat not in ["firerate", "magazine", "reload"]:
        return HTMLResponse(
            status_code=status.HTTP_406_NOT_ACCEPTABLE,
            content=html_error_template(406, f"unknown stat: {stat}")
        )
    
    req = requests.get(f"{URL}weapons")
    
    # if empty response from the server
    if len(req.content) == 0:
        return HTMLResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content=html_error_template(404, f"no API response!")
        )

    data = req.json()["data"]
    saved_stats = {}

    for weapon_stat in data:
        if weapon_stat["shopData"] is not None and min_cost <= weapon_stat["shopData"]["cost"] <= max_cost:
            weapon = weapon_stat["displayName"]
            saved_stats[weapon] = {}

            if stat == "firerate":
                saved_stats[weapon][stat] = weapon_stat["weaponStats"]["fireRate"]
            elif stat == "magazine":
                saved_stats[weapon][stat] = weapon_stat["weaponStats"]["magazineSize"]
            else:
                saved_stats[weapon][stat] = weapon_stat["weaponStats"]["reloadTimeSeconds"]

            saved_stats[weapon]["cost"] = weapon_stat["shopData"]["cost"]

    stat_dict = {
        "firerate" : "firerate",
        "magazine" : "magazine size",
        "reload" : "reload time in seconds"
    }

    html_response = '''<html>
    <head>
        <title>Response</title>
    </head>
    <body>'''

    if len(saved_stats) == 0:
        return html_response + f'''
        <h1>No weapon found in a price range ({min_cost}, {max_cost})!</h1>
    </body>
</html>'''

    html_response += f'''
        <h1>Stats for {stat_dict[stat]} for weapons in a price range ({min_cost}, {max_cost}):</h1>'''
    
    avg = statistics.mean([
        saved_stats[weapon][stat] 
        for weapon in saved_stats 
    ])

    max_stat = (-1, -1)
    min_stat = (-1, float('inf'))

    for weapon in saved_stats:
        if saved_stats[weapon][stat] > max_stat[1]:
            max_stat = (weapon, saved_stats[weapon][stat])
        if saved_stats[weapon][stat] < min_stat[1]:
            min_stat = (weapon, saved_stats[weapon][stat])

    html_response += f'''
        <h2>Average {stat_dict[stat]}: {avg:.2f}</h2>
        <h2>Maximum {stat_dict[stat]}: {max_stat[1]} for a weapon: {max_stat[0]}, which costs {saved_stats[max_stat[0]]["cost"]}</h2>
        <h2>Minimum {stat_dict[stat]}: {min_stat[1]} for a weapon: {min_stat[0]}, which costs {saved_stats[min_stat[0]]["cost"]}</h2>
    </body>
</html>'''
    
    return html_response

@app.post("/agent-form", response_class=HTMLResponse)
async def specific_agent_info(
    name: Annotated[str, Form()],
    option: Annotated[str, Form()],
):
    # acceptable agents list
    agents = [
        "Brimstone", "Phoenix", "Sage", "Sova", "Viper", "Cypher", "Reyna", 
        "Killjoy", "Breach", "Omen", "Jett", "Raze", "Skye", "Yoru", "Astra", 
        "KAY/O", "Chamber", "Neon", "Fade", "Harbor", "Gekko", "Deadlock", 
        "Iso", "Clove", "Vyse", "Tejo"
    ]

    # to prevent nonsensical swagger ui requests
    if name not in agents:
        return HTMLResponse(
            status_code=status.HTTP_406_NOT_ACCEPTABLE,
            content=html_error_template(406, f"unknown agent: {name}")
        )

    if option not in ["abilities", "contract"]:
        return HTMLResponse(
            status_code=status.HTTP_406_NOT_ACCEPTABLE,
            content=html_error_template(406, f"unknown option: {option}")
        )
    
    if option == "abilities":
        req = requests.get(f"{URL}agents")
    else:
        req = requests.get(f"{URL}contracts")

    # if empty response from the server
    if len(req.content) == 0:
        return HTMLResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content=html_error_template(404, f"no API response!")
        )

    data = req.json()["data"]
    saved_info = {}

    if option == "abilities":
        for agent_stat in data:
            if agent_stat["displayName"] == name and agent_stat["isPlayableCharacter"]:
                abilities = agent_stat["abilities"]

                for ability in abilities:
                    saved_info[ability["displayName"]] = [None, None]
                    saved_info[ability["displayName"]][0] = ability["displayIcon"]
                    saved_info[ability["displayName"]][1] = ability["description"]

        html_response = f'''<html>
    <head>
        <title>Response</title>
        <style>''' + '''
            h1, h2, p {
                color: white;
            }

            p {
                margin: 10px 20px;
            }

            .column {
                float: left;
                width: 20%;
            }
            
            .row:after {
                content: "";
                display: table;
                clear: both;
            }''' + f'''
        </style>
    </head>
    <body style="background-color: black;">
        <div class="row">
            <h1>Icons and descriptions for agent {name} abilities:</h1>'''
        
        for ability_name in saved_info:
            html_response += f'''
            <div class="column">
                <h2>'{ability_name}':</h2>'''
            
            if saved_info[ability_name][0] is not None:
                html_response += f'''
                <img src="{saved_info[ability_name][0]}" style="width: 70%;"/>'''
            else:
                html_response += '''
                <br><br><h1 style="text-align: center;">No icon!</h1><br><br>'''

            html_response += f'''
                <p>{saved_info[ability_name][1]}</p>
            </div>'''

        html_response += f'''
        </div>
    </body>
</html>'''
        
        return html_response
    
    else:
        key = f"{name} Gear" if name != "Omen" else "Omen Contract"

        for contract in data:
            if contract["displayName"] == key:
                chapters = contract["content"]["chapters"]

                for i, chapter in enumerate(chapters):
                    levels = chapter["levels"]

                    for j, level in enumerate(levels):
                        saved_info[f"{i+1}.{j+1}"] = {}
                        saved_info[f"{i+1}.{j+1}"]["xp"] = level["xp"]

                        if level["isPurchasableWithDough"]:
                            saved_info[f"{i+1}.{j+1}"]["doughCost"] = level["doughCost"]

        html_response = f'''<html>
    <head>
        <title>Response</title>
    </head>
    <body>
        <h1>Contract stats for agent {name}:</h1>'''
    
        avg_xp = statistics.mean([
            saved_info[level]["xp"] 
            for level in saved_info 
        ])

        max_xp = (-1, -1)
        min_xp = (-1, float('inf'))

        for level in saved_info:
            if saved_info[level]["xp"] > max_xp[1]:
                max_xp = (level, saved_info[level]["xp"])
            if saved_info[level]["xp"] < min_xp[1]:
                min_xp = (level, saved_info[level]["xp"])

        html_response += f'''
        <h2>Average xp for a level in this contract: {avg_xp:.2f}</h2>
        <h2>Max xp: {max_xp[1]} for a level {max_xp[0]}</h2>
        <h2>Min xp: {min_xp[1]} for a level {min_xp[0]}</h2><br>'''

        dough_costs = []

        for level in saved_info:
            if "doughCost" in saved_info[level]: 
                dough_costs.append((level, saved_info[level]["doughCost"]))

        if dough_costs:
            avg_dough = statistics.mean([
                dough_cost 
                for _, dough_cost in dough_costs
            ])

            max_dough = (-1, -1)
            min_dough = (-1, float('inf'))

            for level, dough_cost in dough_costs:
                if dough_cost > max_dough[1]:
                    max_dough = (level, dough_cost)
                if dough_cost < min_dough[1]:
                    min_dough = (level, dough_cost)

            html_response += f'''
        <h2>Average dough cost (if purchasable with dough) for a level in this contract: {avg_dough:.2f}</h2>
        <h2>Max dough cost: {max_dough[1]} for a level {max_dough[0]}</h2>
        <h2>Min dough cost: {min_dough[1]} for a level {min_dough[0]}</h2>'''

        html_response += f'''
    </body>
</html>'''
        
        return html_response
