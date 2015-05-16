var _ = require('underscore');
var db = require('./db');
var auth = require('./auth/default');
var Board = db.Board;
var Action = db.Action;
var User = db.User;

var ERROR_INVALID_PARAMETERS = 100;
var ERROR_INVALID_USER_ID_OR_PASSWORD = 101;
var ERROR_INVALID_ACCESS_TOKEN = 102;

var ERROR_USER_NOT_FOUND = 200;
var ERROR_BOARD_NOT_FOUND = 201;

var ERROR_INTERNAL_ERROR = 500;


var onInvalidParam = function(socket, obj) {
  console.log('Invalid parameter');
  socket.emit(obj.rid, {
    error: ERROR_INVALID_PARAMETERS
  });
}

var logRequest = function(eventName, userId, obj) {
  console.log("===>>> " + eventName + ", " + userId + ", " + obj.rid);
  console.log(obj);
};

var logResponse = function(eventName, userId, obj, body) {
  console.log("<<<===: " + eventName + ", " + userId + ", " + obj.rid);
  console.log(body);
};

var cloneArray = function(array) {
  var ret = [];
  for (var i = 0; i < array.length; i++) {
    ret.push(array[i]);
  }
  return ret;
}

module.exports = function(server) {
  var io = require('socket.io')(server);
  io.on('connection', function (socket) {
    
    var socketId = socket.id;
    var currentUserId;

    // :::LOGIN
    socket.on('login', function(obj) {

      logRequest('login', currentUserId, obj);

      var data = obj.data;

      if (!data.user_id || (!data.user_password && !data.access_token)) {
        onInvalidParam(socket, obj);
        return;
      }

      var beforeSaveUser = function(user) {

        currentUserId = user._id;

        var boards = user.boards;
        for(var i = 0; i < boards.length; i++) {
          socket.join(boards[i]._id.toString(), function(err) {
            // do nothing
          });
        }
      };

      if (data.access_token) {

        User.findOne({ _id: data.user_id, accessToken: data.access_token })
        .populate('boards')
        .exec(function (err, user) {
          if (user) {
            user.socketId = socketId;
            beforeSaveUser(user);
            user.save();

            var body = {
              error: 0,
              result: {
                access_token: data.access_token
              }
            };
            logResponse('login', currentUserId, obj, body);
            socket.emit(obj.rid, body);
          } else {
            console.log('Invalid access token');
            socket.emit(obj.rid, {
              error: ERROR_INVALID_ACCESS_TOKEN
            });
          }
        });

      } else {

        auth(data.user_id, data.user_password, function() {

          User.findOne({ _id: data.user_id })
          .populate('boards')
          .exec(function (err, user) {
            if (!user) {
              user = new User({
                _id: data.user_id
              })
            }

            var token = require('node-uuid').v1();
            user.accessToken = token,
            user.socketId = socketId
            beforeSaveUser(user);
            user.save();

            var body = {
              error: 0,
              result: {
                access_token: token
              }
            };
            logResponse('login', currentUserId, obj, body);
            socket.emit(obj.rid, body);
          });  

        }, function() {
          console.log('Invalid user_id or user_password');
          socket.emit(obj.rid, {
            error: ERROR_INVALID_USER_ID_OR_PASSWORD
          })

        })
      }
    });


    // :::CREATE BOARD
    socket.on('create_board', function(obj) {

      logRequest('create_board', currentUserId, obj);

      var data = obj.data;

      if (!data.members || !data.background || !data.width || !data.height) {
        onInvalidParam(socket, obj);
        return;
      }

      var memberIds = data.members.split(',');
      memberIds.push(currentUserId);

      User.find({_id: {$in: memberIds}}, function(err, users) {
        if (!users || users.length != memberIds.length) {
          console.log('User not found');
          socket.emit(obj.rid, {
            error: ERROR_USER_NOT_FOUND
          });
          return;
        }
        var board = new Board({
          members: cloneArray(users),
          name: data.name,
          background: data.background,
          width: data.width,
          height: data.height,
          extra: data.extra
        });
        board.save(function(err) {
          if (err) {
            console.log('Failed to create board: err=' + err);
            socket.emit(obj.rid, {
              error: ERROR_INTERNAL_ERROR
            });
          } else {
            var body = {
              error: 0,
              result: {
                id: board._id.toString()
              }
            };
            logResponse('create_board', currentUserId, obj, body);
            socket.emit(obj.rid, body);

            // notify all members, also bind user to board
            var boardData = Board.toJSON(board);
            for (var i = 0; i < users.length; i++) {
              var u = users[i];
              if (!u.boards) {
                u.boards = [];
              }
              u.boards.push(board);
              u.save();
              if (u.socketId) {
                var userSocket = io.sockets.connected[u.socketId];
                if (userSocket) {
                  userSocket.join(board._id.toString());
                  logResponse('on_new_board', u._id, {}, boardData);
                  userSocket.emit('on_new_board', boardData);
                }
              }
            }
          }
        });
      });
    });


    // :::GET BOARD INFO
    socket.on('get_board_info', function(obj) {

      logRequest('get_board_info', currentUserId, obj);

      var data = obj.data;

      var populateConditions = { path: 'boards' };
      if (data.ids && data.ids.length > 0) {
        populateConditions.match = {_id: {$in: data.ids}}
      }

      User.findOne({_id: currentUserId})
      .populate(populateConditions)
      .exec(function(err, user) {
        var result = [];
        var boards = user.boards;
        if (boards) {
          for (var i = 0; i < boards.length; i++) {
            result.push(Board.toJSON(boards[i]));
          }
        }

        var body = {
          error: 0,
          result: result
        };
        logResponse('get_board_info', currentUserId, obj, body);
        socket.emit(obj.rid, body);
      });
    });


    // :::GET BOARD ACTIONS
    socket.on('get_board_actions', function(obj) {

      logRequest('get_board_actions', currentUserId, obj);

      var data = obj.data;

      if (!data.board_id) {
        onInvalidParam(socket, obj);
        return;
      }

      var from;
      if (data.from) {
        from = data.from;
      } else {
        from = -1;
      }

      Action.find({board: data.board_id, from: {$gt: from}}, function(err, actions) {
        var result = [];
        if (actions) {
          for (var i = 0; i < actions.length; i++) {
            result.push(Action.toJSON(actions[i]));
          }
        }

        var body = {
          error: 0,
          result: result
        };
        logResponse('get_board_actions', currentUserId, obj, body);
        socket.emit(obj.rid, body);
      });
    });

    // :::SEND ACTION
    socket.on('send_action', function(obj) {

      logRequest('send_action', currentUserId, obj);

      var data = obj.data;

      if (!data.board_id || !data.type || !data.from) {
        onInvalidParam(socket, obj);
        return;
      }

      var action = new Action({
        board: data.board_id,
        type: data.type,
        data: data.data,
        from: data.from,
        duration: data.duration,
        sender: currentUserId
      });
      action.save(function(err) {
        if (err) {
          console.log('Failed to create action: err=' + err);
          socket.emit(obj.rid, {
            error: ERROR_INTERNAL_ERROR
          });
        } else {
          var body = {
            error: 0,
            result: {
              id: action._id.toString()
            }
          };
          logResponse('send_action', currentUserId, obj, body);
          socket.emit(obj.rid, body);

          // notify all socket in room
          var body = Action.toJSON(action);
          logResponse('on_new_action', data.board_id, obj, body);
          io.sockets.in(data.board_id).emit('on_new_action', body);
        }
      });
    });

    // :::DISCONNECT
    socket.on('disconnect', function () {

      logRequest('disconnect', currentUserId, '');

      User.findOne({ socketId: socketId }, function (err, user) {
        if (user) {
          user.socketId = null;
          user.save();
        }
      })
    });
  });
};