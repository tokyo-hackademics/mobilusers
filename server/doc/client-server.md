TEMPLATES
===

Request template
---
- `rid`: request id
- `data`: data to be sent

**Sample**
```json
{
    "rid": "abc#1",
    "data": {
        "user_id": "datdvt",
        "user_password": "dat12345"
    }
}
```

Response template
---
- `error`: 0 if no error, otherwise error code
- `result`: result data (if error = 0)

** Sample **
```json
{
    "error": 0,
    "result": {
        "access_token": "abc123"
    }
}
```

AUTHENTICATION
===

Login
---

Event: "login"

### Request
- `user_id`
- `access_token` (if loggged in) or `user_password` (if not logged in yet)

### Response ###
- `access_token`


Check users
---

Check if users are available in system.
Event: "check_users"

### Request
- `ids`: users ' ids, separated by comma

### Response
ids of available users.


BOARD
===

Create board
---

Event: "create_board"

### Request
- `members`: the other users ' ids, separated by comma
- `name`: board 's name (optional)
- `background`: board 's background, can be RGB code or URL (optional)
- `extra`: extra info, can be anything (optional)

### Response
- `id`: id of newly-created board

Set board info
---

Event: "set_board_info"

### Request
- `id`: board 's id
- `name`: board 's name (optional)
- `background`: board 's background, can be RGB code or URL (optional)
- `extra`: extra info, can be anything (optional)

### Response
Nothing

Invite user to board
---

Event: "invite"

### Request
- `board_id`: board id
- `user_ids`: list of users ' ids to invite, separated by comma

### Response
Nothing

Leave a board
---

Event: "leave"

### Request
- `board_id`: board id

### Response
Nothing

Get board info
---

Event: "get_board_info"

### Request
- `ids`: ids of boards to fetch info, fetch all subscribed boards if empty

### Response
- Arrays of boards

**Sample**
```json
{
    "rid": "abc#1",
    "error": 0,
    "result": [
        {
            "id": "xyz456",
            "members": "user1,user2,user3"
            "name": "Mindmap board"
            "background": "ffffff",
            "extra": "",
            "last_action_id": "qwe098"
        }
    ]
}
```

ACTIONS
===

Get board 's actions
---

Event: "get_board_actions"

### Request
- `board_id`: board id
- `from`: fetch actions whose `from` is newer than this parameter, set NULL if fetch all (optional)

### Response
- Array of actions, sorted by descending `from`

**Sample**
```json
{
    "rid": "abc#1",
    "error": 0,
    "result": [
        {
            "id": "ghj456",
            "type": "line",
            "data": {
                "from": [12, 34],
                "to": [56, 78]
                "color": "ff0000"
            }
            "from": 1234556
            "to": 1239834,
            "sender": "user1"
        },
        ...
    ]
}
```

Send action
---

Event: "send_action"

### Request
- `board_id`: board id
- `type`: action type (free drawing, oval, rectangle, text, image, etc)
- `data`: co-ordinate, color, URL, text content, etc (optional)
- `from`: time when action is started, 0 if first action (in ms)
- `duration`: duration of action (in ms)

**Sample**
```json
{
    "rid": "abc#1",
    "data": {
        "type": "line",
        "data": {
            "from": [12, 34],
            "to": [56, 78]
            "color": "ff0000"
        }
        "from": 1234556
        "duration": 10
    }
}
```
### Response
- `id`: id of newly-created action
