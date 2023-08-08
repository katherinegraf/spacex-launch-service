## **What it is**
A Kotlin app that integrates with the SpaceX API to aggregate information from various API endpoints to present a more unified representation of each SpaceX Launch,  with its associated Launchpad, Payload, and Capsule data.

## **How to use it**
- Set up a local postgres database called `spacex`; set up username/password in `application.yml`
- To run app, use `./gradlew bootrun`
- Open a web browser to `http://localhost:8080/`

## **Example Launch Object**
```{
        "id": "5eb87ceeffd86e000604b341",
        "name": "CRS-7",
        "details": "Launch performance was nominal until an overpressure incident in the second-stage LOX tank, leading to vehicle breakup at T+150 seconds. The Dragon capsule survived the explosion but was lost upon splashdown because its software did not contain provisions for parachute deployment on launch vehicle failure.",
        "date_utc": "2015-06-28T14:21:00.000Z",
        "success": false,
        "failures": [
            {
                "time": 139,
                "altitude": 40,
                "reason": "helium tank overpressure lead to the second stage LOX tank explosion",
                "launchId": "5eb87ceeffd86e000604b341",
                "id": 29
            }
        ],
        "launchpad": {
            "id": "5e9e4501f509094ba4566f84",
            "full_name": "Cape Canaveral Space Force Station Space Launch Complex 40",
            "locality": "Cape Canaveral",
            "region": "Florida",
            "status": "active",
            "details": "SpaceX's primary Falcon 9 pad, where all east coast Falcon 9s launched prior to the AMOS-6 anomaly. Previously used alongside SLC-41 to launch Titan rockets for the US Air Force, the pad was heavily damaged by the AMOS-6 anomaly in September 2016. It returned to flight with CRS-13 on December 15, 2017, boasting an upgraded throwback-style Transporter-Erector modeled after that at LC-39A.",
            "launch_attempts": 99,
            "launch_successes": 97
        },
        "payloads": [
            {
                "id": "5eb0e4beb6c3bb0006eeb1fc",
                "name": "CRS-7",
                "type": "Dragon 1.1",
                "regime": "low-earth",
                "launchId": "5eb87ceeffd86e000604b341",
                "customers": "NASA (CRS)",
                "nationalities": "United States",
                "manufacturers": "SpaceX",
                "mass_kg": 2477.0,
                "mass_lbs": 5460.9
            }
        ],
        "capsules": [
            {
                "id": "5e9e2c5cf35918407d3b266c",
                "serial": "C109",
                "type": "Dragon 1.1",
                "status": "destroyed",
                "last_update": "Destroyed on impact after F9 launch failure",
                "water_landings": 1,
                "land_landings": 0
            }
        ],
        "updated_at": "2023-08-08"
    }
```

## **Note**
This app was first built without a database, to intentionally practice combining data in memory from multiple API calls. It was then redesigned to resolve the issue of too many API calls by storing data in a database.
