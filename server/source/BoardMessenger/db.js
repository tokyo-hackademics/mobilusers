// configurations
var host = 'localhost';
var port = 27017;
var db = 'boardmessenger';

// create mongoose object
var mongoose = require('mongoose');
mongoose.connection.on('error', console.error.bind(console, 'connection error:'));
mongoose.connection.once('open', function (callback) {
  console.log('Successfully connected to Mongodb');
});

// models
var Schema = mongoose.Schema;

var actionSchema = new Schema({
  board: {
    type: Schema.Types.ObjectId,
    ref: 'board'
  },
  type: String,
  data: String,
  from: Number,
  duration: Number,
  sender: {
    type: String,
    ref: 'user'
  },
});
var Action = mongoose.model('action', actionSchema);
Action.toJSON = function(action) {
  return {
    id: action._id.toString(),
    board_id: action.board.toString(),
    type: action.type,
    data: action.data,
    from: action.from,
    duration: action.duration,
    sender: action.sender
  };
}

var boardSchema = mongoose.Schema({
  members: [{
    type: String,
    ref: 'user'
  }],
  name: String,
  background: String,
  width: Number,
  height: Number,
  extra: String
});
var Board = mongoose.model('board', boardSchema);
Board.toJSON = function(board) {
  return {
    id: board._id.toString(),
    members: board.members.join(","),
    name: board.name,
    background: board.background,
    width: board.width,
    height: board.height,
    extra: board.extra
  }
};

var userSchema = new Schema({
  _id: String,
  accessToken: String,
  socketId: String,
  boards: [{
    type: Schema.Types.ObjectId,
    ref: 'board'
  }]
});
var User = mongoose.model('user', userSchema);


// connect to Mongodb
mongoose.connect('mongodb://' + host + ":" + port + "/" + db);

// finally, export to module
module.exports = {
  Board: Board,
  Action: Action,
  User: User
};
