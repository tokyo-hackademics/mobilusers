Board Messenger SDK
---

Android SDK to plug Board Messenger in any application.

### Features
- Communicate with server via Websocket
- Manage Database
- `RenderBoard` class to display a board (just like Google 's MapFragment/MapView)

### Dependencies
- https://github.com/nkzawa/socket.io-client.java
- https://github.com/JakeWharton/NineOldAndroids


Board Tutor
---

Android application that supports online tutoring via an emulated board.

### Features
- Login with Google account
- List all subscribed boards
- Create boards
- Show board detail


### Dependencies
- http://androidannotations.org/
- https://github.com/jfeinstein10/SlidingMenu
- https://github.com/datdojp/mobilib
- https://github.com/square/picasso
- https://github.com/yukuku/ambilwarna


Build
---

Types: `debug`, `release`

Flavors: `dev`, `prod`

### Build dev-debug (for development)
```
./gradlew clean aDD
```

### Build preduct-release (for publishing)
```
./gradlew clean aPR
```

