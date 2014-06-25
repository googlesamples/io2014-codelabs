(function(context) {


  var WINNERS = [
      [1,1,1,0,0,0,0,0,0],
      [0,0,0,1,1,1,0,0,0],
      [0,0,0,0,0,0,1,1,1],
      [1,0,0,1,0,0,1,0,0],
      [0,1,0,0,1,0,0,1,0],
      [0,0,1,0,0,1,0,0,1],
      [1,0,0,0,1,0,0,0,1],
      [0,0,1,0,1,0,1,0,0]
    ];

  var callbacks = {
    onTurnChanged: null,
    onFinished: null
  }

  Board.prototype.PLAYER_1=1;
  Board.prototype.PLAYER_2=-1;

  Board.prototype.STATE_NOT_STARTED=0;
  Board.prototype.STATE_PLAYING=1;
  Board.prototype.STATE_FINISHED=2;

  function Board() {
    this.state = this.STATE_NOT_STARTED;
  }

  Board.prototype.init = function() {
    this.board = [0,0,0,0,0,0,0,0,0];
    this.winner = 0;
    this.winningCells = null;
    this.nextPlayer = this.PLAYER_1;
    this.state = this.STATE_PLAYING;
    callbacks.onTurnChanged && callbacks.onTurnChanged(this);
  }


  Board.prototype.setCallback = function(type, callback) {
    callbacks[type] = callback;
  }

  Board.prototype.play = function(i) {
    if (this.state != this.STATE_PLAYING || this.board[i] != 0) {
      return false;
    }
    this.board[i] = this.nextPlayer;

    if (!checkWinners.apply(this) && checkAvailablePlays.apply(this)) {
      this.nextPlayer = this.nextPlayer * -1;
      callbacks.onTurnChanged && callbacks.onTurnChanged(this);
    } else {
      callbacks.onFinished && callbacks.onFinished(this);
    }
    return true;
  }

  Board.prototype.getCellState = function(cell) {
    return this.board[cell];
  }

  Board.prototype.getBoardState = function() {
    return this.state;
  }

  Board.prototype.getNextPlayer = function() {
    return this.state == this.STATE_PLAYING ? this.nextPlayer : 0;
  }

  Board.prototype.getWinner = function() {
    return this.state == this.STATE_FINISHED ? this.winner : 0;
  }

  Board.prototype.getWinningCells = function() {
    return this.state == this.STATE_FINISHED ? this.winningCells : null;
  }

  // private methods:

  function checkAvailablePlays() {
    for (var c=0; c < this.board.length; c++) {
      if (!this.board[c]) {
        return true;
      }
    }
    this.state = this.STATE_FINISHED;
    this.winner = 0;
    this.nextPlayer = 0;
    return false;
  }

  function checkWinners() {
    // check possible solutions:
    for (var s=0; s < WINNERS.length; s++) {
      var maybeWinner = 0;
      for (var c=0; c < this.board.length; c++) {
        if (WINNERS[s][c] == 1) {
          if ( !this.board[c] || maybeWinner && this.board[c] != maybeWinner) {
            maybeWinner = 0;
            break;
          }
          if ( !maybeWinner || maybeWinner && this.board[c] == maybeWinner) {
            maybeWinner = this.board[c];
          }
        }
      }

      if (maybeWinner) {
        this.state = this.STATE_FINISHED;
        this.winner = maybeWinner;
        this.nextPlayer = 0;
        this.winningCells = WINNERS[s];
        return true;
      }
    }
    return false;
  }

  window.Board = Board;

})(window);
