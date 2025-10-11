# Board Game Rental

## Motivace a stručný popis

Cílem semestrální práce je vytvořit komunitní(tedy bez plateb) systém pro správu půjčovny deskových her. Tento systém umožní hráčům rezervovat a půjčovat hry, sledovat jejich dostupnost a historii výpůjček. Správci systému budou mít možnost spravovat katalog her, potvrzovat výpůjčky a spravovat stav výpůjček. Motivací je zjednodušit proces půjčování a sledování her, eliminovat chyby z manuální evidence a zlepšit přehlednost pro hráče i správce.

Systém poskytuje základní funkcionalitu půjčovny: registrace uživatelů, správa herního katalogu, výpůjčky her, sledování dostupnosti a historie výpůjček, hodnocení her a správa „karmy“ hráčů. Cílem je vytvořit aplikaci, která je intuitivní, snadno použitelná a umožní efektivní správu půjčovny deskových her.

## Hlavní funkce aplikace podle rolí(Use Cases)

### Host (Guest)

* **Registrace a přihlášení** – vytvořit účet a přihlásit se do systému.
* **Prohlížení katalogu her** – zobrazit seznam dostupných her a jejich vlastností (stav: nová, poškozená, legacy).

### Hráč (Player)

* **Prohlížení katalogu her** – zobrazit seznam dostupných her a jejich vlastností + filtrace (stav: nová, poškozená, legacy).
* **Sledování výpůjček** – kontrolovat aktuální a minulá vypůjčení.
* **Hodnocení a komentování her** – recenzovat hry (plus mazat svoje recenze).
* **Zobrazení detailů hry** – zobrazit popis, počet kopií a stav hry.
* **Seznam oblíbených her** – označit hry jako oblíbené pro rychlý přístup.
* **Půjčování her s karmou** – hráč si může půjčit hru pouze tehdy, pokud má karmu **≥ 70 bodů**.  

### Správce (Manager)

* **Správa katalogu her** – přidat, upravit vlastnosti hry (poškozená, nová, legacy) a mazat hry.
* **Potvrzování výpůjček** – schvalovat nebo odmítat výpůjčky hráčů.
* **Správa výpůjček** – aktualizovat stav vypůjčených her a označit je jako vrácené. Při **včasném vrácení se automaticky přičte 5 bodů do karmy hráče**, pokud jeho karma není již 100. Při **pozdním vrácení se automaticky aplikuje penalizace** (např. odečtení bodů z karmy nebo jiná sankce dle pravidel systému).
* **Kontrola dostupnosti her** – ověřovat počet kopií her a jejich aktuální stav.
* **Generování reportů** – vytvářet přehledy o výpůjčkách (jaké hry jsou aktuálně vypůjčené, top nejpůjčovanějších her).

## Cíloví uživatelé

1. **Hráči (Player)** – účastníci půjčovny, kteří si chtějí půjčovat a hodnotit hry.
2. **Správci (Manager)** – osoby odpovědné za správu katalogu a dohled nad výpůjčkami.
3. **Host (Guest)** – nepřihlášené osoby, možnost přihlášení, registrace a prohlížení katalogu.

## Omezení systému

Systém nebude určen pro následující funkce, i když by to mohlo být očekáváno:

* **Diskuzní fórum** – systém nebude poskytovat prostor pro diskuze mezi hráči.
* **Doručování her poštou** – všechny výpůjčky probíhají pouze osobním vyzvednutím.
* **Správa turnajů nebo soutěží** – systém nebude organizovat nebo evidovat herní soutěže.
* **Podpora platebních systémů** – systém nebude umožňovat platby online nebo integrovat platební brány.
* **Košík výpůjček** – nebudeme uvažovat košík výpůjček.
* **Sklad** – budeme uvažovat pouze 1 centrální sklad.
* **Rezervace** – když jsou všechny kusy hry vypůjčeny, nebude možnost si zarezervovat výpůjčku.
* **Nabízení her** – systém nebude umožňovat uživateli nabídnout svojí hru k vypůjčení.