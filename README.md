# offer-tracker

## Goal of project

Main goal of this project is to learn of Kotlin language.

## Project info

- Currently project have no GUI
- It will have CLI
- It will be called from cron
- In the future it can be rewritten to be a service with REST endpoints + some GUI

### Stories

#### Load offers from home seller servieces

- write separate modules for each home seller

#### Store offers in DB

- store all new (or updated old) offers in DB

#### Aggregate offers

- Automatically aggregate offers (based on seller ID)
- Add possibility to manually aggregate offers

#### Ignore offers

- Add possibility to manually ignore offers (hide in DB)

#### CLI

- List offers
- List aggregated offers
- Update offers
- Modify aggregated offers