New board
---

Event: "on_new_board"

Server notifies client when other user added current user to a board

- `id`: board id
- `members`: the other users ' ids, separated by comma
- `name`: board 's name
- `background`: board 's background, can be RGB code or URL
- `extra`: extra info, can be anything

**Sample**
```json
{
    "id": "xyz456",
    "members": "user1,user2,user3"
    "name": "Mindmap board"
    "background": "ffffff",
    "extra": "",
    "last_action_id": "qwe098"
}
```

Board info changed
---

Event: "on_board_info_changed"

Server notifies client when board 's info is changed

- `id`: board id
- `members`: the other users ' ids, separated by comma
- `name`: board 's name
- `background`: board 's background, can be RGB code or URL
- `extra`: extra info, can be anything

**Sample**
```json
{
    "id": "xyz456",
    "members": "user1,user2,user3"
    "name": "Mindmap board"
    "background": "ffffff",
    "extra": "",
    "last_action_id": "qwe098"
}
```

New action
---

Event: "on_new_action"

Server notifies client when new action is sent to a board that current users subsribes to

- `id`: action id
- `board_id`: board id
- `type`: action type (free drawing, oval, rectangle, text, image, etc)
- `data`: co-ordinate, color, URL, text content, etc
- `from`: time when action is started, 0 if first action (in ms)
- `duration`: duration of action (in ms)
- `sender`: id of sender

**Sample**
```json
{
    "id": "ghj456",
    "board_id": "tyu567",
    "type": "line",
    "data": {
        "from": [12, 34],
        "to": [56, 78]
        "color": "ff0000"
    }
    "from": 1234556
    "duration": 10,
    "sender": "user1"
}
```
