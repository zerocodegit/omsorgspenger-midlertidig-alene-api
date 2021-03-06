# Omsorgspenger midlertidig alene API

![CI / CD](https://github.com/navikt/omsorgspenger-midlertidig-alene-api/workflows/CI%20/%20CD/badge.svg)
![NAIS Alerts](https://github.com/navikt/omsorgspenger-midlertidig-alene-api/workflows/Alerts/badge.svg)

# Innholdsoversikt
* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle Krav](#2-funksjonelle-krav)
* [3. Begrensninger](#3-begrensninger)
* [4. Prinsipper](#4-prinsipper)
* [5. Programvarearkitektur](#5-programvarearkitektur)
* [6. Kode](#6-kode)
* [7. Data](#7-data)
* [8. Infrastrukturarkitektur](#8-infrastrukturarkitektur)
* [9. Distribusjon av tjenesten (deployment)](#9-distribusjon-av-tjenesten-deployment)
* [10. Utviklingsmiljø](#10-utviklingsmilj)
* [11. Drift og støtte](#11-drift-og-sttte)

# 1. Kontekst
API for søknad om å bli regnet som midlertidig alene og Kafka producer. 

# 2. Funksjonelle Krav
Denne tjenesten understøtter søknadsprosessen, samt eksponerer endepunkt for innsending av søknad.

API mottar søknaden, validerer og legger den videre på en kafka-topic som 
omsorgspenger-midlertidig-alene-prosessering konsumerer.

# 3. Endepunkter
**GET @/soker --> Gir 200 respons med json av søker**
```
{ 
    "aktør_id": "23456",
    "fornavn": "ARNE",
    "mellomnavn": "BJARNE",
    "etternavn": "CARLSEN",
    "fødselsdato": "1990-01-02"
}
```

**GET @/barn --> Gir 200 respons med json over barn, eventuelt tom liste ved ingen barn.**
```
{
    "barn": [{
        "fødselsdato": "2000-08-27",
        "fornavn": "BARN",
        "mellomnavn": "EN",
        "etternavn": "BARNESEN",
        "aktør_id": "1000000000001"
    }, {
        "fødselsdato": "2001-04-10",
        "fornavn": "BARN",
        "mellomnavn": "TO",
        "etternavn": "BARNESEN",
        "aktør_id": "1000000000002"
    }]
}
```

**POST @/soknad/valider --> 202 respons ved gyldig søknad, ellers 400 og liste over feil. Kan brukes før innsending for å sjekke om søknaden er gyldig**

**POST @/soknad --> 202 respons ved gyldig søknad. Eventuelt tilbake 400 og liste over valideringsbrudd.**

**Validering**
* harForståttRettigheterOgPliker og harBekreftetOpplysninger må være true
* Alle bolske verdier hvor vi tillater true og false blir satt til null dersom noe går falt ved deserialisering, for å unngå default false.
Valideringen sjekker dette og gir feil dersom en bolsk verdi er null.
* AnnenForelder:
  * Navn kan ikke være tom eller kun whitespaces.
  * fnr må være gyldig.
  * situasjon
    * INNLAGT_I_HELSEINSTITUSJON --> vetLengdePåInnleggelseperioden må være satt. Basert på det valideres enten at 
    periodeFraOgMed og periodeTilOgMed er satt, og at fraOgMed er før tilOgMed, eller at periodeOver6Måneder er satt. 
    Satt = ikke null
    * UTØVER_VERNEPLIKT og FENGSEL --> Valider dato som beskrevet over.
    * SYKDOM og ANNET --> Validerer at validerSituasjonBeskrivelse ikke er tom, blank eller null, og dato som beskrevet over.
  * antallBarn må være minimum 1
  * fødselsårBarn kan ikke inneholde verdi høyere enn årstallet vi er i.
* Medlemskap:
  * harBoddIUtlandetSiste12Mnd må være satt til true eller false
  * Hvis harBoddIUtlandetSiste12Mnd er true så må utenlandsoppholdSiste12Mnd inneholde elementer.
  * Hvis harBoddIUtlandetSiste12Mnd er false så må utenlandsoppholdSiste12Mnd være tom.
  * skalBoIUtlandetNeste12Mnd må være satt til true eller false
  * Hvis skalBoIUtlandetNeste12Mnd er true så må utenlandsoppholdNeste12Mnd inneholde elementer.
  * Hvis skalBoIUtlandetNeste12Mnd er false så må utenlandsoppholdNeste12Mnd være tom.

Eksempel json;
```
{
  "id": "123456789",
  "språk": "nb",
  "arbeidssituasjon": [
    "FRILANSER"
  ],
  "annenForelder": {
    "navn": "Berit",
    "fnr": "02119970078",
    "situasjon": "FENGSEL",
    "situasjonBeskrivelse": "Sitter i fengsel..",
    "periodeOver6Måneder": null,
    "periodeFraOgMed": "2020-01-01",
    "periodeTilOgMed": "2020-10-01"
  },
  "antallBarn": 2,
  "fødselsårBarn": [
    2005,
    2003
  ],
  "medlemskap": {
    "harBoddIUtlandetSiste12Mnd": true,
    "utenlandsoppholdSiste12Mnd": [
      {
        "fraOgMed": "2020-01-01",
        "tilOgMed": "2020-01-10",
        "landkode": "DE",
        "landnavn": "Tyskland"
      },
      {
        "fraOgMed": "2020-01-01",
        "tilOgMed": "2020-01-10",
        "landkode": "SWE",
        "landnavn": "Sverige"
      }
    ],
    "skalBoIUtlandetNeste12Mnd": true,
    "utenlandsoppholdNeste12Mnd": [
      {
        "fraOgMed": "2020-10-01",
        "tilOgMed": "2020-10-10",
        "landkode": "BR",
        "landnavn": "Brasil"
      }
    ]
  },
  "harForståttRettigheterOgPlikter": true,
  "harBekreftetOpplysninger": true
}
```

# 4. Prinsipper

# 5. Programvarearkitektur

# 6. Kode

# 7. Data

# 8. Infrastrukturarkitektur

# 9. Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort med bruk av Github Actions.
[Omsorgspenger-midlertidig-alene-API CI / CD](https://github.com/navikt/omsorgspenger-midlertidig-alene-api/actions)

Push til dev-* brancher vil teste, bygge og deploye til dev/staging miljø.
Push/merge til master branche vil teste, bygge og deploye til produksjonsmiljø.

# 10. Utviklingsmiljø
## Bygge Prosjekt
For å bygge kode, kjør:

```shell script
./gradlew clean build
```

## Kjøre Prosjekt
For å kjøre kode, kjør:

```shell script
./gradlew bootRun
```

# 11. Drift og støtte
## Logging
[Kibana](https://tinyurl.com/ydkqetfo)

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).

# Metrics
n/a

### Redis
Vi bruker Redis for mellomlagring. En instanse av Redis må være kjørene før deploy av applikasjonen. 
Dette gjøres manuelt med kubectl både i preprod og prod. Se [nais/doc](https://github.com/nais/doc/blob/master/content/redis.md)

1. `kubectl config use-context preprod-sbs`
2. `kubectl apply -f redis-config.yml`
