function validate1() {
    let episode = document.getElementById("episode")

    // check if episode is given
    if(episode.value === "") { 
        alert("Please give value for episode")
        return false
    }

    // check if correct episode if ascendant is chosen
    let ascendant = document.getElementById("ascendant")

    if(ascendant.checked && episode.value != 5) {
        alert("There was no ascendant rank in episode " + episode.value)
        return false
    }
}

function validate2() {
    let min_cost = document.getElementById("min_cost")
    let max_cost = document.getElementById("max_cost")

    // check if costs are given
    if(min_cost.value === "" || max_cost.value === "") { 
        alert("Please give values for minimum and maximum prices")
        return false
    }

    // check if min > max
    if(+min_cost.value > +max_cost.value) {
        alert("Minimum price cannot be higher than maximum price")
        return false
    }
}

function validate3() {
    let name = document.getElementById("name_agent")

    // acceptable agents list
    const agents = [
        "Brimstone", "Phoenix", "Sage", "Sova", "Viper", "Cypher", "Reyna", 
        "Killjoy", "Breach", "Omen", "Jett", "Raze", "Skye", "Yoru", "Astra", 
        "KAY/O", "Chamber", "Neon", "Fade", "Harbor", "Gekko", "Deadlock", 
        "Iso", "Clove", "Vyse", "Tejo"
    ]

    // check if agent name is correct
    if(!(agents.includes(name.value))) {
        alert("Please give correct agent name. Choose one from the given list")
        return false
    }
}

function toggleList() {
    let button = document.getElementById("toggle")
    let paragraph = document.getElementById("agents_list")

    if(button.textContent === "Show all agents list") {
        paragraph.removeAttribute("hidden")
        button.textContent = "Hide all agents list"
    } else {
        paragraph.setAttribute("hidden", true)
        button.textContent = "Show all agents list"
    }
}
