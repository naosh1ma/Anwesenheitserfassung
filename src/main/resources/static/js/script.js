// Verhindert, dass beide Checkboxen gleichzeitig aktiviert werden
function handleAnwesenheit(checkbox, andereId) {
    let andereCheckbox = document.getElementById(andereId);
    let grundDropdown = document.getElementById("grund-" + checkbox.id.split('-')[1]);

    if (checkbox.checked) {
        andereCheckbox.checked = false;
        grundDropdown.style.display = "none"; // Verstecke Grund der Fehlzeit
    }
}

// Zeigt Dropdown-Menü für Fehlzeit an
function handleAbwesenheit(checkbox, andereId) {
    let andereCheckbox = document.getElementById(andereId);
    let grundDropdown = document.getElementById("grund-" + checkbox.id.split('-')[1]);

    if (checkbox.checked) {
        andereCheckbox.checked = false;
        grundDropdown.style.display = "inline"; // Zeige Grund der Fehlzeit
    } else {
        grundDropdown.style.display = "none";
    }
}

// Speichert die Anwesenheitsdaten (hier nur ein Konsolen-Log als Beispiel)
function speichereAnwesenheit() {
    let studenten = document.querySelectorAll("tr"); // Alle Zeilen holen

    let daten = [];

    studenten.forEach((row, index) => {
        if (index === 0) return; // Erste Zeile (Header) überspringen

        let name = row.cells[0].innerText;
        let anwesend = row.cells[1].querySelector("input").checked;
        let abwesend = row.cells[2].querySelector("input").checked;
        let grund = row.cells[3].querySelector("select") ? row.cells[3].querySelector("select").value : "";
        let ankunft = row.cells[4].querySelector("input").value;
        let verlassen = row.cells[5].querySelector("input").value;

        daten.push({
            name: name,
            anwesend: anwesend,
            abwesend: abwesend,
            grund: grund,
            ankunft: ankunft,
            verlassen: verlassen
        });
    });

    console.log("Gespeicherte Daten:", daten);
    alert("Anwesenheit gespeichert!");
}

