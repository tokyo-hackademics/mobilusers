Board Messenger Server
===
Server for Board Messenger


Documents
---

For client -> server API: `doc/client-server.md`

For server -> client API: `doc/server-client.md`


Installation
---

- Install mongodb: http://docs.mongodb.org/manual/installation
- Install nodejs: https://nodejs.org/download/
- change dir to `<source folder>/server/source/BoardMessenger`
- `sudo npm install`
- Install `forever` (for production only): `sudo npm install -g forever`


Localhost (for development)
---

### Start

- start mongodb: `sudo mongod`
- change dir to `<source folder>/server/source/BoardMessenger`
- `npm start`

### Stop
Just press `Ctrl C`

Real server (for production)
---

### Start

- start mongodb: `sudo mongod`
- change dir to `<source folder>/server/source/BoardMessenger`
- `forever start bin/www`
- Log file: `forever logs`

### Stop

- run `ps aux | grep forever` to get process id of forever
- `kill -9 <process id>`