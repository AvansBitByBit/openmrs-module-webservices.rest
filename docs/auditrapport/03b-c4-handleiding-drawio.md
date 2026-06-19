# Handleiding: Zelf C4-Diagrammen Maken in Draw.io

Deze handleiding legt stap voor stap uit hoe je de C4 architectuurdiagrammen (Level 0: Context en Level 1: Container) zelf kunt tekenen in Draw.io, volgens de officiële C4-conventies. Dit is specifiek toegespitst op de OpenMRS EPD architectuur.

---

## Waarom C4? (De Google Maps methode)
C4 staat voor Context, Containers, Components en Code. Het helpt om complexe architecturen begrijpelijk te maken door in te zoomen:
* **Level 0 (Context):** De "Wereldkaart". Wie zijn de gebruikers en wat is het systeem als één grote zwarte doos?
* **Level 1 (Container):** Het "Land". De doos gaat open en je ziet de draaiende applicaties (Backend, Frontend, Database). Dit niveau is essentieel om Trust Boundaries (netwerkgrenzen) aan te tonen voor security-audits.

---

## De C4 Gouden Regels
Om te zorgen dat je diagram wordt goedgekeurd in een audit of door een docent, moet elk diagram aan deze vier regels voldoen:

1. **De Blok-regel:** Een blok is nooit alleen een naam. Het bevat altijd 3 dingen:
   * **Naam:** (Bijv. *Patiëntendatabase*)
   * **Type/Technologie:** (Bijv. *[Container: MariaDB]*)
   * **Beschrijving:** Korte uitleg wat het doet (Bijv. *Slaat patiëntendossiers op.*)
2. **De Pijl-regel:** Een pijl/lijn is nóóit leeg. Je moet altijd twee dingen vermelden:
   * **Actie:** Wat doet de pijl? (*Leest en schrijft data*)
   * **Protocol/Technologie:** Hoe gebeurt dit? (*[JDBC]*)
3. **De Titel & Legenda:** Elk diagram heeft een duidelijke titel bovenin en een legenda (Key) die de kleuren uitlegt.
4. **Trust Boundaries:** Geef de grenzen van je interne beveiligde netwerk aan met een stippellijn-box.

---

## Voorbereiding in Draw.io
1. Ga naar [app.diagrams.net](https://app.diagrams.net).
2. Start een nieuw leeg diagram (Blank Diagram).
3. Klik links onderin het menu op **"More Shapes..."**
4. Scroll in de lijst naar het kopje *Software* en vink **"C4"** aan. Klik op Apply.
5. Je hebt nu aan de linkerkant een speciaal C4-menu met de juiste blokken.

---

## Stap-voor-stap: Level 0 (Context Diagram) Maken

### 1. Elementen bepalen
* **Person:** Medisch Personeel (Zorgverlener)
* **Person:** Systeembeheerder
* **Software System:** OpenMRS EPD Systeem

### 2. Tekenen
1. Sleep vanuit het C4-menu twee keer het **Person** blokje naar je canvas.
2. Vul de blokjes in. Dubbelklik op de tekst:
   * *Naam:* Medisch Personeel
   * *Beschrijving:* Ziet patiëntendossiers in en legt observaties vast.
3. Sleep een donkerblauw **System** blokje naar het midden.
   * *Naam:* OpenMRS EPD Systeem
   * *Beschrijving:* Het centrale patiëntendossier-systeem.
4. Trek pijlen van de personen naar het systeem.
   * *Tekst op pijl:* Bekijkt en bewerkt dossiers via `[HTTPS]`
5. Voeg een **Title** blokje toe linksboven ("System Context Diagram voor OpenMRS EPD").
6. Sla het diagram op en exporteer het als PNG voor in je rapport.

---

## Stap-voor-stap: Level 1 (Container Diagram) Maken

### 1. Elementen bepalen (uit je docker-compose)
* **Container 1:** Nginx API Gateway `[Nginx]`
* **Container 2:** SPA Frontend `[React / JavaScript]`
* **Container 3:** OpenMRS Backend `[Tomcat / Java]`
* **Container 4:** Patiëntendatabase `[MariaDB]`
* **Boundary:** Het interne netwerk waar de backend en DB in draaien.

### 2. Tekenen
1. Begin een nieuw tabblad in Draw.io.
2. Kopieer je "Medisch Personeel" Person-blokje uit het vorige tabblad en zet deze linksboven.
3. Gebruik nu de lichtblauwe **Container** blokjes uit het menu:
   * Maak een blok voor de Gateway, Frontend, Backend en Database. Zorg dat je de Naam, Technologie (tussen rechte haken) en een korte beschrijving toevoegt.
4. **De Boundary:** Zoek in het C4-menu naar het blokje met de gestippelde rand genaamd **System Boundary**. Sleep deze over je Backend en Database blokken heen.
   * *Titel van boundary:* Interne Server Omgeving (Trust Boundary)
5. **Verbinden:** Trek de pijlen met de juiste protocollen:
   * Van *Person* naar *Frontend* → Werkt met de interface via `[Webbrowser]`
   * Van *Frontend* naar *Gateway* → Maakt API requests via `[JSON/HTTPS]`
   * Van *Gateway* naar *Backend* → Routeert verkeer via `[HTTP/8080]`
   * Van *Backend* naar *Database* → Leest en schrijft data via `[JDBC/3306]`
6. Voeg weer een titel toe ("Container Diagram voor OpenMRS EPD").
7. Exporteer ook deze als PNG.

Nu heb je twee architectuurdiagrammen die visueel en technisch 100% correct zijn volgens de standaard!
